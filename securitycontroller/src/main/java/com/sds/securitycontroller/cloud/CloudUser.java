package com.sds.securitycontroller.cloud;

import java.util.HashMap;
import java.util.Map;

public class CloudUser {
	
	String id;
	private Map<String, CloudVM> vms;
	private Map<String, CloudSubnet> subnets;
	
	public Map<String, CloudVM> getVms() {
		return vms;
	}
	public Map<String, CloudSubnet> getSubnets() {
		return subnets;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	String name;
	boolean enabled;
	String tenantId;
	
	public CloudUser(String id, String name, boolean enabled, String tenantId){
		this.id = id;
		this.name = name;
		this.enabled = enabled;
		this.tenantId = tenantId;
		this.vms = new HashMap<String, CloudVM>();
		this.subnets = new HashMap<String, CloudSubnet>();
				
	}

}
