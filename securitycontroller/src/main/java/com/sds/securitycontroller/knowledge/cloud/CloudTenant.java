/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;

import java.util.HashMap;
import java.util.Map;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class CloudTenant extends KnowledgeEntity {
	
	private static final long serialVersionUID = 8475218985972226090L;
	private String name;
	
//	public CloudNetwork network;
//	public Map<String, CloudPort> portMap = new HashMap<String, CloudPort>();
	public Map<String, CloudUser> users;
	
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.CLOUD_USER,
			KnowledgeType.CLOUD_ROUTER,KnowledgeType.CLOUD_NETWORK,KnowledgeType.CLOUD_SUBNET,
			KnowledgeType.CLOUD_VM,KnowledgeType.CLOUD_PORT};
	
	
	public CloudTenant (String id, String name){
		this.type = KnowledgeType.CLOUD_TENANT;
		this.setId(id);
		this.setName(name);
		this.users = new HashMap<String, CloudUser>();
		super.initAffiliates(affiliatedEntityTypes);
		this.attributeMap.put(KnowledgeEntityAttribute.ID, id);
		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addUser(CloudUser user){
		this.users.put(user.getId(), user);
	}
	

	public CloudUser findUser(String userId){
		return this.users.get(userId);
	}
}
