/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.restserver;

public class RestResponse {
	
	String status;
	String result;
	
	public RestResponse(String status, String result){
		this.status = status;
		this.result = result;
	}

	public String getStatus() {
		return status;
	}

	public String getResult() {
		return result;
	}
	
	@Override
	public String toString(){
		return String.format("{\"status\":\"%s\",\"result\":\"%s\"}", this.status, this.result);
	}

}
