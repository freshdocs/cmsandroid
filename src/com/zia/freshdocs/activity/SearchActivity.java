package com.zia.freshdocs.activity;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.zia.freshdocs.R;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.widget.CMISAdapter;

public class SearchActivity extends ListActivity
{
	private CMISAdapter _adapter = null;
	private boolean _isDirty = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// CMIS should already be initialized at this point
		CMISApplication app = (CMISApplication) getApplication();
		_adapter = new CMISAdapter(this, android.R.layout.simple_list_item_1);
		_adapter.setCmis(app.getCMIS());
		setListAdapter(_adapter);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		handleSearchIntent();
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
			if(_adapter.getCmis() == null)
			{
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(this, R.string.search_ambiguaous, duration);
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
}
