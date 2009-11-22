package com.zia.freshdocs.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.zia.freshdocs.R;
import com.zia.freshdocs.preference.MapPreferencesManager;

public class HostPreferenceActivity extends Activity
{
	public static final String EXTRA_EDIT_SERVER = "edit_server";

	public static final String HOSTNAME_KEY = "hostname";
	public static final String USERNAME_KEY = "username";
	public static final String PASSWORD_KEY = "password";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host_preference);
		
		Intent intent = getIntent();
		
		if(intent.hasExtra(EXTRA_EDIT_SERVER))
		{
			editServer(intent.getStringExtra(EXTRA_EDIT_SERVER));
		}
		
		Button okButton = (Button) findViewById(R.id.add_server_ok);
		okButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				onServerOkClick(v);
			}
		});

		Button cancelButton = (Button) findViewById(R.id.add_server_cancel);
		cancelButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
	}

	@SuppressWarnings("unchecked")
	protected void editServer(String servername)
	{
		MapPreferencesManager prefsMgr = MapPreferencesManager.getInstance();
		Map<String, Object> prefs = prefsMgr.readPreferences(this);

		if(prefs != null && prefs.containsKey(servername))
		{
			Map<String, Object> hostPrefs = (Map<String, Object>) prefs.get(servername);

			((EditText) findViewById(R.id.hostname_edittext)).setText(
					(String) hostPrefs.get(HOSTNAME_KEY)); 
			((EditText) findViewById(R.id.username_edittext)).setText(
					(String) hostPrefs.get(USERNAME_KEY)); 
			((EditText) findViewById(R.id.password_edittext)).setText(
					(String) hostPrefs.get(PASSWORD_KEY)); 			
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void onServerOkClick(View v)
	{
		MapPreferencesManager prefsMgr = MapPreferencesManager.getInstance();
		Map<String, Object> prefs = prefsMgr.readPreferences(this);

		String hostname = ((EditText) findViewById(R.id.hostname_edittext)).getText().toString();
		String username = ((EditText) findViewById(R.id.username_edittext)).getText().toString();
		String password = ((EditText) findViewById(R.id.password_edittext)).getText().toString();

		if(prefs != null)
		{
			Map<String, Object> hostPrefs = null;
			
			if(prefs.containsKey(hostname))
			{
				hostPrefs = (Map<String, Object>) prefs.get(hostname);
			}
			else
			{
				hostPrefs = new HashMap<String, Object>();
				prefs.put(hostname, hostPrefs);
			}
			
			hostPrefs.put(HOSTNAME_KEY, hostname);
			hostPrefs.put(USERNAME_KEY, username);
			hostPrefs.put(PASSWORD_KEY, password);
			
			prefsMgr.storePreferences(this, prefs);
		}
		
		finish();
	}
}

