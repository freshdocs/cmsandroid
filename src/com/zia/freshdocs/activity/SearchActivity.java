package com.zia.freshdocs.activity;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.zia.freshdocs.widget.CMISAdapter;

public class SearchActivity extends ListActivity
{
	private CMISAdapter _adapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent queryIntent = getIntent();
		String queryAction = queryIntent.getAction();

		_adapter = new CMISAdapter(this, android.R.layout.simple_list_item_1);
		setListAdapter(_adapter);
		
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
			search(queryString);
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
