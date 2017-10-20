package com.sds.securitycontroller.utils;

import java.io.IOException; 
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.databind.ObjectMapper;

 
 
public class OutputMessage {
	//json
	private int opt_status=200;
	private Map<String,String> head=new HashMap<String,String>();
	private Map<String,Object> data=new HashMap<String,Object>();

	//if this input is a response to an incoming http request
	private boolean response;	
	private ServerResource serverResource=null;
	
	private String rawDataKey = null;
	private String rawDataValue = null;
	
	public OutputMessage(boolean bResponse){
		this.setResponse(bResponse);
	}
	public OutputMessage(boolean bResponse, ServerResource serverResource){
		this(bResponse);
		this.serverResource=serverResource;
	}
	@Override
	public String toString(){		 
		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter(); 
	
		do{     
			try{
				writer.append("{");
				if(this.isResponse()){		 
					writer.append("\"opt_status\":");
					mapper.writeValue(writer,this.opt_status);
					writer.append(",");
				}
				writer.append("\"head\":");
				mapper.writeValue(writer, this.head);
				writer.append(",");
				writer.append("\"data\":");
				if(rawDataKey != null)
				{
					writer.append("{");
					writer.append("\"" + this.rawDataKey + "\":");
					writer.append(this.rawDataValue);
					writer.append("}");
				}
				else
				{
					mapper.writeValue(writer, this.data); 
				}
				writer.append("}");				
			} catch (IOException e) {
				break;
			}
		}while(false);
		try {
			writer.close();
		} catch (IOException e) {
		    
		}
		return writer.toString();
	}
	 
	
	//http
	public boolean isResponse() {
		return response;
	}
	public void setResponse(boolean response) {
		this.response = response;
	}
	public void setHttp_code(int http_code) {		
		if(null != this.serverResource){
			this.serverResource.setStatus(new Status(http_code));
		}
	}
	public void setServerResource(ServerResource server_resource) {
		this.serverResource = server_resource;
	}	
	public void setOpt_status(int opt_status) {
		this.opt_status=opt_status;
	}
	//message 
	public boolean setHead(String key, String value)  {
		if(null==key || key.isEmpty() || null==value || value.isEmpty()){
			return false;
		}
		this.head.put(key, value);
		return true;
	} 	
	public boolean setData(Map<String,Object> data){
		if(null == data){
			return false;
		}
		this.data=data;
		return true;
	}
	
	public boolean putData(String key, Object value)  {
		if(null==key || key.isEmpty() || null==value){
			return false;
		}
		this.data.put(key, value);
		return true;
	}

	//quick result
	public boolean setResult(String result){	 
		return this.putData("result", result);		 
	}
	
	//machi
	public void setRawResult(int http_code, int opt_status, String key, String value)
	{
		this.rawDataKey = key;
		this.rawDataValue = value;
	}
 
	public boolean setResult(int http_code, int opt_status, String result){
		this.setHttp_code(http_code);
		this.setOpt_status(opt_status);
		return this.setResult(result);
	}
	
	public boolean setResult(int http_code, int opt_status,String type, String result){
		this.setHttp_code(http_code);
		this.setOpt_status(opt_status);
		return this.putData(type,result);
	}

	
	
}
