package com.zia.freshdocs.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.widget.CMISAdapter;

public class NodeBrowseActivity extends ListActivity
{
	private CMISAdapter _adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		registerForContextMenu(getListView());
		initializeListAdapter();
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
			startActivity(favoritesIntent);
			return true;
		case R.id.menu_item_about:
			Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivity(aboutIntent);
			return true;
		case R.id.menu_item_quit:
			Intent quitIntent = new Intent();
			quitIntent.putExtra(Constants.QUIT, true);
			setResult(RESULT_OK, quitIntent);
			finish();
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

	protected void initializeListAdapter()
	{
		CMISApplication app = (CMISApplication) getApplication();
		_adapter = new CMISAdapter(this, android.R.layout.simple_list_item_1);
		_adapter.setCmis(app.getCMIS());
		setListAdapter(_adapter);
		_adapter.home();
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
}