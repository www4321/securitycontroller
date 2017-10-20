/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event;

import com.sds.securitycontroller.knowledge.KnowledgeType;

public class KnowledgeEventArgs extends EventArgs {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2133975441087865667L;
	public KnowledgeType entityType;
	public Object knowledgeObj;
	public KnowledgeEventArgs(KnowledgeType entityType,Object knowledge){
		this.entityType = entityType;
		knowledgeObj = knowledge;
	}
	
}
