package com.zia.freshdocs.cmis;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;

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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.zia.freshdocs.model.NodeRef;

public class CMIS
{
	public enum NetworkStatus
	{
		OK,
		CREDENTIALS_ERROR,
		CONNECTION_ERROR,
		UNKNOWN_ERROR
	}

	public static final int TIMEOUT = 12500;
	
	public static final String ALF_SERVICE_URI = "/service/api";
	public static final String CHILDREN_URI = ALF_SERVICE_URI + "/node/workspace/SpacesStore/%s/children?alf_ticket=%s";
	public static final String LOGIN_URI = ALF_SERVICE_URI + "/login?u=%s&pw=%s";
	public static final String SCRIPT_INFO_URI = ALF_SERVICE_URI + "/cmis?alf_ticket=%s";
	public static final String QUERY_URI = ALF_SERVICE_URI + "/query?alf_ticket=%s";

	private String _hostname;
	private String _username;
	private String _password;
	private String _ticket;
	private String _version;
	private String _rootURI;
	private int _port;
	boolean _SSL;
	private NetworkStatus _networkStatus;

	public CMIS(String hostname, String username, String password, int port, 
			boolean ssl, String rootURI)
	{
		super();
		_hostname = hostname;
		_username = username;
		_password = password;
		_port = port;
		_SSL = ssl;
		_rootURI = rootURI;
		_networkStatus = NetworkStatus.OK;
	}

