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

import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.zia.freshdocs.R;
import com.zia.freshdocs.Constants.NetworkStatus;
import com.zia.freshdocs.cmis.CMIS;

public class SearchActivity extends NodeBrowseActivity {
	private boolean _isDirty = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_HoloEverywhereLight);
		mAdapterInitialized = true;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		registerForContextMenu(getListView());

		StringBuilder title = new StringBuilder(getTitle());
		title.append(" - ").append(getResources().getString(R.string.search));
		setTitle(title.toString());
	}

	@Override
	protected void onResume() {
		super.onResume();
		handleSearchIntent();
	}

	/**
	 * Handles rotation by doing nothing (instead of onCreate being called)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		_isDirty = true;
	}

	protected void handleSearchIntent() {
		Intent queryIntent = getIntent();
		String queryAction = queryIntent.getAction();

		if (Intent.ACTION_SEARCH.equals(queryAction) && _isDirty) {
			CMIS cmis = mAdapter.getCmis();
			if (cmis == null || cmis.getNetworkStatus() != NetworkStatus.OK) {
				int duration = Toast.LENGTH_SHORT;
				int error_id = cmis == null ? R.string.search_ambiguaous
						: R.string.offline_search_error;
				Toast toast = Toast.makeText(this, error_id, duration);
				toast.show();
				finish();
			} else {
				String queryString = queryIntent
						.getStringExtra(SearchManager.QUERY);
				search(queryString);
				_isDirty = false;
			}
		}
	}

	protected void search(String term) {
		mAdapter.query(term);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mAdapter.getChildren(position);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search, menu);
		return true;
	}
}
