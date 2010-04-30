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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.w3c.dom.Document;

import android.util.Log;

import com.zia.freshdocs.Constants.NetworkStatus;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.preference.CMISHost;
import com.zia.freshdocs.util.EasySSLSocketFactory;

public class CMIS
{
	protected static final String CMIS_QUERY_TYPE = "application/cmisquery+xml";

	protected static final int TIMEOUT = 12500;
	
	protected static final String ALF_SERVICE_URI = "/service";
	protected static final String CHILDREN_URI = ALF_SERVICE_URI + "/api/node/workspace/SpacesStore/%s/children";
	protected static final String CMIS_INFO_URI = ALF_SERVICE_URI + "/api/cmis";
	protected static final String LOGIN_URI = ALF_SERVICE_URI + "/api/login?u=%s&pw=%s";
	protected static final String QUERY_URI = ALF_SERVICE_URI + "/api/query";
	protected static final String QUERY_URI_1_0 = ALF_SERVICE_URI + "/cmis/queries";

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
		String res = get(String.format(LOGIN_URI, _prefs.getUsername(), 
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
		String res = get(String.format(CMIS_INFO_URI));
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
				String path = buf.toString();
				String rootURI = _prefs.getWebappRoot();

				if(buf.toString().startsWith(rootURI))
				{
					path = buf.substring(rootURI.length());
				}

				res = get(path);
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
		String res = get(String.format(CHILDREN_URI, uuid));
		if (res != null)
		{
			return _parser.parseChildren(res);
		}

		return null;
	}
	
	public NodeRef[] query(String xmlQuery)
	{
		String uri = String.format(_version.equals("1.0") ? QUERY_URI_1_0 : QUERY_URI);
		String res = post(uri, xmlQuery, CMIS_QUERY_TYPE);
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
		
		if(!path.startsWith(rootURI))
		{
			if(rootURI.endsWith("/"))
			{
				uri.append(rootURI.subSequence(0, rootURI.length() - 2));
			}
			else
			{
				uri.append(rootURI);
			}
		}
		
		uri.append(path);
		
		if(_ticket != null)
		{
			uri.append("?alf_ticket=").append(_ticket);
		}
		
		return uri.toString();
	}

	public String get(String path)
	{
		try
		{
			InputStream is = makeHttpRequest(path);
			if(is != null)
			{
				return IOUtils.toString(is);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String post(String path, String payload, String contentType)
	{
		try
		{
			InputStream is = makeHttpRequest(true, path, payload, contentType);
			if(is != null)
			{
				return IOUtils.toString(is);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public InputStream makeHttpRequest(String path)
	{
		return makeHttpRequest(false, path, null, null);
	}

	public InputStream makeHttpRequest(boolean isPost, String path, 
			String payLoad, String contentType)
	{
		try
		{	
			HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, "utf-8");
	        params.setBooleanParameter("http.protocol.expect-continue", false);
	        params.setParameter("http.connection.timeout", new Integer(TIMEOUT));
	        
	        // registers schemes for both http and https
	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), _prefs.getPort()));
	        registry.register(new Scheme("https", new EasySSLSocketFactory(), _prefs.getPort()));
	        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
	     
	        String url = new URL(_prefs.isSSL() ? "https" : "http", _prefs.getHostname(), 
							buildRelativeURI(path)).toString();
			HttpClient client = new DefaultHttpClient(manager, params);
			client.getParams();
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
				
				if ((statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED )&& entity != null)
				{
					// Just return the whole chunk
					return entity.getContent();
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
