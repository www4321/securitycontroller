/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.flow.manager;

import java.util.List;

import com.sds.securitycontroller.flow.FlowInfo;

public interface IFlowSaver {
	void saveFlowAssets(List<FlowInfo> flows, boolean ignoreZeroCount);
	void saveFlowAsset(FlowInfo flow);
}
