package com.zia.freshdocs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.zia.freshdocs.R;
import com.zia.freshdocs.preference.CMISHost;
import com.zia.freshdocs.preference.CMISPreferencesManager;

public class HostPreferenceActivity extends Activity
{
	public static final String EXTRA_EDIT_SERVER = "edit_server";

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

	protected void editServer(String servername)
	{
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		CMISHost hostPrefs = prefsMgr.getPreferences(this, servername);

		if(hostPrefs != null)
		{
			((EditText) findViewById(R.id.hostname_edittext)).setText(
					(String) hostPrefs.getHostname());
			((EditText) findViewById(R.id.username_edittext)).setText(
					(String) hostPrefs.getUsername());
			((EditText) findViewById(R.id.password_edittext)).setText(
					(String) hostPrefs.getPassword()); 			
			((EditText) findViewById(R.id.port_edittext)).setText(Integer.toString(
					(int) hostPrefs.getPort())); 			
		}
	}
	
	protected void onServerOkClick(View v)
	{
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();

		String hostname = ((EditText) findViewById(R.id.hostname_edittext)).getText().toString();
		String username = ((EditText) findViewById(R.id.username_edittext)).getText().toString();
		String password = ((EditText) findViewById(R.id.password_edittext)).getText().toString();
		int port = Integer.parseInt(((EditText) findViewById(
				R.id.port_edittext)).getText().toString());

		CMISHost hostPrefs = new CMISHost();
		hostPrefs.setHostname(hostname);
		hostPrefs.setUsername(username);
		hostPrefs.setPassword(password);
		hostPrefs.setPort(port);
			
		prefsMgr.setPreferences(this, hostPrefs);
		
		finish();
	}
}