	public String authenticate()
	{
		String res = makeHttpRequest(String.format(LOGIN_URI, _username, _password));

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

	public NodeRef getCompanyHome()
	{
		String res = makeHttpRequest(String.format(SCRIPT_INFO_URI, _ticket));
		if (res != null)
		{
			DocumentBuilder docBuilder = null;
			try
			{
				docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = docBuilder.parse(new ByteArrayInputStream(res.getBytes()));
				
				NodeList nodes = doc.getElementsByTagName("cmis:productVersion");
				String rawVersion = nodes.item(0).getFirstChild().getNodeValue();
				_version = rawVersion.split("\\s")[0];

				nodes = doc.getElementsByTagName("cmis:rootFolderId");
				int n = nodes.getLength();
				
				if(n > 0)
				{
					Node node = nodes.item(0); 
					String rootUrl = node.getFirstChild().getNodeValue(); 
					StringBuilder buf = new StringBuilder(new URL(rootUrl).getPath());
					buf.append("?alf_ticket=").append(_ticket);
					String path = buf.toString();
					
					if(buf.toString().startsWith(_rootURI))
					{
						path = buf.substring(_rootURI.length());
					}
					
					return parseChildren(makeHttpRequest(path))[0];
				}
			}
			catch (Exception e)
			{
				Log.e(CMIS.class.getSimpleName(), "Error getting root folder id", e);
			}
		}

		return null;
	}
	
	public NodeRef[] getChildren(String uuid)
	{
		String res = makeHttpRequest(String.format(CHILDREN_URI, uuid, _ticket));
		if (res != null)
		{
			return parseChildren(res);
		}

		return null;
	}
	
	public NodeRef[] query(String xmlQuery)
	{
		String res = makeHttpRequest(true, String.format(QUERY_URI, _ticket), xmlQuery, 
				"application/cmisquery+xml");
		if (res != null)
		{
			return parseChildren(res);
		}

		return null;
	}

	private NodeRef[] parseChildren(String res) throws FactoryConfigurationError
	{
		NodeRef[] refs = new NodeRef[0];
		DocumentBuilder docBuilder = null;
		
		try
		{
			Pattern pattern = Pattern.compile("&(?![a-zA-Z0-9]+;)");
			Matcher matcher = pattern.matcher(res);
			String sanitized = matcher.replaceAll("&amp;");
			
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(new ByteArrayInputStream(sanitized.getBytes()));
			
			// Iterate over all the entry nodes and build NodeRefs
			NodeList nodes = doc.getElementsByTagName("entry");
			NodeList children = null;
			Element node = null;
			NodeRef nodeRef = null;
			int n = nodes.getLength();
			
			for(int i=0; i<n; i++)
			{
				if(refs.length == 0)
				{
					refs = new NodeRef[n];
				}
				
				node = (Element) nodes.item(i);
				children = node.getElementsByTagName("content");
				
				if(children.getLength() > 0)
				{
					Element contentNode = (Element) children.item(0);
					String content = null;
					nodeRef = new NodeRef();
					
					if(contentNode.hasAttribute("type"))
					{
						nodeRef.setContentType(contentNode.getAttribute("type"));
						content = contentNode.getAttribute("src");
					} 
					else
					{
						content = contentNode.getFirstChild().getNodeValue();
					}
					
					nodeRef.setContent(content);
					
					children = node.getElementsByTagName("title");
					if(children.getLength() > 0)
					{
						nodeRef.setName(children.item(0).getFirstChild().getNodeValue());
					}

					children = node.getElementsByTagName("updated");
					if(children.getLength() > 0)
					{
						nodeRef.setLastModificationDate(
								children.item(0).getFirstChild().getNodeValue()); 
					}

					children = node.getElementsByTagName("cmis:propertyString");
					int nChildren = children.getLength();
					
					if(nChildren > 0)
					{
						for(int j = 0; j < nChildren; j++)
						{
							Element child = (Element) children.item(j);
							
							if(child.getAttribute("cmis:name").equals("BaseType"))
							{
								NodeList valueNode = child.getElementsByTagName("cmis:value");
								String baseType = valueNode.item(0).getFirstChild().getNodeValue(); 
								nodeRef.setFolder(baseType.equals("folder"));
							} 
							else if(child.getAttribute("cmis:name").equals("LastModifiedBy"))
							{
								NodeList valueNode = child.getElementsByTagName("cmis:value");
								nodeRef.setLastModifiedBy(
										valueNode.item(0).getFirstChild().getNodeValue()); 
							}
							else if(child.getAttribute("cmis:name").equals("VersionLabel"))
							{
								NodeList valueNode = child.getElementsByTagName("cmis:value");
								if(valueNode.getLength() > 0)
								{
									nodeRef.setVersion(
											valueNode.item(0).getFirstChild().getNodeValue());
								}
							}
						}
					}
					
					children = node.getElementsByTagName("cmis:propertyInteger");
					nChildren = children.getLength();

					if(nChildren > 0)
					{
						for(int j = 0; j < nChildren; j++)
						{
							Element child = (Element) children.item(j);
							
							if(child.getAttribute("cmis:name").equals("ContentStreamLength"))
							{
								NodeList valueNode = child.getElementsByTagName("cmis:value");
								nodeRef.setContentLength(
										Long.valueOf(valueNode.item(0).getFirstChild().getNodeValue()));
								break;
							}
						}
					}
					
					refs[i] = nodeRef;
				}
			}
		}
		catch (Exception e)
		{
			Log.e(CMIS.class.getSimpleName(), "Error getting children", e);
		}
		
		return refs;
	}
	
	protected String buildRelativeURI(String path)
	{
		StringBuilder uri = new StringBuilder();
		
		if(_rootURI.endsWith("/"))
		{
			uri.append(_rootURI.subSequence(0, _rootURI.length() - 2));
		}
		else
		{
			uri.append(_rootURI);
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
					_SSL ? "https" : "http", _hostname, buildRelativeURI(path)).toString();
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
	
	public String getHostname()
	{
		return _hostname;
	}

	public void setHostname(String hostname)
	{
		this._hostname = hostname;
	}

	public String getUsername()
	{
		return _username;
	}

	public void setUsername(String username)
	{
		this._username = username;
	}

	public String getPassword()
	{
		return _password;
	}

	public void setPassword(String password)
	{
		this._password = password;
	}

	public int getPort()
	{
		return _port;
	}

	public void setPort(int port)
	{
		this._port = port;
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

	public String getRootURI()
	{
		return _rootURI;
	}

	public void setRootURI(String rootURI)
	{
		this._rootURI = rootURI;
	}

	public boolean isSSL()
	{
		return _SSL;
	}

	public void setSSL(boolean sSL)
	{
		_SSL = sSL;
	}
}
