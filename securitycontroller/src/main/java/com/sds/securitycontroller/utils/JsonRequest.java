package com.sds.securitycontroller.utils;

import java.io.IOException; 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonRequest {	
	JsonNode root=null;
	public JsonRequest(String json) throws IOException{	
		ObjectMapper mapper = new ObjectMapper();
		root=mapper.readTree(json);
		
		if(!root.has("HEAD")){
			throw new IOException("message HEAD missing.");
		}
		if(!root.has("DATA")){
			throw new IOException("message DATA missing.");
		}
	}
	public JsonNode getHead() throws IOException{
		return this.root.path("HEAD");
	}
	public JsonNode getData() throws IOException{
		return this.root.path("DATA");
	}

}
