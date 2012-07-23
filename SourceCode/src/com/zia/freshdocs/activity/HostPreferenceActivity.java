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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.zia.freshdocs.R;
import com.zia.freshdocs.preference.CMISHost;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.util.StringUtils;

public class HostPreferenceActivity extends Activity {
	public static final String EXTRA_EDIT_SERVER = "edit_server";

	protected boolean _backPressed;
	protected CMISHost _currentHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_HoloEverywhereLight);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host_preference);

		Intent intent = getIntent();

		if (intent.hasExtra(EXTRA_EDIT_SERVER)) {
			editServer(intent.getStringExtra(EXTRA_EDIT_SERVER));
		}
	}

	protected void editServer(String id) {
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		_currentHost = prefsMgr.getPreferences(this, id);

		if (_currentHost != null) {
			((EditText) findViewById(R.id.hostname_edittext))
					.setText((String) _currentHost.getHostname());
			((EditText) findViewById(R.id.username_edittext))
					.setText((String) _currentHost.getUsername());
			((EditText) findViewById(R.id.password_edittext))
					.setText((String) _currentHost.getPassword());
			((EditText) findViewById(R.id.webapp_root))
					.setText((String) _currentHost.getWebappRoot());
			((EditText) findViewById(R.id.port_edittext)).setText(Integer
					.toString((int) _currentHost.getPort()));
			((CheckBox) findViewById(R.id.ssl))
					.setChecked(_currentHost.isSSL());
			((CheckBox) findViewById(R.id.hidden_files))
					.setChecked(_currentHost.isShowHidden());
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !updateHost()) {
			if (!_backPressed) {
				_backPressed = true;
				return false;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	protected boolean updateHost() {
		String hostname = ((EditText) findViewById(R.id.hostname_edittext))
				.getText().toString().trim();
		String username = ((EditText) findViewById(R.id.username_edittext))
				.getText().toString().trim();
		String password = ((EditText) findViewById(R.id.password_edittext))
				.getText().toString().trim();
		String webappRoot = ((EditText) findViewById(R.id.webapp_root))
				.getText().toString().trim();
		boolean isSSL = ((CheckBox) findViewById(R.id.ssl)).isChecked();
		boolean showHidden = ((CheckBox) findViewById(R.id.hidden_files))
				.isChecked();

		int port = 80;

		String portVal = ((EditText) findViewById(R.id.port_edittext))
				.getText().toString().trim();

		if (StringUtils.isEmpty(portVal)) {
			toastError("Port is a required field.");
			return false;
		}

		port = Integer.parseInt(portVal);

		if (_currentHost == null) {
			_currentHost = new CMISHost();
		}

		if (StringUtils.isEmpty(hostname)) {
			toastError("Hostname is a required field.");
			return false;
		}

		_currentHost.setHostname(hostname);

		if (StringUtils.isEmpty(username)) {
			toastError("Username is a required field.");
			return false;
		}

		_currentHost.setUsername(username);

		if (StringUtils.isEmpty(password)) {
			toastError("Password is a required field.");
			return false;
		}

		_currentHost.setPassword(password);
		_currentHost.setPort(port);
		_currentHost.setSSL(isSSL);
		_currentHost.setShowHidden(showHidden);

		if (StringUtils.isEmpty(webappRoot)) {
			toastError("URL is a required field.");
			return false;
		}

		_currentHost.setWebappRoot(webappRoot);

		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		prefsMgr.setPreferences(this, _currentHost);

		return true;
	}

	protected void toastError(String msg) {
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(this, msg
				+ "\nPress back again to cancel editing.", duration);
		toast.show();
	}
}
