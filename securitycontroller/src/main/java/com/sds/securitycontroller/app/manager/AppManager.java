/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.app.AppFactory;
import com.sds.securitycontroller.app.AppRealtimeInfo;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.storage.RowOrdering;
import com.sds.securitycontroller.utils.realtime.IRealtimeBasicManagement;
import com.sds.securitycontroller.utils.realtime.RealtimeBasic;
import com.sds.securitycontroller.utils.realtime.RealtimeBasicManager;

public class AppManager implements IAppManagementService, ISecurityControllerModule {
	
	protected IRestApiService restApi; 
	protected IEventManagerService eventManager;
	protected IStorageSourceService storageSource;
    protected IRegistryManagementService serviceRegistry;
	protected IRealtimeBasicManagement realtimeManager;
	protected static Logger log = LoggerFactory.getLogger(AppManager.class);

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services = new ArrayList<Class<? extends ISecurityControllerService>>();
		services.add(IAppManagementService.class);
		return services;
	}
	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = 
				new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IAppManagementService.class, this);
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
		this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
		this.storageSource = context.getServiceImpl(IStorageSourceService.class, this);
        restApi = context.getServiceImpl(IRestApiService.class, this);
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
        this.storageSource = context.getServiceImpl(IStorageSourceService.class, this);
        this.realtimeManager=new RealtimeBasicManager(this.storageSource);
		log.info("BUPT security controller app manager initialized.");
	}
	@Override
	public void startUp(SecurityControllerModuleContext context) {
        // register REST interface
		AppManagerRoutable r = new AppManagerRoutable();
        restApi.addRestletRoutable(r);
        serviceRegistry.registerService(r.basePath(), this);
        storageSource.createTable(this.getTableName("app"), this.getTableColumns("app"));
        storageSource.setTablePrimaryKeyName(this.getTableName("app"), this.getTablePK("app"));
        
        storageSource.createTable(this.getTableName("realtimeInfo"), this.getTableColumns("realtimeInfo"));
        storageSource.setTablePrimaryKeyName(this.getTableName("realtimeInfo"), this.getTablePK("realtimeInfo"));
	  
		log.info("BUPT security controller app manager started.");
	}
	@Override
	public Map<String, App> getConnectedApps() {
		return null;
	}
	
	
	//------app related methods------
	@Override
	public App getApp(String id){
		App app = null;
		do{
			if(null == id || id.isEmpty()){
				break;
			}
			app = (App)this.storageSource.getEntity(this.getTableName("app"), id, App.class);	
			app = AppFactory.createApp(app);
		}while(false);
		
		return app;
	}
	
	@Override
	public synchronized boolean addApp(App app){		
		String id = this.applyAppId();
		if(null == id){
			log.error("apply app id failed:{} ", app);
			return false;
		}
		app.setId(id);
		
		int res = storageSource.insertEntity(this.getTableName("app"), app);
		if(res<=0){
		    log.error("Insert app to DB failed {} ", app);
			return false;
		} 
		
		if(!this.addAppRealtimeInfo(app.getId())){
			log.error("add app realtimeinfo failed:",app.getId());
		}
		
        Event event = new Event(EventType.APP_ADDED, app, this, null);
        this.eventManager.addEvent(event);
        return true;
	}
	
	@Override
	public boolean removeApp(App app) {
 		int res = storageSource.deleteEntity(this.getTableName("app"), app.getId());
 		if(res<0)
 			return false;
        
 		if(!removeAppRealtimeInfo(app.getId())){
 			log.error("remove app realtimeinfo failed:",app.getId());
 		}
 		
        Event event = new Event(EventType.APP_REMOVED, app, this, null);
        this.eventManager.addEvent(event);
        return true;
	}
	@Override
	public boolean updateApp(App app, JsonNode jn) throws IOException {		
		app.updateInfo(jn);				
		int ret = storageSource.updateOrInsertEntity(this.getTableName("app"), app);
		if(ret<=0) 
			return false;
		return true;
	}

	@Override
	public List<App> getAllApps() {
		return getAllApps(null, -1);
	}
    @Override
    public List<App> getAllApps(String from, int size) {
    	QueryClause qc = null;
    	List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
		//if 'from' not provided, we do it from table begining
		if(null != from){
			items.add(new QueryClauseItem("id",from,QueryClauseItem.OpType.GT));
			qc = new QueryClause(items, this.getTableName("app"), null, new RowOrdering("id"));
		}else{
			qc = new QueryClause(this.getTableName("app"));
		}	
     
    	@SuppressWarnings("unchecked")
		List<App> result = (List<App>)storageSource.executeQuery(qc, App.class);
    	if(size<0)
    		return result;
    	List<App> allApps = new ArrayList<App>();
    	int count=size;
    	for (App object : result) {
    		allApps.add(object);
    		count--;
    		if(0 == count){break;}
    	}
		return allApps;
	}
    /*
     * notice: this method is synchronized 
     */
 
	private String applyAppId() {
		int MAX=50;//how many times to try
		Random r=new Random();
		long nid=System.currentTimeMillis();		
		String id=Long.toString(nid);
		int count=0;
		
		while(count<MAX && null != this.getApp(id)){
			nid+=r.nextInt(200);
			id=Long.toString(nid);
			count++;
		}
		if(MAX == count){
			return null;
		}
		return id;
	}
	@Override
	public String appRegistered(App app) {
		List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
		items.add(new QueryClauseItem("name",app.getName(),QueryClauseItem.OpType.EQ));
		items.add(new QueryClauseItem("type",app.getType(),QueryClauseItem.OpType.EQ));
		items.add(new QueryClauseItem("version",app.getVersion(),QueryClauseItem.OpType.EQ));
		items.add(new QueryClauseItem("hash",app.getHash(),QueryClauseItem.OpType.EQ));
		items.add(new QueryClauseItem("host",app.getHost(),QueryClauseItem.OpType.EQ));
		items.add(new QueryClauseItem("port",app.getPort(),QueryClauseItem.OpType.EQ));
		items.add(new QueryClauseItem("root_url",app.getRoot_url(),QueryClauseItem.OpType.EQ));
		
		QueryClause qc = new QueryClause(items, this.getTableName("app"), null, null);
		List<? extends IDBObject> result =storageSource.executeQuery(qc, App.class);
		if(result.size() > 0){
			return ((App)result.get(0)).getId();
		}
	
		return null;
	}
	
	
	//-------realtime information------
	
	/*
	 * this method must be synchronized 
	 */
	@Override
	public synchronized boolean  addAppRealtimeInfo(String appId) {
		return this.realtimeManager.addRealtimeBasic(appId, "APP");
	}

	@Override
	public boolean updateAppRealtimeInfo(String appId, JsonNode jn) throws IOException {
		RealtimeBasic rb=new RealtimeBasic();
			int tcpu=jn.path("cpu").asInt();
			int tmu=jn.path("memory_used").asInt();
			int tmt=jn.path("memory_total").asInt();
			int tdu=jn.path("disk_used").asInt();
			int tdt=jn.path("disk_total").asInt();
			int tst=jn.path("start_time").asInt();
			
			if(tcpu<0 || tcpu>100 || tmu<0||tmt<tmu||tdu<0||tdt<tdu||tst<0){
				throw new IOException("value range error");
			}
			
			rb.setCpu(tcpu);
			rb.setMemory_used(tmu);
			rb.setMemory_total(tmt);
			rb.setDisk_used(tdu);
			rb.setDisk_total(tdt);
			rb.setState(jn.path("state").asText());
			rb.setStart_time(tst);
		return this.realtimeManager.updateRealtimeBasic(appId, "APP", rb);		 
	}
	@Override
	public boolean removeAppRealtimeInfo(String appId) {
		return this.realtimeManager.removeRealtimeBasic(appId, "APP");
	}
	@Override
	public AppRealtimeInfo getAppRealtimeInfo(String appId) {
		RealtimeBasic rb=this.realtimeManager.getRealtimeBasic(appId, "APP");
		return new AppRealtimeInfo(rb);
	}
	
	@Override
	public List<AppRealtimeInfo> getAllAppRealtimeInfo() {
	 
		
		List<RealtimeBasic> result =  this.realtimeManager.getAllRealtimeBasic("APP");
    	List<AppRealtimeInfo> allInfos = new ArrayList<AppRealtimeInfo>();       
    	for (RealtimeBasic object : result) {
    		allInfos.add(new AppRealtimeInfo(object));
    	}
    	return allInfos;
	} 
 

	//----------snapshot method-------------------
	@Override
	public String getSnapshotdir(){
		return "/opt/sds/snapshot/";
	}
	
	
	
	
	//----------package method-------------------
	@Override
	public String getPackagedir(){
		return "/opt/sds/package/";
	}
	
	@Override
	public void fillUpdateInfo(App app){
		
		@SuppressWarnings("unused")
		boolean bflag=false;
		do{
			if(null == app){
				return;
			}
			String pkgVer=getPackageVersion(app.getGuid());
			if(null == pkgVer){
				break;
			}	
			int cmp=app.getVersion().compareToIgnoreCase(pkgVer);
			if(cmp>=0){
				break;
			}
			bflag=true;
		}while(false);		
		//app.setUpdatable(bflag);		
	}
	@Override
	public String getPackageVersion(String guid){
		String dpath=this.getPackagedir() + guid;
    	File d = new File(dpath);
		if (!d.isDirectory()) {
			return null;
		}
		File[] files = d.listFiles();
		if (0 == files.length) {
			return null;
		}
		String pkgVer=files[0].getName().toLowerCase(); 			
		int k=pkgVer.lastIndexOf(".");
		if(k>0){
			pkgVer=pkgVer.substring(0, k);
		} 
		return pkgVer;
		
		
	}

	//----------DB related methods --------------

	public String getTableName(String type) {
	    String appTableName = "t_apps";	 
	    String realtimeInfoTableName = "t_realtime_basic"; 
	    
	    switch(type){
	    case "app":
	    	return appTableName;
	    case "realtimeInfo":
	    	return realtimeInfoTableName; 
		default:
	    	return "";
	    }
	}
	
	public String getTablePK(String type) {
		String appPK = "id"; 
		String realtimeInfoPK = "obj_id";
 
	    switch(type){
	    case "app":
	    	return appPK;
	    case "realtimeInfo":
	    	return realtimeInfoPK; 
	    default:
	    	return "";
	    }		 
	}

	public Map<String, String> getTableColumns(String type) {
	    Map<String, String> appTableColumns = new HashMap<String, String>(){
			private static final long serialVersionUID = 343251L;

		{
	    	put("id", 				"VARCHAR(32)");
	    	put("guid",		 		"VARCHAR(32)");
	    	put("name", 			"VARCHAR(32)");
	    	put("alias", 			"VARCHAR(32)");
	    	put("hash", 			"VARCHAR(64)");
	    	put("version", 			"VARCHAR(16)");	    	
	    	put("enable", 			"BOOLEAN");
	    	put("protocol", 		"VARCHAR(8)");
	    	put("host", 			"VARCHAR(64)");
	    	put("port", 			"INTEGER(4)");
	    	put("root_url", 		"VARCHAR(64)");
	    	put("manage_url", 		"VARCHAR(128)");
	    	put("reg_time", 		"INTEGER");
	    	put("type", 			"VARCHAR(16)");	 
	    	put("category", 		"VARCHAR(16)");
	    }};
	    
	    Map<String, String> rtTableColumns = new HashMap<String, String>(){
			private static final long serialVersionUID = 343251L;

		{ 
	    	put("obj_id", 		"VARCHAR(45)");
	    	put("type", 		"VARCHAR(10)");
	    	put("state", 		"VARCHAR(10)");
	    	put("update_time", 	"INTEGER");
	    	put("start_time",	"INTEGER");
	    	put("cpu", 			"INTEGER");	    	
	    	put("memory_used", 	"INTEGER");
	    	put("memory_total", "INTEGER");
	    	put("disk_used", 	"INTEGER");
	    	put("disk_total", 	"INTEGER");
	    }};
	   
	    switch(type){
	    case "app":
	    	return appTableColumns;
	    case "realtimeInfo":
	    	return rtTableColumns;
 
	    default:
	    	return null;
	    } 	    
	    
	}
}
