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
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.cmis.CMIS.NetworkStatus;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.preference.CMISHost;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.widget.CMISAdapter;

public class NodeBrowseActivity extends ListActivity
{
	protected CMISAdapter _adapter;
	protected boolean _adapterInitialized = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		initializeListView();
		registerForContextMenu(getListView());
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if(!_adapterInitialized && _adapter != null)
		{
			_adapterInitialized = true;
			_adapter.home();
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
			
			CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
			String hostname = _adapter.getCmis().getHostname();
			CMISHost prefs = prefsMgr.getPreferences(this, hostname);
			NodeRef ref = _adapter.getItem(position);
			Set<NodeRef> favorites = prefs.getFavorites();
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

		if(cmis.getNetworkStatus() == NetworkStatus.OK)
		{
			setContentView(R.layout.nodes);			
		}
		else
		{
			setContentView(R.layout.nodes_offline);						
		}

		_adapter = new CMISAdapter(this, R.layout.node_ref_item, R.id.node_ref_label);
		_adapter.setCmis(app.getCMIS());
		setListAdapter(_adapter);

		Resources res = getResources();
		StringBuilder title = new StringBuilder(res.getString(R.string.app_name)).append(" - ").
			append(cmis.getHostname());
		setTitle(title.toString());
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