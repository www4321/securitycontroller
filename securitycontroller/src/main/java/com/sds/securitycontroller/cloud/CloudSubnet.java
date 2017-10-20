package com.sds.securitycontroller.cloud;

public class CloudSubnet {
	String id;
	String name;
	String network_id;
	String tenantId;
	String allocation_pools_start;
	String allocation_pools_end;
	String gateway_ip;
	String cidr;
	boolean enable_dhcp;
	
	public CloudSubnet(String id,	String name,	String network_id,	String tenantId,
			String allocation_pools_start,	String allocation_pools_end,	String gateway_ip,	String cidr,	boolean enable_dhcp){
		this.id = id;
		this.name = name;
		this.network_id = network_id;
		this.tenantId = tenantId;
		this.allocation_pools_start = allocation_pools_start;
		this.allocation_pools_end = allocation_pools_end;
		this.gateway_ip = gateway_ip;
		this.cidr = cidr;
		this.enable_dhcp =enable_dhcp ;
	}
	public String getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public String getTenantId(){
		return this.tenantId;
	}
	public String getNetwork_id(){
		return this.network_id;
	}
	public String getAllocation_pools_start(){
		return this.allocation_pools_start;
	}
	public String getAllocation_pools_end(){
		return this.allocation_pools_end;
	}
	public String getGateway_ip(){
		return this.gateway_ip;
	}
	public String getCidr(){
		return this.cidr;
	}
	public boolean getEnable_dhcp(){
		return this.enable_dhcp;
	}

}
