package com.sds.securitycontroller.policy;

public class AllowFlowActionArgs extends PolicyActionArgs {

	private static final long serialVersionUID = -4451129371217672798L;

	int idleTimeout;
	int hardTimeout;
	String swIPAddress;
	String user;
	long dpid;
	short inPort;

	public AllowFlowActionArgs() {
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
