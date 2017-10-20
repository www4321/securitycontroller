package com.sds.securitycontroller.app;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppPackage {
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
	protected static Map<String, Method> dbFieldMapping;    
    protected static Logger log = LoggerFactory.getLogger(App.class);

    private String guid="";

	private String version="";	
	private int    update_time=0;
	private long   size=0;

	public AppPackage(){
		this("","",0,0);
	}
	public AppPackage(String guid, String version, int update_time, long size){
		this.setGuid(guid);

		this.version=version;
		this.update_time=update_time;
		this.size=size;
	}
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(int update_time) {
		this.update_time = update_time;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}


}
