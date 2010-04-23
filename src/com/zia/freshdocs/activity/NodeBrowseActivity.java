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

import java.util.Set;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.zia.freshdocs.Constants;
import com.zia.freshdocs.R;
import com.zia.freshdocs.Constants.NetworkStatus;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.widget.CMISAdapter;

public class NodeBrowseActivity extends ListActivity
{
	private static final String HOST_ID_KEY = "id";
	
	protected CMISAdapter _adapter;
	protected boolean _adapterInitialized = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		restoreCMIS(savedInstanceState);
		
		initializeListView();
		registerForContextMenu(getListView());
		
		if(!_adapterInitialized && _adapter != null && _adapter.getCmis() != null)
		{
			_adapterInitialized = true;
			_adapter.home();
		}
	}
	
	protected void restoreCMIS(Bundle savedInstanceState)
	{
		if(savedInstanceState != null && savedInstanceState.containsKey(HOST_ID_KEY))
		{
			String id = savedInstanceState.getString(HOST_ID_KEY);
			CMISApplication app = (CMISApplication) getApplication();
			app.initCMIS(id);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		CMISApplication app = (CMISApplication) getApplication();
		CMIS cmis = app.getCMIS();
		
		if(cmis != null)
		{
			outState.putString(HOST_ID_KEY, cmis.getPrefs().getId());
		}
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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.browser, menu);    
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_item_refresh:
			_adapter.refresh();
			return true;
		case R.id.menu_item_search:
			onSearch();
			return true;
		case R.id.menu_item_favorites:
			Intent favoritesIntent = new Intent(this, FavoritesActivity.class);
			startActivityForResult(favoritesIntent, 0);
			return true;
		case R.id.menu_item_about:
			Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivityForResult(aboutIntent, 0);
			return true;
		case R.id.menu_item_quit:
			onQuit();
			return true;
		default:
			return false;
		}
	}
	
	protected void onQuit()
	{
		Intent quitIntent = new Intent();
		quitIntent.putExtra(Constants.QUIT, true);
		setResult(RESULT_OK, quitIntent);
		finish();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo)
	{
		int position = ((AdapterContextMenuInfo) menuInfo).position;
		
		if(!_adapter.isFolder(position))
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.node_context_menu, menu);
			MenuItem item = menu.findItem(R.id.menu_item_favorite);
			
			NodeRef ref = _adapter.getItem(position);
			CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
			Set<NodeRef> favorites = prefsMgr.getFavorites(this);

			if(favorites.contains(ref))
			{
				item.setTitle(R.string.remove_favorite);
			}

		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId())
		{
		case R.id.menu_item_send:	
			_adapter.shareContent(info.position);
			return true;
		case R.id.menu_item_favorite:
			_adapter.toggleFavorite(info.position);
			return true;
		}
		
		return false;
	}

	protected void initializeListView()
	{
		CMISApplication app = (CMISApplication) getApplication();
		CMIS cmis = app.getCMIS();

		if(cmis != null && cmis.getNetworkStatus() == NetworkStatus.OK)
		{
			setContentView(R.layout.nodes);			
		}
		else
		{
			setContentView(R.layout.nodes_offline);						
		}

		_adapter = new CMISAdapter(this, R.layout.node_ref_item, R.id.node_ref_label);
		_adapter.setCmis(cmis);
		setListAdapter(_adapter);

		if(cmis != null)
		{
			Resources res = getResources();
			StringBuilder title = new StringBuilder(res.getString(R.string.app_name)).append(" - ").
			append(cmis.getPrefs().getHostname());
			setTitle(title.toString());
		}
	}	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK && _adapter != null && _adapter.hasPrevious())
		{
			_adapter.previous();
			return true;
		} 
		else
		{
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		_adapter.getChildren(position);
	}
	
	protected void onSearch()
	{
		onSearchRequested();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(data != null && data.hasExtra(Constants.QUIT))
		{
			onQuit();
		}
	}
}
