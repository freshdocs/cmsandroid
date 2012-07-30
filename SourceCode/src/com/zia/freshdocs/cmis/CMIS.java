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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
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

import android.text.format.DateFormat;
import android.util.Log;

import com.zia.freshdocs.Constants.NetworkStatus;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.net.EasySSLSocketFactory;
import com.zia.freshdocs.preference.CMISHost;

public class CMIS {
	protected static final String CMIS_QUERY_TYPE = "application/cmisquery+xml";

	protected static final int TIMEOUT = 12500;

	protected static final String ALF_SERVICE_URI = "/service";
	protected static final String CHILDREN_URI = ALF_SERVICE_URI + "/api/node/workspace/SpacesStore/%s/children";
	protected static final String CMIS_INFO_URI = ALF_SERVICE_URI + "/api/cmis";
	protected static final String LOGIN_URI = ALF_SERVICE_URI + "/api/login?u=%s&pw=%s";
	protected static final String QUERY_URI = ALF_SERVICE_URI + "/api/query";
	protected static final String QUERY_URI_1_0 = ALF_SERVICE_URI + "/cmis/queries";
	
	
	protected static final String CREATE_FOLDER_URI = "/alfresco/service/cmis/i/%s/children";
	

	private CMISHost mPrefs;
	private CMISParser mParser;
	private String mTicket;
	private String mVersion;
	private NetworkStatus mNetworkStatus;

	public CMIS(CMISHost prefs) {
		super();
		mPrefs = prefs;
		mNetworkStatus = NetworkStatus.OK;
	}

	public String authenticate() {
		InputStream res = get(String.format(LOGIN_URI, mPrefs.getUsername(),
				mPrefs.getPassword()));

		if (res != null) {
			DocumentBuilder docBuilder = null;
			try {
				docBuilder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = docBuilder.parse(res);
				mTicket = doc.getDocumentElement().getFirstChild()
						.getNodeValue();
			} catch (Exception e) {
				Log.e(CMIS.class.getSimpleName(),
						"Error getting Alfresco ticket", e);
			}
		}

		return mTicket;
	}

	public NodeRef[] getCompanyHome() {
		InputStream res = get(String.format(CMIS_INFO_URI));
		if (res != null) {
			CMISParser parser = new CMISParserBase();
			CMISInfo cmisInfo = parser.getCMISInfo(res);
			mVersion = cmisInfo.getVersion();

			// This should probably be a factory method
			if (mVersion.equals("1.0")) {
				mParser = new CMISParser10();
			} else {
				mParser = new CMISParser06();
			}

			try {
				String rootUrl = cmisInfo.getRootURI();
				StringBuilder buf = new StringBuilder(
						new URL(rootUrl).getPath());
				String path = buf.toString();
				String rootURI = mPrefs.getWebappRoot();

				if (buf.toString().startsWith(rootURI)) {
					path = buf.substring(rootURI.length());
				}

				res = get(path);
				if (res != null) {
					return mParser.parseChildren(res);
				}
			} catch (MalformedURLException e) {
				Log.e(CMIS.class.getSimpleName(), "Error parsing root uri", e);
			}
		}

		return null;
	}
	
	public NodeRef[] getChildren(String uuid) {
		InputStream res = get(String.format(CHILDREN_URI, uuid));
		if (res != null) {
			return mParser.parseChildren(res);
		}

		return null;
	}

	public NodeRef[] query(String xmlQuery) {
		String uri = String.format(mVersion.equals("1.0") ? QUERY_URI_1_0
				: QUERY_URI);
		InputStream res = post(uri, xmlQuery, CMIS_QUERY_TYPE);
		if (res != null) {
			return mParser.parseChildren(res);
		}

		return null;
	}

	protected String buildRelativeURI(String path) {
		StringBuilder uri = new StringBuilder();
		String rootURI = mPrefs.getWebappRoot();

		if (!path.startsWith(rootURI)) {
			if (rootURI.endsWith("/")) {
				uri.append(rootURI.subSequence(0, rootURI.length() - 2));
			} else {
				uri.append(rootURI);
			}
		}

		uri.append(path);

		if (mTicket != null) {
			uri.append("?alf_ticket=").append(mTicket);
		}

		return uri.toString();
	}

	public InputStream get(String path) {
		return makeHttpRequest(path);
	}

	public InputStream post(String path, String payload, String contentType) {
		return makeHttpRequest(true, path, payload, contentType);
	}

	public InputStream makeHttpRequest(String path) {
		return makeHttpRequest(false, path, null, null);
	}

