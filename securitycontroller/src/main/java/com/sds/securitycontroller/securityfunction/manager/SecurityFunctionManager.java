package com.sds.securitycontroller.securityfunction.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sds.securitycontroller.device.Device;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.securityfunction.ErrorCode;
import com.sds.securitycontroller.securityfunction.ISecurityFunction;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;
import com.sds.securitycontroller.securityfunction.securitydeviceallocator.ISecurityDeviceAllocator;
import com.sds.securitycontroller.securityfunction.securitydevicedriver.ISecurityDeviceDriver;
import com.sds.securitycontroller.storage.IStorageSourceService;

public class SecurityFunctionManager implements ISecurityFunctionManagerService, ISecurityControllerModule {
	private HashMap<String, ISecurityFunction> securityFunctionMap = null;
	
	private static Logger log = LoggerFactory.getLogger(SecurityFunctionManager.class);
	private static SecurityFunctionManager securityFunctionManager = null;
	private IRestApiService restApi = null;
	private IStorageSourceService storageSource = null;
	protected IEventManagerService eventManager;
	
	public SecurityFunctionManager()
	{
		this.securityFunctionMap = new HashMap<String, ISecurityFunction>();
		SecurityFunctionManager.securityFunctionManager = this;
	}
	
	public static SecurityFunctionManager getInstance()
	{
		if(SecurityFunctionManager.securityFunctionManager == null)
		{
			SecurityFunctionManager.securityFunctionManager = new SecurityFunctionManager();
		}
		return SecurityFunctionManager.securityFunctionManager;
	}
	
	/**
	 * 
	 * @param securityFunction
	 * @param request
	 * 			request should be a json-string like this:
	 * 				{
	 * 					"head" : {
	 * 						"tenantID" : "xxxx", 
	 * 						"token" : "xxxx", 
	 * 						"signature" : "xxxx", 
	 * 						...
	 * 					}, 
	 * 					"data" : {
	 * 						"method" : "xxxx", 
	 * 						"opType" : "xxxx", 
	 * 						"arg1" : "xxxx", 
	 * 						"arg2" : xxxx", 
	 * 						...
	 * 					}
	 * 				}
	 * 
	 * @return
	 * @throws Exception
	 */
	@Override
	public SecurityFunctionRequestAndResponse processRequest(String httpMethod, String securityFunction, String securityFunctionType, Object request) throws Exception
	{
		SecurityFunctionRequestAndResponse reqAndRes = new SecurityFunctionRequestAndResponse(securityFunction, securityFunctionType);
		
		do
		{
			if(!this.securityFunctionMap.containsKey(securityFunction))
			{
				reqAndRes.response.errorCode =  ErrorCode.SECURITY_FUNCTION_NOT_SUPPORTED;
				log.error("[processRequest] security function '{}' is not supported", securityFunction);
				break;
			}
			
			ISecurityFunction secFunc = this.securityFunctionMap.get(securityFunction);
			
			if((reqAndRes.response.errorCode = reqAndRes.parseRequest(httpMethod, request, secFunc)) != ErrorCode.SUCCESS)
			{
				log.error("[processRequest] parseRequest failed, errorCode: {}, errorString: {}", 
						reqAndRes.response.errorCode.toString(), reqAndRes.response.errorString);
				break;
			}
			
			log.debug("[processRequest] START PROCESSING {}", reqAndRes.request.toString());
			reqAndRes.response.errorCode = secFunc.callSecurityFunction(reqAndRes);
		}while(false);
		
		return reqAndRes;
	}
	
