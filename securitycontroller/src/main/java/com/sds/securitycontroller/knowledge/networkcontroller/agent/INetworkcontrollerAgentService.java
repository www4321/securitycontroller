/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller.agent;

import java.util.Map;

import com.sds.securitycontroller.knowledge.networkcontroller.*;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface INetworkcontrollerAgentService extends ISecurityControllerService{
	Topology getTopologyStatus();
	NetworkController getNetworkControllerStatus();
	Map<String,Switch> getSwitches();
	Map<String,NetworkDeviceEntity> getDevices();
//	Map<String,FlowTable> getFlowTables();
//	Map<String,PortStatus> getPortStatus();
//	List<TopologyLink> getTopologyLinks();
	
}