	public InputStream makeHttpRequest(boolean isPost, String path,
			String payLoad, String contentType) {
		try {
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, "utf-8");
			params.setBooleanParameter("http.protocol.expect-continue", false);
			params.setParameter("http.connection.timeout", Integer.valueOf(TIMEOUT));

			// registers schemes for both http and https
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), mPrefs.getPort()));
			registry.register(new Scheme("https", new EasySSLSocketFactory(),
					mPrefs.getPort()));
			ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
					params, registry);

			String url = new URL(mPrefs.isSSL() ? "https" : "http",
					mPrefs.getHostname(), buildRelativeURI(path)).toString();
			HttpClient client = new DefaultHttpClient(manager, params);
			client.getParams();
			mNetworkStatus = NetworkStatus.OK;

			HttpRequestBase request = null;

			if (isPost) {
				request = new HttpPost(url);
				((HttpPost) request).setEntity(new StringEntity(payLoad));
			} else {
				request = new HttpGet(url);
			}

			try {

				if (contentType != null) {
					request.setHeader("Content-type", contentType);
				}

				HttpResponse response = client.execute(request);
				StatusLine status = response.getStatusLine();
				HttpEntity entity = response.getEntity();
				int statusCode = status.getStatusCode();

				if ((statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED)
						&& entity != null) {
					// Just return the whole chunk
					return entity.getContent();
				} else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
					mNetworkStatus = NetworkStatus.CREDENTIALS_ERROR;
				}
			} catch (Exception ex) {
				Log.e(CMIS.class.getName(), "Get method error", ex);
				mNetworkStatus = NetworkStatus.CONNECTION_ERROR;
			}
		} catch (Exception ex) {
			Log.e(CMIS.class.getName(), "Get method error", ex);
			mNetworkStatus = NetworkStatus.UNKNOWN_ERROR;
		}

		return null;
	}
	
	public void createFolder(String parentFolder, String folderName, String description)
			throws ClientProtocolException, IOException {

		  String path = String.format(CREATE_FOLDER_URI, parentFolder);
		  String url = new URL(mPrefs.isSSL() ? "https" : "http",
					mPrefs.getHostname(), buildRelativeURI(path)).toString();
		  
		  String contentType = "application/atom+xml;type=entry";
		  String updateDate = DateFormat.format("yyyy-MM-ddThh:mm:sszzz", System.currentTimeMillis()).toString();
		
		  String xml = 
				"<?xml version='1.0' encoding='utf-8'?>\n" +
				"<entry xmlns=\"http://www.w3.org/2005/Atom\"\n" +  
				"xmlns:app=\"http://www.w3.org/2007/app\"\n" +
				"xmlns:cmisra=\"http://docs.oasis-open.org/ns/cmis/restatom/200908/\" xmlns:cmis=\"http://docs.oasis-open.org/ns/cmis/core/200908/\">\n" +
				"<author>\n" +
				"<name>admin</name>\n" +
				"</author>\n" +
				"<id>ignored</id>\n" +
				"<summary>"+ description +"</summary>\n" +
				"<title>"+ folderName +"</title>\n" +
				"<updated>" + updateDate + "</updated>\n" +
				"<cmisra:object>\n" +
				"<cmis:properties>\n" +
				"<cmis:propertyId propertyDefinitionId=\"cmis:objectTypeId\">\n" +
				"<cmis:value>cmis:folder</cmis:value> \n" +
				"</cmis:propertyId>\n" +
				"<cmis:propertyString propertyDefinitionId=\"cmis:name\">\n" +
				"<cmis:value></cmis:value>\n" +
				"</cmis:propertyString>\n" +
				"</cmis:properties>\n" +
				"</cmisra:object>\n" +
				"</entry>";
		  
		  DefaultHttpClient httpclient = new DefaultHttpClient();
		
		  HttpPost httppost = new HttpPost(url);
		  httppost.setHeader("Content-type", contentType);
		  
		  StringEntity requestEntity = new StringEntity(xml, "UTF-8");
		  httppost.setEntity(requestEntity);
		
		  
		  System.out.println("executing request" + httppost.getRequestLine());
		  HttpResponse response = httpclient.execute(httppost);
		  HttpEntity entity = response.getEntity();
		
		  System.out.println("----------------------------------------");
		  System.out.println(response.getStatusLine());
	      
	      // When HttpClient instance is no longer needed,
	      // shut down the connection manager to ensure
	      // immediate deallocation of all system resources
	      httpclient.getConnectionManager().shutdown();
	   }

	public String getTicket() {
		return mTicket;
	}

	public String getVersion() {
		return mVersion;
	}

	public void setVersion(String version) {
		this.mVersion = version;
	}

	public NetworkStatus getNetworkStatus() {
		return mNetworkStatus;
	}

	public CMISHost getPrefs() {
		return mPrefs;
	}

	public void setPrefs(CMISHost prefs) {
		this.mPrefs = prefs;
	}
}
