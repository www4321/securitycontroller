package com.sds.securitycontroller.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

class Response{
	public int	opt_status = 200;
	public Map<String, Object> head;
	public Map<String, Object> data;
	
	public Response(){
	}
}

public class ResponseFactory {
	
	
	protected static Logger logger = LoggerFactory.getLogger(ResponseFactory.class);
	protected Response response = new Response();
	protected ServerResource server = null;
	
	public static ResponseFactory CreateResponse(String jsonResp) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return new ResponseFactory(mapper.readValue(jsonResp, Response.class));
	}
	
	protected ResponseFactory(Response response){
		this.response =	response;
	}
	
	public ResponseFactory(){
		
	}
	
	public ResponseFactory(int code, String key, Object data, ServerResource server){
		this.putData(code, key, data, server);
	}
	
    //properties
	public int getCode(){
		return this.response.opt_status;
	}
	
	public void setServer(ServerResource server){
		this.server = server;
	}
	
	public void setCode(int code){
		this.response.opt_status = code;
		if(this.server != null){
			this.server.setStatus(new Status(code));
		}
	}
	
	
	public void putData(String key, Object data){
		if(this.response.data == null){
			this.response.data = new HashMap<String, Object>();
		}
		
		this.response.data.put(key, data);
	}
	
	public void putData(int code, String key, Object data, ServerResource server){
		this.setServer(server);
		this.setCode(code);
		this.putData(key, data);
	}
	
	public Object getData(String key){
		if(this.response.data == null){
			return null;
		}
		return this.response.data.get(key);
	}
	
	public void putHead(String key, Object data){
		if(this.response.head == null){
			this.response.head = new HashMap<String, Object>();
		}
		
		this.response.head.put(key, data);
	}
	
	public Object getHead(String key){
		if(this.response.head == null){
			return null;
		}
		return this.response.head.get(key);
	}
	
	public boolean isError(){
		return this.response.opt_status != 200;
	}
	
	
	@Override
	public String toString(){
		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter();  
		String jsonReq = "";
		JsonGenerator gen;
		try {
			gen = new JsonFactory().createGenerator(writer);
			mapper.writeValue(gen, this.response);  
	        jsonReq = writer.toString(); 
	        gen.close();  
	        writer.close();
	        return jsonReq;
		} catch (IOException e) {
			logger.error("Error when convert REST request: {}", e.getMessage());
			return String.format("{\n\"opt_status\":%d\n\"head\":{\"%s\"},\n\"data\":{\"%s\"}\n}", 
					400,
					null,
					"error when convert REST request: " + e.getMessage());
		}
	}
	

}
