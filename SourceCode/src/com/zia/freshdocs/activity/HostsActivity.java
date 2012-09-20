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
package com.zia.freshdocs.activity;

import java.util.Collection;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.zia.freshdocs.R;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.model.Constants;
import com.zia.freshdocs.preference.CMISHost;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.widget.adapter.HostAdapter;
import com.zia.freshdocs.widget.quickaction.QuickActionWindow;

public class HostsActivity extends ListActivity implements OnItemLongClickListener
{
	private static final String INITIALIZED_KEY = "initialized";
	public static final String REQUESTED_FROM_HOME = "requested_from_home";
	private static final int NEW_HOST_REQ = 0;
	private static final int EDIT_HOST_REQ = 1;
	private static final int SPLASH_REQUEST_REQ = 2;

	private boolean isCalledByHome = false;
	public static boolean isExiting = false;
	private SharedPreferences mPrefs;
	private boolean hostsScreenShown;
	private final String hostsScreenShownPref = "hostsScreenShown";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		this.setTheme(R.style.Theme_HoloEverywhereLight);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.hosts);
		
		Intent intent = getIntent();
		if(intent.hasExtra(REQUESTED_FROM_HOME)) {
			isCalledByHome = true;
		}
		
		getListView().setOnItemLongClickListener(this);
		
		if(!isCalledByHome && (savedInstanceState == null || !savedInstanceState.getBoolean(INITIALIZED_KEY)))
		{
			startActivityForResult(new Intent(this, SplashActivity.class), SPLASH_REQUEST_REQ);
		}else{
			initializeHostList();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		CMISApplication app = (CMISApplication) getApplication();
		app.cleanupCache();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(INITIALIZED_KEY, true);
	}	

	protected void initializeHostList()
	{
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		Collection<CMISHost> prefs = prefsMgr.getAllPreferences(this);
		
		HostAdapter serverAdapter = new HostAdapter(this, 
				R.layout.host_list_item, R.id.host_textview,
				prefs.toArray(new CMISHost[]{}));
		setListAdapter(serverAdapter);
		
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		MenuInflater inflater = getMenuInflater();
//	    inflater.inflate(R.menu.hosts_menu, menu);    
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item)
//	{
//		switch (item.getItemId())
//		{
//		case R.id.menu_add_server:
//			addServer();
//			return true;
//		case R.id.menu_item_favorites:
//			Intent favoritesIntent = new Intent(this, FavoritesActivity.class);
//			startActivityForResult(favoritesIntent, 0);
//			return true;
//		case R.id.menu_item_about:
//			Intent aboutIntent = new Intent(this, AboutActivity.class);
//			startActivity(aboutIntent);
//			return true;
//		case R.id.menu_item_quit:
//			this.finish();
//			return true;
//		default:
//			return false;
//		}
//	}

	protected void addServer()
	{
		Intent newHostIntent = new Intent(HostsActivity.this, HostPreferenceActivity.class);
		startActivityForResult(newHostIntent, NEW_HOST_REQ);
	}
	
	/**
	 * Handles rotation by doing nothing (instead of onCreate being called)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}
	protected void deleteServer(String id)
	{
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		prefsMgr.deletePreferences(this, id);
		initializeHostList();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode)
		{
		case NEW_HOST_REQ:		
		case EDIT_HOST_REQ:
			initializeHostList();
			break;
		case SPLASH_REQUEST_REQ:	
			if(!isCalledByHome){
				// Show hosts screen on first launch only
				mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
				hostsScreenShown = mPrefs.getBoolean(hostsScreenShownPref, false);
				
				if (!hostsScreenShown) { // first launch
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putBoolean(hostsScreenShownPref, true);
					hostsScreenShown = true;
					editor.commit(); 
					initializeHostList();   
				}else{
					// Go to HomeActivity
					Intent homeIntent = new Intent(HostsActivity.this, HomeActivity.class);
					homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(homeIntent);
					finish();
				}
			}
			break;
		}
	}
	
	protected void onSearch()
	{
		onSearchRequested();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
//		final CMISApplication app = (CMISApplication) getApplication();
//		final Context ctx = this;
		final HostAdapter adapter = (HostAdapter) getListAdapter();
//		final View container = v;
		CMISHost pref = adapter.getItem(position);
		
		if(pref.getId().equals(Constants.NEW_HOST_ID))
		{
			addServer();
			return;
		}
		
		CMISHost prefs = adapter.getItem(position);
		
		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this.getApplicationContext());
		
		Editor prefsEditor = appSharedPrefs.edit();
		Gson gson = new Gson();
		
		String json = gson.toJson(prefs);
		prefsEditor.putString(Constants.CMISHOST, json);
		prefsEditor.commit();

		Intent homeIntent = new Intent(HostsActivity.this, HomeActivity.class);
		homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeIntent);
		finish();
		
	}
	

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view,
			int position, long idValue) {

		HostAdapter adapter = (HostAdapter) getListAdapter();
		CMISHost host = adapter.getItem(position);

		if (!host.getId().equals(Constants.NEW_HOST_ID)) {
			CMISHost prefs = (CMISHost) getListAdapter().getItem(position);
			final String id = prefs.getId();

			// array to hold the coordinates of the clicked view
			int[] xy = new int[2];
			// fills the array with the computed coordinates
			view.getLocationInWindow(xy);
			// rectangle holding the clicked view area
			Rect rect = new Rect(xy[0], xy[1], xy[0] + view.getWidth(), xy[1]
					+ view.getHeight());

			// a new QuickActionWindow object
			final QuickActionWindow quickAction = new QuickActionWindow(
					HostsActivity.this, view, rect);

			quickAction.addItem(getResources().getDrawable(R.drawable.context_edit),
					getString(R.string.edit_server), new OnClickListener() {
						public void onClick(View v) {
							quickAction.dismiss();
							Intent newHostIntent = new Intent(HostsActivity.this,HostPreferenceActivity.class);
							newHostIntent.putExtra(HostPreferenceActivity.EXTRA_EDIT_SERVER,id);
							startActivityForResult(newHostIntent, EDIT_HOST_REQ);
						}
					});

			quickAction.addItem(getResources().getDrawable(R.drawable.context_delete),
					getString(R.string.delete_server), new OnClickListener() {
						public void onClick(View v) {
							deleteServer(id);
							quickAction.dismiss();
						}
					});
			// shows the quick action window on the screen
			quickAction.show();
		}

		return false;
	}
}
