package com.zia.freshdocs.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.zia.freshdocs.R;

public class Main extends Activity
{
	public static final int SETTINGS_ITEM = 0;
	public static final int QUIT_ITEM = 1;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (!prefs.contains("hostname"))
		{
			Intent prefsIntent = new Intent(this, Preferences.class);
			startActivity(prefsIntent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, SETTINGS_ITEM, 0, R.string.settings);
		menu.add(0, QUIT_ITEM, 0, R.string.quit);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case SETTINGS_ITEM:
				Intent prefsIntent = new Intent(this, Preferences.class);
				startActivity(prefsIntent);
				return true;
			case QUIT_ITEM:
				this.finish();
				return true;
			default:
				return false;
		}
	}	

}