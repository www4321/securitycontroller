/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;

import java.util.HashMap;
import java.util.Map;

import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.common.User;

public class CloudUser extends User {
	
	private static final long serialVersionUID = -9199973647890493457L;
	private Map<String, CloudVM> vms;
	private Map<String, CloudSubnet> subnets;
	
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.CLOUD_SUBNET,KnowledgeType.CLOUD_VM,KnowledgeType.CLOUD_PORT};
	
	public Map<String, CloudVM> getVms() {
		return vms;
	}
	public Map<String, CloudSubnet> getSubnets() {
		return subnets;
	}

	
	public CloudUser(String id, String name, boolean enabled, String tenantId){
		super(name, enabled, tenantId);
		this.type = KnowledgeType.CLOUD_USER;
		this.id = id;
		this.vms = new HashMap<String, CloudVM>();
		this.subnets = new HashMap<String, CloudSubnet>();
		super.initAffiliates(affiliatedEntityTypes);
		this.attributeMap.put(KnowledgeEntityAttribute.ID, id);
		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);
	}
}
