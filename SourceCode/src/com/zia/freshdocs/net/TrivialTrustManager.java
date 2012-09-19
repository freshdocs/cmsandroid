package com.zia.freshdocs.net;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Used to work around a bug in Android 1.6:
 * 
 * @see http://code.google.com/p/android/issues/detail?id=1946
 */
public class TrivialTrustManager implements X509TrustManager
{
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException
	{
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException
	{
	}

	public X509Certificate[] getAcceptedIssuers()
	{
		return new X509Certificate[0];
	}
}
