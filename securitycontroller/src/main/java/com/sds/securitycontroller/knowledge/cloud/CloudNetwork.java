/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;
import java.util.List;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;
/**
 * 云网络，对应tenant，有三个从属实体：PORT，SUBNET，VM）
 * @author xpn
 *
 */
public class CloudNetwork  extends KnowledgeEntity{

	private static final long serialVersionUID = 8669949851460066406L;

	String name;
	String status;
	String tenant_id;//租户ID
	boolean admin_state_up;
	boolean shared;

	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.CLOUD_PORT,//KnowledgeType.CLOUD_ROUTER,
			KnowledgeType.CLOUD_SUBNET,KnowledgeType.CLOUD_VM};
	List<String> subnetIDs; 
	
	public CloudNetwork(String id,String name,String status,String tenant_id,boolean admin_state_up,
			boolean shared, List<String> subnetIDs){
		super.initAffiliates(affiliatedEntityTypes);
		this.type = KnowledgeType.CLOUD_NETWORK;
		this.id = id;
		this.name = name;
		this.status = status;
		this.tenant_id=tenant_id;
		this.admin_state_up = admin_state_up;
		this.shared = shared;
		this.subnetIDs = subnetIDs;

		this.attributeMap.put(KnowledgeEntityAttribute.ID, id);
		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);
		this.attributeMap.put(KnowledgeEntityAttribute.STATUS, admin_state_up?"up":"down");
	}
	
	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public boolean isAdmin_state_up() {
		return admin_state_up;
	}

	public boolean isShared() {
		return shared;
	}


	public List<String> getSubnetIDs() {
		return subnetIDs;
	}
}
