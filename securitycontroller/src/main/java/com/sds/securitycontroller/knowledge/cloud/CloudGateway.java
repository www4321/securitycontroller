/** 
*    Copyright 2014 BUPTT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class CloudGateway extends KnowledgeEntity implements java.io.Serializable{
	private static final long serialVersionUID = 8837599909970429096L;
	String network_id;
	public CloudGateway(String network_id){
		this.network_id = network_id;
		this.type = KnowledgeType.UNDEFINED;
	}
	public String getNetwork_id() {
		return network_id;
	}
	public void setNetwork_id(String network_id) {
		this.network_id = network_id;
	}
}
