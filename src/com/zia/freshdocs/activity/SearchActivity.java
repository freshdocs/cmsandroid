package com.zia.freshdocs.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.zia.freshdocs.R;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.cmis.CMIS.NetworkStatus;

public class SearchActivity extends NodeBrowseActivity
{
	private boolean _isDirty = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		registerForContextMenu(getListView());		
		_adapterInitialized = true;
		
		StringBuilder title = new StringBuilder(getTitle());
		title.append(" - ").append(getResources().getString(R.string.search));
		setTitle(title.toString());
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		handleSearchIntent();
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
	protected void onNewIntent(Intent intent)
	{
		setIntent(intent);
		_isDirty = true;
	}

	protected void handleSearchIntent()
	{
		Intent queryIntent = getIntent();
		String queryAction = queryIntent.getAction();
		
		if (Intent.ACTION_SEARCH.equals(queryAction) && _isDirty) 
		{
			CMIS cmis = _adapter.getCmis();
			if(cmis == null || cmis.getNetworkStatus() != NetworkStatus.OK)
			{
				int duration = Toast.LENGTH_SHORT;
				int error_id = cmis == null ? R.string.search_ambiguaous : 
					R.string.offline_search_error;
				Toast toast = Toast.makeText(this, error_id, duration);
				toast.show();
				finish();
			}
			else
			{
				String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
				search(queryString);
				_isDirty = false;
			}
		}				
	}
	
	protected void search(String term)
	{
		_adapter.query(term);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		_adapter.getChildren(position);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.search, menu);    
		return true;
	}	
}
