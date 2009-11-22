package com.zia.freshdocs.app;

import java.util.Map;

import android.app.Application;

import com.zia.freshdocs.activity.HostPreferenceActivity;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.preference.MapPreferencesManager;

public class CMISApplication extends Application
{
	CMIS _cmis;
	
	public CMISApplication()
	{
		super();
	}

	public CMIS getCMIS()
	{
		return _cmis;
	}

	@SuppressWarnings("unchecked")
	public boolean initCMIS(String hostname)
	{
		boolean status = false;

		Map<String, Object> prefs = MapPreferencesManager.getInstance().readPreferences(this);
		
		if(prefs != null && prefs.containsKey(hostname))
		{
			Map<String, String> hostPrefs = (Map<String, String>) prefs.get(hostname);
			int port = 80;
			String username = (String) hostPrefs.get(HostPreferenceActivity.USERNAME_KEY); 
			String password = (String) hostPrefs.get(HostPreferenceActivity.PASSWORD_KEY);
			
			_cmis = new CMIS(hostname, username, password, port);

			if (_cmis != null)
			{
				status = _cmis.authenticate() != null;
			}
		}
		
		return status;
	}
}
