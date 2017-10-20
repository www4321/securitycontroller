/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;

import java.util.List;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class NetworkDeviceEntity extends KnowledgeEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7550776924078948966L;
	String entityClass;
	String macAddress;
	String ipv4Address;
	List<String> vlans;
	AttachmentPoint attachmentPoint;
	long lastSeen;
	String dhcpClientName;
	
//	public Map<String, Switch> attachedSwitches = new HashMap<String, Switch>();
//	public CloudPort attachedCloudPort;
//	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.SWITCH};
	
	public NetworkDeviceEntity(String entityClass,String macAddress,String ipv4Address,List<String> vlans,AttachmentPoint attachmentPoint,long lastSeen,String dhcpClientName){
		this.id = macAddress;
		this.type = KnowledgeType.NETWORK_DEVICE;
		this.entityClass = entityClass;
		this.macAddress = macAddress;
		this.ipv4Address = ipv4Address;
		this.vlans = vlans;
		this.attachmentPoint = attachmentPoint;
		this.lastSeen = lastSeen;
		this.dhcpClientName = dhcpClientName;
//		Date date = new Date();
//		String d1 = "dd"+ new Date().getTime();
//		super.initAffiliates(affiliatedEntityTypes);
		this.attributeMap.put(KnowledgeEntityAttribute.HARDWARE_ADDRESS, macAddress);
		this.attributeMap.put(KnowledgeEntityAttribute.IP_ADDRESS, ipv4Address);

	}

	public String getEntityClass() {
		return entityClass;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public String getIpv4Address() {
		return ipv4Address;
	}

	public List<String> getVlans() {
		return vlans;
	}

	public AttachmentPoint getAttachmentPoint() {
		return attachmentPoint;
	}

	public long getLastSeen() {
		return lastSeen;
	}

	public String getDhcpClientName() {
		return dhcpClientName;
	}
}
