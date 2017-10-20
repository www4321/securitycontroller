package com.sds.securitycontroller.securityfunction.securitydevice;

import com.sds.securitycontroller.securityfunction.ErrorCode;
import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionRequestAndResponse;
import com.sds.securitycontroller.securityfunction.securitydevicedriver.ISecurityDeviceDriver;

public class SecurityDevice
{
	public SecurityDeviceType type;
	
	//<protocol>://<ip>:<port>/<baseURL>
	public String protocol = null;
	public String ip = null;
	public int port = 0;
	public String base_url = null;
	
	public String rest_version = null;
	
	public String device_id = null;
	
	public String auth_type = null;
	
	public ISecurityDeviceDriver driver = null;
	
	public SecurityDevice()
	{
	}
	
	public SecurityDevice(String protocol, String ip, int port, String base_url, String rest_version, String device_id, SecurityDeviceType type)
	{
		this.protocol = protocol;
		this.ip = ip;
		this.port = port;
		this.base_url = base_url;
		this.rest_version = rest_version;
		this.device_id = device_id;
		this.type = type;
	}
	
	@Override
	public String toString()
	{
		return "Device<" + this.protocol + "://" + this.ip + ":" + this.port + this.base_url + ">: RestVersion(" + this.rest_version + "), DeviceID(" + this.device_id + ")";
	}
	
	public String getBaseURL()
	{
		return this.protocol + "://" + this.ip + ":" + this.port + this.base_url;
	}
	
	public ErrorCode callSecurityFunction(SecurityFunctionRequestAndResponse reqAndRes) throws Exception
	{
		if(this.driver == null)
		{
			return ErrorCode.DEVICE_NOT_SUPPORTED_BY_DRIVER;
		}
		
		return this.driver.callSecurityFunction(this, reqAndRes);
	}
}
