package com.sds.securitycontroller.securityfunction.manager;

import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;

public interface ISecurityFunctionManagerService extends
		ISecurityControllerService {
	
	public SecurityFunctionRequestAndResponse processRequest(String httpMethod, String securityFunction, String securityFunctionType, Object request) throws Exception;
	public void freeSecurityDevice(SecurityDevice dev);
	public SecurityDevice allocateSecurityDevice(SecurityDeviceType type);
}
