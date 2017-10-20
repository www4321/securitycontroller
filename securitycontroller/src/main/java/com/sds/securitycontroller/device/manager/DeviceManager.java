/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.device.manager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.asset.manager.AssetManager;
import com.sds.securitycontroller.core.IDeviceMessageListener;
import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.device.BootDevice;
import com.sds.securitycontroller.device.DevRealtimeInfo;
import com.sds.securitycontroller.device.Device;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.device.DeviceStatus;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.KnowledgeEventArgs;
import com.sds.securitycontroller.event.RequestEventArgs;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.security.SecurityDeviceEntity;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.policy.AtomPolicy;
import com.sds.securitycontroller.policy.FlowPolicyObject;
import com.sds.securitycontroller.policy.PolicyAction;
import com.sds.securitycontroller.policy.PolicyActionArgs;
import com.sds.securitycontroller.policy.PolicyActionType;
import com.sds.securitycontroller.policy.PolicyInfo;
import com.sds.securitycontroller.policy.PolicySubject;
import com.sds.securitycontroller.policy.PolicySubject.PolicySubjectType;
import com.sds.securitycontroller.policy.resolver.PolicyEventArgs;
import com.sds.securitycontroller.protocol.DeviceMessageType;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.utils.ListenerDispatcher;
import com.sds.securitycontroller.utils.http.HTTPHelper;
import com.sds.securitycontroller.utils.http.HTTPHelperResult;
import com.sds.securitycontroller.utils.realtime.IRealtimeBasicManagement;
import com.sds.securitycontroller.utils.realtime.RealtimeBasic;
import com.sds.securitycontroller.utils.realtime.RealtimeBasicManager;

