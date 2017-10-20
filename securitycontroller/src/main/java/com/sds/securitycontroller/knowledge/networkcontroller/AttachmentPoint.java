/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;

public class AttachmentPoint  implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5082081723823441805L;
	String switchDPID;
	int port;
	String errorStatus;
	
	public AttachmentPoint(String switchDPID,int port,String errorStatus){
		this.errorStatus = errorStatus;
		this.switchDPID = switchDPID;
		this.port = port;
	}

	public String getSwitchDPID() {
		return switchDPID;
	}

	public int getPort() {
		return port;
	}

	public Object getErrorStatus() {
		return errorStatus;
	}
}
