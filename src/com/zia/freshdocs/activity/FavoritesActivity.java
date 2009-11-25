package com.zia.freshdocs.activity;

import java.io.File;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.zia.freshdocs.R;

public class FavoritesActivity extends ListActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorites);
		initializeFavorites();
	}
	
	protected void initializeFavorites()
	{
		ArrayAdapter<String> favorites = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1);
		
		String[] files = fileList();
		int n = files.length;
		
		for(int i = 0; i < n; i++)
		{
			favorites.add(files[i]);
		}
		
		setListAdapter(favorites);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		viewFile((String) getListAdapter().getItem(position));
	}
	
	protected void viewFile(String name)
	{
		File file = getFileStreamPath(name);
		Uri uri = Uri.fromFile(file);
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		viewIntent.setData(uri);
//		viewIntent.setDataAndType(uri, "text/plain");
		try
		{
			startActivity(viewIntent);
		}
		catch(ActivityNotFoundException e)
		{
			String text = "No viewer found for " + name;
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
		}
	}
}