	@SuppressWarnings("unused")
	private boolean checkSignature(String request)
	{
		return true;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services = new ArrayList<Class<? extends ISecurityControllerService>>();
		services.add(ISecurityFunctionManagerService.class);
		return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = 
				new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(ISecurityFunctionManagerService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		return null;
	}
	
	public class SecurityFunctionInitializeContext
	{
		public SecurityControllerModuleContext securityControllerModuleContext = null;
		public IStorageSourceService stroageSource = null;
		public SecurityFunctionManager manager = null;
		public ISecurityDeviceAllocator allocator = null;
	}
	
	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		this.storageSource = context.getServiceImpl(IStorageSourceService.class, this);
		this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
		SecurityFunctionInitializeContext secFuncInitCtx = new SecurityFunctionInitializeContext();
		secFuncInitCtx.securityControllerModuleContext = context;
		secFuncInitCtx.stroageSource = this.storageSource;
		secFuncInitCtx.manager = this;
		
//		Object tmp = null;
//		
//		this.storageSource.setTablePrimaryKeyName("secfunc_device", "device_id");
//		
//		tmp = this.storageSource.executeSql("CREATE TABLE IF NOT EXISTS `secfunc_device` ("
//				+ "`device_id` varchar(32) NOT NULL, "
//				+ "`device_type` varchar(32) NOT NULL, "
//				+ "`protocol` varchar(8) NOT NULL, "
//				+ "`ip` varchar(16) NOT NULL, "
//				+ "`port` int(11) NOT NULL, "
//				+ "`base_url` varchar(64) NOT NULL, "
//				+ "`rest_version` varchar(32) NOT NULL "
//				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8", null);
//		if((int)tmp == 1)
//		{
//			log.debug("[init] table secfunc_device created");
//		}
		
//		List<SecurityDevice> devs = this.getAllSecurityDevices();
		
		restApi = context.getServiceImpl(IRestApiService.class, this);
		SecurityFunctionConfig config = null;
		ErrorCode errorCode;
		
		try
		{
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
			config = new SecurityFunctionConfig();
			saxParser.parse(new File("SecurityFunction.xml"), config);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		Iterator<SecurityFunctionItem> secFuncIt = config.SecurityFunctions.iterator();
		while(secFuncIt.hasNext())
		{
			SecurityFunctionItem item = secFuncIt.next();
			try
			{
				ISecurityFunction secFunc = (ISecurityFunction)Class.forName(item._class).newInstance();
				
				secFuncInitCtx.allocator = (ISecurityDeviceAllocator)Class.forName(item.allocator).newInstance();
				
				if((errorCode = secFuncInitCtx.allocator.initialize(this, secFunc, this.storageSource)) != ErrorCode.SUCCESS)
				{
					log.error("SecurityDeviceAllocator " + secFuncInitCtx.allocator.getClass().toString() + " initialize failed for " + item._class);
					continue;
				}
				else
				{
					log.debug("SecurityDeviceAllocator " + secFuncInitCtx.allocator.getClass().toString() + " initialzie success for " + item._class);
				}
				
				if((errorCode = secFunc.initialize(secFuncInitCtx)) == ErrorCode.SUCCESS)
				{
					this.securityFunctionMap.put(item.name, secFunc);
					log.debug("SecurityFunction " + item.name + " : " + item._class + " installed");
				}
				else
				{
					log.error("SecurityFunction " + item.name + " : " + item._class + " initialized failed, " + errorCode.toString());
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		Iterator<SecurityDeviceDriverItem> secDevDrvIt = config.SecurityDeviceDirvers.iterator();
		while(secDevDrvIt.hasNext())
		{
			SecurityDeviceDriverItem item = secDevDrvIt.next();
			ISecurityDeviceDriver secDevDrv = null;
			
			try
			{
				secDevDrv = (ISecurityDeviceDriver)Class.forName(item._class).newInstance();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				continue;
			}
			
			boolean bInstalled = false;
			Iterator<String> secFuncKeyIt = this.securityFunctionMap.keySet().iterator();
			while(secFuncKeyIt.hasNext())
			{
				ISecurityFunction secFunc = this.securityFunctionMap.get(secFuncKeyIt.next());
				try
				{
					if(secFunc.probe(secDevDrv) == ErrorCode.SUCCESS)
					{
						bInstalled = true;
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			if(bInstalled)
			{
				
//				Iterator<SecurityDevice> it = devs.iterator();
//				while(it.hasNext())
//				{
//					SecurityDevice dev = it.next();
//					log.debug("[initialize] probing device " + dev.toString());
//					try 
//					{
//						secDevDrv.probe(dev);
//					} 
//					catch (Exception e)
//					{}
//				}
				
				log.debug("Driver " + item._class + " installed");
			}
			else
			{
				log.debug("Driver " + item._class + " not installed");
			}
		}
		
		log.info("SecurityFunctionManager Initialized~~~~~~~~~~~~~~~~~");
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		restApi.addRestletRoutable(new SecurityFunctionRoutable());
		log.info("SecurityFunctionManager Started UP!!!!!!!!!!!!!");
	}
	
//	protected List<SecurityDevice> getAllSecurityDevices()
//	{
//		ArrayList<SecurityDevice> list = new ArrayList<SecurityDevice>();
//		
//		QueryClause qc = new QueryClause(new ArrayList<QueryClauseItem>(), "secfunc_device", null, null);
//		qc.setType(QueryClauseType.EMPTY);
//		List<Object> dbResult = this.storageSource.executeQuery(qc, new SecurityDevice());
//		
//		if(dbResult != null && dbResult.size() > 0)
//		{
//			Iterator<Object> it = dbResult.iterator();
//			while(it.hasNext())
//			{
//				list.add((SecurityDevice)it.next());
//			}
//		}
//		
//		return list;
//	}
	
	@Override
	public void freeSecurityDevice(SecurityDevice dev)
	{
		eventManager.makeRPCCall(com.sds.securitycontroller.device.manager.IDeviceManagementService.class, "devfree", new Object[] { dev.device_id });
	}
	
	@Override
	public SecurityDevice allocateSecurityDevice(SecurityDeviceType type)
	{
		SecurityDevice secDev = null;
		String devID = null;
		Device dev = null;
		DeviceType deviceType = null;
		
		log.debug("[allocateSecurityDevice] trying to allocate a {} device from DeviceManager...", type.toString());
		
		switch(type)
		{
		case SD_WAF:
			deviceType = DeviceType.WAF;
			break;
		case SD_WVSS:
			deviceType = DeviceType.WVSS;
			break;
		default:
			log.error("[allocateSecurityDevice] un-supported device type {}", type.toString());
			return null;
		}
		
		do
		{
			devID = (String)eventManager.makeRPCCall(com.sds.securitycontroller.device.manager.IDeviceManagementService.class, "devalloc", new Object[] { deviceType, "TEMP" });
			if(devID == null)
			{
				log.error("[allocateSecurityDevice] devalloc failed");
				break;
			}
			
			dev = (Device)eventManager.makeRPCCall(com.sds.securitycontroller.device.manager.IDeviceManagementService.class, "getdev", new Object[] { devID });
			if(dev == null)
			{
				log.error("[allocateSecurityDevice] getdev failed");
				break;
			}
			
			secDev = new SecurityDevice();
			secDev.base_url = dev.getRoot_url();
			secDev.device_id = dev.getId();
			secDev.ip = dev.getIp();
			secDev.port = dev.getPort();
			secDev.protocol = dev.getProtocol();
			secDev.rest_version = dev.getApi_ver();
			secDev.type = type;
			
			log.debug("[allocateSecurityDevice] allocate success");
		}while(false);
		
		return secDev;
	}

	class SecurityFunctionItem
	{
		public String name = null;
		public String _class = null;
		public String allocator = null;
	}
	
	class SecurityDeviceDriverItem
	{
		public String _class = null;
	}
	
	class SecurityFunctionConfig extends DefaultHandler
	{
		private ArrayList<SecurityFunctionItem> SecurityFunctions = null;
		private ArrayList<SecurityDeviceDriverItem> SecurityDeviceDirvers = null;
		
		@Override
		public void startDocument() throws SAXException
		{
			this.SecurityFunctions = new ArrayList<SecurityFunctionItem>();
			this.SecurityDeviceDirvers = new ArrayList<SecurityDeviceDriverItem>();
		}
		
		@Override
		public void endDocument() throws SAXException
		{
			//PolicyFactory.tmpPolicyFactories = this.policyFactories;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException
		{
			if(qName.equalsIgnoreCase("SecurityFunction"))
			{
				SecurityFunctionItem item = new SecurityFunctionItem();
				item.name = attributes.getValue("name");
				item._class = attributes.getValue("class");
				item.allocator = attributes.getValue("allocator");
				if(item.allocator == null)
				{
					item.allocator = "com.sds.securitycontroller.securityfunction.securitydeviceallocator.DefaultSecurityDeviceAllocator";
				}
				this.SecurityFunctions.add(item);
			}
			else if(qName.equalsIgnoreCase("SecurityDeviceDriver"))
			{
				SecurityDeviceDriverItem item = new SecurityDeviceDriverItem();
				item._class = attributes.getValue("class");
				this.SecurityDeviceDirvers.add(item);
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
		}
	}
}
