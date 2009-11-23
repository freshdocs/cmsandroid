package com.zia.freshdocs.preference;

import java.io.Serializable;

public class CMISHost implements Serializable
{
	private static final long serialVersionUID = -7004962852122155333L;

	private String _hostname;
	private String _username;
	private String _password;
	private int _port = 80;
	private boolean _showHidden = true;

	public boolean isShowHidden() {
		return _showHidden;
	}

	public void setShowHidden(boolean showHidden) {
		this._showHidden = showHidden;
	}

	public String getHostname() {
		return _hostname;
	}

	public void setHostname(String hostname) {
		this._hostname = hostname;
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		this._username = username;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		this._password = password;
	}

	public int getPort() {
		return _port;
	}

	public void setPort(int port) {
		this._port = port;
	}
}
