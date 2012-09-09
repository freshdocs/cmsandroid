package com.zia.freshdocs.util;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesAccess {
	/**
	 * Save value to SharedPreferences.
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */

	public static void saveValueToSharedPreferences(Context context, String key, String value) {
		SharedPreferences settings;
		SharedPreferences.Editor prefs;
		settings = context.getSharedPreferences(key, 0);
		prefs = settings.edit();
		prefs.putString(key, value);
		prefs.commit();
	}

	/**
	 * get value from SharedPreferences.
	 * 
	 * @param context
	 * @return key
	 */

	public static String getValueFromSharedPreferences(Context context, String key) {
		SharedPreferences settings = context.getSharedPreferences(key, 0);
		return settings.getString(key, "");
	}
	
	
	public static void saveJSONArrayToSharedPreferences(Context c, String key, JSONArray array) {
        SharedPreferences settings = c.getSharedPreferences(key, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, array.toString());
        editor.commit();
    }


	public static JSONArray loadJSONArrayToSharedPreferences(Context c, String key) throws JSONException {
        SharedPreferences settings = c.getSharedPreferences(key, 0);
        return new JSONArray(settings.getString(key, "[]"));
    }
}
