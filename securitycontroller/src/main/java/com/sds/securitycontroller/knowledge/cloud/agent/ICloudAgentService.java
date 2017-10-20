/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud.agent;

import java.util.Map;

import com.sds.securitycontroller.knowledge.cloud.CloudNetwork;
import com.sds.securitycontroller.knowledge.cloud.CloudPort;
import com.sds.securitycontroller.knowledge.cloud.CloudRouter;
import com.sds.securitycontroller.knowledge.cloud.CloudSubnet;
import com.sds.securitycontroller.knowledge.cloud.CloudTenant;
import com.sds.securitycontroller.knowledge.cloud.CloudUser;
import com.sds.securitycontroller.knowledge.cloud.CloudVM;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface ICloudAgentService extends ISecurityControllerService{
	public Map<String, CloudTenant> getTenants();
	public Map<String, CloudUser> getUsers();

	public Map<String, CloudNetwork> getNetworks();
	public Map<String, CloudSubnet> getSubnets();
	public Map<String, CloudVM> getVMs();
	public Map<String, CloudPort> getPorts();
	public Map<String, CloudRouter> getRouters();
	
	String findTenantMacByIP(String tenantId, String ip);
	String findTenantIPByMac(String tenantId, String mac);
	

	Map<String, CloudPort> getMacIPMapping();

	
	void ResolveTenantUserMapping();
}
