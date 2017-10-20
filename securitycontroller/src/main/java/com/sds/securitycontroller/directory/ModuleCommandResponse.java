package com.sds.securitycontroller.directory;

import java.io.Serializable;
import java.util.Map;

public class ModuleCommandResponse implements Serializable{

	private static final long serialVersionUID = -2544918735203945900L;
	
	//[command] [subcommand] [object] -a -b ...
	String command;
	String subcommand;
	String object;
	Map<String, String> options;
	

	public ModuleCommandResponse(String command, String subcommand, String object, Map<String, String> options){
		this.command = command;
		this.subcommand = subcommand;
		this.object = object;
		this.options = options;
	}	
		

	public void setSubcommand(String subcommand) {
		this.subcommand = subcommand;
	}


	public void setCommand(String command) {
		this.command = command;
	}

	public String getObject() {
		return object;
	}


	public Map<String, String> getOptions() {
		return options;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public String getCommand() {
		return command;
	}


	public String getSubcommand() {
		return subcommand;
	}


	public void setObject(String object) {
		this.object = object;
	}
	
	
}
