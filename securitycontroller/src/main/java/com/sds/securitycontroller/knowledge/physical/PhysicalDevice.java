package com.sds.securitycontroller.knowledge.physical;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;

public class PhysicalDevice extends KnowledgeEntity {

	public PhysicalDevice(String macAddress,String type) {
		super();
		this.macAddress = macAddress;
		this.type=type;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2071310042082349819L;
	
	String macAddress=null;
	String type = null;

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
}
