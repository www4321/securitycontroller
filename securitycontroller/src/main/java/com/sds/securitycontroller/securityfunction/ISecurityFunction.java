package com.sds.securitycontroller.securityfunction;

import java.util.ArrayList;
import java.util.Set;

import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionRequestAndResponse;
import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionManager.SecurityFunctionInitializeContext;
import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionRequestAndResponse.IRequestParser;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;
import com.sds.securitycontroller.securityfunction.securitydevicedriver.ISecurityDeviceDriver;

public interface ISecurityFunction extends IRequestParser {
	
	ErrorCode initialize(SecurityFunctionInitializeContext secFuncInitCtx) throws Exception;
	
	ErrorCode probe(SecurityDevice dev) throws Exception;
	
	ErrorCode probe(ISecurityDeviceDriver drv) throws Exception;
	
	ErrorCode callSecurityFunction(SecurityFunctionRequestAndResponse reqAndRes) throws Exception;
	
	ErrorCode getSupportedSecurityDeviceType(ArrayList<SecurityDeviceType> secDevTypeList) throws Exception;
	
	ErrorCode getSupportedSecurityFunctionType(Set<String> funcTypeList) throws Exception;
	
//	ErrorCode getSecurityDeviceList(ArrayList<SecurityDevice> secDevList) throws Exception;
//	
//	SecurityDevice getSecurityDevice(SecurityDeviceType devType) throws Exception;
//	
//	SecurityDevice getSecurityDevice(String deviceID) throws Exception;
//	
//	SecurityDevice getSecurityDeviceFromDeviceManagerByDeviceID(String deviceID) throws Exception;
	
	String getSecurityFunctionName();
	
	ErrorCode getInstalledSecurityDeviceDriverList(ArrayList<ISecurityDeviceDriver> secDevDrvList) throws Exception;
	
	@Override
	ErrorCode parseRequest(SecurityFunctionRequestAndResponse reqAndRes) throws Exception;
}
