/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;

//import com.sds.securitycontroller.knowledge.networkcontroller.SwitchPort;
import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class CloudPort  extends KnowledgeEntity{

	private static final long serialVersionUID = 3497373415798807548L;
	String ipaddr;
	String mac;
	String subnet;
	String deviceId;
	String tenantId;
	String networkId;
	boolean stateUp;
	
//	public CloudVM cloudDevice;
//	public CloudTenant tenant;
//	public CloudSubnet subnetObj;
//	public CloudNetwork network;
	
//	public Device netDevice;
//	public SwitchPort NCSwitchPort;
	
	public CloudPort(String id, String ipaddr, String mac, String subnet, String deviceId, String tenantId, String networkId, boolean stateUp){
		this.type = KnowledgeType.CLOUD_PORT;
		this.id = id;
		this.ipaddr = ipaddr;
		this.mac = mac;
		this.subnet = subnet;
		this.deviceId = deviceId;
		this.tenantId = tenantId;
		this.networkId = networkId;
		this.stateUp = stateUp;
		this.attributeMap.put(KnowledgeEntityAttribute.ID, id);
		this.attributeMap.put(KnowledgeEntityAttribute.HARDWARE_ADDRESS, mac);
		this.attributeMap.put(KnowledgeEntityAttribute.STATUS, stateUp?"up":"down");
//		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);
	}

	public String getIpaddr() {
		return ipaddr;
	}

	public String getMac() {
		return mac;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getSubnet() {
		return subnet;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public String getNetworkId() {
		return networkId;
	}

	public boolean isStateUp() {
		return stateUp;
	}

}
