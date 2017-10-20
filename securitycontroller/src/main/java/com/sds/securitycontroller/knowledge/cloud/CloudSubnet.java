/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class CloudSubnet extends KnowledgeEntity {
	private static final long serialVersionUID = 2481901245942467350L;
	String name;
	String network_id;
	String tenantId;
	String allocation_pools_start;
	String allocation_pools_end;
	String gateway_ip;
	String cidr;
	boolean enable_dhcp;

//	public CloudUser user;
//	public Map<String,CloudTenant> tenantMap = new HashMap<String,CloudTenant>();
//	public Map<String,CloudVM> vmMap = new HashMap<String,CloudVM>();
//	public Map<String,CloudPort> portMap = new HashMap<String,CloudPort>();
//	public CloudNetwork network;
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.CLOUD_PORT,KnowledgeType.CLOUD_ROUTER,KnowledgeType.CLOUD_VM,KnowledgeType.SECURITY_DEVICE};
	
	public CloudSubnet(String id,	String name,	String network_id,	String tenantId,
			String allocation_pools_start,	String allocation_pools_end,	String gateway_ip,	String cidr,	boolean enable_dhcp){
		this.type = KnowledgeType.CLOUD_SUBNET;
		this.id = id;
		this.name = name;
		this.network_id = network_id;
		this.tenantId = tenantId;
		this.allocation_pools_start = allocation_pools_start;
		this.allocation_pools_end = allocation_pools_end;
		this.gateway_ip = gateway_ip;
		this.cidr = cidr;
		this.enable_dhcp =enable_dhcp ;
		super.initAffiliates(affiliatedEntityTypes);

		this.attributeMap.put(KnowledgeEntityAttribute.ID, id);
		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);
		this.attributeMap.put(KnowledgeEntityAttribute.STATISTIC_INFO, "allocation_pools_end:"+allocation_pools_end+",allocation_pools_start:"+allocation_pools_start);
		this.attributeMap.put(KnowledgeEntityAttribute.IP_ADDRESS, gateway_ip);
		
		
	}
	@Override
	public String getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public String getTenantId(){
		return this.tenantId;
	}
	public String getNetwork_id(){
		return this.network_id;
	}
	public String getAllocation_pools_start(){
		return this.allocation_pools_start;
	}
	public String getAllocation_pools_end(){
		return this.allocation_pools_end;
	}
	public String getGateway_ip(){
		return this.gateway_ip;
	}
	public String getCidr(){
		return this.cidr;
	}
	public boolean getEnable_dhcp(){
		return this.enable_dhcp;
	}

}
