package com.sds.securitycontroller.cloud;

public class CloudPort {
	String ipaddr;
	String mac;
	String id;
	String subnet;
	String deviceId;
	String tenantId;
	String networkId;
	boolean stateUp;
	
	public CloudPort(String id, String ipaddr, String mac, String subnet, String deviceId, String tenantId, String networkId, boolean stateUp){
		this.id = id;
		this.ipaddr = ipaddr;
		this.mac = mac;
		this.subnet = subnet;
		this.deviceId = deviceId;
		this.tenantId = tenantId;
		this.networkId = networkId;
		this.stateUp = stateUp;
	}

}
