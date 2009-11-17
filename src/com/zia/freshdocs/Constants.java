package com.zia.freshdocs;

import java.util.HashMap;

public class Constants 
{
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

}
