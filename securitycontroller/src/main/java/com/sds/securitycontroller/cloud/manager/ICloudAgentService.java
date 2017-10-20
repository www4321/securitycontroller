package com.sds.securitycontroller.cloud.manager;

import java.util.Map;

import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.event.manager.IRPCHandler;
import com.sds.securitycontroller.knowledge.cloud.CloudPort;
import com.sds.securitycontroller.knowledge.cloud.CloudSubnet;
import com.sds.securitycontroller.knowledge.cloud.CloudTenant;
import com.sds.securitycontroller.knowledge.cloud.CloudUser;
import com.sds.securitycontroller.knowledge.cloud.CloudVM;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface ICloudAgentService extends ISecurityControllerService, IRPCHandler{
	public Map<String, CloudTenant> getTenants();
	public Map<String, CloudUser> getUsers();
	public Map<String, CloudSubnet> getSubnets();
	public Map<String, CloudVM> getVMs();
	String findTenantMacByIP(String tenantId, String ip);
	String findTenantIPByMac(String tenantId, String mac);
	

	Map<String, CloudPort> getMacIPMapping();
	
	void ResolveTenantUserMapping();
	
	

	//vmid is the output para
	public int newVm(DeviceType type, StringBuffer vmid);
	public boolean powerOn(String vmid);
	public boolean powerOff(String vmid);
	public boolean powerReset(String vmid);
	public int getStatus(String vmid);
	
	//for test:
	public int delVm(String vmid);
	public int getDevStatus(String vmid, StringBuffer status);
	public boolean setDevStatus(String vmid, String status);
	public boolean getIp(String vmid, StringBuffer ip);
	public boolean newDev(DeviceType type);
	
}
