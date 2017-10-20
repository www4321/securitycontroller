package com.sds.securitycontroller.directory;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sds.securitycontroller.utils.IJsonable;

public class ModuleCommand implements IJsonable, Serializable{

	private static final long serialVersionUID = -2544918735203945900L;
	
	//[command] [subcommand] [object] -a -b ...
	String command;
	List<String> subcommand;
	Set<String> options;
	String comment;
	/* like:
	 * object: what is object \n -a value1: what is -a \n ...
	 * */
	String module;
	

	public ModuleCommand(String command){
		this.command = command;
	}
	
	
	public ModuleCommand(String command, String module){
		this.command = command;
		this.module = module;
	}
		
	public List<String> getSubcommand() {
		return subcommand;
	}


	public void setSubcommand(List<String> subcommand) {
		this.subcommand = subcommand;
	}


	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Set<String> getOptions() {
		return options;
	}

	public void setOptions(Set<String> options) {
		this.options = options;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}	
	
	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}


	@Override
	public String toJsonString() throws JsonGenerationException, IOException {
		StringWriter writer = new StringWriter();
		JsonFactory jasonFactory = new JsonFactory();       
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject(); 
			
	    	generator.writeStringField("command", 	this.command);
	    	generator.writeArrayFieldStart("subcommand");
	    	for(String subcmd: this.subcommand)
	    		generator.writeString(subcmd);
	    	generator.writeEndArray();	    
	    	generator.writeArrayFieldStart("options");
	    	for(String option: this.options)
	    		generator.writeString(option);
	    	generator.writeEndArray();	    	
	    	if(comment != null)
	    		generator.writeStringField("comment", 	this.comment);
	    	
			generator.writeEndObject();
			generator.close();
		} catch (Exception e) {
			throw new IOException("json writter error");		
		}
	 
		return writer.toString();
	}
}
