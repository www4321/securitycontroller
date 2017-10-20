package com.sds.securitycontroller.cloud.manager;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.sds.securitycontroller.cloud.ICloudPlugin;
import com.sds.securitycontroller.cloud.KvmCloudPlugin;
import com.sds.securitycontroller.cloud.VirtualMachine;
import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.knowledge.cloud.CloudPort;
import com.sds.securitycontroller.knowledge.cloud.CloudSubnet;
import com.sds.securitycontroller.knowledge.cloud.CloudTenant;
import com.sds.securitycontroller.knowledge.cloud.CloudUser;
import com.sds.securitycontroller.knowledge.cloud.CloudVM;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.utils.HTTPUtils;
import com.sds.securitycontroller.utils.http.HTTPHelper;
import com.sds.securitycontroller.utils.http.HTTPHelperResult;

public class OpenstackApiAgent implements ICloudAgentService, ISecurityControllerModule {

    protected static Logger log = LoggerFactory.getLogger(OpenstackApiAgent.class);
    
	String regionName="sdsExprRegion";
	String password="sds";
	String authUrl="http://os.research.intra.sds.com:35357/v2.0";
	String quantumUrl="http://os.research.intra.sds.com:9696/v2.0";
	String novaUrl="http://os.research.intra.sds.com:8774/v2";
	String userName="adminUser";
	String tenantName="admin";
	String tenantId = null;
	
	Map<String, CloudTenant> tenantMap;
	Map<String, CloudUser> userMap; 
	Map<String, CloudVM> vmMap; 
	Map<String, CloudSubnet> subnetMap; 
	Map<String, CloudPort> portMapping; 
	Map<String, Map<String, String>> tenantMacIPMapping; 
	Map<String, Map<String, String>> tenantIPMacMapping; 
	
	String token = null;
	Date tokenExpireTime = null;
	
	boolean useUTCTime = true;

    //protected IStorageSourceService storageSource;
    String tenantTableName = "tenants";
    protected final String tenantPrimaryKeyName = "id";
    String userTableName = "users";
    protected final String userPrimaryKeyName = "id";
    String subnetTableName = "subnets";
    protected final String subnetPrimaryKeyName = "id";

	protected IRestApiService restApi;
    protected IRegistryManagementService serviceRegistry;
    
    
    
    

	protected ICloudPlugin cloudPlugin = null;
	protected IStorageSourceService storageSource;
	private String vmTableName = "t_vms";
	
	private String myip=null;

	public enum VmStatus {
		RESERVED,
		STANDBY,
		IPSET,
		REGED,
		UNKNOWN
	}
	
	
	public boolean isTokenExpired(){
		if(this.token == null || this.tokenExpireTime==null)
			return true;
		Date now = new Date();
		if(this.useUTCTime){
			//Convert the date from the local timezone to UTC timezone
			 SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
			String snow = sdf1.format(now);  
			 SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			//sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));		
			try {
				now = sdf2.parse(snow);
			} catch (ParseException e) {
			}
		}
		
		
		if(this.tokenExpireTime.before(now))
			return true;
		return false;
	}
	/*
	 * get token:  curl -d '{"auth":{"passwordCredentials":{"username": "adminUser", "password": "admin"}}}' -H "Content-type: application/json" http://os.research.intra.sds.com:5000/v2.0/tokens
 */
	
	class AuthRequestBean{
		class Auth{
			String tenantName;
			class PasswordCredentials{
				String username;
				public String getUsername() {
					return username;
				}
				public void setUsername(String username) {
					this.username = username;
				}
				public String getPassword() {
					return password;
				}
				public void setPassword(String password) {
					this.password = password;
				}
				String password;
			}
			PasswordCredentials passwordCredentials = new PasswordCredentials();

			public String getTenantName() {
				return tenantName;
			}
			public void setTenantName(String tenantName) {
				this.tenantName = tenantName;
			}
			public PasswordCredentials getPasswordCredentials() {
				return passwordCredentials;
			}
			public void setPasswordCredentials(PasswordCredentials passwordCredentials) {
				this.passwordCredentials = passwordCredentials;
			}
		}
		Auth auth = new Auth();
		public Auth getAuth() {
			return auth;
		}
		public void setAuth(Auth auth) {
			this.auth = auth;
		}		
	}
	
	
	
