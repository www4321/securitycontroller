/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class VifPort extends KnowledgeEntity {

	private static final long serialVersionUID = -3359985213864113338L;
	private int ofport=-1;
	private String vifMac;
	private String vifId="null";
	private String portName;
	private String brName;
	
	public VifPort(){}
	
	public VifPort(String vifId,String vifMac,String portName,String brName){	
		type=KnowledgeType.CLOUD_VIFPORT;
		setVifMac(vifMac);
		setVifId(vifId);
		setPortName(portName);
		setBrName(brName);
	}
	
	public int getOfport() {
		return ofport;
	}
	public void setOfport(int ofport) {
		this.ofport = ofport;
	}
	public String getVifMac() {
		return vifMac;
	}
	public void setVifMac(String vifMac) {
		this.vifMac = vifMac;
		this.attributeMap.put(KnowledgeEntityAttribute.HARDWARE_ADDRESS, vifMac);
	}
	public String getVifId() {
		return vifId;
	}
	public void setVifId(String vifId) {
		this.vifId = vifId;
		this.attributeMap.put(KnowledgeEntityAttribute.ID, vifId);
	}
	public String getPortName() {
		return portName;
	}
	public void setPortName(String portName) {
		this.portName = portName;
	}
	public String getBrName() {
		return brName;
	}
	public void setBrName(String brName) {
		this.brName = brName;
	}
	
}