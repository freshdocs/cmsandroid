package com.zia.freshdocs.activity;

import java.util.Collection;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.zia.freshdocs.R;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.preference.CMISPreferencesManager;

public class HostsActivity extends ListActivity
{
	private static final int NEW_HOST_REQ = 0;
	private static final int EDIT_HOST_REQ = 1;
	private static final int SPLASH_REQUEST_CODE = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hosts);
		registerForContextMenu(getListView());
		startActivityForResult(new Intent(this, SplashActivity.class), SPLASH_REQUEST_CODE);		
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		initializeHostList();
	}
	
	protected void initializeHostList()
	{
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		Collection<String> keys = prefsMgr.getHostnames(this);
		ArrayAdapter<String> serverAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, 
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
		case R.id.menu_item_search:
			onSearch();
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
			startActivityForResult(newHostIntent, NEW_HOST_REQ);
			
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
		case SPLASH_REQUEST_CODE:			
			initializeHostList();
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
		CMISApplication app = (CMISApplication) getApplication();
		String hostname = ((TextView) v).getText().toString();
		
		if(app.initCMIS(hostname))
		{
			Intent browseIntent = new Intent(this, NodeBrowseActivity.class);
			startActivity(browseIntent);
		}
	}
}
