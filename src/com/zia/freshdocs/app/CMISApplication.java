package com.zia.freshdocs.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zia.freshdocs.cmis.CMIS;

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

	public boolean initCMIS()
	{
		boolean status = false;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		_cmis = new CMIS(prefs.getString("hostname", ""), 
				prefs.getString("username", ""),
				prefs.getString("password", ""), 
				Integer.parseInt(prefs.getString("port", "80")));
		
		if (_cmis != null)
		{
			status = _cmis.authenticate() != null;
		}
		
		return status;
	}
	
}
