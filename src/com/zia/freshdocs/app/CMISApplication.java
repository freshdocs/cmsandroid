package com.zia.freshdocs.app;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import android.app.Application;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.zia.freshdocs.Constants;
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
	
	protected StringBuilder getAppStoragePath()
	{
		StringBuilder targetPath = new StringBuilder();
		
		File sdCard = Environment.getExternalStorageDirectory();
		
		if(sdCard != null)
		{
			targetPath.append(sdCard.getAbsolutePath()).append(File.separator);
			String packageName = Constants.class.getPackage().getName();
			targetPath.append(packageName);
		}
		
		return targetPath;
	}
	
	public File getFile(String name)
	{
		File sdCard = Environment.getExternalStorageDirectory();
		StringBuilder targetPath = getAppStoragePath();
		
		if(sdCard.canWrite() && targetPath.length() > 0)
		{
			File appStorage = new File(targetPath.toString());
			
			if(!appStorage.exists())
			{
				if(!appStorage.mkdir())
				{
					return null;
				}
			}
			
			targetPath.append(File.separator).append(name);
			File target = new File(targetPath.toString());
			
			try
			{
				if(target.exists())
				{
					target.delete();
				}
				
				if(target.createNewFile())
				{
					target.deleteOnExit();
					return target;
				}
			}
			catch (IOException e)
			{
				Log.e("FILE_ERROR", "Error in getFileStream", e);
			}
		}
		
		return null;
	}
	
	public void cleanupCache()
	{
		StringBuilder appStoragePath = getAppStoragePath();
		File storage = new File(appStoragePath.toString());
		
		if(storage.exists() && storage.isDirectory())
		{
			File[] files = storage.listFiles(new FileFilter()
			{	
				public boolean accept(File pathname)
				{
					return !pathname.isDirectory();
				}
			});
			
			int n = files.length;
			File file = null;
			
			for(int i = 0; i < n; i++)
			{
				file = files[i];
				file.delete();
			}
		}
	}
}
