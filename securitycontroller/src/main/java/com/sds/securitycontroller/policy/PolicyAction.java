package com.sds.securitycontroller.policy;

import java.io.Serializable;

public class PolicyAction implements Serializable {

	private static final long serialVersionUID = -2279106291656674156L;
	//采取策略的类型
	PolicyActionType type;
	PolicyActionArgs actionArgs;

	public PolicyAction(PolicyActionType actionType,PolicyActionArgs actionArgs){
		this.type = actionType;
		this.actionArgs = actionArgs;
	}
	
	public PolicyActionType getType() {
		return type;
	}
	public void setType(PolicyActionType type) {
		this.type = type;
	}
	public PolicyActionArgs getActionArgs() {
		return actionArgs;
	}
	public void setActionArgs(PolicyActionArgs actionArgs) {
		this.actionArgs = actionArgs;
	}
}
