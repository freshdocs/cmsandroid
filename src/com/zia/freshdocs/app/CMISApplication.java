package com.zia.freshdocs.app;

import android.app.Application;
import android.content.res.Resources;
import android.widget.Toast;

import com.zia.freshdocs.R;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.preference.CMISHost;
import com.zia.freshdocs.preference.CMISPreferencesManager;

public class CMISApplication extends Application
{
	CMIS _cmis;
	String _reason;
	
	public CMISApplication()
	{
		super();
	}

	public CMIS getCMIS()
	{
		return _cmis;
	}

	public String getReason()
	{
		return _reason;
	}

	public boolean initCMIS(String hostname)
	{
		boolean status = false;

		CMISPreferencesManager prefMgr = CMISPreferencesManager.getInstance();
		CMISHost prefs = prefMgr.getPreferences(this, hostname);
		
		if(prefs != null)
		{
			_cmis = new CMIS(prefs.getHostname(), prefs.getUsername(), 
					prefs.getPassword(), prefs.getPort(), prefs.isSSL(), prefs.getWebappRoot());

			if (_cmis != null)
			{
				status = _cmis.authenticate() != null;
			}
		}
		
		return status;
	}

	public void handleNetworkStatus()
	{
		Resources res = getResources();
		_reason = res.getString(R.string.error_server_error);
		
		switch(_cmis.getNetworkStatus())
		{
		case CONNECTION_ERROR:
			_reason = res.getString(R.string.error_connection_failed);
			break;
		case CREDENTIALS_ERROR:
			_reason = res.getString(R.string.error_invalid_credentials);
			break;
		}

		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(this, _reason, duration);
		toast.show();
	}
}
