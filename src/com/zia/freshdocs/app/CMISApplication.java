package com.zia.freshdocs.app;

import android.app.Application;

import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.preference.CMISHost;
import com.zia.freshdocs.preference.CMISPreferencesManager;

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

	public boolean initCMIS(String hostname)
	{
		boolean status = false;

		CMISPreferencesManager prefMgr = CMISPreferencesManager.getInstance();
		CMISHost prefs = prefMgr.getPreferences(this, hostname);
		
		if(prefs != null)
		{
			_cmis = new CMIS(prefs.getHostname(), prefs.getUsername(), 
					prefs.getPassword(), prefs.getPort());

			if (_cmis != null)
			{
				status = _cmis.authenticate() != null;
			}
		}
		
		return status;
	}
}
