/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;

import java.util.Map;


import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class LocalVnet extends KnowledgeEntity {

	private static final long serialVersionUID = -3109406274209825383L;
	String net_Uuid;
	Map<String, LocalVlan> localVlanMap;
	
	public LocalVnet(String net_Uuid,Map<String, LocalVlan> localVlanMap){
		this.type = KnowledgeType.CLOUD_VLAN_MAP;
		this.net_Uuid = net_Uuid;
		this.attributeMap.put(KnowledgeEntityAttribute.ID, net_Uuid);
		this.localVlanMap = localVlanMap;		

		this.attributeMap.put(KnowledgeEntityAttribute.ID, net_Uuid);
		
	}
	
	public void addLocalVlan(LocalVlan vlan){
		try{
			this.localVlanMap.put(vlan.getHost(), vlan);
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}


	public String getNet_Uuid() {
		return net_Uuid;
	}

	public Map<String, LocalVlan> getLocalVlanMap() {
		return localVlanMap;
	}

}
