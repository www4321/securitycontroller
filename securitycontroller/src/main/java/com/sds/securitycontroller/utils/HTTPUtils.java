/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HTTPUtils {

    protected static Logger log = LoggerFactory.getLogger(HTTPUtils.class);
    
	public static String httpPost(String reqUrl, String content, Map<String, String> headers){
		return httpRequest(reqUrl, "POST", content, headers);
	}

	public static String httpDelete(String reqUrl, Map<String, String> headers) throws Exception{
		URL url = new URL(reqUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("DELETE");
        for(Entry<String, String> entry : headers.entrySet()){
	        connection.setRequestProperty(entry.getKey(), entry.getValue());	        	
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
        StringBuilder sb = new StringBuilder();
        String line="";
        while ((line = reader.readLine()) != null){
        	sb.append(line);
        }
        reader.close();
        connection.disconnect();
        return sb.toString();
	}

	public static String httpRequest(String reqUrl, String method, String content, Map<String, String> headers){
		URL url;
		try {
			url = new URL(reqUrl);
			HttpURLConnection connection = (HttpURLConnection) url
	                .openConnection();
			connection.setDoOutput(true);

	        connection.setDoInput(true);

	        connection.setRequestMethod(method);

	        connection.setUseCaches(false);
	        connection.setInstanceFollowRedirects(true);
	        for(Entry<String, String> entry : headers.entrySet()){
		        connection.setRequestProperty(entry.getKey(), entry.getValue());	        	
	        }
	        connection.connect();
	        DataOutputStream out = new DataOutputStream(connection
	                .getOutputStream());

	        out.writeBytes(content); 
	        out.flush();
	        out.close();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
	        StringBuilder sb = new StringBuilder();

	        String line="";
	        while ((line = reader.readLine()) != null){
	        	sb.append(line);
	        	sb.append('\n');
	        }

	        reader.close();
	        connection.disconnect();
	        return sb.toString();
		} catch (IOException e) {
			log.error("error while posting request: {}", e.getMessage());
			return null;
		} 
	}

	//Specified URL and response content type
	public static String httpGet(String reqUrl,  Map<String, String> otherHeaders){
		URL url;
		try {
			url = new URL(reqUrl);
			
			/*
			 * url.openconnection() returns a HttpURLConnection object, it represents the connection to the
			 * remote object which referenced by the URL
			*/
			HttpURLConnection connection = (HttpURLConnection) url
	                .openConnection();
			
	        for(Entry<String, String> entry : otherHeaders.entrySet()){
		        connection.setRequestProperty(entry.getKey(), entry.getValue());	        	
	        }
	        
	        //Establish connection
	        connection.connect();
	        
	        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
	        StringBuilder sb = new StringBuilder();
	        String line="";
	        while ((line = reader.readLine()) != null){
	        	sb.append(line);
	        }
	        reader.close();
	        connection.disconnect();
	        return sb.toString();
		} catch (IOException e) {
			log.error("error while posting request: {}", e.getMessage());
			return null;
		} 
	}
	
	

}
