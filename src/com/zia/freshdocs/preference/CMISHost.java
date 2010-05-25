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
package com.zia.freshdocs.preference;

import java.io.Serializable;
import java.util.UUID;

import com.zia.freshdocs.Constants;

public class CMISHost implements Serializable, Comparable<CMISHost>
{
	private static final long serialVersionUID = -7004962852122155333L;

	private String _id;
	private String _hostname;
	private String _username;
	private String _password;
	private String _webappRoot = "/alfresco";
	private int _port = 80;
	private boolean _SSL = false;
	private boolean _showHidden = true;

	public CMISHost()
	{
		_id = UUID.randomUUID().toString();
	}
	
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

	public String getWebappRoot()
	{
		return _webappRoot;
	}

	public void setWebappRoot(String webappRoot)
	{
		this._webappRoot = webappRoot;
	}

	public boolean isSSL()
	{
		return _SSL;
	}

	public void setSSL(boolean sSL)
	{
		_SSL = sSL;
	}
	
	public void setId(String id)
	{
		this._id = id;
	}

	public String getId()
	{
		return _id;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CMISHost other = (CMISHost) obj;
		if (_id == null)
		{
			if (other._id != null)
				return false;
		}
		else if (!_id.equals(other._id))
			return false;
		return true;
	}

	@Override
	public int compareTo(CMISHost another)
	{
		if(another != null)
		{
			if(this._id == Constants.NEW_HOST_ID)
			{
				return 1;
			} 
			else if(another.getId() == Constants.NEW_HOST_ID)
			{
				return -1;
			}
			
			return this._hostname.compareTo(another.getHostname());
		}
		else
		{
			return 0;
		}
	}

	@Override
	public String toString()
	{
		return getHostname();
	}
}
