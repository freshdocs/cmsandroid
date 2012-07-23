/*
 * Copyright (C) 2011-2012 Wglxy.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zia.freshdocs.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.zia.freshdocs.Constants;
import com.zia.freshdocs.R;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.net.Downloadable;
import com.zia.freshdocs.preference.CMISHost;

/**
 * This is a simple activity that demonstrates the dashboard user interface
 * pattern.
 * 
 */

public class HomeActivity extends DashboardActivity {

	public static final int NUM_HOME_BUTTONS = 4;
	private static final int NODE_BROWSE_REQ = 1;
	private CMISHost mHost;
	private static final String OK_KEY = "ok";

	// Image resources for the buttons
	private Integer[] mImageIds = { R.drawable.home_button1,
			R.drawable.home_button2, R.drawable.home_button3,
			R.drawable.home_button4, R.drawable.home_button5,
			R.drawable.home_button6 };

	// Labels for the buttons
	private Integer[] mLabelIds = { R.string.title_feature1,
			R.string.title_feature2, R.string.title_feature3,
			R.string.title_feature4, R.string.title_feature5,
			R.string.title_feature6 };

	// Ids for the frames that define where the images go
	private Integer[] mFrameIds = { R.id.frame1, R.id.frame2, R.id.frame3,
			R.id.frame4, R.id.frame5, R.id.frame6 };

	/**
	 * onCreate - called when the activity is first created. Called when the
	 * activity is first created. This is where you should do all of your normal
	 * static set up: create views, bind data to lists, etc. This method also
	 * provides you with a Bundle containing the activity's previously frozen
	 * state, if there was one.
	 * 
	 * Always followed by onStart().
	 * 
	 */

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		/*
		 * GridView gridview = (GridView) findViewById(R.id.buttons_grid);
		 * gridview.setAdapter(new HomeButtonsAdapter(this));
		 */

