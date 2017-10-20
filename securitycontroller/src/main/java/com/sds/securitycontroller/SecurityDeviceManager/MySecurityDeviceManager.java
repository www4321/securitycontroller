package com.sds.securitycontroller.SecurityDeviceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.utils.HTTPUtils;



public class MySecurityDeviceManager implements ISecurityControllerModule,IMySecurityDeviceManagerService{
	protected IRestApiService restApi; 
	protected IEventManagerService eventManager;
	protected IStorageSourceService storageSource;
    protected IRegistryManagementService serviceRegistry;
	protected static Logger log = LoggerFactory.getLogger(MySecurityDeviceManager.class);
	private List<MySecurityDevice> scanDeviceList=new ArrayList<MySecurityDevice>();

	@Override
	public boolean addSecurityDevice(MySecurityDevice device) {
		// TODO Auto-generated method stub
//		int res = this.storageSource.insertEntity("scanDevice", device);
//		if(res<=0){
//		    log.error("Insert order to DB failed {} ", device);
//			return false;
//		} 
		this.scanDeviceList.add(device);
		return true;
	}
	@Override
	public void addList(MySecurityDevice scanDevice){
		this.scanDeviceList.add(scanDevice);
	}

	@Override
	public boolean removeSecurityDevice(MySecurityDevice device) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MySecurityDevice getSecurityDevice(String url) {
		// TODO Auto-generated method stub
	
		Map<String, String> headers=new HashMap<String, String>();
		headers.put("Content-Type","application/json");
		String jasonResp=HTTPUtils.httpGet(url,headers);
		System.out.println(jasonResp);

		return null;
	}

	@Override
	public List<MySecurityDevice> getAllSecurityDevices(String from, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MySecurityDevice> getAllSecurityDevices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateSecurityDevice(String id, Map<String, Object> values) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services = new ArrayList<Class<? extends ISecurityControllerService>>();
		services.add(IMySecurityDeviceManagerService.class);
		return services;
	}
	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = 
				new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IMySecurityDeviceManagerService.class, this);
		return m;
	}
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {		
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IStorageSourceService.class);
        l.add(IRestApiService.class);
        l.add(IStorageSourceService.class);
		return l;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		// TODO Auto-generated method stub
		 this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
		 this.restApi = context.getServiceImpl(IRestApiService.class);
		 this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);
		 this.storageSource = context.getServiceImpl(IStorageSourceService.class, this);
		 log.info("BUPT security controller scanDevicemanager initialized...");
//		 this.scanDeviceList.clear();
		
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		// TODO Auto-generated method stub
		MySecurityDeviceRoutable ohr=new MySecurityDeviceRoutable();
		restApi.addRestletRoutable(ohr);
		serviceRegistry.registerService(ohr.basePath(), this);
		storageSource.createTable("scanDevice", this.getTableColumns());
        storageSource.setTablePrimaryKeyName("scanDevice", "ip");
		log.info("BUPT security controller scanDevicemanager started...");
		
	}
	public Map<String, String> getTableColumns() {
	    Map<String, String> appTableColumns = new HashMap<String, String>(){
			private static final long serialVersionUID = 343251L;

		{
	    	put("ip", 				"VARCHAR(32)");
	    	put("userName", "VARCHAR(32)");
	    	put("passwd", "VARCHAR(32)");
	    	
	    	put("type", 			"VARCHAR(64)");
	    	put("factory", 			"VARCHAR(64)");
	    	put("engineLocation",		"VARCHAR(32)");
	    	put("get_speedmax", 			"DOUBLE");
	    	put("send_speedmax", 		"DOUBLE");
	    	put("cpu_usagey", 		"INT");
	    	put("memory_usage", 	"INT");
	    	
	    	put("disk_usage", 		"INT");	    	
	    	put("get_speed", 	"VARCHAR(32)");
	    	put("send_speed", 		    "VARCHAR(64)");
	    	put("name", 		    "VARCHAR(64)");
	    	put("memory", 		    "VARCHAR(64)");
	    	put("disk", 		    "VARCHAR(64)");
	    }};
	    //System.out.println("生成scandevice表头"); 
	    	return appTableColumns;
	}
	@Override
	public List<MySecurityDevice> getList() {
		// TODO Auto-generated method stub
		return this.scanDeviceList;
	}
	@Override
	public void remove(String ip) {
		// TODO Auto-generated method stub
		
	}

}

