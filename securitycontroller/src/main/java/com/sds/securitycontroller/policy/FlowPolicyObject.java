package com.sds.securitycontroller.policy;

import java.util.ArrayList;
import java.util.List;

import com.sds.securitycontroller.flow.FlowMatch;

public class FlowPolicyObject extends PolicyObject {

	private static final long serialVersionUID = 1L;
	protected FlowMatch flowPattern = null;
	protected TrafficPattern trafficPattern;
	protected List<FlowMatch> resolvedFlowMatches = new ArrayList<FlowMatch>();  
	protected Object flowArgs;
	
	public Object getFlowArgs() {
		return flowArgs;
	}
	public void setFlowArgs(Object flowArgs) {
		this.flowArgs = flowArgs;
	}
	public FlowPolicyObject() {
	}
	public FlowPolicyObject(List<FlowMatch> resolvedFlowMatches) {
		this.resolvedFlowMatches=resolvedFlowMatches;
	}
	public FlowMatch getFlowPattern() {
		return flowPattern;
	}

	public void setFlowPattern(FlowMatch flowPattern) {
		this.flowPattern = flowPattern;
	}

	public TrafficPattern getTrafficPattern() {
		return trafficPattern;
	}

	public void setTrafficPattern(TrafficPattern trafficPattern) {
		this.trafficPattern = trafficPattern;
	}

	public List<FlowMatch> getResolvedFlowMatches() {
		return resolvedFlowMatches;
	}

	public void setResolvedFlowMatches(List<FlowMatch> resolvedFlowMatches) {
		this.resolvedFlowMatches = resolvedFlowMatches;
	}
	@Override
	public Object getObject() {
		return this;
	}
}
