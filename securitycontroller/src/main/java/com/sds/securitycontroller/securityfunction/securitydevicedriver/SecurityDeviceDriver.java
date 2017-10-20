package com.sds.securitycontroller.securityfunction.securitydevicedriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.securityfunction.ErrorCode;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;

public abstract class SecurityDeviceDriver implements ISecurityDeviceDriver {
	protected static Logger log = LoggerFactory.getLogger(SecurityDeviceDriver.class);
	protected ArrayList<SecurityDevice> devList = null;
	protected HashSet<String> funcTypeSet = null;
	protected String version = null;
	protected HashSet<String> supportedRestVersion = null;
	protected HashMap<SecurityDeviceType, HashSet<String>> supportedDeviceType = null;
	
	public SecurityDeviceDriver()
	{
		this.devList = new ArrayList<SecurityDevice>();
		this.funcTypeSet = new HashSet<String>();
		this.supportedDeviceType = new HashMap<SecurityDeviceType, HashSet<String>>();
	}

	@Override
	public ErrorCode probe(SecurityDevice dev) throws Exception {
		// TODO Auto-generated method stub
		ErrorCode ret = ErrorCode.DEVICE_NOT_SUPPORTED_BY_DRIVER;
		
		Iterator<SecurityDeviceType> keyIt = this.supportedDeviceType.keySet().iterator();
		while(keyIt.hasNext())
		{
			SecurityDeviceType type = keyIt.next();
			
			if(type != dev.type)
			{
				continue;
			}
			
			HashSet<String> versions = this.supportedDeviceType.get(type);
			if(versions != null && versions.size() > 0)
			{
				if(versions.contains(dev.rest_version))
				{
					log.debug("Device probe SUCCESS: " + dev.toString() + "----" + this.getClass().toString());
					if(dev.driver == null)
					{
						dev.driver = this;
						this.devList.add(dev);
					}
					ret = ErrorCode.SUCCESS;
				}
			}
		}
		
		return ret;
	}

	@Override
	public ErrorCode getSecurityDevices(ArrayList<SecurityDevice> _devList)
			throws Exception {
		// TODO Auto-generated method stub
		_devList.clear();
		
		if(this.devList.size() == 0)
		{
			return ErrorCode.SUCCESS;
		}
		
		Iterator<SecurityDevice> it = this.devList.iterator();
		
		while(it.hasNext())
		{
			_devList.add(it.next());
		}
		
		return ErrorCode.SUCCESS;
	}

	@Override
	public ErrorCode getSupportedSecurityFunctionType(
			Set<String> _funcTypeList) throws Exception {
		// TODO Auto-generated method stub
		Iterator<String> it = this.funcTypeSet.iterator();
		
		while(it.hasNext())
		{
			_funcTypeList.add(it.next());
		}
		
		return ErrorCode.SUCCESS;
	}

	@Override
	public boolean isSecurityFunctionTypeSupported(String funcType) {
		// TODO Auto-generated method stub
		return this.funcTypeSet.contains(funcType);
	}
	
	@Override
	public String getVersion()
	{
		return this.version;
	}
	
//	@Override
//	public SecurityDevice getSecurityDevice(String deviceID)
//	{
//		Iterator<SecurityDevice> it = this.devList.iterator();
//		
//		while(it.hasNext())
//		{
//			SecurityDevice dev = it.next();
//			if(dev.device_id.equalsIgnoreCase(deviceID))
//			{
//				return dev;
//			}
//		}
//		
//		return null;
//	}
}
