package com.zia.freshdocs.preference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.res.Resources;

import com.zia.freshdocs.R;

public class MapPreferencesManager
{
	private static final String PREFS_DAT = "prefs.dat";

	// Private constructor prevents instantiation from other classes
	private MapPreferencesManager()
	{
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder
	{
		private static final MapPreferencesManager INSTANCE = new MapPreferencesManager();
	}

	public static MapPreferencesManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	// The returned map should be CONCURRENT!
	@SuppressWarnings("unchecked")
	public synchronized Map<String, Object> readPreferences(Context ctx)
	{
		ConcurrentHashMap<String, Object> prefs = new ConcurrentHashMap<String, Object>();
		Resources res = ctx.getResources();

		try
		{
			FileInputStream fis = ctx.openFileInput(res
					.getString(R.string.preference_filename));
			byte[] repr = IOUtils.toByteArray(fis);
			ByteArrayInputStream bais = new ByteArrayInputStream(repr);
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object obj = ois.readObject();

			if (obj != null)
			{
				prefs = (ConcurrentHashMap<String, Object>) obj;
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return prefs;
	}

	public void storePreferences(Context ctx, Map<String, Object> map)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(map);
			oos.close();

			FileOutputStream fos = ctx.openFileOutput(PREFS_DAT,
					Context.MODE_PRIVATE);
			fos.write(baos.toByteArray());
			fos.flush();
			fos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}
