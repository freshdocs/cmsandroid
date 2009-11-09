package com.zia.freshdocs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class Welcome extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		
		if(!prefs.contains("default.server_name"))
		{
			Intent prefsIntent = new Intent(this, Preferences.class);
			startActivity(prefsIntent);
		}
	}
}