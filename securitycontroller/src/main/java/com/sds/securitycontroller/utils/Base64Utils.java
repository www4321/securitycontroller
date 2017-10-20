package com.sds.securitycontroller.utils;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class Base64Utils {
	public static String base64Encode(String s)
	{
		if(s == null)
		{
			return null;
		}
		Base64 base64 = new Base64();	
		byte[] textBytes;
		try {
			textBytes = s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		return  base64.encodeToString(textBytes);
	}
	
	public static String base64Decode(String s)
	{
		if(s == null)
		{
			return null;
		}

		Base64 base64 = new Base64();
		try
		{
			return new String(base64.decode(s), "UTF-8");
		}
		catch(Exception e)
		{
			return null;
		}
	}
}
