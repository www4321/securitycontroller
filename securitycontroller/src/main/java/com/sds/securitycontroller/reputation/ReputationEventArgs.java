/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.reputation;

import com.sds.securitycontroller.event.EventArgs;

public class ReputationEventArgs  extends EventArgs{

	private static final long serialVersionUID = 801208652545058942L;
	public Object sourceObject;
	
	public ReputationEventArgs(Object sourceObject){
		this.sourceObject = sourceObject;
	}
}