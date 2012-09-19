package com.zia.freshdocs.activity;

import android.app.Activity;
import android.os.Bundle;

import com.zia.freshdocs.R;
import com.zia.freshdocs.widget.UITableView;

/**
 * Activity with UITable layout
 *
 */
public abstract class UITableViewActivity extends Activity {

	private UITableView mTableView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_HoloEverywhereLight);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.uitableview_activity);
		mTableView = (UITableView) findViewById(R.id.tableView);
		populateList();
		mTableView.commit();
	}

	protected UITableView getUITableView() {
		return mTableView;
	}

	protected abstract void populateList();

}
