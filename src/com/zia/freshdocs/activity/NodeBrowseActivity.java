package com.zia.freshdocs.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.zia.freshdocs.R;
import com.zia.freshdocs.widget.CMISAdapter;

public class NodeBrowseActivity extends ListActivity
{
	public static final int SETTINGS_ITEM = 0;
	public static final int QUIT_ITEM = 1;
	public static final int REFRESH_ITEM = 2;
	public static final int SEARCH_ITEM = 3;
	public static final int FAVORITES_ITEM = 4;
	
	private CMISAdapter _adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}


	@Override
	protected void onResume()
	{
		super.onResume();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (!prefs.contains("hostname"))
		{
			Intent prefsIntent = new Intent(this, PreferencesActivity.class);
			startActivity(prefsIntent);
		}
		else
		{
			initializeListAdapter();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		Resources res = getResources();
		menu.add(0, REFRESH_ITEM, 0, R.string.refresh).setIcon(
				res.getDrawable(R.drawable.refresh));
		menu.add(0, SETTINGS_ITEM, 0, R.string.settings).setIcon(
				res.getDrawable(android.R.drawable.ic_menu_preferences));
		menu.add(0, SEARCH_ITEM, 0, R.string.search).setIcon(
				res.getDrawable(android.R.drawable.ic_menu_search));
		menu.add(0, FAVORITES_ITEM, 0, R.string.favorites).setIcon(
				res.getDrawable(android.R.drawable.btn_star));
		menu.add(0, QUIT_ITEM, 0, R.string.quit).setIcon(
				res.getDrawable(android.R.drawable.ic_lock_power_off));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case REFRESH_ITEM:
			initializeListAdapter();
			return true;
		case SEARCH_ITEM:
			onSearch();
			return true;
		case SETTINGS_ITEM:
			Intent prefsIntent = new Intent(this, PreferencesActivity.class);
			startActivity(prefsIntent);
			return true;
		case QUIT_ITEM:
			this.finish();
			return true;
		default:
			return false;
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

	protected void initializeListAdapter()
	{
		if (_adapter == null)
		{
			_adapter = new CMISAdapter(this, android.R.layout.simple_list_item_1);
			setListAdapter(_adapter);
			_adapter.home();
		}
	}
	
	protected void onSearch()
	{
		onSearchRequested();
	}
}