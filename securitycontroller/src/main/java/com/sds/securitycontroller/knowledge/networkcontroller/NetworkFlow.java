/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;

import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class NetworkFlow extends KnowledgeEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3206841699479954476L;
	String switchDpid;
	FlowInfo flowInfo;
	
	public NetworkFlow(FlowInfo flowInfo){
		this.type = KnowledgeType.NETWORK_FLOW;
		this.flowInfo = flowInfo;
	}
}
