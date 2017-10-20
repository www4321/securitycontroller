/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;

import java.util.List;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class CloudRouter extends KnowledgeEntity {

	private static final long serialVersionUID = -4509909839014728918L;
	String name;
	String status;
	String tenant_id;
	boolean admin_state_up;
	CloudGateway externalGatewayInfo;
	List<String> portIdList=null;  
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.CLOUD_PORT,KnowledgeType.CLOUD_NETWORK,
			KnowledgeType.CLOUD_SUBNET,KnowledgeType.CLOUD_VM};
	
	public CloudRouter(String id,String name,String status,String tenant_id,boolean admin_state_up,CloudGateway externalGatewayInfo){
		this.type = KnowledgeType.CLOUD_ROUTER;
		this.id = id;
		this.name = name;
		this.status = status;
		this.tenant_id = tenant_id;
		this.admin_state_up = admin_state_up;
		this.externalGatewayInfo = externalGatewayInfo;

		super.initAffiliates(affiliatedEntityTypes);
		this.attributeMap.put(KnowledgeEntityAttribute.ID, id);
		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);
		this.attributeMap.put(KnowledgeEntityAttribute.STATUS, status);
		this.attributeMap.put(KnowledgeEntityAttribute.EXTERNAL_GATEWAY, externalGatewayInfo.network_id);
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

	public CloudGateway getExternalGatewayInfo() {
		return externalGatewayInfo;
	}

	public KnowledgeType[] getAffiliatedEntityTypes() {
		return affiliatedEntityTypes;
	}

	public List<String> getPortIdList() {
		return portIdList;
	}

	public void setPortIdList(List<String> portIdList) {
		this.portIdList = portIdList;
	}

	public void setExternalGatewayInfo(CloudGateway externalGatewayInfo) {
		this.externalGatewayInfo = externalGatewayInfo;
	}
}
