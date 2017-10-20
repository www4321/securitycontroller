package com.sds.securitycontroller.policy;

import java.io.Serializable
;
import java.util.ArrayList;
import java.util.List;

public class TrafficPattern implements Serializable {

	private static final long serialVersionUID = -867092828103512527L;
	String hostIP;
	List<String> ignoredIPList = new ArrayList<String>();
	String trafficDirection;
	String targetIP;
	String servicePort;
	String serviceDetails;
	String inPortName;
	
	// use for switch port
	String srcMac;
	
	public String getHostIP() {
		return hostIP;
	}
	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}
	public String getTrafficDirection() {
		return trafficDirection;
	}
	public void setTrafficDirection(String trafficDirection) {
		this.trafficDirection = trafficDirection;
	}
	public String getTargetIP() {
		return targetIP;
	}
	public void setTargetIP(String targetIP) {
		this.targetIP = targetIP;
	}
	public String getServicePort() {
		return servicePort;
	}
	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}
	public String getServiceDetails() {
		return serviceDetails;
	}
	public void setServiceDetails(String serviceDetails) {
		this.serviceDetails = serviceDetails;
	}
	public List<String> getIgnoredIPList() {
		return ignoredIPList;
	}
	public void addIgnoredIP(String IP){
		ignoredIPList.add(IP);
	}
	
	public void setSrcMac(String srcMacString){
		this.srcMac = srcMacString;
	}
	public String getSrcMac(){
		return this.srcMac;
	}
	public String getInPortName() {
		return inPortName;
	}
	public void setInPortName(String inPortName) {
		this.inPortName = inPortName;
	}
}
