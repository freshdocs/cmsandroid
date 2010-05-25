/*******************************************************************************
 * The MIT License
 * 
 * Copyright (c) 2010 Zia Consulting, Inc
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.zia.freshdocs.activity;

import java.util.Collection;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.zia.freshdocs.Constants;
import com.zia.freshdocs.R;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.net.Downloadable;
import com.zia.freshdocs.preference.CMISHost;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.widget.HostAdapter;

public class HostsActivity extends ListActivity
{
	private static final String INITIALIZED_KEY = "initialized";
	
	private static final int NEW_HOST_REQ = 0;
	private static final int EDIT_HOST_REQ = 1;
	private static final int SPLASH_REQUEST_REQ = 2;
	private static final int NODE_BROWSE_REQ = 3;
	
	private static final String OK_KEY = "ok";
	
	private ChildDownloadThread _dlThread = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hosts);
		registerForContextMenu(getListView());
		
		if(savedInstanceState == null || !savedInstanceState.getBoolean(INITIALIZED_KEY))
		{
			startActivityForResult(new Intent(this, SplashActivity.class), SPLASH_REQUEST_REQ);
		}
		
		initializeHostList();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		CMISApplication app = (CMISApplication) getApplication();
		app.cleanupCache();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(INITIALIZED_KEY, true);
	}	

	protected void initializeHostList()
	{
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		Collection<CMISHost> prefs = prefsMgr.getAllPreferences(this);

		HostAdapter serverAdapter = new HostAdapter(this, 
				R.layout.host_list_item, R.id.host_textview,
				prefs.toArray(new CMISHost[]{}));
		setListAdapter(serverAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.hosts_menu, menu);    
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_add_server:
			addServer();
			return true;
		case R.id.menu_item_favorites:
			Intent favoritesIntent = new Intent(this, FavoritesActivity.class);
			startActivityForResult(favoritesIntent, 0);
			return true;
		case R.id.menu_item_about:
			Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivity(aboutIntent);
			return true;
		case R.id.menu_item_quit:
			this.finish();
			return true;
		default:
			return false;
		}
	}

	protected void addServer()
	{
		Intent newHostIntent = new Intent(this, HostPreferenceActivity.class);
		startActivityForResult(newHostIntent, NEW_HOST_REQ);
	}
	
	/**
	 * Handles rotation by doing nothing (instead of onCreate being called)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo)
	{
		HostAdapter adapter = (HostAdapter) getListAdapter();
		CMISHost host = adapter.getItem(((AdapterContextMenuInfo) menuInfo).position);
		
		if(!host.getId().equals(Constants.NEW_HOST_ID))
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.host_context_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		CMISHost prefs = (CMISHost) getListAdapter().getItem(info.position);
		String id = prefs.getId();

		switch (item.getItemId())
		{
		case R.id.menu_edit_server:
			Intent newHostIntent = new Intent(this, HostPreferenceActivity.class);
			newHostIntent.putExtra(HostPreferenceActivity.EXTRA_EDIT_SERVER, id);
			startActivityForResult(newHostIntent, EDIT_HOST_REQ);
			
			return true;
		case R.id.menu_delete_server:
			deleteServer(id);
			break;
		}
		
		return false;
	}

	protected void deleteServer(String id)
	{
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		prefsMgr.deletePreferences(this, id);
		initializeHostList();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode)
		{
		case NEW_HOST_REQ:			
		case EDIT_HOST_REQ:			
		case SPLASH_REQUEST_REQ:			
			initializeHostList();
			break;
		case NODE_BROWSE_REQ:
			if(resultCode == RESULT_OK && data != null && 
					data.getBooleanExtra(Constants.QUIT, false))
			{
				finish();
			}
			break;
		}
	}
	
	protected void onSearch()
	{
		onSearchRequested();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		final CMISApplication app = (CMISApplication) getApplication();
		final Context ctx = this;
		final HostAdapter adapter = (HostAdapter) getListAdapter();
		final View container = v;
		CMISHost pref = adapter.getItem(position);
		
		if(pref.getId().equals(Constants.NEW_HOST_ID))
		{
			addServer();
			return;
		}
		
		CMISHost prefs = adapter.getItem(position);
		final String hostId = prefs.getId();

		adapter.toggleError(container, false);
		adapter.toggleProgress(container, true);
		
		_dlThread = new ChildDownloadThread(new Handler() 
		{
			public void handleMessage(Message msg) 
			{
				boolean ok = msg.getData().getBoolean(OK_KEY);

				adapter.toggleProgress(container, false);					

				if(!ok)
				{
					adapter.toggleError(container, true);
					app.handleNetworkStatus();
				} 
				else
				{
					Intent browseIntent = new Intent(ctx, NodeBrowseActivity.class);
					startActivityForResult(browseIntent, NODE_BROWSE_REQ);
				}
			}
		}, 
		new Downloadable()
		{
			public Object execute()
			{
				return app.initCMIS(hostId);
			}
		});
		
		_dlThread.start();
	}
	
	private class ChildDownloadThread extends Thread 
	{
		Handler _handler;
		Downloadable _delegate;

		ChildDownloadThread(Handler h, Downloadable delegate) 
		{
			_handler = h;
			_delegate = delegate;
		}

		public void run() 
		{
			Boolean result = (Boolean) _delegate.execute();
			Message msg = _handler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean(OK_KEY, result);
			msg.setData(b);
			_handler.sendMessage(msg);
		}		
	}
}
