package com.zia.freshdocs.preference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.zia.freshdocs.R;

public class CMISPreferencesManager
{
	private static final String FIRST_RUN = "first_run";
	private static final String SERVERS_KEY = "servers";
	
	// Private constructor prevents instantiation from other classes
	private CMISPreferencesManager()
	{
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder
	{
		private static final CMISPreferencesManager INSTANCE = new CMISPreferencesManager();
	}

	public static CMISPreferencesManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, CMISHost> readPreferences(Context ctx)
	{
		ConcurrentHashMap<String, CMISHost> prefs = new ConcurrentHashMap<String, CMISHost>();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);

		try
		{
			String encPrefs = null;

			if(sharedPrefs.contains(SERVERS_KEY))
			{
				encPrefs = sharedPrefs.getString(SERVERS_KEY, null);
				
				if(encPrefs != null)
				{
					byte[] repr = Base64.decodeBase64(encPrefs.getBytes());
					ByteArrayInputStream bais = new ByteArrayInputStream(repr);
					ObjectInputStream ois = new ObjectInputStream(bais);
					Object obj = ois.readObject();

					if (obj != null)
					{
						prefs = (ConcurrentHashMap<String, CMISHost>) obj;
					}					
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return prefs;
	}

	protected void storePreferences(Context ctx, Map<String, CMISHost> map)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(map);
			byte[] encPrefs = Base64.encodeBase64(baos.toByteArray());
			oos.close();
			
			Editor prefsEditor = sharedPrefs.edit();
			prefsEditor.putString(SERVERS_KEY, new String(encPrefs));
			prefsEditor.commit();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public CMISHost getPreferences(Context ctx, String hostname)
	{
		Map<String, CMISHost> prefs = readPreferences(ctx);
		
		if(prefs.containsKey(hostname))
		{
			return prefs.get(hostname);
		}
		
		return null;
	}
	
	public void setPreferences(Context ctx, CMISHost hostPrefs)
	{
		if(hostPrefs == null)
		{
			return;
		}
		
		Map<String, CMISHost> prefs = readPreferences(ctx);
		
		if(prefs == null)
		{
			prefs = new HashMap<String, CMISHost>();
		}
		
		prefs.put(hostPrefs.getHostname(), hostPrefs);
		storePreferences(ctx, prefs);
	}
	
	public void deletePreferences(Context ctx, String hostname)
	{
		Map<String, CMISHost> prefs = readPreferences(ctx);

		if(hostname == null || prefs == null)
		{
			return;
		}
		
		if(prefs.containsKey(hostname))
		{
			prefs.remove(hostname);
			storePreferences(ctx, prefs);		
		}
	}
	
	protected boolean isFirstTime(Context ctx)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return !sharedPrefs.contains(FIRST_RUN) || sharedPrefs.getBoolean(FIRST_RUN, false);
	}
	
	protected CMISHost createDemoServer(Context ctx)
	{
		Resources res = ctx.getResources();
		
		CMISHost host = new CMISHost();
		host.setHostname(res.getString(R.string.demo_servername));
		host.setUsername(res.getString(R.string.demo_username));
		host.setPassword(res.getString(R.string.demo_password));
		
		return host;
	}
	
	public Set<String> getHostnames(Context ctx)
	{
		if(isFirstTime(ctx))
		{
			CMISHost host = createDemoServer(ctx);
			setPreferences(ctx, host);
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			Editor editor = sharedPrefs.edit();
			editor.putBoolean(FIRST_RUN, false);
			editor.commit();
		}
		
		Map<String, CMISHost> prefs = readPreferences(ctx);

		if(prefs != null)
		{
			return new TreeSet<String>(prefs.keySet());
		}		
		
		return new HashSet<String>();
	}
	
	public Collection<CMISHost> getAllPreferences(Context ctx)
	{
		if(isFirstTime(ctx))
		{
			CMISHost host = createDemoServer(ctx);
			setPreferences(ctx, host);
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			Editor editor = sharedPrefs.edit();
			editor.putBoolean(FIRST_RUN, false);
			editor.commit();
		}
		
		Map<String, CMISHost> prefs = readPreferences(ctx);

		if(prefs != null)
		{
			return prefs.values();
		}		
		
		return new ArrayList<CMISHost>();
	}
}
