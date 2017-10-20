/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.command;


public class CommandPushRequest implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String commandUrl;
	ResolvedCommand data;

	public String getCommandUrl() {
		return commandUrl;
	}

	public void setCommandUrl(String commandUrl) {
		this.commandUrl = commandUrl;
	}

	public ResolvedCommand getData() {
		return data;
	}

	public void setData(ResolvedCommand data) {
		this.data = data;
	}

	public CommandPushRequest(String commandUrl, ResolvedCommand data) {
		this.commandUrl = commandUrl;
		this.data = data;
	}

}
