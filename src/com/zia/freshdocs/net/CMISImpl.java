package com.zia.freshdocs.net;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;

public class CMISImpl implements CMIS
{
	public static final String SCRIPT_INFO_URI = "/alfresco/service/api/cmis";

	private String hostname;
	private String username;
	private String password;
	private int port;

	public Object getRootFolderId()
	{
		return null;
	}

	public Document get()
	{
		try
		{
			URI uri = new URI("http", hostname, SCRIPT_INFO_URI);
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(uri);

			try
			{
				HttpResponse response = client.execute(request);
				System.out.println();
			} catch (Exception ex)
			{
			}
		} catch (Exception ex)
		{
		}

		return null;
	}

	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}
}
