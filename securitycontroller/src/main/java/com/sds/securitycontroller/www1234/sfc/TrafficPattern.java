package com.sds.securitycontroller.www1234.sfc;

// Traffic Pattern is used to illustrate that what kind of traffic is steered by service function chain.
public class TrafficPattern {
	
	protected String dataLayerSource;
    protected String dataLayerDestination;
    protected String networkSource ;
    protected String networkDestination ;
    protected int transportSource;
    protected int transportDestination;
    protected int networkProtocol;
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
	public TrafficPattern(String dataLayerSource, String dataLayerDestination, String networkSource,
			String networkDestination, int transportSource, int transportDestination, int networkProtocol) {
		super();
		this.dataLayerSource = dataLayerSource;
		this.dataLayerDestination = dataLayerDestination;
		this.networkSource = networkSource;
		this.networkDestination = networkDestination;
		this.transportSource = transportSource;
		this.transportDestination = transportDestination;
		this.networkProtocol = networkProtocol;
	}
	@Override
	public String toString() {
		return "TrafficPattern [src_mac=*"  + ", dst_mac=*" 
				+ ", src_ip=" + networkSource + ",\n dst_ip=" + networkDestination
				+ ", src_port=" + transportSource + ", dst_port=" + transportDestination
				+ ", protocol=" + networkProtocol + "]";
	}
}
