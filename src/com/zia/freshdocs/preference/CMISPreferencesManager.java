/*******************************************************************************
 * The MIT License
 * 
 * Copyright (c) 2010 Zia Consulting, Inc
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.zia.freshdocs.preference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.SerializationUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.zia.freshdocs.Constants;
import com.zia.freshdocs.R;
import com.zia.freshdocs.model.NodeRef;

public class CMISPreferencesManager
{
	private static final String FAVORITES_KEY = "favorites";
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
		String encPrefs = null;

		if(sharedPrefs.contains(SERVERS_KEY))
		{
			encPrefs = sharedPrefs.getString(SERVERS_KEY, null);

			if(encPrefs != null)
			{
				byte[] repr = Base64.decodeBase64(encPrefs.getBytes());
				Object obj = SerializationUtils.deserialize(repr);

				if (obj != null)
				{
					prefs = (ConcurrentHashMap<String, CMISHost>) obj;
				}					
			}
		}

		return prefs;
	}

	protected void storePreferences(Context ctx, Map<String, CMISHost> map)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);		
		byte[] encPrefs = Base64.encodeBase64(SerializationUtils.serialize((Serializable) map));
		Editor prefsEditor = sharedPrefs.edit();
		prefsEditor.putString(SERVERS_KEY, new String(encPrefs));
		prefsEditor.commit();
	}
	
	public CMISHost getPreferences(Context ctx, String id)
	{
		Map<String, CMISHost> prefs = readPreferences(ctx);
		
		if(prefs.containsKey(id))
		{
			return prefs.get(id);
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
		
		prefs.put(hostPrefs.getId(), hostPrefs);
		storePreferences(ctx, prefs);
	}
	
	public void deletePreferences(Context ctx, String id)
	{
		Map<String, CMISHost> prefs = readPreferences(ctx);

		if(id == null || prefs == null)
		{
			return;
		}
		
		if(prefs.containsKey(id))
		{
			prefs.remove(id);
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
	
	protected CMISHost createAddServer(Context ctx)
	{
		CMISHost host = new CMISHost();
		host.setId(Constants.NEW_HOST_ID);
		
		return host;
	}
	
	public Collection<CMISHost> getAllPreferences(Context ctx)
	{
		ArrayList<CMISHost> results = new ArrayList<CMISHost>();
		
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
			results.addAll(prefs.values());
			results.add(createAddServer(ctx));
		}		
		
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public Set<NodeRef> getFavorites(Context ctx)
	{
		Set<NodeRef> favorites = new HashSet<NodeRef>();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String enc = null;

		if(sharedPrefs.contains(FAVORITES_KEY))
		{
			enc = sharedPrefs.getString(FAVORITES_KEY, null);

			if(enc != null)
			{
				byte[] repr = Base64.decodeBase64(enc.getBytes());
				Object obj = SerializationUtils.deserialize(repr);

				if (obj != null)
				{
					favorites = (Set<NodeRef>) obj;
				}					
			}
		}
		
		return favorites;
	}
	
	public void storeFavorites(Context ctx, Set<NodeRef> favorites)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);		
		byte[] enc = Base64.encodeBase64(
				SerializationUtils.serialize((Serializable) favorites));
		Editor prefsEditor = sharedPrefs.edit();
		prefsEditor.putString(FAVORITES_KEY, new String(enc));
		prefsEditor.commit();
	}
}
