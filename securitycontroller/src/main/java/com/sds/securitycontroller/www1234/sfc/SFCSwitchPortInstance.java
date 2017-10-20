package com.sds.securitycontroller.www1234.sfc;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;

public class SFCSwitchPortInstance {
	protected String sfc_id;
	protected Date date;
	protected List<NodePortTuple> path; 

	protected long priority;
	/**
	 * "trafficPattern": {
        	"src-ip":"10.0.0.1",
        	"dst-ip":"10.0.0.4",
        	"src-mac":"00:00:00:00:00:01",
        	"dst-mac":"00:00:00:00:00:04",
        	"src-port":"22",
        	"dst-port":"80",
        	"protocol":"TCP"
        },
	 */
	protected Map<String,String> trafficPattern;

	public String getSfc_id() {
		return sfc_id;
	}
	public void setSfc_id(String sfc_id) {
		this.sfc_id = sfc_id;
	}
	
	public List<NodePortTuple> getPath() {
		return path;
	}
	public void setPath(List<NodePortTuple> path) {
		this.path = path;
	}
	public long getPriority() {
		return priority;
	}
	public void setPriority(long priority) {
		this.priority = priority;
	}
	public Map<String, String> getTrafficPattern() {
		return trafficPattern;
	}
	public void setTrafficPattern(Map<String, String> trafficPattern) {
		this.trafficPattern = trafficPattern;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public SFCSwitchPortInstance(String sfc_id, Date date, List<NodePortTuple> path, long priority,
			Map<String, String> trafficPattern) {
		super();
		this.sfc_id = sfc_id;
		this.date = date;
		this.path = path;
		this.priority = priority;
		this.trafficPattern = trafficPattern;
	}
	
}
