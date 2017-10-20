package com.sds.securitycontroller.securityfunction.securitydevicedriver;

import java.util.ArrayList;
import java.util.Set;

import com.sds.securitycontroller.securityfunction.ErrorCode;
import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionRequestAndResponse;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;

public interface ISecurityDeviceDriver {
	
	ErrorCode probe(SecurityDevice dev) throws Exception;
	
	ErrorCode getSecurityDevices(ArrayList<SecurityDevice> devList) throws Exception;
	
	ErrorCode callSecurityFunction(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes) throws Exception;
	
	ErrorCode getSupportedSecurityFunctionType(Set<String> funcTypeList) throws Exception;
	
	boolean isSecurityFunctionTypeSupported(String funcType);
	
	String getVersion();
	
	//SecurityDevice getSecurityDevice(String deviceID);
	
	boolean isSecurityDeviceTypeSupported(SecurityDeviceType devType);
}
