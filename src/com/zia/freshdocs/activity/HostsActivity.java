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
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.zia.freshdocs.Constants;
import com.zia.freshdocs.R;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.util.Downloadable;
import com.zia.freshdocs.widget.HostAdapter;

public class HostsActivity extends ListActivity
{
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
//		startActivityForResult(new Intent(this, SplashActivity.class), SPLASH_REQUEST_REQ);		
		initializeHostList();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	protected void initializeHostList()
	{
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		Collection<String> keys = prefsMgr.getHostnames(this);
		HostAdapter serverAdapter = new HostAdapter(this, 
				R.layout.host_list_item, R.id.host_textview,
				keys.toArray(new String[]{}));
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
			Intent newHostIntent = new Intent(this, HostPreferenceActivity.class);
			startActivityForResult(newHostIntent, NEW_HOST_REQ);
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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.host_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String hostname = (String) getListAdapter().getItem(info.position);

		switch (item.getItemId())
		{
		case R.id.menu_edit_server:
			Intent newHostIntent = new Intent(this, HostPreferenceActivity.class);
			newHostIntent.putExtra(HostPreferenceActivity.EXTRA_EDIT_SERVER, hostname);
			startActivityForResult(newHostIntent, EDIT_HOST_REQ);
			
			return true;
		case R.id.menu_delete_server:
			deleteServer(hostname);
			break;
		}
		
		return false;
	}

	protected void deleteServer(String hostname)
	{
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		prefsMgr.deletePreferences(this, hostname);
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
	
		TextView hostTextView = (TextView) v.findViewById(R.id.host_textview);
		final String hostname = hostTextView.getText().toString();

		adapter.toggleError(container, false);
		adapter.toggleProgress(container, true);
		
		_dlThread = new ChildDownloadThread(new Handler() 
		{
			public void handleMessage(Message msg) 
			{
				boolean ok = msg.getData().getBoolean(OK_KEY);

				adapter.toggleProgress(container, false);

				if(ok)
				{
					Intent browseIntent = new Intent(ctx, NodeBrowseActivity.class);
					startActivityForResult(browseIntent, NODE_BROWSE_REQ);
					adapter.toggleError(container, false);
				}			
				else
				{
					app.handleNetworkStatus();
					adapter.toggleError(container, true);
				}

			}
		}, 
		new Downloadable()
		{
			public Object execute()
			{
				return app.initCMIS(hostname);
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
