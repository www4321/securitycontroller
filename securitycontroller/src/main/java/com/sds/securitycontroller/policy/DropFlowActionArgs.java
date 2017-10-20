package com.sds.securitycontroller.policy;

public class DropFlowActionArgs extends PolicyActionArgs {

	private static final long serialVersionUID = -6407241032895149071L;
	int idleTimeout;
	int hardTimeout;
	long dpid;
	short inPort;
	
	public DropFlowActionArgs() {
		super();
	}

	public int getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public int getHardTimeout() {
		return hardTimeout;
	}

	public void setHardTimeout(int hardTimeout) {
		this.hardTimeout = hardTimeout;
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


}
