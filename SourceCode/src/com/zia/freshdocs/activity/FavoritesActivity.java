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

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;

import com.zia.freshdocs.R;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.preference.CMISPreferencesManager;

public class FavoritesActivity extends NodeBrowseActivity
 {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_HoloEverywhereLight);
		// Prevent call to home() in parent
		mAdapterInitialized = true;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorites);
		registerForContextMenu(getListView());
		mAdapter.setFavoritesView(true);

		Resources res = getResources();
		StringBuilder title = new StringBuilder(
				res.getString(R.string.app_name));
		title.append(" - ")
				.append(getResources().getString(R.string.favorites));
		setTitle(title.toString());
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeFavorites();
	};

	protected void initializeFavorites() {
		mAdapter.clear();

		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		Set<NodeRef> favorites = prefsMgr.getFavorites(this);

		for (NodeRef ref : favorites) {
			mAdapter.add(ref);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mAdapter.viewFavorite(position);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.favorites, menu);
		return true;
	}

}
