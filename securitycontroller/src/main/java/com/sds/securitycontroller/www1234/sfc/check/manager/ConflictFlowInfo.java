package com.sds.securitycontroller.www1234.sfc.check.manager;

import java.util.List;

import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.www1234.sfc.SFCFlowInfo;

public class ConflictFlowInfo {
	/*
	 *  Flow rules Information installed by SFC policy.
	 */
	private SFCFlowInfo sfcFlowInfo;
	/*
	 *  all conflict flow rules Information.
	 */
	private List<FlowInfo> conflictFlows;
	public ConflictFlowInfo(SFCFlowInfo sfcFlowInfo, List<FlowInfo> conflictFlows) {
		super();
		this.sfcFlowInfo = sfcFlowInfo;
		this.conflictFlows = conflictFlows;
	}
	public SFCFlowInfo getSfcFlowInfo() {
		return sfcFlowInfo;
	}
	public void setSfcFlowInfo(SFCFlowInfo sfcFlowInfo) {
		this.sfcFlowInfo = sfcFlowInfo;
	}
	public List<FlowInfo> getConflictFlows() {
		return conflictFlows;
	}
	public void setConflictFlows(List<FlowInfo> conflictFlows) {
		this.conflictFlows = conflictFlows;
	}
	@Override
	public String toString() {
		return "ConflictFlowInfo [sfcFlowInfo=" + sfcFlowInfo + ", conflictFlows=" + conflictFlows.toString() + "]";
	}
	
}
