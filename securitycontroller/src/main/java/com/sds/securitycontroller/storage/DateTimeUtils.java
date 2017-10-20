/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.storage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {
	
	public static String convertDateTime(Date date){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}
	
	public static String getNow(){
		return "NOW()";
	}
	
	public static String getAddTime(){
		return "ADDTIME";
	}

}
