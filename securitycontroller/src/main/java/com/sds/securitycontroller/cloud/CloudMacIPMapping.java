package com.sds.securitycontroller.cloud;

public class CloudMacIPMapping {
	String ipaddr;
	String mac;
	String id;
	String subnet;
	
	public CloudMacIPMapping(String id, String ipaddr, String mac, String subnet){
		this.id = id;
		this.ipaddr = ipaddr;
		this.mac = mac;
		this.subnet = subnet;			
	}

}