	public boolean GetToken() {
		String url = this.authUrl+"/tokens";
		AuthRequestBean req = new AuthRequestBean();
		req.auth.tenantName = this.tenantName;
		req.auth.passwordCredentials.username = this.userName;
		req.auth.passwordCredentials.password = this.password;
				
		String jsonReq = "";		
		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter();  
		JsonGenerator gen;
		try {
			gen = new JsonFactory().createGenerator(writer);
			mapper.writeValue(gen, req);  
	        jsonReq = writer.toString(); 
	        gen.close();  
	        writer.close();  	
		} catch (IOException e) {
			log.error("Error when getting keystone token: {}", e.getMessage());
			return false;
		}  
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type","application/json");
		String jsonResp = HTTPUtils.httpPost(url, jsonReq, headers);
		if(jsonResp == null) //Error
			return false;
		try {
			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			 SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			 String exp = root.path("access").path("token").path("expires").asText();
			this.tokenExpireTime = simpleDateFormat.parse(exp);
			this.token = root.path("access").path("token").path("id").asText();
			this.tenantId = root.path("access").path("token").path("tenant").path("id").asText();
		} catch (IOException | ParseException e) {
			System.out.print("Error when getting keystone token: "+ e.getMessage());
			log.error("error parsing json response: {}", e.getMessage());
			return false;
		}  
		return true;
		
	}
	
	//format:http://docs.openstack.org/api/openstack-network/2.0/content/list_subnets.html

	@Override
	public Map<String, CloudSubnet> getSubnets() {
		if(isTokenExpired())
			GetToken();
		log.info("Getting cloud subnet infomation");
		//query quantum to get subnet infomation
		String url = this.quantumUrl + "/subnets";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Auth-Token",this.token);
		String jsonResp = HTTPUtils.httpGet(url, headers);
		if(jsonResp == null) //Error
			return null;
		this.subnetMap.clear();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {

			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			JsonNode ts = root.path("subnets");
			for(int i = 0;i<ts.size();i++){
				JsonNode t = ts.get(i);
				String id = t.path("id").asText();
				String name = t.path("name").asText();
				String network_id = t.path("network_id").asText();
				String tenantId = t.path("tenant_id").asText();
				String allocation_pools_start = t.path("allocation_pools").path("start").asText();
				String allocation_pools_end = t.path("allocation_pools").path("end").asText();
				String gateway_ip = t.path("gateway_ip").asText();
				String cidr = t.path("cidr").asText();
				boolean enable_dhcp = t.path("enable_dhcp").asBoolean();
				CloudSubnet subnet = new CloudSubnet(id, name, network_id, tenantId, allocation_pools_start, 
						allocation_pools_end,gateway_ip, cidr, enable_dhcp);
				this.subnetMap.put(id, subnet);
			}			
		} catch (IOException e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		}  
		
		return this.subnetMap;
	}
	
	

	@Override
	public Map<String, CloudPort> getMacIPMapping() {
		if(isTokenExpired())
			GetToken();
		log.info("Getting cloud subnet infomation");
		//query quantum to get subnet infomation
		String url = this.quantumUrl + "/ports";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Auth-Token",this.token);
		String jsonResp = HTTPUtils.httpGet(url, headers);
		if(jsonResp == null) //Error
			return null;
		this.portMapping.clear();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {

			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			JsonNode ts = root.path("ports");
			for(int i = 0;i<ts.size();i++){
				JsonNode t = ts.get(i);
				String id = t.path("id").asText();
				String mac = t.path("mac_address").asText();
				String networkId = t.path("network_id").asText();
				String tenantId = t.path("tenant_id").asText();
				String deviceId = t.path("device_id").asText();
				boolean stateUp = t.path("admin_state_up").asBoolean();
				
				JsonNode fixedips = t.path("fixed_ips");
				String ip = fixedips.get(0).path("ip_address").asText();
				String subnet = fixedips.get(0).path("subnet_id").asText();				
				
				CloudPort port = new CloudPort(id, ip, mac, subnet, deviceId, tenantId, networkId, stateUp);
				this.portMapping.put(id, port);
				
				if(!this.tenantMacIPMapping.containsKey(tenantId)){
					this.tenantMacIPMapping.put(tenantId, new HashMap<String, String>());					
				}
				this.tenantMacIPMapping.get(tenantId).put(mac, ip);
				if(!this.tenantIPMacMapping.containsKey(tenantId)){
					this.tenantIPMacMapping.put(tenantId, new HashMap<String, String>());
				}
				this.tenantIPMacMapping.get(tenantId).put(ip, mac);
			}			
		} catch (IOException e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		}  
		
		return this.portMapping;
	}


