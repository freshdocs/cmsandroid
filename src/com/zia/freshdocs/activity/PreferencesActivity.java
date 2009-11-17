package com.zia.freshdocs.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

import com.zia.freshdocs.R;

public class PreferencesActivity extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{			
			setResult(RESULT_OK);
			finish();
			return true;
		} 
		else
		{
			return super.onKeyDown(keyCode, event);
		}
	}
}
