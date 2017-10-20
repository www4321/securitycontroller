package com.sds.securitycontroller.securityfunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionManager;
import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionRequestAndResponse;
import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionManager.SecurityFunctionInitializeContext;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydeviceallocator.ISecurityDeviceAllocator;
import com.sds.securitycontroller.securityfunction.securitydevicedriver.ISecurityDeviceDriver;
import com.sds.securitycontroller.storage.IStorageSourceService;

public abstract class SecurityFunction implements ISecurityFunction {
	private static Logger log = LoggerFactory.getLogger(SecurityFunction.class);
	protected ArrayList<ISecurityDeviceDriver> driverList = null;
	protected HashSet<String> essentialSecFuncList = null;
	protected HashSet<String> optionalSecFuncList = null;
	protected HashSet<String> essentialParamSet = null;
	protected String securityFunctionName = null;
	protected IStorageSourceService storageSource = null;
	protected SecurityFunctionManager manager = null;
	protected ISecurityDeviceAllocator devAllocator = null;
	
	public SecurityFunction()
	{
		this.driverList = new ArrayList<ISecurityDeviceDriver>();
		this.essentialSecFuncList = new HashSet<String>();
		this.optionalSecFuncList = new HashSet<String>();
		this.essentialParamSet = new HashSet<String>();
	}
	
	@Override
	public ErrorCode initialize(SecurityFunctionInitializeContext secFuncInitCtx) throws Exception
	{
		this.storageSource = secFuncInitCtx.stroageSource;
		this.manager = secFuncInitCtx.manager;
		this.devAllocator = secFuncInitCtx.allocator;
		
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode probe(SecurityDevice dev) throws Exception {
		// TODO Auto-generated method stub
		ErrorCode ret = ErrorCode.DEVICE_NOT_SUPPORTED_BY_DRIVER;
		
		do
		{
			Iterator<ISecurityDeviceDriver> drvIt = this.driverList.iterator();
			while(drvIt.hasNext())
			{
				ret = drvIt.next().probe(dev);
				if(ret == ErrorCode.SUCCESS)
				{
					break;
				}
			}
		}while(false);
		
		return ret;
	}

	@Override
	public ErrorCode probe(ISecurityDeviceDriver drv) throws Exception {
		// TODO Auto-generated method stub
		ErrorCode ret = ErrorCode.SUCCESS;
		String drvName = drv.getClass().toString();
		String secFuncName = this.getClass().toString();
		log.debug("[probe:drv] SecurityFunction: " + secFuncName + ", Driver: " + drvName);
		
		do
		{
			HashSet<String> drvSecFuncSet = new HashSet<String>();
			
			if((ret = drv.getSupportedSecurityFunctionType(drvSecFuncSet)) != ErrorCode.SUCCESS)
			{
				log.debug("[probe:drv] get supported security function types from driver failed, " + ret.toString());
				break;
			}
			
			Iterator<String> it = this.essentialSecFuncList.iterator();
			while(it.hasNext())
			{
				String tmp = it.next();
				if(!drvSecFuncSet.contains(tmp))
				{
					log.debug("[probe:drv] does contain " + tmp);
					ret = ErrorCode.SECURITY_FUNCTION_TYPE_NOT_SUPPORTED;
					break;
				}
			}
		}while(false);
		
		if(ret == ErrorCode.SUCCESS)
		{
			log.debug("[probe:drv] success, add " + drvName + " to " + secFuncName);
			this.driverList.add(drv);
		}
		
		return ret;
	}
	
	@Override
	public ErrorCode getSupportedSecurityFunctionType(Set<String> funcTypeList)
			throws Exception {
		// TODO Auto-generated method stub
		ErrorCode ret = ErrorCode.SUCCESS;
		
		Iterator<ISecurityDeviceDriver> drvIt = this.driverList.iterator();
		while(drvIt.hasNext())
		{
			ISecurityDeviceDriver drv = drvIt.next();
			
			HashSet<String> secFuncTypes = new HashSet<String>();
			if((ret = drv.getSupportedSecurityFunctionType(secFuncTypes)) != ErrorCode.SUCCESS)
			{
				break;
			}
			
			Iterator<String> secFuncTypeIt = secFuncTypes.iterator();
			while(secFuncTypeIt.hasNext())
			{
				funcTypeList.add(secFuncTypeIt.next());
			}
		}
		
		return ret;
	}

	@Override
	public String getSecurityFunctionName() {
		// TODO Auto-generated method stub
		return this.securityFunctionName;
	}
	
	@Override
	public ErrorCode getInstalledSecurityDeviceDriverList(ArrayList<ISecurityDeviceDriver> secDevDrvList) throws Exception
	{
		ErrorCode ret = ErrorCode.SUCCESS;
		
		Iterator<ISecurityDeviceDriver> it = this.driverList.iterator();
		while(it.hasNext())
		{
			secDevDrvList.add(it.next());
		}
		
		return ret;
	}
	
//	@Override
//	public ErrorCode getSecurityDeviceList(ArrayList<SecurityDevice> secDevList) throws Exception
//	{
//		Iterator<ISecurityDeviceDriver> drvIt = this.driverList.iterator();
//		while(drvIt.hasNext())
//		{
//			ISecurityDeviceDriver drv = drvIt.next();
//			ArrayList<SecurityDevice> devList = new ArrayList<SecurityDevice>();
//			
//			if(drv.getSecurityDevices(devList) != ErrorCode.SUCCESS)
//			{
//				continue;
//			}
//			
//			Iterator<SecurityDevice> devIt = devList.iterator();
//			while(devIt.hasNext())
//			{
//				secDevList.add(devIt.next());
//			}
//		}
//		
//		return ErrorCode.SUCCESS;
//	}
//	
//	@Override
//	public SecurityDevice getSecurityDevice(SecurityDeviceType devType) throws Exception
//	{
//		Iterator<ISecurityDeviceDriver> drvIt = this.driverList.iterator();
//		
//		while(drvIt.hasNext())
//		{
//			ISecurityDeviceDriver drv = drvIt.next();
//			
//			if(!drv.bSupportSecurityDeviceType(devType))
//			{
//				continue;
//			}
//			
//			ArrayList<SecurityDevice> devList = new ArrayList<SecurityDevice>();
//			if(drv.getSecurityDevices(devList) != ErrorCode.SUCCESS)
//			{
//				continue;
//			}
//			
//			if(devList.size() == 0)
//			{
//				continue;
//			}
//			
//			return devList.get(0);
//		}
//		
//		return null;
//	}
//	
//	@Override
//	public SecurityDevice getSecurityDevice(String deviceID) throws Exception
//	{
//		Iterator<ISecurityDeviceDriver> drvIt = this.driverList.iterator();
//		
//		while(drvIt.hasNext())
//		{
//			SecurityDevice dev = drvIt.next().getSecurityDevice(deviceID);
//			if(dev != null)
//			{
//				return dev;
//			}
//		}
//		
//		return null;
//	}
//	
//	@Override
//	public abstract SecurityDevice getSecurityDeviceFromDeviceManagerByDeviceID(String deviceID) throws Exception;
	
	@Override
	public ErrorCode parseRequest(SecurityFunctionRequestAndResponse reqAndRes) throws Exception
	{
		
		Iterator<String> it = this.essentialParamSet.iterator();
		while(it.hasNext())
		{
			String param = it.next();
			if(!reqAndRes.request.args.containsKey(param))
			{
				reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
				reqAndRes.response.errorString = "missing argument " + param;
				break;
			}
		}
		
		return reqAndRes.response.errorCode;
	}
}
