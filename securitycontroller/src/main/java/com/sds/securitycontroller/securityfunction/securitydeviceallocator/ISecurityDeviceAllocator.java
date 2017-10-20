package com.sds.securitycontroller.securityfunction.securitydeviceallocator;

import java.util.List;

import com.sds.securitycontroller.securityfunction.ErrorCode;
import com.sds.securitycontroller.securityfunction.ISecurityFunction;
import com.sds.securitycontroller.securityfunction.manager.ISecurityFunctionManagerService;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;
import com.sds.securitycontroller.storage.IStorageSourceService;

public interface ISecurityDeviceAllocator
{
	public ErrorCode initialize(ISecurityFunctionManagerService manager, ISecurityFunction securityFunction, IStorageSourceService storageSource);
	
	public SecurityDevice allocate(SecurityDeviceType deviceType, int flag, String appID, String tenantID, Object reserved);
	
	public void free(SecurityDevice device);
	
	public SecurityDevice findSecurityDevice(String deviceID);
	
	public List<SecurityDevice> getAllSecurityDevices();
}
