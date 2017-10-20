package com.sds.securitycontroller.knowledge.globaltraffic.analyzer;

import java.util.List;
import java.util.Map;

import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.flow.FlowTrafficStats;
import com.sds.securitycontroller.knowledge.globaltraffic.MatchPath;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IGlobalTrafficAnalyzeService extends ISecurityControllerService{
//	Map<String,MatchPath> calculateGloableTrafficMapping(List<FlowInfo> filistFlowInfos);
	void calculateGloableTrafficMapping(List<FlowInfo> flowList) throws InterruptedException;
	void recordTrafficStatus(Map<String, MatchPath> globalFlowMap);
	FlowTrafficStats queryTrafficStatus(Map<String, String> queryConditions);
	Map<String,MatchPath> getGloableTrafficMapping();
	List<MatchPath> queryFlowMatch(FlowMatch objMatch,boolean getOne);
	int getAllCount();
}