		//
		// Add the buttons that make up the Dashboard.
		// We do this with a LayoutInflater. Doing it that way gives us more
		// control
		// over the size of the images and labels. Size values are defined in
		// the layout
		// for the image button (see activity_home_button.xml). Since each of
		// the different screen
		// sizes has their own dimens.xml file, you can adjust the sizes and
		// scaling as needed.
		// (Values folders: values, values-xlarge, values-sw600dp,
		// values-sw720p)
		//
		LayoutInflater li = this.getLayoutInflater();
		int imageButtonLayoutId = R.layout.activity_home_button;
		for (int j = 0; j < NUM_HOME_BUTTONS; j++) {
			int frameId = mFrameIds[j];
			int labelId = mLabelIds[j];
			int imageId = mImageIds[j];

			// Inflate a view for the image button. Set its image and label.
			View v = li.inflate(imageButtonLayoutId, null);
			ImageView iv = (ImageView) v.findViewById(R.id.home_btn_image);
			if (iv != null) {
				iv.setImageResource(imageId);
				// Assign a value for the tag so the onClickFeature handler can
				// determine which button was clicked.
				iv.setTag(new Integer(j + 1));
			}
			TextView tv = (TextView) v.findViewById(R.id.home_btn_label);
			if (tv != null)
				tv.setText(labelId);

			// Find the frame where the image goes.
			// Attach the inflated view to that frame.
			View buttonView = v;
			FrameLayout frame = (FrameLayout) findViewById(frameId);
			if (frame != null) {
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.FILL_PARENT, Gravity.CENTER);
				frame.addView(buttonView, lp);
			}

		}

		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this.getApplicationContext());
		Gson gson = new Gson();
		String json = appSharedPrefs.getString(Constants.CMISHOST, "");

		mHost = gson.fromJson(json, CMISHost.class);
	}

	/**
	 * onDestroy The final call you receive before your activity is destroyed.
	 * This can happen either because the activity is finishing (someone called
	 * finish() on it, or because the system is temporarily destroying this
	 * instance of the activity to save space. You can distinguish between these
	 * two scenarios with the isFinishing() method.
	 * 
	 */

	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * onPause Called when the system is about to start resuming a previous
	 * activity. This is typically used to commit unsaved changes to persistent
	 * data, stop animations and other things that may be consuming CPU, etc.
	 * Implementations of this method must be very quick because the next
	 * activity will not be resumed until this method returns. Followed by
	 * either onResume() if the activity returns back to the front, or onStop()
	 * if it becomes invisible to the user.
	 * 
	 */

	protected void onPause() {
		super.onPause();
	}

	/**
	 * onRestart Called after your activity has been stopped, prior to it being
	 * started again. Always followed by onStart().
	 * 
	 */

	protected void onRestart() {
		super.onRestart();
	}

	/**
	 * onResume Called when the activity will start interacting with the user.
	 * At this point your activity is at the top of the activity stack, with
	 * user input going to it. Always followed by onPause().
	 * 
	 */

	protected void onResume() {
		super.onResume();
	}

	/**
	 * onStart Called when the activity is becoming visible to the user.
	 * Followed by onResume() if the activity comes to the foreground, or
	 * onStop() if it becomes hidden.
	 * 
	 */

	protected void onStart() {
		super.onStart();
	}

	/**
	 * onStop Called when the activity is no longer visible to the user because
	 * another activity has been resumed and is covering this one. This may
	 * happen either because a new activity is being started, an existing one is
	 * being brought in front of this one, or this one is being destroyed.
	 * 
	 * Followed by either onRestart() if this activity is coming back to
	 * interact with the user, or onDestroy() if this activity is going away.
	 */

	protected void onStop() {
		super.onStop();
	}

	/**
	 * Handle the click of a Feature button by starting the activity for that
	 * feature.
	 * 
	 * @param v
	 *            View
	 * @return void
	 */

	public void onClickFeature(View v) {
		Integer featureNum = (Integer) v.getTag();
		if (featureNum == null)
			return;

		switch (featureNum) {
		case 1:
			final CMISApplication app = (CMISApplication) getApplication();
			
			final String hostId = mHost.getId();
			
			ChildDownloadThread _dlThread = new ChildDownloadThread(new Handler() {
						public void handleMessage(Message msg) {
							boolean ok = msg.getData().getBoolean(OK_KEY);
							if (!ok) {
								app.handleNetworkStatus();
							} else {
								Intent browseIntent = new Intent(
										HomeActivity.this,
										NodeBrowseActivity.class);
								startActivityForResult(browseIntent,
										NODE_BROWSE_REQ);
							}
						}
					}, 
			new Downloadable()
			{
				public Object execute()
				{
					return app.initCMIS(hostId);
				}
			});
			
			_dlThread.start();

//			startActivity(new Intent(getApplicationContext(), F1Activity.class));
			break;
		case 2:
			startActivity(new Intent(getApplicationContext(), F2Activity.class));
			break;
		case 3:
			startActivity(new Intent(getApplicationContext(), F3Activity.class));
			break;
		case 4:
			startActivity(new Intent(getApplicationContext(), F4Activity.class));
			break;
		case 5:
			startActivity(new Intent(getApplicationContext(), F5Activity.class));
			break;
		case 6:
			startActivity(new Intent(getApplicationContext(), F6Activity.class));
			break;
		default:
			break;
		}
	}

	private class ChildDownloadThread extends Thread {
		Handler _handler;
		Downloadable _delegate;

		ChildDownloadThread(Handler h, Downloadable delegate) {
			_handler = h;
			_delegate = delegate;
		}

		public void run() {
			Boolean result = (Boolean) _delegate.execute();
			Message msg = _handler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean(OK_KEY, result);
			msg.setData(b);
			_handler.sendMessage(msg);
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case NODE_BROWSE_REQ:
			if (resultCode == RESULT_OK && data != null && data.getBooleanExtra(Constants.QUIT, false)) {
				finish();
			}
			break;
		}
	}

}