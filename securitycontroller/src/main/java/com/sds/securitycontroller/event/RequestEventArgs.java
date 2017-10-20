/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event;

import com.sds.securitycontroller.knowledge.KnowledgeType;

public class RequestEventArgs extends EventArgs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8024591354641472425L;
	public KnowledgeType entityType;
	
	public RequestEventArgs(KnowledgeType entityType){
		this.entityType = entityType;
	}

}
