package com.zia.freshdocs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.zia.freshdocs.R;

public class SplashActivity extends Activity
{
	private static final int DELAY = 3000;
	
	private Handler _handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			Intent browseActivity = new Intent(getBaseContext(), NodeBrowseActivity.class);
			startActivity(browseActivity);
			
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		_handler.sendMessageDelayed(new Message(), DELAY);
	}
}
