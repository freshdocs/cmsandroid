package com.zia.freshdocs.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.zia.freshdocs.R;

public class Preferences extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
