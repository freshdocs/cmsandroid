package com.zia.freshdocs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.zia.freshdocs.R;
import com.zia.freshdocs.preference.CMISHost;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.util.StringUtils;

public class HostPreferenceActivity extends Activity
{
	public static final String EXTRA_EDIT_SERVER = "edit_server";
	
	protected boolean _backPressed;
	
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK && !updateHost())
		{			
			if(!_backPressed)
			{
				_backPressed = true;
				return false;
			}
		} 

		return super.onKeyDown(keyCode, event);
	}


	protected boolean updateHost()
	{
		String hostname = ((EditText) findViewById(R.id.hostname_edittext)).getText().toString();
		String username = ((EditText) findViewById(R.id.username_edittext)).getText().toString();
		String password = ((EditText) findViewById(R.id.password_edittext)).getText().toString();
		String webappRoot = ((EditText) findViewById(R.id.webapp_root)).getText().toString();
		boolean isSSL = ((CheckBox) findViewById(R.id.ssl)).isChecked();
		boolean showHidden = ((CheckBox) findViewById(R.id.hidden_files)).isChecked();
		
		int port = 80;
		
		String portVal = ((EditText) findViewById(
				R.id.port_edittext)).getText().toString();

		if(StringUtils.isEmpty(portVal))
		{
			toastError("Port is a required field.");
			return false;
		}
		
		port = Integer.parseInt(portVal);

		CMISHost hostPrefs = new CMISHost();

		if(StringUtils.isEmpty(hostname))
		{
			toastError("Hostname is a required field.");
			return false;
		}
		
		hostPrefs.setHostname(hostname);
		
		if(StringUtils.isEmpty(username))
		{
			toastError("Username is a required field.");
			return false;
		}

		hostPrefs.setUsername(username);
		
		if(StringUtils.isEmpty(password))
		{
			toastError("Password is a required field.");
			return false;
		}
		
		hostPrefs.setPassword(password);
		hostPrefs.setPort(port);
		hostPrefs.setSSL(isSSL);
		hostPrefs.setShowHidden(showHidden);

		if(StringUtils.isEmpty(webappRoot))
		{
			toastError("URL is a required field.");
			return false;
		}
		
		hostPrefs.setWebappRoot(webappRoot);

		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		prefsMgr.setPreferences(this, hostPrefs);
		
		return true;
	}
	
	protected void toastError(String msg)
	{
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(this, msg + "\nPress back again to cancel editing.", duration);
		toast.show();
	}
}

