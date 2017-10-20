package com.sds.securitycontroller.utils.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPHelper {

    protected static Logger log = LoggerFactory.getLogger(HTTPHelper.class);
    
	public static HTTPHelperResult httpPost(String reqUrl, String content, Map<String, String> headers){
		return httpRequest(reqUrl, "POST", content, headers);
	}
	
	public static HTTPHelperResult httpRequest(String reqUrl, String method, String content, Map<String, String> headers){
		URL url = null;
		HttpURLConnection connection = null;
		BufferedReader reader=null;
		HTTPHelperResult result = new HTTPHelperResult(); 
		
		do{
			try {
				url = new URL(reqUrl);
			} catch (MalformedURLException e) {
				log.error("url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
				break;
			}
			
	        try {
				connection = (HttpURLConnection) url.openConnection();
				connection.setDoOutput(true);			 
				connection.setDoInput(true);			 
				connection.setRequestMethod(method);		 
				connection.setUseCaches(false);
				connection.setInstanceFollowRedirects(true);
				for (Entry<String, String> entry : headers.entrySet()) {
					connection.setRequestProperty(entry.getKey(),
							entry.getValue());
				}
				connection.connect();
			} catch (IOException e) {
				log.error("error while posting request: {}", e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
				break;
			}
			
			DataOutputStream out;
			try {
				out = new DataOutputStream(connection.getOutputStream());
				out.write(content.getBytes());//out.writeBytes(content); // The URL-encoded contend
				out.flush();
				out.close(); // flush and close				
			} catch (IOException e) {
				log.error("error while writing content: {}", e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
				break;
			}
			
			int code=0;			
			try {
				code=connection.getResponseCode();
			} catch (IOException e1) {
				log.error("error while get response code url {}, msg{}",reqUrl, e1.getMessage());
				result.setCode(-1);
				result.setMsg(e1.getMessage());
				break;
			}
		 
			//from now on, we CORRECTly accessed the sever 
			result.setCode(code);
			
	        try {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
			} catch (IOException e) {
		        try {
					reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(),"utf-8"));
				} catch (IOException er) {
					log.error("error while get response data url {}, msg{}",reqUrl, e.getMessage());
					result.setCode(-1);
					result.setMsg(e.getMessage());
					break;
				}		        
			}
	        
	        StringBuilder sb = new StringBuilder();
	        try {
		        String line="";
				while ((line = reader.readLine()) != null){
					sb.append(line);
				}
			} catch (IOException e) {
				log.error("error while read content url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg("SC error while getting the content");
				break;
			}
			
	        result.setMsg(sb.toString());
		}while(false);
		
		if(null != connection){
			connection.disconnect();
		}
		if(null != reader){
			try {
				reader.close();
			} catch (IOException e) {
				log.error("error while close reader url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
			}
		}
		
		return result;
	}

	public static HTTPHelperResult httpGet(String reqUrl,  Map<String, String> otherHeaders){
		URL url = null;
		HttpURLConnection connection = null;
		BufferedReader reader=null;
		HTTPHelperResult result = new HTTPHelperResult(); 
		
		do{
			try {
				url = new URL(reqUrl);
			} catch (MalformedURLException e) {
				log.error("url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
				break;
			}
			try {
				connection = (HttpURLConnection) url.openConnection();
				for (Entry<String, String> entry : otherHeaders.entrySet()) {
					connection.setRequestProperty(entry.getKey(),
							entry.getValue());
				}
				connection.connect();
				
			} catch (IOException e) {
				log.error("error while connecting url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
				break;
			}
			
			int code=0;			
			try {
				code=connection.getResponseCode();
			} catch (IOException e1) {
				log.error("error while get response code url {}, msg{}",reqUrl, e1.getMessage());
				result.setCode(-1);
				result.setMsg(e1.getMessage());
				break;
			}
		 
			//from now on, we CORRECTly accessed the sever 
			result.setCode(code);
			
	        try {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
			} catch (IOException e) {
		        try {
					reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(),"utf-8"));
				} catch (IOException er) {
					log.error("error while get response data url {}, msg{}",reqUrl, e.getMessage());
					result.setCode(-1);
					result.setMsg(e.getMessage());
					break;
				}
			}
	        
	        StringBuilder sb = new StringBuilder();
	        try {
		        String line="";
				while ((line = reader.readLine()) != null){
					sb.append(line);
				}
			} catch (IOException e) {
				log.error("error while read content url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
				break;
			}
	        
	       result.setMsg(sb.toString());
			
		}while(false);
		
		if(null != connection){
			connection.disconnect();
		}
		if(null != reader){
			try {
				reader.close();
			} catch (IOException e) {
				log.error("error while close reader url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
			}
		}
		return result;		 
	}
	
	public static HTTPHelperResult httpDelete(String reqUrl,  Map<String, String> otherHeaders){
		URL url = null;
		HttpURLConnection connection = null;
		BufferedReader reader=null;
		HTTPHelperResult result = new HTTPHelperResult(); 
		
		do{
			try {
				url = new URL(reqUrl);
			} catch (MalformedURLException e) {
				log.error("url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
				break;
			}
			try {
				connection = (HttpURLConnection) url.openConnection();
				for (Entry<String, String> entry : otherHeaders.entrySet()) {
					connection.setRequestProperty(entry.getKey(),
							entry.getValue());
				}connection.setRequestMethod("DELETE");
				connection.connect();
				
			} catch (IOException e) {
				log.error("error while connecting url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
				break;
			}
			
			int code=0;			
			try {
				code=connection.getResponseCode();
			} catch (IOException e1) {
				log.error("error while get response code url {}, msg{}",reqUrl, e1.getMessage());
				result.setCode(-1);
				result.setMsg(e1.getMessage());
				break;
			}
		 
			//from now on, we CORRECTly accessed the sever 
			result.setCode(code);
			
	        try {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
			} catch (IOException e) {
		        try {
					reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(),"utf-8"));
				} catch (IOException er) {
					log.error("error while get response data url {}, msg{}",reqUrl, e.getMessage());
					result.setCode(-1);
					result.setMsg(e.getMessage());
					break;
				}
			}
	        
	        StringBuilder sb = new StringBuilder();
	        try {
		        String line="";
				while ((line = reader.readLine()) != null){
					sb.append(line);
				}
			} catch (IOException e) {
				log.error("error while read content url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
				break;
			}
	        
	       result.setMsg(sb.toString());
			
		}while(false);
		
		if(null != connection){
			connection.disconnect();
		}
		if(null != reader){
			try {
				reader.close();
			} catch (IOException e) {
				log.error("error while close reader url {}, msg{}",reqUrl, e.getMessage());
				result.setCode(-1);
				result.setMsg(e.getMessage());
			}
		}
		return result;		 
	}
	
	public static HTTPHelperResult Str2HttpHelperResult(String str){
		HTTPHelperResult httpHelperResult=new HTTPHelperResult();
		
		return httpHelperResult;
	}
}
