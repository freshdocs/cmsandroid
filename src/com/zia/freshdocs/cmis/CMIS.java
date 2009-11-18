package com.zia.freshdocs.cmis;

import java.io.ByteArrayInputStream;
import java.net.URL;

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
	public static final String ALF_SERVICE_URI = "/alfresco/service/api";
	public static final String CHILDREN_URI = ALF_SERVICE_URI + "/node/workspace/SpacesStore/%s/children?alf_ticket=%s";
	public static final String LOGIN_URI = ALF_SERVICE_URI + "/login?u=%s&pw=%s";
	public static final String SCRIPT_INFO_URI = ALF_SERVICE_URI + "/cmis?alf_ticket=%s";
	public static final String QUERY_URI = ALF_SERVICE_URI + "/query?alf_ticket=%s";

	private String _hostname;
	private String _username;
	private String _password;
	private String _ticket;
	private String _version;
	private int _port;

	public CMIS(String hostname, String username, String password, int port)
	{
		super();
		_hostname = hostname;
		_username = username;
		_password = password;
		_port = port;
	}

	public String authenticate()
	{
		String res = get(String.format(LOGIN_URI, _username, _password));

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
		String res = get(String.format(SCRIPT_INFO_URI, _ticket));
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
					StringBuilder buf = new StringBuilder(
							new URL(node.getFirstChild().getNodeValue()).getPath());
					buf.append("?alf_ticket=").append(_ticket);
					return parseChildren(get(buf.toString()))[0];
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
		String res = get(String.format(CHILDREN_URI, uuid, _ticket));
		if (res != null)
		{
			return parseChildren(res);
		}

		return null;
	}
	
	public NodeRef[] query(String xmlQuery)
	{
		String res = post(String.format(QUERY_URI, _ticket), xmlQuery, "application/cmisquery+xml");
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
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(new ByteArrayInputStream(res.getBytes()));
			
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

					children = node.getElementsByTagName("cmis:propertyString");
					if(children.getLength() > 0)
					{
						for(int j = 0; j < children.getLength(); j++)
						{
							Element child = (Element) children.item(j);
							
							if(child.getAttribute("cmis:name").equals("BaseType"))
							{
								children = child.getElementsByTagName("cmis:value");
								String baseType = children.item(0).getFirstChild().getNodeValue(); 
								nodeRef.setFolder(baseType.equals("folder"));
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

	protected String get(String path)
	{
		try
		{
			URL url = new URL("http", _hostname, path);
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url.toString());

			try
			{
				HttpResponse response = client.execute(request);
				StatusLine status = response.getStatusLine();
				HttpEntity entity = response.getEntity();

				if (status.getStatusCode() == HttpStatus.SC_OK && entity != null)
				{
					// Just return the whole chunk
					return EntityUtils.toString(entity);
				}
			}
			catch (Exception ex)
			{
				Log.e(CMIS.class.getName(), "Get method error", ex);
			}
		}
		catch (Exception ex)
		{
			Log.e(CMIS.class.getName(), "Get method error", ex);
		}

		return null;
	}
	
	protected String post(String path, String payLoad, String contentType)
	{
		try
		{
			URL url = new URL("http", _hostname, path);
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost(url.toString());

			try
			{
				request.setEntity(new StringEntity(payLoad));
				request.setHeader("Content-type", contentType);
				HttpResponse response = client.execute(request);
				StatusLine status = response.getStatusLine();
				HttpEntity entity = response.getEntity();

				if (status.getStatusCode() == HttpStatus.SC_OK && entity != null)
				{
					// Just return the whole chunk
					return EntityUtils.toString(entity);
				}
			}
			catch (Exception ex)
			{
				Log.e(CMIS.class.getName(), "Get method error", ex);
			}
		}
		catch (Exception ex)
		{
			Log.e(CMIS.class.getName(), "Get method error", ex);
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
}
