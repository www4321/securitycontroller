package com.sds.securitycontroller.TaskManager;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyFirewallOrder {
	private String id;
	private String type;
	private String src_ip;
	private String dst_ip;
	protected static Logger log = LoggerFactory.getLogger(MyTask.class);
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getSrc_ip() {
		return src_ip;
	}
	public void setSrc_ip(String src_ip) {
		this.src_ip = src_ip;
	}
	public String getDst_ip() {
		return dst_ip;
	}
	public void setDst_ip(String dst_ip) {
		this.dst_ip = dst_ip;
	}
	
	public MyFirewallOrder(String id, String type, String src_ip, String dst_ip) {
		super();
		this.id = id;
		this.type = type;
		this.src_ip = src_ip;
		this.dst_ip = dst_ip;
	}
	public  MyFirewallOrder() {
	}
}