public class DeviceManager implements  ISecurityControllerModule, 
IDeviceManagementService, IEventListener {
	

	// The connectedDevices map contains only those devices that are connected 
    // and controlled by us


	protected String reverseProxyUrl="192.168.19.179:9999/4sdnapi/";
	protected String wafManageIp="192.168.19.179" ;
	protected String wafProtectedHost="";
    //public final String tableName = "devices";
	//public final String tableName = "t_devs";
    protected final String primaryKeyName = "id";
    protected Map<String,SecurityDeviceEntity> securityDevicesFromKnowledgeDB=new HashMap<String,SecurityDeviceEntity>();
    protected Map<DeviceType, List<String>> bootAgentUrls = new HashMap<DeviceType, List<String>>();
    protected Map<String,BootDevice> bootDevices =new HashMap<String, BootDevice>();
    

	protected AssetManager assetmanager=new AssetManager();
	protected boolean useDB=true;
	protected String myip=null;
    protected static Logger log = LoggerFactory.getLogger(DeviceManager.class);
    
	protected IStorageSourceService storageSource; 
    protected IEventManagerService eventManager;
    protected IRestApiService restApi;
    protected IRealtimeBasicManagement realtimeManager;
    protected IRegistryManagementService serviceRegistry;
    protected ConcurrentMap<DeviceMessageType,
    ListenerDispatcher<DeviceMessageType,IDeviceMessageListener>>
        messageListeners;


    @Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		 Collection<Class<? extends ISecurityControllerService>> services =
	                new ArrayList<Class<? extends ISecurityControllerService>>(1);
	        services.add(IDeviceManagementService.class);

	        return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
        Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
        // We are the class that implements the service
        m.put(IDeviceManagementService.class, this);

        return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
        Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
       	l.add(IStorageSourceService.class);
        l.add(IRestApiService.class);
        return l;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {

		this.assetmanager.init(context);
		
		
        this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
        this.messageListeners = new ConcurrentHashMap<DeviceMessageType,
                                      ListenerDispatcher<DeviceMessageType,
                                      IDeviceMessageListener>>();
        this.storageSource = context.getServiceImpl(IStorageSourceService.class, this, "");
        this.restApi = context.getServiceImpl(IRestApiService.class);
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	

		realtimeManager=new RealtimeBasicManager(this.storageSource);
		this.useDB=true;
		//get sc ip
		GlobalConfig global = GlobalConfig.getInstance();
		this.myip = global.myip;
		if(null == this.myip){
			throw new SecurityControllerModuleException("my ip is not set");
		}
		this.reverseProxyUrl=context.getConfigParams(this).get("reverseProxyUrl");
		myip.trim();

        log.info("BUPT security controller device manager initialized...");
        
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		this.assetmanager.startUp(context);
		if(this.useDB){
			this.storageSource.setTablePrimaryKeyName(getTableName("dev"), this.primaryKeyName);
		}

        eventManager.addEventListener(EventType.REQUEST_KNOWLEDGE, this);
        // register REST interface
        DeviceManagerRoutable r = new DeviceManagerRoutable();
        restApi.addRestletRoutable(r);
        serviceRegistry.registerService(r.basePath(), this);
		log.info("BUPT security controller device manager started");
	}

	//------device related method--------

	@Override
    public boolean addDev(Device dev) {
		String id = this.applyDevId();
		boolean ret=false;
		do{
			if(null == id){
				log.error("apply dev id failed:{} ", dev);
				break;
			}
			dev.setId(id);
			int res = storageSource.insertEntity(getTableName("dev"), dev);
			if(res<=0){
			    log.error("Insert dev to DB failed {} ", dev);
				break;
			} 
			if(this.addDevRealtimeInfo(dev.getId())){
				log.error("add dev realtimeinfo failed:",dev.getId());
			}
			Event event = new Event(EventType.ADD_DEVICE, dev, this, null);
			this.eventManager.addEvent(event);
			ret=true;
		}while(false);
		return ret;
    }	

	@Override
	public Device getDev(String id){
		Device dev = null;
		do{
			if(null == id || id.isEmpty()){
				break;
			}
			dev = (Device)this.storageSource.getEntity(getTableName("dev"), id, Device.class);				
		}while(false);		
		return dev;
	}
	

	@Override
    public boolean removeDev(Device dev) {
 		int res = storageSource.deleteEntity(getTableName("dev"), dev.getId());
 		if(res<0){
 			return false;
 		}
 		if(!removeDevRealtimeInfo(dev.getId())){
 			log.error("remove app realtimeinfo failed:",dev.getId());
 		}
 		if(!rpc_DeleteDevInfo(dev.getVmid())){
 			log.error("remove app rpc_DeleteDevInfo failed:",dev.getId());
 		}
        Event event = new Event(EventType.REMOVE_DEVICE, dev, this, null);
        this.eventManager.addEvent(event);
        return true;
    }
	

	@Override
	public boolean updateDev(Device dev, JsonNode jn) throws IOException {		
		dev.updateInfo(jn);				
		int ret = storageSource.updateOrInsertEntity(getTableName("dev"), dev);
		if(ret<=0) 
			return false;
		return true;
	}  
	

	//--------------get dev from DB-----------------------------
	public List<Device> getDevs(List<QueryClauseItem> items) {
		QueryClause qc = new QueryClause(getTableName("dev"));
    	@SuppressWarnings("unchecked")
		List<Device> allDevices = (List<Device>)storageSource.executeQuery(qc, Device.class);
    	
    	return allDevices;
		
	}
	//without any condition
    @Override
    public List<Device> getDevs() {
    	List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
    	return getDevs(items);    	
	}
    
    public List<Device> getDevs(String key, String value) {    	
		List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
		items.add(new QueryClauseItem(key, value,QueryClauseItem.OpType.EQ));		
		return getDevs(items);   
	}
    
    public List<Device> getDevs(String key, String value, QueryClauseItem.OpType optType) {    	
		List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
		items.add(new QueryClauseItem(key, value, optType));		
		return getDevs(items);   
	}

    //--------------get dev from DB-----------------------------
    
    
	@Override
	public String devRegistered(Device dev) {
		List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
		items.add(new QueryClauseItem("ip",dev.getIp(),QueryClauseItem.OpType.EQ));
		
		QueryClause qc = new QueryClause(items, getTableName("dev"), null, null);
		@SuppressWarnings("unchecked")
		List<Device> result = (List<Device>)storageSource.executeQuery(qc, Device.class);
		if(result.isEmpty()){
			return null;
		}
	
		return result.get(0).getId();
	}
    
	//-----------reltime information--------------
	/*
	 * this method must be synchronized 
	 */
	@Override
	public synchronized boolean  addDevRealtimeInfo(String appId) {
 
		return this.realtimeManager.addRealtimeBasic(appId, "DEV");
	}
	
	@Override
	public boolean updateDevRealtimeInfo(String appId, JsonNode jn) throws IOException {
		RealtimeBasic rb=new RealtimeBasic();
		int tcpu=jn.path("cpu").asInt();
		int tmu=jn.path("memory_used").asInt();
		int tmt=jn.path("memory_total").asInt();
		int tdu=jn.path("disk_used").asInt();
		int tdt=jn.path("disk_total").asInt();
		
		if(tcpu<0 || tcpu>100 || tmu<0||tmt<tmu||tdu<0||tdt<tdu){
			throw new IOException("value range error");
		}
		
		rb.setCpu(tcpu);
		rb.setMemory_used(tmu);
		rb.setMemory_total(tmt);
		rb.setDisk_used(tdu);
		rb.setDisk_total(tdt);
		rb.setState(jn.path("state").asText());
		rb.setStart_time(jn.path("start_time").asInt());
		return this.realtimeManager.updateRealtimeBasic(appId, "DEV", rb);		 
	}
	@Override
	public boolean removeDevRealtimeInfo(String appId) {
		return this.realtimeManager.removeRealtimeBasic(appId, "DEV");
	}
	@Override
	public DevRealtimeInfo getDevRealtimeInfo(String appId) {
		RealtimeBasic rb=this.realtimeManager.getRealtimeBasic(appId, "DEV");
		return new DevRealtimeInfo(rb);
	}
	
	@Override
	public List<DevRealtimeInfo> getAllDevRealtimeInfo() {
		List<RealtimeBasic> result = this.realtimeManager.getAllRealtimeBasic("DEV");
		List<DevRealtimeInfo> allInfos = new ArrayList<DevRealtimeInfo>();       
		for (RealtimeBasic object : result) {
    		allInfos.add(new DevRealtimeInfo(object));
    	}
    	return allInfos;
		
	}
	 
	//-------snapshot path--------------
	@Override
	public String getSnapshotdir(){
		return "/opt/sds/snapshot/";
	}
	
	
    
	//------knowledge related methods-------------
	Map<String, SecurityDeviceEntity> getSecurityDeviceMap() {
		Map<String, SecurityDeviceEntity> secDevKnowledgeList = new HashMap<String, SecurityDeviceEntity>();
		List<Device> devlist = this.getDevs();
		for (Device d : devlist) {
			secDevKnowledgeList.put(d.getId(),
					new SecurityDeviceEntity(d));
		}
		return secDevKnowledgeList;
	}
 
	
	//-------------event related methods----------

	@Override
	public void addDeviceMessageListener(DeviceMessageType type,
			IDeviceMessageListener listener) {
		ListenerDispatcher<DeviceMessageType, IDeviceMessageListener> ldd =
	            messageListeners.get(type);
	        if (ldd == null) {
	            ldd = new ListenerDispatcher<DeviceMessageType, IDeviceMessageListener>();
	            messageListeners.put(type, ldd);
	        }
	        ldd.addListener(type, listener);
	}

	@Override
	public void removeDeviceMessageListener(DeviceMessageType type,
			IDeviceMessageListener listener) {
		ListenerDispatcher<DeviceMessageType, IDeviceMessageListener> ldd =
	            messageListeners.get(type);
	        if (ldd != null) {
	            ldd.removeListener(listener);
	        }		
	} 
	
	@Override
	public void processEvent(Event e) {
		// handle with security device info retrieving request

		if(e.type!=EventType.REQUEST_KNOWLEDGE){
			log.debug(" cloud agent does not know how to deal with event of {} type",e.type);
			return;
		}
		KnowledgeType entityType = ((RequestEventArgs)e.args).entityType;	
		// deal with only requesting security devices
		if(entityType!=KnowledgeType.SECURITY_DEVICE)
			return;
		// return all devices
		log.debug(" get device info of {} type",entityType);
		Event eventSend = new Event(EventType.RETRIEVED_KNOWLEDGE, null,this,new KnowledgeEventArgs(entityType,getSecurityDeviceMap()));
		eventManager.addEvent(eventSend);
	}

	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		
	}

	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		
	}
	
	
	private String applyDevId() {
		int MAX=50;//how many times to try
		Random r=new Random();
		long nid=System.currentTimeMillis();		
		String id="2"+Long.toString(nid);
		int count=0;
		
		while(count<MAX && null != this.getDev(id)){
			nid+=r.nextInt(200);
			id="2"+Long.toString(nid);
			count++;
		}
		if(MAX == count){
			return null;
		}
		return id;
	}
	//----DB related-----
 
	public String getTableName(String type) {
	    String devTableName = "t_devs";	 
	    String realtimeInfoTableName = "t_realtime_basic";
	    
	    
	    switch(type){
	    case "dev":
	    	return devTableName;
	    case "realtimeInfo":
	    	return realtimeInfoTableName; 
		default:
	    	return "";
	    }
	}
 
	public String getTablePK(String type) {
		String devPK = "id"; 
		String realtimeInfoPK = "obj_id";
 
	    switch(type){
	    case "dev":
	    	return devPK;
	    case "realtimeInfo":
	    	return realtimeInfoPK; 
	    default:
	    	return "";
	    }		 
	}
 
	public Map<String, String> getTableColumns(String type) {
	     Map<String, String> devTableColumns = new HashMap<String, String>(){
			private static final long serialVersionUID = 1L;

		{
	    	put("id", "VARCHAR(30)");
	    	put("vmid", "VARCHAR(30)");
	    	put("name", "VARCHAR(30)");	    	
	    	put("alias", "VARCHAR(30)");
	    	put("hash", "VARCHAR(32)");	
	    	put("api_ver", "VARCHAR(16)");
	    	put("rule_ver", "VARCHAR(16)");
	    	put("ip", "VARCHAR(128)");
	    	put("port", "INTEGER");
	    	put("protocol", "VARCHAR(10)");
	    	put("root_url", "VARCHAR(128)");
	    	put("enable", "BOOLEAN");
	    	put("reg_time","INTEGER");
	    	put("manage_url", "VARCHAR(128)");
	    	put("license", "VARCHAR(16)");	    	
	    	put("busy", "BOOLEAN");
	    	put("type", "VARCHAR(20)");
	    	put("category", "VARCHAR(20)");
	    	put("mac_addrs", "VARCHAR(128)");
	    	put("service", "VARCHAR(64)");
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
	    case "dev":
	    	return devTableColumns;
	    case "realtimeInfo":
	    	return rtTableColumns;
 
	    default:
	    	return null;
	    } 
	     
	}

	
	/*
	 * for different operations, we call different modules
	 */
	@Override
	public boolean controlDev(String operation) {
		return true;
	}
	@Override
	public String getMyIp()throws Exception{
		if(null == this.myip){
			throw new Exception("myip is not configured");
		}
		return this.myip;
	}

	@Override
	public AssetManager getAssetManager(){
		return this.assetmanager;
	}
	
	//private functions
	public boolean setDevBusyFlag(String devid, boolean busy){		
		Device dev=getDev(devid);
		dev.setBusy(busy);
		int ret = storageSource.updateOrInsertEntity(getTableName("dev"), dev);
		if(ret<=0){
			return false;
		}
		return true;
	}  
	
	//handle rpc
	@Override
	public Object handleRPCRequest(String methodName, Object[] args) {
		if(null==args ){
			return null;
		}
		switch(methodName){
		case "getalldevs":
			if(0==args.length){
				break;
			}
			return this.getDevs("service",(String)args[0]);
		case "getdev":
			if(0==args.length){
				break;
			}
			return this.getDev((String)args[0]);
		case "devalloc"://for policyMgr
			if(2 > args.length){
				break;
			}
			DeviceType type=(DeviceType)args[0];
			if(!rpc_NewDev(type)){
				log.error("dev mgr:new dev request failed");
			}
			return this.rpc_Handler_DevAlloc(type, (String)args[1]);
		case "devfree"://for policyMgr
			if(0==args.length){
				break;
			}
			return this.rpc_Handler_DevFree((String)args[0]);		
		default:
			break;
			
		}
		return null;
	}
	//RPC handler functions
	private String rpc_Handler_DevAlloc(DeviceType type, String apiVer){
		if(null == type || null == apiVer){
    		return null;
    	}
    
		String devid=null;
    	do{
    		List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
    		items.add(new QueryClauseItem("type", type.toString(), QueryClauseItem.OpType.EQ));
    		items.add(new QueryClauseItem("busy", false, QueryClauseItem.OpType.EQ));
    		//items.add(new QueryClauseItem("api_ver", apiVer, QueryClauseItem.OpType.EQ));
    		QueryClause qc = new QueryClause(items, getTableName("dev"), new String[]{"id"}, null);
    		Device device = (Device)storageSource.executeQuery(qc, Device.class);
    		if(device == null)
    			break;    		
    		devid = device.getId();
    		if(devid == null)
    			break;
        	if(!setDevBusyFlag(devid, true)){
        		log.error("set dev busy flag error");
        		break;
        	}
        	
    	}while(false);
		 
    	
    	return devid;
	}
	private boolean rpc_Handler_DevFree(String devid){
		boolean ret=false;
		do{
			if(!rpc_Handler_DevReset(devid)){    		
				log.error("resetDev error");
				break;
	    	}
			if(!setDevBusyFlag(devid, false)){    		
				log.error("set dev busy flag error");
				break;
	    	}	
			ret=true;
		}while(false);

		return ret;
	}
	private boolean rpc_Handler_DevReset(String devid){
		
		return true;
	}

	//RPC Calls 
	private boolean rpc_DeleteDevInfo(String vmid){
		Object[] rqArgs = {vmid};
		Object result = eventManager.makeRPCCall(com.sds.securitycontroller.cloud.manager.ICloudAgentService.class, "deletedev", rqArgs);
		return result == null ? false : (boolean)result;
	}
 
	private boolean rpc_NewDev(DeviceType type){
		Object[] rqArgs = {type};
		Object result = eventManager.makeRPCCall(com.sds.securitycontroller.cloud.manager.ICloudAgentService.class, "newdev", rqArgs);
		return result == null ? false : (boolean)result;
	}
		
	
	
	@Override
	public BootDevice getBootDevice(String id) {
		return this.bootDevices.get(id);
	}
	
	@Override
	public List<BootDevice> getAllBootDevices() {
		return new ArrayList<BootDevice>(this.bootDevices.values());
	}

	@Override
	public BootDevice bootSecurityDeviceInstance(String name, String type, String managementIp, 
			String connectType, final Map<String, Object> attrs) throws Exception{
		BootDevice device = null;
		String deviceId = null;
		String status = null;
		//start a new thread  to post request and then query periodically update the device status.
		ObjectMapper mapper = new ObjectMapper();
		DeviceType deviceType = Enum.valueOf(DeviceType.class, type);
		if(!this.bootAgentUrls.containsKey(deviceType) || this.bootAgentUrls.get(deviceType).size()==0){
			log.error("no such device type in all boot agents.");
			throw new Exception("no such device type in all boot agents.");
		}
		final String bootAgentUrl = this.bootAgentUrls.get(deviceType).get(0);
		
		//String id = UUID.randomUUID().toString();
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("name", name);
		data.put("management-ip", managementIp);
		data.put("connect-type", connectType);
		if(deviceType == DeviceType.WAF){
			data.put("type", "waf");
            data.put("ifs", attrs.get("connections"));
            data.put("protected-host", attrs.get("protected_host"));
		}

		String content = "";
		
		try{
			
			content = mapper.writeValueAsString(data);
			
			URL url = new URL(bootAgentUrl);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.connect();
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes(content);
			out.flush();
			out.close();
			
			/*
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = "";
			while((line=reader.readLine())!=null){
				sb.append(line);
			}
			reader.close();
			conn.disconnect();
			
			String result = sb.toString();
			JsonNode root = mapper.readTree(result);
			*/
			mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(conn.getInputStream());
			status = root.path("status").asText();
			if(!status.equals("ok")){
				log.error(root.path("message").asText());
				return null;
			}
			conn.disconnect();
			JsonNode result = root.path("result");
			DeviceStatus deviceStatus = Enum.valueOf(DeviceStatus.class, result.path("status").asText().toUpperCase());
			deviceId = result.path("id").asText();
			connectType = result.path("connect_type").asText();
			managementIp = result.path("management_ip").asText();

			device = new BootDevice(deviceType, name, deviceId, deviceStatus, connectType, managementIp);

			if(deviceType == DeviceType.WAF){
				JsonNode cs = result.path("ifs");
				Iterator<JsonNode> it = cs.elements();
				List<Map<String, String>> connections = new ArrayList<Map<String, String>>();
				while(it.hasNext()){
					JsonNode c = it.next();
					Map<String, String> connection = new HashMap<String, String>();
					Iterator<Entry<String, JsonNode>> children = c.fields();
					while(children.hasNext()){
						Entry<String, JsonNode> entry = children.next();
						connection.put(entry.getKey(), entry.getValue().asText());		
					}
					connections.add(connection);
				}				
				device.setAttribute("ifs", connections);
			}
		}
		catch (JsonProcessingException e) {
			log.error("Json process error when booting device: "+e.getMessage());
			throw new Exception("Json process error when booting device: "+e.getMessage());
		} catch (IOException e) {
			log.error("IO error when booting device: "+e.getMessage());
			throw new Exception("IO error when booting device: "+e.getMessage());
		} catch (Exception e) {
			log.error("error when booting device: "+e.getMessage());
			throw new Exception("error when booting device: "+e.getMessage());
		}
		
		this.bootDevices.put(device.getId(), device);

		final String deviceUrl = bootAgentUrl+"/"+deviceId;
		final String dId = deviceId;
		wafManageIp=data.get("management-ip").toString();
		
		wafProtectedHost=data.get("protected-host").toString();
		Thread t = new Thread(){
			@Override
			public void run(){

				log.info("Retrieving device status");
				int maxRetry = 100;
				int count = 0;
				DeviceStatus deviceStatus = DeviceStatus.UNKNOWN;
				while(deviceStatus!= DeviceStatus.FAILED && deviceStatus!=DeviceStatus.READY && count<maxRetry){
					try{
						log.info("Retrieve device: "+dId+" status, current status: ("+deviceStatus+")");
						URL datasource = new URL(deviceUrl);
						ObjectMapper mapper = new ObjectMapper();
						JsonNode root = mapper.readTree(datasource);
						String status = root.path("status").asText();
						if(!status.equals("ok")){
							log.error("Retrieve device status error: "+root.path("message").asText());
							return;
						}

						JsonNode result = root.path("result");
						deviceStatus = Enum.valueOf(DeviceStatus.class, result.path("status").asText().toUpperCase());
						BootDevice device = bootDevices.get(dId);
						device.setStatus(deviceStatus);
                        //get device work interface mac
                        if (deviceStatus == DeviceStatus.READY) {
                            JsonNode workIfNode = result.path("ifs").get(1);
                            String workIfMac = workIfNode.get("mac").asText();
                            device.setAttribute("workIfMac", workIfMac);
                            
                            FlowPolicyObject object=new FlowPolicyObject();
                            object.setFlowArgs(result);
                            AtomPolicy ap = new AtomPolicy(object, new PolicyAction(PolicyActionType.PROXY, new PolicyActionArgs()));
                            AtomPolicy[] atomarray = new AtomPolicy[1];
                            atomarray[0] = ap;
                            PolicyInfo policyInfo = new PolicyInfo(this.toString(), atomarray, false);
                            policyInfo.setSubject(new PolicySubject("",
                            		PolicySubjectType.NETWORK_CONTROLLER));
                            policyInfo.setActionType(PolicyActionType.PROXY);
                            policyInfo.setNegated(false);
                            PolicyEventArgs args = new PolicyEventArgs(policyInfo);
                            eventManager.addEvent(new Event(EventType.RECEIVED_POLICY, null,
                                    this, args));

                        }

                        Thread.sleep(1000*3);
						count++;
					}
					catch (JsonProcessingException e) {
						log.error(" Json process error when querying booting device: "+e.getMessage());
					} catch (IOException e) {
						log.error ("IO error when querying booting device: "+e.getMessage());
					} catch (InterruptedException e) {
						continue;
					}
				}


				log.info("Current device "+dId+" status:"+deviceStatus);
				if(deviceStatus==DeviceStatus.READY){
					boolean result =reverseProxyWaf("admin","wafProxySite",wafManageIp,wafProtectedHost);
					if(result==true){
						log.info("the waf policy is sucessful");
					}
				}
			}
		};
		t.start();	
		
		return device;
		
	}

    @Override
	public boolean registerBootAgent(DeviceType type, String url){
		List<String> urls = null;
		if(this.bootAgentUrls.containsKey(type)){
			urls = this.bootAgentUrls.get(type);
		}
		if(urls == null)
			urls = new ArrayList<String>();
		urls.add(url);
		this.bootAgentUrls.put(type, urls);
		
		return true;
	}
	/***
	 * 
	 * @param lesse 租户信息（header cookie USERNAME=lesse)
	 * @param siteGroup 站点组 siteGroup
	 * @param managementIp waf管理接口ip(192.168.19.230)
	 * @param protectedHosts 防护主机("192.168.1.1:90:10031,192.168.1.1:40:10032,192.168.1.3:00:10033")
	 * @return
	 */
	public boolean reverseProxyWaf(String lesse,String siteGroup,String managementIp, 
			String protectedHosts){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("cookie", "USERNAME="+lesse);
		String content = "";
		String reqSgUrl = this.reverseProxyUrl.replace("[manageIp]", managementIp)+"clouds/"+siteGroup;
		HTTPHelperResult resultSg = HTTPHelper.httpPost(reqSgUrl, content, headers);
		if(200==resultSg.getCode()){
			log.info("the siteGroup created sucessfully");
		}
		String reqSitesUrl=reqSgUrl+"/website";
		content=wafJsongenerator(managementIp, protectedHosts);
		HTTPHelperResult resultSites = HTTPHelper.httpPost(reqSitesUrl, content, headers);
		if(200==resultSites.getCode()){
			log.info("the sites created sucessfully");
			return true;
		}else {
			log.error(resultSites.getMsg());
		}
		return false;
	}
	/***
	 * {
                "SITE_LIST": [
                    {
                        "NAME": "kobe",                       ---->防护站点名称（自定义）
                        "ACTIVE": "true",                     ---->这个参数的意义忘了，你就默认为true吧
                        "IP": "10.65.10.24",                  ---->waf工作组IP（一般为eth1或者eth2的IP）
                        "PORT": "11123",                      ---->代理端口 （自定义）
                        "DESC": "kobe byrant website",        ---->站点描述 （自定义）
                        "WEBACCESS_ENABLED": "true",          ---->是否开启站点访问日志
                        "HOST": [                             
                            {
                                "DOMAIN": "10.65.10.24",      ---->站点域名
                                "SSL": "true",                ---->是否开启ssl  默认为true
                                "SERVER": [
                                    {
                                        "REAL_IP": "192.168.1.100",      ---->防护站点的ip
                                        "REAL_PORT": "80"                ---->防护站点的端口
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
	 * @param managementIp
	 * @param protectedHosts
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String wafJsongenerator(String managementIp, 
			String protectedHosts){
		JSONObject outData=new JSONObject();
		
		JSONArray site_list=new JSONArray();
		
		String[] hosts=protectedHosts.split(",");
		for (String host : hosts) {
			JSONObject site=new JSONObject();
			String[] p_host=host.substring(1, host.length()-1).split(":");
			if(p_host.length!=3){
				log.error("protectedHost is not valided");
				return "";
			}
			String hostIp=p_host[0];
			String hostPort=p_host[1];
			String propxyPort=p_host[2];
			site.put("NAME", hostIp+hostPort);
			site.put("ACTIVE", "true");
			site.put("IP", managementIp);
			site.put("PORT", propxyPort);
			site.put("DESC", hostIp+hostPort);
			site.put("WEBACCESS_ENABLED", "true");
			//site.put(key, value)
			JSONArray hostobject=new JSONArray();
			
			JSONObject server=new JSONObject();
			server.put("DOMAIN", hostIp);
			server.put("SSL", "true");
			JSONArray pdHosts=new JSONArray();
			JSONObject pdHost=new JSONObject();
			pdHost.put("REAL_IP", hostIp);
			pdHost.put("REAL_PORT", hostPort);
			pdHosts.add(pdHost);
			server.put("SERVER", pdHosts);
			hostobject.add(server);
			site.put("HOST", hostobject);
			site_list.add(site);
		}
		outData.put("SITE_LIST", site_list);
		
		return outData.toJSONString();
	}

	@Override
	public BootDevice getBootDevice(DeviceType deviceType) {
		List<BootDevice> bootDevices= new ArrayList<BootDevice>(this.bootDevices.values());
		for (BootDevice bootDevice : bootDevices) {
			if(bootDevice.getType()==deviceType)
				return bootDevice;
		}
		return null;
	}
}
