/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.command;

import java.util.List;

import com.sds.securitycontroller.policy.PolicyActionType;

public class ResolvedCommand   implements java.io.Serializable{
	//在重定向时，commandlist中一个CommandObject对应一台VM，如果重定向一个USER，则commandlist元素个数为USER的VM数	

	private static final long serialVersionUID = 1L;

	String id = null;
	//策略类型
	PolicyActionType type;
	//策略中的每条规则
	List<? extends FlowCommandBase> commandlist;
	List<? extends HttpCommandBase> httpCommandList;
	
	boolean force;
	
	public List<? extends HttpCommandBase> getHttpCommandList() {
		return httpCommandList;
	}

	public void setHttpCommandList(List<? extends HttpCommandBase> httpCommandList) {
		this.httpCommandList = httpCommandList;
	}

	public void setForce(boolean force){
		this.force = force;
	}

	public boolean isForce() {
		return this.force;
	}

	public PolicyActionType getType() {
		return type;
	}
	
	public void setId(String id){
		this.id = id;
	}


	public String getId(){
		return this.id;
	}
	
	public void setType(PolicyActionType type) {
		this.type = type;
	}

	public List<? extends FlowCommandBase> getCommandlist() {
		return commandlist;
	}

	public void setCommandlist(List<? extends FlowCommandBase> commandlist) {
		this.commandlist = commandlist;
	}

	@Override
	public String toString() {
		return "CommandResult [type=" + type
				+ ", commandlist=" + commandlist + "]";
	}
}
