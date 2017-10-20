package com.sds.securitycontroller.command;

import java.io.Serializable;
import java.util.Map;

/**
 * @author xpn
 * HTTP命令封装，便于CommandPusher调用
 */
public class HttpCommandBase implements Serializable{

	private static final long serialVersionUID = 1L;

	public HttpCommandBase() {
		super();
	}
	private String url;
	private String method;
	private Map<String, String> heads;
	/**
	 * 
	 * @param url
	 * @param method
	 * @param heads
	 * @param data 
	 */
	public HttpCommandBase(String url, String method,
			Map<String, String> heads, String data) {
		super();
		this.url = url;
		this.method = method;
		this.heads = heads;
		this.data = data;
	}
	private String data;

	
	public Map<String, String> getHeads() {
		return heads;
	}
	public void setHeads(Map<String, String> heads) {
		this.heads = heads;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}

}
