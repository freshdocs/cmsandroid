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
package com.zia.freshdocs.cmis;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import android.util.Log;

import com.zia.freshdocs.Constants.NetworkStatus;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.preference.CMISHost;

public class CMIS
{
	protected static final int TIMEOUT = 12500;
	
	protected static final String ALF_SERVICE_URI = "/service/api";
	protected static final String CHILDREN_URI = ALF_SERVICE_URI + "/node/workspace/SpacesStore/%s/children?alf_ticket=%s";
	protected static final String CMIS_INFO_URI = ALF_SERVICE_URI + "/cmis?alf_ticket=%s";
	protected static final String LOGIN_URI = ALF_SERVICE_URI + "/login?u=%s&pw=%s";
	protected static final String QUERY_URI = ALF_SERVICE_URI + "/query?alf_ticket=%s";

	private CMISHost _prefs;
	private CMISParser _parser;
	private String _ticket;
	private String _version;
	private NetworkStatus _networkStatus;

	public CMIS(CMISHost prefs)
	{
		super();
		_prefs = prefs;
		_networkStatus = NetworkStatus.OK;
	}

	public String authenticate()
	{
		String res = makeHttpRequest(String.format(LOGIN_URI, _prefs.getUsername(), 
				_prefs.getPassword()));

		if (res != null)
		{
			DocumentBuilder docBuilder = null;
			try
			{
				docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = docBuilder.parse(new ByteArrayInputStream(res.getBytes()));
				_ticket = doc.getDocumentElement().getFirstChild().getNodeValue();
			}
			catch (Exception e)
			{
				Log.e(CMIS.class.getSimpleName(), "Error getting Alfresco ticket", e);
			}
		}

		return _ticket;
	}

	public NodeRef[] getCompanyHome()
	{
		String res = makeHttpRequest(String.format(CMIS_INFO_URI, _ticket));
		if (res != null)
		{
			CMISParser parser = new CMISParserBase();
			CMISInfo cmisInfo = parser.getCMISInfo(res);
			_version = cmisInfo.getVersion();
			
			// This should probably be a factory method
			if(_version.equals("1.0"))
			{
				_parser = new CMISParser10();
			}
			else
			{
				_parser = new CMISParser06();
			}
			
			try
			{
				String rootUrl = cmisInfo.getRootURI();
				StringBuilder buf = new StringBuilder(new URL(rootUrl).getPath());
				buf.append("?alf_ticket=").append(_ticket);
				String path = buf.toString();
				String rootURI = _prefs.getWebappRoot();

				if(buf.toString().startsWith(rootURI))
				{
					path = buf.substring(rootURI.length());
				}

				res = makeHttpRequest(path);
				if(res != null)
				{
					return _parser.parseChildren(res);
				}
			}
			catch (MalformedURLException e)
			{
				Log.e(CMIS.class.getSimpleName(), "Error parsing root uri", e);				
			}
		}
		
		return null;
	}
	
	public NodeRef[] getChildren(String uuid)
	{
		String res = makeHttpRequest(String.format(CHILDREN_URI, uuid, _ticket));
		if (res != null)
		{
			return _parser.parseChildren(res);
		}

		return null;
	}
	
	public NodeRef[] query(String xmlQuery)
	{
		String res = makeHttpRequest(true, String.format(QUERY_URI, _ticket), xmlQuery, 
				"application/cmisquery+xml");
		if (res != null)
		{
			return _parser.parseChildren(res);
		}

		return null;
	}

	protected String buildRelativeURI(String path)
	{
		StringBuilder uri = new StringBuilder();
		String rootURI = _prefs.getWebappRoot();
		
		if(rootURI.endsWith("/"))
		{
			uri.append(rootURI.subSequence(0, rootURI.length() - 2));
		}
		else
		{
			uri.append(rootURI);
		}

		uri.append(path);
		
		return uri.toString();
	}

	protected String makeHttpRequest(String path)
	{
		return makeHttpRequest(false, path, null, null);
	}

	protected String makeHttpRequest(boolean isPost, String path, 
			String payLoad, String contentType)
	{
		try
		{	
			String url = new URL(
					_prefs.isSSL() ? "https" : "http", _prefs.getHostname(), 
							buildRelativeURI(path)).toString();
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter("http.connection.timeout", new Integer(TIMEOUT));
			_networkStatus = NetworkStatus.OK;

			HttpRequestBase request = null;
			
			if(isPost)
			{
				request = new HttpPost(url);
				((HttpPost) request).setEntity(new StringEntity(payLoad));
			}
			else
			{
				request = new HttpGet(url);
			}

			try
			{
				
				if(contentType != null)
				{
					request.setHeader("Content-type", contentType);					
				}
				
				HttpResponse response = client.execute(request);
				StatusLine status = response.getStatusLine();
				HttpEntity entity = response.getEntity();
				int statusCode = status.getStatusCode();
				
				if (statusCode == HttpStatus.SC_OK && entity != null)
				{
					// Just return the whole chunk
					return EntityUtils.toString(entity);
				}
				else if(statusCode == HttpStatus.SC_UNAUTHORIZED)
				{
					_networkStatus = NetworkStatus.CREDENTIALS_ERROR;
				}
			}
			catch (Exception ex)
			{
				Log.e(CMIS.class.getName(), "Get method error", ex);
				_networkStatus = NetworkStatus.CONNECTION_ERROR;
			}
		}
		catch (Exception ex)
		{
			Log.e(CMIS.class.getName(), "Get method error", ex);
			_networkStatus = NetworkStatus.UNKNOWN_ERROR;
		}

		return null;
	}
	

	public String getTicket()
	{
		return _ticket;
	}

	public String getVersion()
	{
		return _version;
	}

	public void setVersion(String version)
	{
		this._version = version;
	}

	public NetworkStatus getNetworkStatus()
	{
		return _networkStatus;
	}

	public CMISHost getPrefs()
	{
		return _prefs;
	}

	public void setPrefs(CMISHost prefs)
	{
		this._prefs = prefs;
	}	
}
