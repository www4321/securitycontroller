/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;

import java.util.List;
import java.util.Map;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class Switch extends KnowledgeEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 381512110389139127L;
	String dpid;
	String harole;
	long actions;
	boolean supportsOfppFlood;
	boolean supportsNxRole;
	long FastWildcards;
	boolean supportsOfppTable;
	public List<SwitchPort> ports;
	int buffers;
	String software;
	String hardware;
	String manufacturer;
	String serialNum;
	String datapath;
	int capabilities;
	String inetAddress;
	long connectedSince;
	Map<String, FlowTable> flowTableMap; 
	
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.NETWORK_SWITCH_PORT,KnowledgeType.NETWORK_DEVICE,KnowledgeType.CLOUD_VM};
	
	public Switch(String dpid,String harole,long actions,boolean supportsOfppFlood,
			boolean supportsNxRole,long FastWildcards,boolean supportsOfppTable,
			List<SwitchPort> ports,int buffers,String software,String hardware,
			String manufacturer,String serialNum,String datapath,int capabilities,
			String inetAddress,long connectedSince,Map<String, FlowTable> flowTableMap){
		this.type = KnowledgeType.NETWORK_SWITCH;
		this.dpid = dpid;
		this.id = dpid;
		this.harole = harole;
		this.actions = actions;
		this.supportsOfppFlood = supportsOfppFlood;
		this.supportsNxRole = supportsNxRole;
		this.FastWildcards = FastWildcards;
		this.supportsOfppTable = supportsOfppTable;
		this.ports = ports;
		this.buffers = buffers;
		this.software = software;
		this.hardware = hardware;
		this.manufacturer = manufacturer;
		this.serialNum = serialNum;
		this.datapath = datapath;
		this.capabilities = capabilities;
		this.inetAddress = inetAddress;
		this.connectedSince = connectedSince;
		this.flowTableMap = flowTableMap;
		super.initAffiliates(affiliatedEntityTypes);
		
		this.attributeMap.put(KnowledgeEntityAttribute.ID, dpid);
		this.attributeMap.put(KnowledgeEntityAttribute.STATUS, harole);
		this.attributeMap.put(KnowledgeEntityAttribute.IP_ADDRESS, inetAddress);
		
		String description = "software: "+software+",hardware: "+hardware+",manufacturer: "
				+manufacturer+",serialNum: "+serialNum+",datapath: "+datapath;
		this.attributeMap.put(KnowledgeEntityAttribute.DESCRIPTION, description);

	}

	public String getDpid() {
		return dpid;
	}

	public String getHarole() {
		return harole;
	}

	public long getActions() {
		return actions;
	}

	public boolean isSupportsOfppFlood() {
		return supportsOfppFlood;
	}

	public boolean isSupportsNxRole() {
		return supportsNxRole;
	}

	public long getFastWildcards() {
		return FastWildcards;
	}

	public boolean isSupportsOfppTable() {
		return supportsOfppTable;
	}

	public List<SwitchPort> getPorts() {
		return ports;
	}

	public int getBuffers() {
		return buffers;
	}

	public String getSoftware() {
		return software;
	}

	public String getHardware() {
		return hardware;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getSerialNum() {
		return serialNum;
	}

	public String getDatapath() {
		return datapath;
	}

	public int getCapabilities() {
		return capabilities;
	}

	public String getInetAddress() {
		return inetAddress;
	}

	public long getConnectedSince() {
		return connectedSince;
	}

	public Map<String, FlowTable> getFlowTableMap() {
		return flowTableMap;
	}
	
}
