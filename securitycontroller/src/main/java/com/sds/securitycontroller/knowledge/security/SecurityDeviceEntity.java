package com.sds.securitycontroller.knowledge.security;

import java.io.Serializable;

import com.sds.securitycontroller.device.Device;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class SecurityDeviceEntity extends KnowledgeEntity implements Serializable {

	private static final long serialVersionUID = -7052712429938267796L;
	
	DeviceType deviceType=DeviceType.UNKNOWN;
	String ip;
	String name;
	String details;
	String[] macAddresses;

	public SecurityDeviceEntity(Device device){
		this.type=KnowledgeType.SECURITY_DEVICE;
		this.id=device.getId();
		this.deviceType=device.getType();
		this.ip=device.getIp();
		String[] macs=new String[device.getMac_addrs().size()];
		device.getMac_addrs().toArray(macs);
		
		this.macAddresses=macs;
		//this.macAddresses=(String[]) device.getMac_addrs().toArray(); /error
		this.name=device.getName();
		this.attributeMap.put(KnowledgeEntityAttribute.ID, id);
		this.attributeMap.put(KnowledgeEntityAttribute.IP_ADDRESS, ip);
		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);
		this.attributeMap.put(KnowledgeEntityAttribute.TYPE, deviceType);
		
	}
	
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.CLOUD_PORT};

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String[] getMacAddresses() {
		return macAddresses;
	}

	public void setMacAddresses(String[] macAddresses) {
		this.macAddresses = macAddresses;
	}

	public KnowledgeType[] getAffiliatedEntityTypes() {
		return affiliatedEntityTypes;
	}

	public void setAffiliatedEntityTypes(KnowledgeType[] affiliatedEntityTypes) {
		this.affiliatedEntityTypes = affiliatedEntityTypes;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	
	
	
}
