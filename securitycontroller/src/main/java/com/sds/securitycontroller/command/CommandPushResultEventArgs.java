/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.command;

import com.sds.securitycontroller.event.EventArgs;

public class CommandPushResultEventArgs extends EventArgs{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3242260460018686616L;
	public CommandPushRequest commandPushResult;
	
	public CommandPushResultEventArgs(CommandPushRequest commandPushResult){
		this.commandPushResult = commandPushResult;
	}
}
