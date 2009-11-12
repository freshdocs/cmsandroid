package com.zia.freshdocs.util;

import java.net.URL;

import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

public class URLUtils
{
	public static Builder toUriBuilder(String sUrl)
	{
		try
		{
			URL url = new URL(sUrl);
			Uri.Builder builder = new Uri.Builder();
			builder.scheme(url.getProtocol());
			builder.encodedAuthority(url.getAuthority());
			String path = url.getPath();
			builder.appendEncodedPath(path.charAt(0) == '/' ? path.substring(1) : path);
			builder.encodedQuery(url.getQuery());
			return builder;
		}
		catch (Exception e)
		{
			Log.e(URLUtils.class.getSimpleName(), "Error initiating view intent", e);
		}

		return null;
	}
}
