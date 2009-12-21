package com.zia.freshdocs.app;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import android.app.Application;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.zia.freshdocs.Constants;
import com.zia.freshdocs.R;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.model.NodeRef;
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

	public boolean initCMIS(String id)
	{
		boolean status = false;

		CMISPreferencesManager prefMgr = CMISPreferencesManager.getInstance();
		CMISHost prefs = prefMgr.getPreferences(this, id);
		
		if(prefs != null)
		{
			_cmis = new CMIS(prefs);

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
	
	public File getFile(String name, long filesize)
	{
		File sdCard = Environment.getExternalStorageDirectory();
		StringBuilder targetPath = getAppStoragePath();
		
		if(sdCard.canWrite() && targetPath.length() > 0)
		{
			File appStorage = new File(targetPath.toString());
			
			if(!appStorage.exists())
			{
				if(!appStorage.mkdirs())
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
					 if(target.length() != filesize && filesize != -1)
					 {
						 target.delete();
					 }
					 else
					 {
						 return target;
					 }
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
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		final Set<NodeRef> favorites = prefsMgr.getFavorites(this);
		File storage = new File(appStoragePath.toString());
					
		if(favorites != null)
		{
			if(storage.exists() && storage.isDirectory())
			{
				File[] files = storage.listFiles(new FileFilter()
				{	
					public boolean accept(File pathname)
					{
						NodeRef ref = new NodeRef();
						ref.setName(pathname.getName());
						
						int index = Collections.binarySearch(new ArrayList<NodeRef>(favorites), 
								ref, new Comparator<NodeRef>()
						{
							public int compare(NodeRef object1, NodeRef object2)
							{
								return object1.getName().compareTo(object2.getName());
							}
						});
						return index < 0 && !pathname.isDirectory();
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
	
	public void cleanupHostCache(String id)
	{
		StringBuilder appStoragePath = getAppStoragePath();
		appStoragePath.append(File.separator).append(id);
		File cacheDir = new File(appStoragePath.toString());

		try
		{
			FileUtils.deleteDirectory(cacheDir);
		}
		catch (IOException e)
		{
			Log.e("ERROR", "Failed in cleanupHostCache", e);
		}
	}
}
