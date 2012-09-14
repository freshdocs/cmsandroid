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
package com.zia.freshdocs.model;

import java.util.HashMap;

import com.zia.freshdocs.R;
import com.zia.freshdocs.R.drawable;

public class Constants 
{
	public enum NetworkStatus
	{
		OK,
		CREDENTIALS_ERROR,
		CONNECTION_ERROR,
		UNKNOWN_ERROR
	}

	public static  HashMap<String, Integer> mimeMap = new HashMap<String, Integer>();
	static
	{
		Constants.mimeMap.put("application/msword", R.drawable.word);
		Constants.mimeMap.put("application/pdf", R.drawable.pdf);
		Constants.mimeMap.put("application/vnd.powerpoint", R.drawable.powerpoint);
		Constants.mimeMap.put("application/vnd.excel", R.drawable.excel);
		Constants.mimeMap.put("application/zip", R.drawable.archive);
		Constants.mimeMap.put("audio/mpeg", R.drawable.audio);
		Constants.mimeMap.put("audio/x-wav", R.drawable.audio);
		Constants.mimeMap.put("cmis/folder", R.drawable.folder);
		Constants.mimeMap.put("image/gif", R.drawable.image);
		Constants.mimeMap.put("image/jpeg", R.drawable.image);
		Constants.mimeMap.put("image/png", R.drawable.image);
		Constants.mimeMap.put("text/plain", R.drawable.txt);
		Constants.mimeMap.put("text/xml", R.drawable.xml);
		Constants.mimeMap.put(null, R.drawable.document);
	}

	public static final String NEW_HOST_ID = "-1";
	
	public static final String QUIT = "quit";
	
	public static final String CMISHOST = "CMISHost";
}

