package com.zia.freshdocs.util;

public class StringUtils
{
	public static boolean isEmpty(String src)
	{
		if(src == null)
		{
			return true;
		}
		
		return src.trim().length() == 0;
	}
}
