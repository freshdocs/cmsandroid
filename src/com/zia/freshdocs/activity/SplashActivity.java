package com.zia.freshdocs.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.zia.freshdocs.R;

public class SplashActivity extends Activity
{
	private static final int DELAY = 3000;
		
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		Handler handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				launchMain();
			}
		};
		handler.sendMessageDelayed(new Message(), DELAY);
	}

	protected void launchMain()
	{
		setResult(RESULT_OK);
		finish();
	}
}
