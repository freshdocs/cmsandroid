package com.zia.freshdocs;

import java.util.HashMap;

public class Constants 
{
	public static  HashMap<String, Integer> mimeMap = new HashMap<String, Integer>();
	static
	{
		Constants.mimeMap.put("application/pdf", R.drawable.pdf);
		Constants.mimeMap.put("application/zip", R.drawable.archive);
		Constants.mimeMap.put("cmis/folder", R.drawable.folder);
		Constants.mimeMap.put("text/plain", R.drawable.txt);
		Constants.mimeMap.put(null, R.drawable.document);
	}

}
