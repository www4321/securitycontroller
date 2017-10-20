/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.policy.resolver;

import com.sds.securitycontroller.event.EventArgs;
import com.sds.securitycontroller.policy.PolicyInfo;

public class PolicyEventArgs extends EventArgs{

	/**
	 * 
	 */
	private static final long serialVersionUID = 801208652545058940L;
	public PolicyInfo policyInfo;
	
	public PolicyEventArgs(PolicyInfo policyInfo){
		this.policyInfo = policyInfo;
	}
}
