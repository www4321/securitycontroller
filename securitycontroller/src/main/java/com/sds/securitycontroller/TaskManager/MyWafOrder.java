package com.sds.securitycontroller.TaskManager;




public class MyWafOrder  {
	private String id;
	private String type;
	private String ip;
	private String siteName;
	private String rules;
	
	public MyWafOrder(String id, String type, String ip, String siteName,
			String rules) {
		super();
		this.id = id;
		this.type = type;
		this.ip = ip;
		this.siteName = siteName;
		this.rules = rules;
	}

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
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String sitName) {
		this.siteName = sitName;
	}
	public String getRules() {
		return rules;
	}
	public void setRules(String rule) {
		this.rules = rule;
	}

	



}
