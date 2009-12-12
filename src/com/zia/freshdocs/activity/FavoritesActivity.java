package com.zia.freshdocs.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;

import com.zia.freshdocs.R;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.preference.CMISHost;

public class FavoritesActivity extends NodeBrowseActivity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorites);
		registerForContextMenu(getListView());
		_adapterInitialized = true;
		
		StringBuilder title = new StringBuilder(getTitle());
		title.append(" - ").append(getResources().getString(R.string.favorites));		
		setTitle(title.toString());
	}
	
	@Override
	protected void onResume() 
	{		
		super.onResume();
		initializeFavorites();
	};
	
	protected void initializeFavorites()
	{
		_adapter.clear();
		
		CMISHost prefs = _adapter.getCmis().getPrefs();
		
		if(prefs != null)
		{
			for(NodeRef ref : prefs.getFavorites())
			{
				_adapter.add(ref);
			}
		}
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
	    inflater.inflate(R.menu.favorites, menu);    
		return true;
	}
}
