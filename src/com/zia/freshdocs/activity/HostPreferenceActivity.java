package com.zia.freshdocs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
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
			((EditText) findViewById(R.id.webapp_root)).setText(
					(String) hostPrefs.getWebappRoot()); 			
			((EditText) findViewById(R.id.port_edittext)).setText(Integer.toString(
					(int) hostPrefs.getPort())); 			
			((CheckBox) findViewById(R.id.ssl)).setChecked(hostPrefs.isSSL()); 			
			((CheckBox) findViewById(R.id.hidden_files)).setChecked(hostPrefs.isShowHidden()); 			
		}
	}
	
	protected void onServerOkClick(View v)
	{
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();

		String hostname = ((EditText) findViewById(R.id.hostname_edittext)).getText().toString();
		String username = ((EditText) findViewById(R.id.username_edittext)).getText().toString();
		String password = ((EditText) findViewById(R.id.password_edittext)).getText().toString();
		String webappRoot = ((EditText) findViewById(R.id.webapp_root)).getText().toString();
		boolean isSSL = ((CheckBox) findViewById(R.id.ssl)).isChecked();
		boolean showHidden = ((CheckBox) findViewById(R.id.hidden_files)).isChecked();
		
		int port = 80;
		
		String portVal = ((EditText) findViewById(
				R.id.port_edittext)).getText().toString();
		if(portVal != null)
		{
			port = Integer.parseInt(portVal);
		}

		CMISHost hostPrefs = new CMISHost();
		hostPrefs.setHostname(hostname);
		hostPrefs.setUsername(username);
		hostPrefs.setPassword(password);
		hostPrefs.setPort(port);
		hostPrefs.setSSL(isSSL);
		hostPrefs.setShowHidden(showHidden);
		
		if(webappRoot != null && webappRoot.length() > 0)
		{
			hostPrefs.setWebappRoot(webappRoot);
		}
			
		prefsMgr.setPreferences(this, hostPrefs);
		
		finish();
	}
}

