package com.sds.securitycontroller.directory;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sds.securitycontroller.utils.IJsonable;

public class ServiceInfo implements IJsonable, Serializable {
	
	private static final long serialVersionUID = -3916232413219101057L;

	public enum ServiceStatus{
		LIVE,
		STOP,
		ERR,
	}
	
	private String id;
	public String serviceName;
	public String host;
	public String url;
	public ServiceStatus status;
	
	public ServiceInfo(String serviceName, String host, String url, ServiceStatus status){
		this.serviceName = serviceName;
		this.host = host;
		this.url = url;
		this.status = status;
		this.id = calulateId(this.serviceName, this.host);
	}
	

	public ServiceStatus getStatus() {
		return status;
	}

	public void setStatus(ServiceStatus status) {
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getHost() {
		return host;
	}

	public String getUrl() {
		return url;
	}

	
	public static String calulateId(String serviceName, String host){
		//return Cypher.getMD5(new String[]{serviceName, host});
		return serviceName + "@" + host;
	}

	@Override
    public String toJsonString() throws JsonGenerationException, IOException{
		StringWriter writer = new StringWriter();
		JsonFactory jasonFactory = new JsonFactory();       
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject(); 
			
	    	generator.writeStringField("id", 			this.id);
	    	generator.writeStringField("name", 			this.serviceName);
	    	generator.writeStringField("host", 			this.host);
	    	generator.writeStringField("url", 			this.url);
	    	generator.writeStringField("status", 		this.status.toString());
	    	
			generator.writeEndObject();
			generator.close();
		} catch (IOException e) {
			throw new IOException("json writter error");
		} catch (Exception e) {
			throw new IOException("json writter error");
		}
	 
		return writer.toString();
    }
	

}