	/*{"tenants_links": [], "tenants": 
	 * [{"description": null, "enabled": true, "id": "117e6786e6fc48dcb7dcb281024b1b68",
	 *  "name": "invisible_to_admin"}, {"description": null, "enabled": true, 
	 *  "id": "2755db390fcd4c9bb504242617d5f6a0", "name": "service"}, 
	 *  {"description": "", "enabled": true, "id": "3e35c8913898498e82d9bbb2c712b585", 
	 *  "name": "user"}, {"description": null, "enabled": true,
	 *   "id": "53707d290204404dbff625378969c25c", "name": "admin"},
	 *   ...
	 *   ]}
	 * */
	class GetTenantResponseBean{
		class Tenant{
			public String getDescription() {
				return description;
			}
			public void setDescription(String description) {
				this.description = description;
			}
			public boolean isEnabled() {
				return enabled;
			}
			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
			}
			public String getId() {
				return id;
			}
			public void setId(String id) {
				this.id = id;
			}
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}

			String description;
			boolean enabled;
			String id;
			String name;
		}
		List<Tenant> tenants;
		public List<Tenant> getTenants() {
			return tenants;
		}
		public void setTenants(List<Tenant> tenants) {
			this.tenants = tenants;
		}
	}
	
	
	@Override
	public Map<String, CloudTenant> getTenants() {
		if(isTokenExpired())
			GetToken();
		log.info("Getting cloud tenant infomation");
		String url = this.authUrl + "/tenants";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Auth-Token",this.token);
		String jsonResp = HTTPUtils.httpGet(url, headers);
		if(jsonResp == null) //Error
			return null;
		this.tenantMap.clear();
		ObjectMapper mapper = new ObjectMapper();
		try {

			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			JsonNode ts = root.path("tenants");
			for(int i = 0;i<ts.size();i++){
				JsonNode t = ts.get(i);
				String id = t.path("id").asText();
				CloudTenant tenant = new CloudTenant(id,t.path("name").asText());
				this.tenantMap.put(id, tenant);
			}
		} catch (IOException e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		}  
		
		return this.tenantMap;
	}
	

	@Override
	public Map<String, CloudUser> getUsers() {
		if(isTokenExpired())
			GetToken();
		log.info("Getting cloud user infomation");
		String url = this.authUrl + "/users";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Auth-Token",this.token);
		String jsonResp = HTTPUtils.httpGet(url, headers);
		if(jsonResp == null) //Error
			return null;
		this.userMap.clear();
		ObjectMapper mapper = new ObjectMapper();
		try {

			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			JsonNode ts = root.path("users");
			for(int i = 0;i<ts.size();i++){
				JsonNode t = ts.get(i);
				String id = t.path("id").asText();
				String name = t.path("name").asText();
				boolean enabled = t.path("enabled").asBoolean();
				String tenantId = t.path("tenantId").asText();
				CloudUser user = new CloudUser(id, name, enabled, tenantId);
				this.userMap.put(id, user);
			}			
		} catch (IOException e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		} 		
		return this.userMap;
	}
	
	

	@Override
	public Map<String, CloudVM> getVMs() {
		if(isTokenExpired())
			GetToken();
		log.info("Getting cloud user infomation");
		String url = this.novaUrl + "/"+this.tenantId + "/servers/detail";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Auth-Token",this.token);
		String jsonResp = HTTPUtils.httpGet(url, headers);
		if(jsonResp == null) //Error
			return null;
		this.vmMap.clear();
		ObjectMapper mapper = new ObjectMapper();
		 mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(Visibility.ANY));

		try {
			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			JsonNode ts = root.path("servers");
			for(int i = 0;i<ts.size();i++){
				JsonNode t = ts.get(i);
				String id = t.path("id").asText();
				String name = t.path("name").asText();
				String userid = t.path("user_id").asText();
				String tenantid = t.path("tenant_id").asText();
				Map<String, CloudVM.NetworkInfo> networks = new HashMap<String, CloudVM.NetworkInfo>();
				CloudVM vm = new CloudVM(id, name, userid, tenantid, networks);
				Iterator<Entry<String, JsonNode>> ns = t.path("addresses").fields();
				while(ns.hasNext()){
					CloudVM.NetworkInfo netInfo = vm.new NetworkInfo();
					Entry<String, JsonNode> entry = ns.next();
					String netName =  entry.getKey();
					JsonNode netNode = entry.getValue();
					Iterator<JsonNode> as = netNode.elements();
					while(as.hasNext()){
						JsonNode addrNode = as.next();
						netInfo.addAddress(addrNode.path("version").asInt(),
								addrNode.path("addr").asText(), addrNode.path("OS-EXT-IPS:type").asText());
					}
					networks.put(netName, netInfo);
				}
				this.vmMap.put(id, vm);
			}			
		} catch (IOException e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		} 		
		return this.vmMap;
	}

	
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		 Collection<Class<? extends ISecurityControllerService>> services =
	                new ArrayList<Class<? extends ISecurityControllerService>>(1);
	        services.add(ICloudAgentService.class);
	        return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
        // We are the class that implements the service
        m.put(ICloudAgentService.class, this);
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
		//from xi'an dev
        this.storageSource = context.getServiceImpl(IStorageSourceService.class);
		this.storageSource.setTablePrimaryKeyName(this.vmTableName, "vmid");
		GlobalConfig global = GlobalConfig.getInstance();
		//get sc ip
		this.myip = global.myip;
		if(null == this.myip){
			throw new SecurityControllerModuleException("my ip is not set");
		}
		myip = myip.trim();
		//get cloud api host
		
		String cloud_url = global.iaasHost;
		if(null == cloud_url){
			throw new SecurityControllerModuleException("api_root is not set");
		}
		cloud_url = cloud_url.trim();
		cloud_url=cloud_url.endsWith("/")?cloud_url:cloud_url+"/";
		this.cloudPlugin = new KvmCloudPlugin(cloud_url);
		
		
		
        this.tenantMap = new HashMap<String, CloudTenant>();
        this.userMap = new HashMap<String, CloudUser>(); 
        this.subnetMap = new HashMap<String, CloudSubnet>(); 
        this.vmMap = new HashMap<String, CloudVM>(); 
        this.portMapping = new HashMap<String, CloudPort>(); 
        this.tenantMacIPMapping = new HashMap<String, Map<String, String>>();
        this.tenantIPMacMapping = new HashMap<String, Map<String, String>>();
        if(context != null)
        	this.restApi = context.getServiceImpl(IRestApiService.class);
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
		getMacIPMapping();
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		CloudAgentRoutable r = new CloudAgentRoutable();
        restApi.addRestletRoutable(r);
        serviceRegistry.registerService(r.basePath(), this);
		//this.storageSource.setTablePrimaryKeyName(this.tenantTableName, this.tenantPrimaryKeyName);
		//this.storageSource.setTablePrimaryKeyName(this.userTableName, this.userPrimaryKeyName);
		//this.storageSource.setTablePrimaryKeyName(this.subnetTableName, this.subnetPrimaryKeyName);
        

		// run a working thread to monitor vms in db
		Thread vmApplyThread = new Thread(new VmApplyThread());
		Thread vmSetIpThread = new Thread(new VmSetIpThread());
		Thread vmConfThread = new Thread(new DevRegThread());
		vmApplyThread.start();
		vmSetIpThread.start();
		vmConfThread.start();
	}

	@Override
	public void ResolveTenantUserMapping() {
		for(Entry<String, CloudUser> entry: this.userMap.entrySet()){
			CloudUser user = entry.getValue();
			String tenantId = user.getTenantId();
			if(this.tenantMap.containsKey(tenantId)){
				this.tenantMap.get(tenantId).addUser(user);
			}
			else{
				log.info("Warning: no tenant {} exists", tenantId);
			}
		}		
	}
	
	public static void main(String [] args) throws SecurityControllerModuleException{
        System.setProperty("org.restlet.engine.loggerFacadeClass", 
                "org.restlet.ext.slf4j.Slf4jLoggerFacade");
		OpenstackApiAgent client = new OpenstackApiAgent();
		client.init(null);
		client.getSubnets();
		client.getTenants();
		client.getUsers();
		client.getVMs();
		client.getMacIPMapping();
		client.ResolveTenantUserMapping();
	}

	@Override
	public String findTenantMacByIP(String tenantId, String ip) {
		if(!this.tenantIPMacMapping.containsKey(tenantId))
			return null;
		return this.tenantIPMacMapping.get(tenantId).get(ip);
	}

	@Override
	public String findTenantIPByMac(String tenantId, String mac) {
		if(!this.tenantMacIPMapping.containsKey(tenantId))
			return null;
		return this.tenantMacIPMapping.get(tenantId).get(mac);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//from xi'an dev

	// functions 
    public boolean setVmDbStatus(String vmid, String status) {
        if("" == vmid || null == vmid){
            return true;
        }
        VirtualMachine tmp = this.getVmDbItem(vmid);
        if (null == tmp) {
            return false;
        }
        tmp.setStatus(status);
        int ret = storageSource.updateOrInsertEntity(this.vmTableName, tmp);
        if (ret <= 0)
            return false;
        return true;
    }
    String getVmDbStatus(String vmid) {
        VirtualMachine tmp = this.getVmDbItem(vmid);
        if (null != tmp) {
            return tmp.getStatus();
        }
        return null;
    }
    public boolean setVmDbIp(String vmid, String ip) {
        VirtualMachine tmp = this.getVmDbItem(vmid);
        if (null == tmp) {
            return false;
        }
        tmp.setIp(ip);
        int ret = storageSource.updateOrInsertEntity(this.vmTableName, tmp);
        if (ret <= 0)
            return false;
        return true;
    }
    String getVmDbIp(String vmid) {
        VirtualMachine tmp = this.getVmDbItem(vmid);
        if (null != tmp) {
            return tmp.getIp();
        }
        return null;
    }
    boolean addVmDbItem(String vmid, String type, String status) {
        VirtualMachine item = new VirtualMachine();
        item.setVmid(vmid);
        item.setStatus(status);
        item.setType(type);
        int res = storageSource.insertEntity(this.vmTableName, item);
        if (res <= 0) {
            log.error("Insert vm item to DB failed {} ", item);
            return false;
        }
        return true;
    }


    public synchronized boolean incVmDbCfgCounter(String vmid) {
        VirtualMachine tmp = this.getVmDbItem(vmid);
        if (null == tmp) {
            return false;
        }
        tmp.setCfg_counter(tmp.getCfg_counter()+1);
        int ret = storageSource.updateOrInsertEntity(this.vmTableName, tmp);
        if (ret <= 0)
            return false;
        return true;
    }
    public boolean deleteVmDbItem(String vmid) {
        int res = storageSource.deleteEntity(this.vmTableName, vmid);
        if (res < 0) {
            return false;
        }
        return true;
    }
    VirtualMachine getVmDbItem(String vmid) {
        return (VirtualMachine) this.storageSource.getEntity(this.vmTableName, vmid,
                VirtualMachine.class);
    }

    public List<VirtualMachine> getVmDbItems(List<QueryClauseItem> items){
        QueryClause qc = new QueryClause(items, this.vmTableName, null, null);
        @SuppressWarnings("unchecked")
        List<VirtualMachine> result = (List<VirtualMachine>)storageSource.executeQuery(qc, VirtualMachine.class);

        List<VirtualMachine> allItems = new ArrayList<VirtualMachine>();
        for (Object object : result) {
            allItems.add((VirtualMachine) object);
        }
        return allItems;
    }
    //without any condition
    public List<VirtualMachine> getVmDbItems() {
        List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
        return getVmDbItems(items);
    }

    //with status and type
    public List<VirtualMachine> getVmDbItems(String status, String type){
        List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
        items.add(new QueryClauseItem("status", status, QueryClauseItem.OpType.EQ));
        items.add(new QueryClauseItem("type",   type,   QueryClauseItem.OpType.EQ));
        return getVmDbItems(items);
    }
    //with status only
    public List<VirtualMachine> getVmDbItems(String status){
        List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
        items.add(new QueryClauseItem("status", status, QueryClauseItem.OpType.EQ));
        return getVmDbItems(items);

    }
    //with status, conf_limit, opt
    public List<VirtualMachine> getVmDbItems(String status, int ip_conf_limit, QueryClauseItem.OpType optType) {
        List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
        items.add(new QueryClauseItem("status", status, QueryClauseItem.OpType.EQ));
        items.add(new QueryClauseItem("ip_conf_times", ip_conf_limit, optType));
        return getVmDbItems(items);
    }

    //--------------------------------------------


    //RPC
	/*
	 * vmid="",means no such ip is VM,
	 * vmid=null,means error
	 *
	 */
    public String getVmidByIp(String ip) {
        List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
        items.add(new QueryClauseItem("ip", ip, QueryClauseItem.OpType.EQ));
        QueryClause qc = new QueryClause(items, this.vmTableName, null, null);
        @SuppressWarnings("unchecked")
        List<VirtualMachine> result = (List<VirtualMachine>)storageSource.executeQuery(qc, VirtualMachine.class);

        if (0 == result.size()) {
            return "";
        }

        String vmid=(result.get(0)).getVmid();

        return vmid;
    }

    //inherited functions
    public int newVm(DeviceType type, StringBuffer vmid, String status) {
        int ret = cloudPlugin.newVm(type, vmid);
        if (0 != ret) {
            return -1;
        }
        if (!addVmDbItem(vmid.toString(), type.toString(),status)) {
            log.error("add vm db item error");
            if(0 != cloudPlugin.deleteVm(vmid.toString())){
                log.error("try to delete vm failed after add db failed");
            }
            return -1;
        }
        return 0;
    }

    @Override
    public int newVm(DeviceType type, StringBuffer vmid) {
        return newVm(type,vmid,VmStatus.STANDBY.toString());
    }

    @Override
    public int delVm(String vmid){
        int ret = cloudPlugin.deleteVm(vmid);
        if (0 != ret) {
            log.error("get ip failed in handle_req_reserved");
            return -1;
        }
        if(!deleteVmDbItem(vmid)){
            return -1;
        }
        return 0;
    }
    @Override
    public boolean powerOn(String vmid) {
        return 0 == cloudPlugin.powerOn(vmid);
    }

    @Override
    public boolean powerOff(String vmid) {
        return 0 == cloudPlugin.powerOff(vmid);
    }

    @Override
    public boolean powerReset(String vmid) {
        return 0 == cloudPlugin.powerReset(vmid);
    }
    @Override
    public int getStatus(String vmid) {
        return cloudPlugin.getStatus(vmid);
    }


    @Override
    public int getDevStatus(String vmid, StringBuffer status) {

        do{
            String stat=getVmDbStatus(vmid);
            switch(stat.toUpperCase()){
                case "READY":
                case "RESERVED":
                case "STANDBY":
                case "IPSET":
                case "REGED":
                    status.append(stat);
                    return 0;
                default:
                    return -1;
            }
        }while(false);
    }
    @Override
    public boolean setDevStatus(String vmid, String status){
        boolean ret = false;
        switch(status.toUpperCase()){
            case "RESERVED":
            case "STANDBY":
            case "IPSET":
            case "REGED":
                ret=setVmDbStatus(vmid,status);
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    public boolean newDev(DeviceType type){
        dev_rsv_req_queue.offer(new DevRequest(type));
        return this.dev_req_queue.offer(new DevRequest(type));
    }

    @Override
    public boolean getIp(String vmid, StringBuffer ip){
        int ret = cloudPlugin.getIp(vmid, ip);
        if (0 != ret) {
            return false;
        }
        return true;
    }
    //private tools:
    public String getMyIp()throws Exception{
        if(null == this.myip){
            throw new Exception("myip is not configured");
        }
        return this.myip;
    }



    @Override
    public Object handleRPCRequest(String methodName, Object[] args) {
        if (null == args || 0 == args.length) {
            return null;
        }
        switch (methodName) {
            case "getvmidbyip"://for dev register
                return this.getVmidByIp((String) args[0]);
            case "newdev"://deviceMgr apply a dev
                return this.newDev((DeviceType) args[0]);
            case "deletedev"://for deviceMgr.removeDev dev delete
                return setVmDbStatus((String) args[0], VmStatus.RESERVED.toString());
            default:
                break;

        }
        return null;
    }
    //rpc handlers


    /*
     * initially, we keep count_STANDBY vms in STANDBY
     * we receive REQUEST from deviceMgr, for each REQUEST, we do a 'RESERVED->STANDBY'
     * Transference; if we dont have RESERVED ones, we apply some vm and make it STANDBY
     * then, RESERVED is our optional pool
     */
    List<VirtualMachine> list_waf_ipset=null;
    List<VirtualMachine> list_wvss_ipset=null;

    //handles   RESERVED->STANDBY
    class VmApplyThread implements Runnable {
        private int count_STANDBY = 2;
        private int count_RESERVED = 2;
        private DevRequest request = null;


        @Override
        public void run() {
            log.info("VmApplyThread starting...");

            handle_init(DeviceType.WAF);
            handle_init(DeviceType.WVSS);
            dev_req_queue.offer(new DevRequest(DeviceType.WAF));
            dev_req_queue.offer(new DevRequest(DeviceType.WVSS));
            while (true) {
                try {
                    if (null == request) {
                        request = dev_rsv_req_queue.take();
                    }
                    if(handle_req(request)){
                        //not matter if the req is handled, we ignoral it here
                        log.info("dev request {} success",request.getType().toString());
                    }else{
                        if(request.getRetry_times()<1000){
                            dev_rsv_req_queue.offer(request);
                        }
                        if(request.getRetry_times()%100==1){
                            log.error("dev request {} failed:{}",request.getType().toString(),request.getRetry_times());
                        }
                        Thread.sleep(500);
                    }
                    request=null;
                } catch (Exception e) {
                    log.error("error in vmApplyThread: {}", e.getMessage());
                }

            }
        }

        private void handle_init(DeviceType type) {
            List<VirtualMachine> list_ipset = getVmDbItems(
                    VmStatus.IPSET.toString(), type.toString());
            int size= count_STANDBY-list_ipset.size();
            for (int i = 0; i < size; i++) {
                StringBuffer vmid = new StringBuffer();
                int ret = newVm(type, vmid, VmStatus.STANDBY.toString());
                if (0 != ret) {
                    log.error("init applyvm thread error in new {}",
                            type.toString());
                }
            }
        }

        private boolean handle_req(DevRequest request) {
            boolean ret = false;
            StringBuffer vmid = new StringBuffer();
            VirtualMachine vm = null;
            do {
                request.incRetry_times();
                List<VirtualMachine> list_reserved = getVmDbItems(
                        VmStatus.RESERVED.toString(), request.getType().toString());

                while(list_reserved.size() > count_RESERVED){
                    vm=list_reserved.get(0);
                    if (0 != delVm(vm.getVmid())) {
                        log.error("delete vm failed");
                        break;
                    }
                    list_reserved.remove(0);
                }

                if (list_reserved.size() > 0) {
                    vm = list_reserved.get(0);
                    if(!setVmDbStatus(vm.getVmid(), VmStatus.STANDBY.toString())){
                        break;
                    }
                } else {
                    if (0 != newVm(request.getType(), vmid, VmStatus.STANDBY.toString())) {
                        log.error("init vmapply thread error in new vm");
                        break;
                    }
                }
                ret=true;
            } while (false);

            return ret;
        }
    }
    /*
     * vmid,
     * type,
     * ip,
     * status(RESERVED->STANDBY->IPSET->REGED),
     * msg: some info
     * cfg_counter: how many times we tried to configure it
     *
     * RESERVED: stopped or started, may recycle to cloud platform or be used
     * STANDBY: started or to be started, waitting to be ip-configured
     * IPSET:   started, ip was set, waitting to be REGed
     * REGED:  started, ip was set, inuse
     *                    [new vm]=\
     *  [delete devinfo]=>RESERVED->STANDBY->IPSET->REGED
     *
     */
    class VmSetIpThread implements Runnable {


        @Override
        public void run() {
            log.info("VmSetIpThread starting...");
            while (true) {
                try {
                    Thread.sleep(5000);
                    //get VMs that need ip
                    List<VirtualMachine> items = getVmDbItems(VmStatus.STANDBY.toString());
                    if (null == items || items.isEmpty()) {
                        continue;
                    }
                    for (VirtualMachine item : items) {
                        handle_standby_vm(item);
                    }// for

                } catch (Exception e) {
                    log.error("error in vmConfThread: {}", e.getMessage());
                }
            }//while
        }//run()

        private void handle_standby_vm(VirtualMachine vm){
            do {
                if (!incVmDbCfgCounter(vm.getVmid())) {
                    log.error("increaseVmDbIpConfTimes failed in monitor thread");
                }
                int ret = cloudPlugin.getStatus(vm.getVmid());
                if (2 != ret) {
                    log.error("vm is not started in monitor thread[{}]", vm.getVmid());
                    if (0 != cloudPlugin.powerOn(vm.getVmid())) {
                        log.error("start vm failed in monitor thread[{}]", vm.getVmid());
                    }
                    break;
                }
                StringBuffer ip = new StringBuffer();
                ret = cloudPlugin.getIp(vm.getVmid(), ip);
                if (0 != ret ) {
                    log.error("get ip failed in monitor thread");
                    break;
                }

                if (!setVmDbIp(vm.getVmid(), ip.toString())) {
                    log.error("setVmDbIp failed in monitor thread");
                    break;
                }
                if (!setVmDbStatus(vm.getVmid(), VmStatus.IPSET.toString())) {
                    log.error("setVmDbStatus failed:{} in REG vmSetIpThread@cloudMgr",
                            vm.getVmid());
                    break;
                }
            } while (false);
        }

    }
    /*
     * handle the DEV apply request, which means not enough register DEVs
     * work: get a VM, register it
     *
     */
    class DevRegThread implements Runnable {
        private DevRequest request=null;

        @Override
        public void run() {
            log.info("DevRegThread starting...");
            while(true){
                try{

                    if(null == request){
                        request=dev_req_queue.take();
                    }
                    if(handle_req(request)){
                        //set to default
                        log.info("device {} request success",request.getType().toString());
                    }else{
                        if(request.getRetry_times()<1000){
                            //recycle until we have ready vms, and try next request
                            dev_req_queue.offer(request);
                        }
                        if(request.getRetry_times()%100==1){
                            log.warn("device {} request failed {} times",request.getType().toString(),request.getRetry_times());
                        }
                        Thread.sleep(500);
                    }
                    request = null;
                }catch (Exception e) {
                    log.error("error in vmConfThread: {}", e.getMessage());
                }
            }//while


        }
        private boolean handle_req(DevRequest reqest){
            boolean ret=false;
            do {
                //increase the tried number
                request.incRetry_times();
                // get VMs that need to be REGed
                List<VirtualMachine> items = getVmDbItems(
                        VmStatus.IPSET.toString(), request.getType().toString());
                if (null == items || items.isEmpty()) {
                    log.debug("no ip-configured dev available: {}", request.getType().toString());
                    break;
                }

                for(VirtualMachine item:items){
                    // we have IPSET vms
                    if (!incVmDbCfgCounter(item.getVmid())) {
                        log.error("increaseVmDbIpConfTimes failed in DevRegThread thread");
                    }
                    if ("" == item.getIp()) {
                        log.error("ip not found in REG DevRegThread@cloudMgr");
                        continue;
                    }
                    if (!this.devRegister(item.getIp())) {
                        log.error("devRegRpc failed:{} in REG DevRegThread@cloudMgr",
                                item.getVmid());
                        continue;
                    }
                    if (!setVmDbStatus(item.getVmid(), VmStatus.REGED.toString())) {
                        log.error("setVmDbStatus failed:{} in REG DevRegThread@cloudMgr",
                                item.getVmid());
                        continue;
                    }
                    ret=true;
                    break;
                }
            } while (false);
            return ret;
        }
        private boolean devRegister(String devip){
            String myIp="";
            try {
                myIp = getMyIp();
            } catch (Exception e) {
                log.error("can not get my ip");
            }
            String url="http://"+devip+":9999/4sdnapi/register";
            String content="{\"head\":{},\"data\":{\"sc\":\"http://"+ myIp +":8888/sc/devs/info\"}}";
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type","application/json");

            HTTPHelperResult result=null;
            result=HTTPHelper.httpPost(url, content, headers);

            if(200 != result.getCode()){
                log.error("dev_reg FAILED url[{}],rcode:[{}],msg:[{}],content[{}]",url,result.getCode(),result.getMsg(),content);
                return false;
            }
            return true;
        }

    }

    class DevRequest{
        public DevRequest(DeviceType type){
            this.setType(type);
            this.setRetry_times(0);
        }
        public DeviceType getType() {
            return type;
        }
        public void setType(DeviceType type) {
            this.type = type;
        }
        public int getRetry_times() {
            return retry_times;
        }
        public void setRetry_times(int retry_times) {
            this.retry_times = retry_times;
        }
        public void incRetry_times(){
            this.retry_times+=1;
        }
        private DeviceType type;
        private int retry_times;
    }

    //deviceMgr ask cloudMgr to REG more devs
    BlockingQueue<DevRequest> dev_req_queue = new ArrayBlockingQueue<DevRequest>(256);
    //opon each dev_req, we ask the applyvm thread to add som STANDBY ones
    BlockingQueue<DevRequest> dev_rsv_req_queue = new ArrayBlockingQueue<DevRequest>(256);
}
