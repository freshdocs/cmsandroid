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

import java.util.Set;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.zia.freshdocs.Constants;
import com.zia.freshdocs.Constants.NetworkStatus;
import com.zia.freshdocs.R;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.widget.adapter.CMISAdapter;
import com.zia.freshdocs.widget.quickaction.QuickActionWindow;

public class NodeBrowseActivity extends DashboardActivity implements OnItemLongClickListener
{
	private static final String HOST_ID_KEY = "id";
	
	protected CMISAdapter mAdapter;
	protected boolean mAdapterInitialized = false;
	private QuickActionWindow mQuickAction;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_HoloEverywhereLight);
		super.onCreate(savedInstanceState);

		restoreCMIS(savedInstanceState);

		initializeListView();
		
		getListView().setOnItemLongClickListener(this);

		if (!mAdapterInitialized && mAdapter != null && mAdapter.getCmis() != null) {
			mAdapterInitialized = true;
			mAdapter.home();
		}
	}
	
	protected void restoreCMIS(Bundle savedInstanceState){
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(HOST_ID_KEY)) {
			String id = savedInstanceState.getString(HOST_ID_KEY);
			CMISApplication app = (CMISApplication) getApplication();
			app.initCMIS(id);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		CMISApplication app = (CMISApplication) getApplication();
		CMIS cmis = app.getCMIS();

		if (cmis != null) {
			outState.putString(HOST_ID_KEY, cmis.getPrefs().getId());
		}
	}

	/**
	 * Handles rotation by doing nothing (instead of onCreate being called)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.browser, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_refresh:
			mAdapter.refresh();
			return true;
		case R.id.menu_item_search:
			onSearch();
			return true;
		case R.id.menu_item_favorites:
			Intent favoritesIntent = new Intent(this, FavoritesActivity.class);
			startActivityForResult(favoritesIntent, 0);
			return true;
		case R.id.menu_item_about:
			Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivityForResult(aboutIntent, 0);
			return true;
		case R.id.menu_item_quit:
			onQuit();
			return true;
		default:
			return false;
		}
	}
	
	protected void onQuit() {
		Intent quitIntent = new Intent();
		quitIntent.putExtra(Constants.QUIT, true);
		setResult(RESULT_OK, quitIntent);
		finish();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		int position = ((AdapterContextMenuInfo) menuInfo).position;

		if (!mAdapter.isFolder(position)) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.node_context_menu, menu);
			MenuItem item = menu.findItem(R.id.menu_item_favorite);

			NodeRef ref = mAdapter.getItem(position);
			CMISPreferencesManager prefsMgr = CMISPreferencesManager
					.getInstance();
			Set<NodeRef> favorites = prefsMgr.getFavorites(this);

			if (favorites.contains(ref)) {
				item.setTitle(R.string.remove_favorite);
			}

		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId())
		{
		case R.id.menu_item_send:	
			mAdapter.shareContent(info.position);
			return true;
		case R.id.menu_item_favorite:
			mAdapter.toggleFavorite(info.position);
			return true;
		}
		
		return false;
	}

	protected void initializeListView() {
		CMISApplication app = (CMISApplication) getApplication();
		CMIS cmis = app.getCMIS();

		if (cmis != null && cmis.getNetworkStatus() == NetworkStatus.OK) {
			setContentView(R.layout.nodes);
		} else {
			setContentView(R.layout.nodes_offline);
		}

		mAdapter = new CMISAdapter(this, R.layout.node_ref_item, R.id.node_ref_label);
		mAdapter.setCmis(cmis);
		setListAdapter(mAdapter);

//		if (cmis != null) {
//			Resources res = getResources();
//			StringBuilder title = new StringBuilder(
//					res.getString(R.string.app_name)).append(" - ").append(
//					cmis.getPrefs().getHostname());
//			setTitle(title.toString());
//		}
	}	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mAdapter != null
				&& mAdapter.hasPrevious()) {
			mAdapter.previous();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mAdapter.getChildren(position);
	}

	protected void onSearch() {
		onSearchRequested();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null && data.hasExtra(Constants.QUIT)) {
			onQuit();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view,
			final int position, long value) {
		final boolean isFile;
		
		// array to hold the coordinates of the clicked view
		int[] xy = new int[2];
		// fills the array with the computed coordinates
		view.getLocationInWindow(xy);
		// rectangle holding the clicked view area
		Rect rect = new Rect(xy[0], xy[1], xy[0] + view.getWidth(), xy[1] + view.getHeight());
		
		// a new QuickActionWindow object
		mQuickAction = new QuickActionWindow(
				NodeBrowseActivity.this, view, rect);

		if (!mAdapter.isFolder(position)) {
			isFile = true;
			NodeRef ref = mAdapter.getItem(position);
			CMISPreferencesManager prefsMgr = CMISPreferencesManager
					.getInstance();
			Set<NodeRef> favorites = prefsMgr.getFavorites(this);

			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					getString(R.string.send), new OnClickListener() {
						public void onClick(View v) {
							mQuickAction.dismiss();
							mAdapter.shareContent(position);
						}
					});

			String favoriteTitle = getString(R.string.add_favorite);
			if (favorites.contains(ref)) {
				favoriteTitle = getString(R.string.remove_favorite);
			}

			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					favoriteTitle, new OnClickListener() {
						public void onClick(View v) {
							mQuickAction.dismiss();
							mAdapter.toggleFavorite(position);
						}
					});

			// Show file information

			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					getString(R.string.str_file_information),
					new OnClickListener() {
						public void onClick(View v) {
							mAdapter.showFileInfo(NodeBrowseActivity.this, position, isFile);
							mQuickAction.dismiss();
						}
					});

		} else {
			isFile = false;
			// Show folder information
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					getString(R.string.str_folder_information),
					new OnClickListener() {
						public void onClick(View v) {
							mAdapter.showFileInfo(NodeBrowseActivity.this, position, isFile);	
							mQuickAction.dismiss();
						}
					});
		}
		// shows the quick action window on the screen
		mQuickAction.show();

		return false;
	}
	
}
