package com.sds.securitycontroller.command;

import java.util.Date;
/**
 * describe command status
 * @author wxt
 *
 */
public class CommandRecord{
	public String commandID;				//command description digest
	public String commandSetID = null;//belong to which resolved command
	public Date issuedTime = new Date();			//when command was issued to NC
	public boolean isApplied = false;		//whether NC comfirm that the command has been applied
	FlowCommandBase command=null;
	
	public CommandRecord(String resolvedCommandID,FlowCommandBase command){
		this.commandSetID = resolvedCommandID;
		this.command = command;
		this.commandID = command.generateId();
	}
}