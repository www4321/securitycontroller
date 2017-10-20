/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class NetworkController  extends KnowledgeEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8955141392647673364L;
	String role;
	String role_change_description;
	Date role_change_time;
	long totalMemory;
	long freeMemory;
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.NETWORK_SWITCH};
	public Map<String, SwitchCluster> switchMap = new HashMap<String, SwitchCluster>();
	
	public NetworkController(String role, String role_change_description, Date role_change_time, long totalMemory, long freeMemory){
		this.type = KnowledgeType.NETWORK_CONTROLLER;
		this.role = role;
		this.role_change_description = role_change_description;
		this.role_change_time = role_change_time;
		this.totalMemory = totalMemory;
		this.freeMemory = freeMemory;
		super.initAffiliates(affiliatedEntityTypes);
		this.attributeMap.put(KnowledgeEntityAttribute.ID, role);
		

	}

	public String getRole() {
		return role;
	}

	public String getRole_change_description() {
		return role_change_description;
	}

	public Date getRole_change_time() {
		return role_change_time;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public long getFreeMemory() {
		return freeMemory;
	}
}
