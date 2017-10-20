package com.sds.securitycontroller.www1234.sfc;

public class SFCFlowInfo {
	protected String dpid;
	protected short in_port;
	protected String dataLayerSource;
	protected String dataLayerDestination;
	protected String networkSource ;
	protected String networkDestination ;
	private int networkDestinationMaskLen;
    private int networkSourceMaskLen;
	protected int transportSource;
	protected int transportDestination;
	protected int networkProtocol;
	protected long priority;
	protected short out_port;
	
	
	public SFCFlowInfo(String dpid, short in_port, String dataLayerSource, String dataLayerDestination,
			String networkSource, String networkDestination, int networkDestinationMaskLen, int networkSourceMaskLen,
			int transportSource, int transportDestination, int networkProtocol, long priority, short out_port) {
		super();
		this.dpid = dpid;
		this.in_port = in_port;
		this.dataLayerSource = dataLayerSource;
		this.dataLayerDestination = dataLayerDestination;
		this.networkSource = networkSource;
		this.networkDestination = networkDestination;
		this.networkDestinationMaskLen = networkDestinationMaskLen;
		this.networkSourceMaskLen = networkSourceMaskLen;
		this.transportSource = transportSource;
		this.transportDestination = transportDestination;
		this.networkProtocol = networkProtocol;
		this.priority = priority;
		this.out_port = out_port;
	}
	
	@Override
	public String toString() {
		return "SFCFlowInfo [dpid=" + dpid + ", in_port=" + in_port + ", dataLayerSource=" + dataLayerSource
				+ ", dataLayerDestination=" + dataLayerDestination + ", networkSource=" + networkSource
				+ ", networkDestination=" + networkDestination + ", networkDestinationMaskLen="
				+ networkDestinationMaskLen + ", networkSourceMaskLen=" + networkSourceMaskLen + ", transportSource="
				+ transportSource + ", transportDestination=" + transportDestination + ", networkProtocol="
				+ networkProtocol + ", priority=" + priority + ", out_port=" + out_port + "]";
	}

	public int getNetworkDestinationMaskLen() {
		return networkDestinationMaskLen;
	}
	public void setNetworkDestinationMaskLen(int networkDestinationMaskLen) {
		this.networkDestinationMaskLen = networkDestinationMaskLen;
	}
	public int getNetworkSourceMaskLen() {
		return networkSourceMaskLen;
	}
	public void setNetworkSourceMaskLen(int networkSourceMaskLen) {
		this.networkSourceMaskLen = networkSourceMaskLen;
	}
	public String getDpid() {
		return dpid;
	}
	public void setDpid(String dpid) {
		this.dpid = dpid;
	}
	public short getIn_port() {
		return in_port;
	}
	public void setIn_port(short in_port) {
		this.in_port = in_port;
	}
	public String getDataLayerSource() {
		return dataLayerSource;
	}
	public void setDataLayerSource(String dataLayerSource) {
		this.dataLayerSource = dataLayerSource;
	}
	public String getDataLayerDestination() {
		return dataLayerDestination;
	}
	public void setDataLayerDestination(String dataLayerDestination) {
		this.dataLayerDestination = dataLayerDestination;
	}
	public String getNetworkSource() {
		return networkSource;
	}
	public void setNetworkSource(String networkSource) {
		this.networkSource = networkSource;
	}
	public String getNetworkDestination() {
		return networkDestination;
	}
	public void setNetworkDestination(String networkDestination) {
		this.networkDestination = networkDestination;
	}
	public int getTransportSource() {
		return transportSource;
	}
	public void setTransportSource(int transportSource) {
		this.transportSource = transportSource;
	}
	public int getTransportDestination() {
		return transportDestination;
	}
	public void setTransportDestination(int transportDestination) {
		this.transportDestination = transportDestination;
	}
	public int getNetworkProtocol() {
		return networkProtocol;
	}
	public void setNetworkProtocol(int networkProtocol) {
		this.networkProtocol = networkProtocol;
	}
	public long getPriority() {
		return priority;
	}
	public void setPriority(long priority) {
		this.priority = priority;
	}
	public short getOut_port() {
		return out_port;
	}
	public void setOut_port(short out_port) {
		this.out_port = out_port;
	}
	 
}
