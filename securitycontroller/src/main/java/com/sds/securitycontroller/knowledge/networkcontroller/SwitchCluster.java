/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;

import java.util.List;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class SwitchCluster extends KnowledgeEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7505367340342337755L;
	List<String> switcheIDs;
	public SwitchCluster(List<String> switcheIDs){
		this.type = KnowledgeType.UNDEFINED;
		this.switcheIDs = switcheIDs;
	}
	public List<String> getSwitcheIDs() {
		return switcheIDs;
	}
}
