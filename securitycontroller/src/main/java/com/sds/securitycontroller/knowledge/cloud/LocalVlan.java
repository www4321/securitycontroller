/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;

import java.util.HashMap;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

	
public class LocalVlan extends KnowledgeEntity {
	
	private static final long serialVersionUID = -3110038553288702603L;
	private String netUuid=null;
	private String segmentationId;
	private int vlan;
	private String host;
	private String physicalNetwork;
	private String networkType;
	private HashMap<String,VifPort> vifPorts=new HashMap<String,VifPort>();
	
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.CLOUD_VIFPORT};
	
	public LocalVlan(){

		super.initAffiliates(affiliatedEntityTypes);

		this.type = KnowledgeType.CLOUD_VLAN;
		this.attributeMap.put(KnowledgeEntityAttribute.ID, netUuid);
	}
	
	
	public String getNetUuid() {
		return netUuid;
	}
	public void setNetUuid(String netUuid) {
		this.netUuid = netUuid;
	}
	public String getSegmentationId() {
		return segmentationId;
	}
	public void setSegmentationId(String segmentationId) {
		this.segmentationId = segmentationId;
	}
	public int getVlan() {
		return vlan;
	}
	public void setVlan(int vlan) {
		this.vlan = vlan;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPhysicalNetwork() {
		return physicalNetwork;
	}
	public void setPhysicalNetwork(String physicalNetwork) {
		this.physicalNetwork = physicalNetwork;
	}
	public String getNetworkType() {
		return networkType;
	}
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}
	public HashMap<String, VifPort> getVifPorts() {
		return vifPorts;
	}
	public void setVifPorts(HashMap<String, VifPort> vifPorts) {
		this.vifPorts = vifPorts;
	}
	

}
