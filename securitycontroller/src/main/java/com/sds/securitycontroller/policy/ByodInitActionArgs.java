package com.sds.securitycontroller.policy;

public class ByodInitActionArgs extends PolicyActionArgs {

	private static final long serialVersionUID = -5626351080326462361L;

	long dpid;
	short inPort;
	String serverIp;
	String serverMac;
	String network;
	short mask;
	
	public ByodInitActionArgs() {
		super();
	}
	
	public ByodInitActionArgs(long dpid, short inPort, String serverIp, String serverMac,
			String network, short mask) {
		super();
		this.dpid = dpid;
		this.inPort = inPort;
		this.serverIp = serverIp;
		this.serverMac = serverMac;
		this.network = network;
		this.mask = mask;
	}

	public long getDpid() {
		return dpid;
	}

	public void setDpid(long dpid) {
		this.dpid = dpid;
	}

	public short getInPort() {
		return inPort;
	}

	public void setInPort(short inPort) {
		this.inPort = inPort;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getServerMac() {
		return serverMac;
	}

	public void setServerMac(String serverMac) {
		this.serverMac = serverMac;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public short getMask() {
		return mask;
	}

	public void setMask(short mask) {
		this.mask = mask;
	}
	



}
