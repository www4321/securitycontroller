/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.flow.manager;

import java.util.List;

import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IFlowPollingService extends ISecurityControllerService{
	void addFlowSaver(IFlowSaver saver);
	public List<FlowInfo> getAllFlows();
}
