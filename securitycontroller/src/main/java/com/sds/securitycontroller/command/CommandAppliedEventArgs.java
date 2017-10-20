package com.sds.securitycontroller.command;

import com.sds.securitycontroller.event.EventArgs;

public class CommandAppliedEventArgs extends EventArgs {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5263415215549876835L;
	public RedirectFlowCommand flowCommand;
	public String policyID = null;
	public CommandAppliedEventArgs(RedirectFlowCommand flowCommand,String policyID){
		this.flowCommand = flowCommand;
		this.policyID = policyID;
	}
}
