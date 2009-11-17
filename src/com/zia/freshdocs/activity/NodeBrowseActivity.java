package com.zia.freshdocs.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.zia.freshdocs.R;
import com.zia.freshdocs.widget.CMISAdapter;

public class NodeBrowseActivity extends ListActivity
{
	private static final int SETTINGS_REQUEST_CODE = 0;
	private static final int SPLASH_REQUEST_CODE = 1;
	
	private CMISAdapter _adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		registerForContextMenu(getListView());
		startActivityForResult(new Intent(this, SplashActivity.class), SPLASH_REQUEST_CODE);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();		
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
	    inflater.inflate(R.menu.main, menu);    
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
		case R.id.menu_item_settings:
			Intent prefsIntent = new Intent(this, PreferencesActivity.class);
			startActivityForResult(prefsIntent, SETTINGS_REQUEST_CODE);
			return true;
		case R.id.menu_item_favorites:
			return true;
		case R.id.menu_item_quit:
			this.finish();
			return true;
		default:
			return false;
		}
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
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_item_send:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			_adapter.emailContent(info.position);
			return true;
		}
		
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode)
		{
		case SETTINGS_REQUEST_CODE:
			_adapter.initCMIS();
			_adapter.home();
			break;
		case SPLASH_REQUEST_CODE:			
			initializeListAdapter();
			_adapter.home();
			break;
		}
	}
	
	protected void initializeListAdapter()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!prefs.contains("hostname"))
		{
			Intent prefsIntent = new Intent(this, PreferencesActivity.class);
			startActivity(prefsIntent);
		} 
		else if (_adapter == null)
		{
			_adapter = new CMISAdapter(this, android.R.layout.simple_list_item_1);
			setListAdapter(_adapter);
		}
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK && _adapter.hasPrevious())
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
}