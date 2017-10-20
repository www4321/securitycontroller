package com.sds.securitycontroller.directory;

import com.sds.securitycontroller.event.EventArgs;

public class CommandEventArgs extends EventArgs{
	private static final long serialVersionUID = 194542312828560349L;
	public ModuleCommand[] commands;	
	public String module;
	
	public CommandEventArgs(String module, ModuleCommand[] commands){
		this.module = module;
		this.commands = commands;
	}
}
