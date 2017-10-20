package com.sds.securitycontroller.utils;

 
import java.text.SimpleDateFormat; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
 

public class Snapshot{
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static Logger log = LoggerFactory.getLogger(Snapshot.class);
	
	private String obj_id = null;
	private String name = null;
	private String type = null;
	private String filename = null;
	private int upload_time = 0;
	private long size = 0;
	
	
	public Snapshot(){
		this("","","","",0,0);
	}
	public Snapshot(String appid, String type, String name, String filename, int time, long size){ 
		this.setAppid(appid);
		this.setType(type);
		this.setName(name);
		this.setFilename(filename);
		this.setSize(size);
		this.setUpload_time(time);
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
	@JsonIgnore 
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAppid() {
		return obj_id;
	}
	public void setAppid(String appid) {
		this.obj_id = appid;
	}


	
	
	
	
	
	

}
