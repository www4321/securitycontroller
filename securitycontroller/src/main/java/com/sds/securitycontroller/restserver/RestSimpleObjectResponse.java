package com.sds.securitycontroller.restserver;

import java.io.Serializable;

public class RestSimpleObjectResponse implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1734750340229684468L;
	String status = null;
	Object result = null;
	
	public RestSimpleObjectResponse(String status,Object result){
		this.status = status;
		if(result instanceof Serializable)
			this.result = result;
	}

	public String getStatus() {
		return status;
	}

	public Object getResult() {
		return result;
	}

}
