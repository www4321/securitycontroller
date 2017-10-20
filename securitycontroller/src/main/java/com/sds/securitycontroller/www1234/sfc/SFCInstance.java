package com.sds.securitycontroller.www1234.sfc;

import java.util.Date;
import java.util.List;

import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;

public class SFCInstance {
	protected String sfc_id;
	protected Date date;
	protected NodePortTuple start; // Service Function Chain start point.
	protected NodePortTuple end;   // Service Function Chain end point.
	protected long priority;
	protected TrafficPattern trafficPattern;
	private List<ServiceFunction> sfc;
	public String getSfc_id() {
		return sfc_id;
	}
	public void setSfc_id(String sfc_id) {
		this.sfc_id = sfc_id;
	}
	public List<ServiceFunction> getSfc() {
		return sfc;
	}
	public void setSfc(List<ServiceFunction> sfc) {
		this.sfc = sfc;
	}
	public NodePortTuple getStart() {
		return start;
	}
	public void setStart(NodePortTuple start) {
		this.start = start;
	}
	public NodePortTuple getEnd() {
		return end;
	}
	public void setEnd(NodePortTuple end) {
		this.end = end;
	}
	public long getPriority() {
		return priority;
	}
	public void setPriority(long priority) {
		this.priority = priority;
	}
	public TrafficPattern getTrafficPattern() {
		return trafficPattern;
	}
	public void setTrafficPattern(TrafficPattern trafficPattern) {
		this.trafficPattern = trafficPattern;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public SFCInstance(String sfc_id, TrafficPattern trafficPattern, List<ServiceFunction> sfc, NodePortTuple start, NodePortTuple end, 
			long priority,Date date) {
		super();
		this.sfc_id = sfc_id;
		this.start = start;
		this.end = end;
		this.priority = priority;
		this.trafficPattern = trafficPattern;
		this.sfc = sfc;
		this.date = date;
	}
	
}
