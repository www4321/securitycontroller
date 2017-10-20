package com.sds.securitycontroller.app;

import java.io.IOException;
import java.io.StringWriter;
 
import java.text.SimpleDateFormat; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
 
import com.sds.securitycontroller.utils.IJsonable;

public class AppSnapshot implements IJsonable, Comparable<AppSnapshot> {
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static Logger log = LoggerFactory.getLogger(AppRealtimeInfo.class);
 
	
	
	private String appid = null;
	private String appname = null;
	private String type = "APP";
	private String filename = null;
	private int upload_time = 0;
	private long size = 0;
	
	
	public AppSnapshot(){
		this("","","",0,0);
	}
	public AppSnapshot(String appid, String appname, String filename, int time, long size){ 
		this.setAppid(appid);
		this.setAppname(appname);
		this.setFilename(filename);
		this.setSize(size);
		this.setUpload_time(time);
	}

	@Override
	public String toJsonString() throws JsonGenerationException, IOException {
		StringWriter writer = new StringWriter();
		JsonFactory jasonFactory = new JsonFactory();       
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject();
			
			generator.writeStringField("appid", this.getAppid());
			generator.writeStringField("appname", this.getAppname());
			generator.writeStringField("filename", this.getFilename());
			generator.writeStringField("upload_time", format.format(this.getUpload_time()*1000));
			generator.writeNumberField("size", this.getSize());
			
			generator.writeEndObject();
			generator.close();
		} catch (IOException e) {
			throw new IOException("json writter error");
		} catch (Exception e) {
			throw new IOException("json writter error");
		}
	 
		return writer.toString();
	}
	@Override
	public int compareTo(AppSnapshot as) {	  
		return this.appid.compareToIgnoreCase(as.appid); 
	}
 
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public long getUpload_time() {
		return upload_time;
	}
	public void setUpload_time(int upload_time) {
		this.upload_time = upload_time;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getType() {
		return type;
	}
	@SuppressWarnings("unused")
	private void setType(String type) {
		this.type = type;
	}
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	

	
	
	
	
	
	

}
