package com.sds.securitycontroller.utils;

import java.io.IOException; 
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class InputMessage {	
	JsonNode root=null;
	//if this input is a response from an http request
	private boolean response;
	public InputMessage(boolean bResponse, String json) throws IOException{
		this.setResponse(bResponse);
		this.parse(json);
	}
	
	//parse an input String into json nodes
	private void parse(String json) throws IOException{	
		ObjectMapper mapper = new ObjectMapper();
		root=mapper.readTree(json);
		
		if(isResponse() && !root.has("opt_status")){
			throw new IOException("message opt_status missing.");
		}		
		if(root.has("opt_status") && !root.path("opt_status").isInt()){
			throw new IOException("message opt_status is not a number.");
		}
		if(!root.has("head")){
			throw new IOException("message head missing.");
		}
		if(!root.has("data")){
			throw new IOException("message data missing.");
		}
	}
	
	@Override
	public String toString(){
		if(isResponse()){
			return root.toString();			
		}
		return "{"+root.path("head").toString()+","+root.path("data").toString()+"}";	
	}
	
	//properties
	public int getOpt_status() {
		return root.path("opt_status").asInt();
	}
 
	public JsonNode getHead() {
		return root.path("head");
	} 
	
	//get the 'data' as a JsonNode
	public JsonNode getData() {
		return root.path("data");
	} 
	public Map<String, Object> getDataMap() throws IOException{
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> result = mapper.readValue(this.root.path("data").toString(), new TypeReference<Map<String,Object>>() { });
		return result;
	}
	
	//quick result 
	public void setResult(String result) {
		String json = null == result?"":result;
		json="{\"opt_status\":-1, \"head\":{}, \"data\":{\"result\":"+result+"}}";
		ObjectMapper mapper = new ObjectMapper();
		try {
			root=mapper.readTree(json); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isResponse() {
		return response;
	}

	public void setResponse(boolean response) {
		this.response = response;
	}
	
}
