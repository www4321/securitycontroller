/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.log;



public class LogPushRequest {

	String logurl;
	Report logdata;

	public String getLogurl() {
		return logurl;
	}

	public void setLogurl(String logurl) {
		this.logurl = logurl;
	}

	public Report getLogdata() {
		return logdata;
	}

	public void setLogdata(Report logdata) {
		this.logdata = logdata;
	}

	public LogPushRequest(String logurl, Report logdata) {
		this.logurl = logurl;
		this.logdata = logdata;
	}

	
}
