package com.sds.securitycontroller.storage;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommonTool {
	
	public static String GetCurrentTimeStr(){
		return GetDateStrFromObj(new Date());
	}
	
	public static String GetDateStrFromObj(Object obj){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.SIMPLIFIED_CHINESE);
		return sdf.format((Date)obj);
	}
	
	public static Object GetDateFromStr(String date){
		try{
			if(date == null || date.isEmpty()){
				return null;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.SIMPLIFIED_CHINESE);
			return sdf.parse(date);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	 public static String getCaller()  
	    {  
	        StackTraceElement stack[] = (new Throwable()).getStackTrace();
	        String appender = "";
	        if(stack.length >= 2) 
	        {   
	            appender += String.format(" ClassName:\t%s\n",  stack[1].getClassName());  
	            appender += String.format("MethodName:\t%s\n",  stack[1].getMethodName());  
	            appender += String.format("  FileName:\t%s\n",  stack[1].getFileName());  
	            appender += String.format("LineNumber:\t%s\n",	stack[1].getLineNumber());  
	            
	            System.out.println(appender);
	        }
	        
	        return appender;
	    }  

}
