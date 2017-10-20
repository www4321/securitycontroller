/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud.agent;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.KnowledgeEventArgs;
import com.sds.securitycontroller.event.RequestEventArgs;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.cloud.CloudGateway;
import com.sds.securitycontroller.knowledge.cloud.CloudNetwork;
import com.sds.securitycontroller.knowledge.cloud.CloudPort;
import com.sds.securitycontroller.knowledge.cloud.CloudRouter;
import com.sds.securitycontroller.knowledge.cloud.CloudSubnet;
import com.sds.securitycontroller.knowledge.cloud.CloudTenant;
import com.sds.securitycontroller.knowledge.cloud.CloudUser;
import com.sds.securitycontroller.knowledge.cloud.CloudVM;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.utils.HTTPUtils;

public class OpenstackClient implements ICloudAgentService, ISecurityControllerModule,IEventListener {

    protected IEventManagerService eventManager;
    protected static Logger log = LoggerFactory.getLogger(OpenstackClient.class);
    
//	String regionName="sdsExprRegion";
	String regionName="regionOne";
	String password="sds";//"xd";
	String authUrl="http://controller.research.intra.sds.com:35357/v2.0";
	String quantumUrl="http://controller.research.intra.sds.com:9696/v2.0";
//	String quantumUrl="http://172.16.0.10:9696/v2.0";
	String novaUrl="http://controller.research.intra.sds.com:8774/v2";
	
	public static String cloudAgentCommandUrl = "http://nc.research.intra.sds.com:8081/wm/securitycontrolleragent/policyaction";
	
	String userName="adminUser";//"admin";//updated at 2013-11-14
	String tenantName="admin";//updated at 2013-11-14
	String tenantId = null;
	
	Map<String, CloudTenant> tenantMap;
	Map<String, CloudUser> userMap; 
	Map<String, CloudNetwork> networkMap;
	Map<String, CloudVM> vmMap; 
	Map<String, CloudSubnet> subnetMap; 
	Map<String, CloudPort> portMapping; 
	Map<String, Map<String, String>> tenantMacIPMapping; 
	Map<String, Map<String, String>> tenantIPMacMapping; 
	Map<String, CloudRouter> routerMap; 
	
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
	
	public boolean isTokenExpired(){
		if(this.token == null || this.tokenExpireTime==null)
			return true;
		Date now = new Date();
		if(this.useUTCTime){
			//Convert the date from the  timezone to UTC timezone
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
	
//	class AuthRequestBean{
//		class Auth{
//			String tenantName;
//			class PasswordCredentials{
//				String username;
//				public String getUsername() {
//					return username;
//				}
//				public void setUsername(String username) {
//					this.username = username;
//				}
//				public String getPassword() {
//					return password;
//				}
//				public void setPassword(String password) {
//					this.password = password;
//				}
//				String password;
//			}
//			PasswordCredentials passwordCredentials = new PasswordCredentials();
//
//			public String getTenantName() {
//				return tenantName;
//			}
//			public void setTenantName(String tenantName) {
//				this.tenantName = tenantName;
//			}
//			public PasswordCredentials getPasswordCredentials() {
//				return passwordCredentials;
//			}
//			public void setPasswordCredentials(PasswordCredentials passwordCredentials) {
//				this.passwordCredentials = passwordCredentials;
//			}
//		}
//		Auth auth = new Auth();
//		public Auth getAuth() {
//			return auth;
//		}
//		public void setAuth(Auth auth) {
//			this.auth = auth;
//		}		
//	}
//	
	
	
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
	
	@Override
	public Map<String, CloudNetwork> getNetworks() {
		if(isTokenExpired())
			GetToken();
		log.debug("Getting cloud subnet infomation");
		//query quantum to get subnet infomation
		String url = this.quantumUrl + "/networks";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Auth-Token",this.token);
		String jsonResp = HTTPUtils.httpGet(url, headers);
		if(jsonResp == null) //Error
			return null;
		this.networkMap.clear();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {
			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			JsonNode ns = root.path("networks");
			for(int i=0;i<ns.size();i++){
				JsonNode nw = ns.get(i);
				String status = nw.path("status").asText();
				JsonNode subnetsNode = nw.path("subnets");
				List<String> subnetIDs = new ArrayList<String>();
				for(int j=0;j<subnetsNode.size();j++){
					String id = subnetsNode.get(j).asText();
					subnetIDs.add(id);
				}
				String name = nw.path("name").asText();
				boolean admin_state_up = nw.path("admin_state_up").asBoolean();
				String tenant_id = nw.path("tenant_id").asText();
				String id = nw.path("id").asText();
				boolean shared = nw.path("shared").asBoolean();
				CloudNetwork network = new CloudNetwork(id, name, status, tenant_id, admin_state_up, shared, subnetIDs);
//				network.setType(KnowledgeType.CLOUD_NETWORK);
				networkMap.put(id, network);
			}
		}
		catch (IOException e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		} 
		catch (Exception e) {
			log.error("error when parsing json response: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}  
		
		return networkMap;
	}
	
	//format:http://docs.openstack.org/api/openstack-network/2.0/content/list_subnets.html

	@Override
	public Map<String, CloudSubnet> getSubnets() {
		if(isTokenExpired())
			GetToken();
		log.debug("Getting cloud subnet infomation");
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
//				subnet.setType(KnowledgeType.CLOUD_SUBNET);
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
		log.debug("Getting cloud subnet infomation");
		//query quantum to get subnet infomation
		String url = this.quantumUrl + "/ports";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Auth-Token",this.token);
		String jsonResp = HTTPUtils.httpGet(url, headers);
		
		if(jsonResp == null) //Error
			return null;
//		System.err.println(jsonResp);
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
				port.setType(KnowledgeType.CLOUD_PORT);
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


	List<String> getRouterPortBinding(String routerDeviceID){
		if(isTokenExpired())
			GetToken();
		String url = this.quantumUrl + "/ports.json?device_id="+routerDeviceID;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Auth-Token",this.token);
		String jsonResp = HTTPUtils.httpGet(url, headers);
		//parse router-port mapping
		ObjectMapper mapper = new ObjectMapper();
		List<String> portIdList=new ArrayList<String>();
			
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {
			JsonNode rootNode=mapper.readValue(jsonResp, JsonNode.class);
			JsonNode portsNode = rootNode.path("ports");
			String portID=null;
			for(int i=0;i<portsNode.size();i++){
				JsonNode portNode = portsNode.get(i);
				portID = portNode.path("id").asText();
				if(portID!=null){
					portIdList.add(portID);
				}
			}
			return portIdList;
		}
		catch (IOException e) {
			e.printStackTrace();
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		}
		// 
		
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
		log.debug("Getting cloud tenant infomation");
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
//				tenant.setType(KnowledgeType.CLOUD_TENANT);
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
		log.debug("Getting cloud user infomation");
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
//				System.err.println("name:"+name);
//				System.err.println("id:"+id);
//				System.err.println("tid:"+tenantId);
//				System.err.println(t.toString());
				CloudUser user = new CloudUser(id, name, enabled, tenantId);
//				user.setType(KnowledgeType.CLOUD_USER);
				this.userMap.put(id, user);
			}			
		} catch (IOException e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		} 		
		return this.userMap;
	}
	
	@Override
	public Map<String, CloudRouter> getRouters(){
		if(isTokenExpired())
			GetToken();
		log.debug("Getting cloud user infomation");
		String url = this.quantumUrl + "/routers";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Auth-Token",this.token);
		String jsonResp = HTTPUtils.httpGet(url, headers);
		
		if(jsonResp == null) 
			return null;
		this.routerMap.clear();
		ObjectMapper mapper = new ObjectMapper();
		 mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(Visibility.ANY));
		try {
			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			JsonNode ts = root.path("routers");
			for(int i = 0;i<ts.size();i++){
				JsonNode t = ts.get(i);
				String id = t.path("id").asText();
				String name = t.path("name").asText();
				String status = t.path("status").asText();
				JsonNode externalGatewayInfoNode = t.path("external_gateway_info");
				String network_id = externalGatewayInfoNode.path("network_id").asText();
				CloudGateway externalGatewayInfo = new CloudGateway(network_id);
				boolean admin_state_up = t.path("admin_state_up").asBoolean();
				String tenant_id = t.path("tenant_id").asText();
				
				CloudRouter router = new CloudRouter(id, name, status,tenant_id, admin_state_up, externalGatewayInfo);
//				router.setType(KnowledgeType.CLOUD_ROUTER);
				//TODO add router-
				List<String> routerPortIDList = getRouterPortBinding(router.getId());
				if(routerPortIDList!=null){
					router.setPortIdList(routerPortIDList);
				}
				this.routerMap.put(id, router);
				}			
			} catch (IOException e) {
				log.error("error parsing json response: {}", e.getMessage());
				return null;
			} 		
			return this.routerMap;
	}

	@Override
	public Map<String, CloudVM> getVMs() {
		if(isTokenExpired())
			GetToken();
		log.debug("Getting cloud user infomation");
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
				Iterator<Entry<String, JsonNode>> ns = t.path("addresses").fields();
				CloudVM vm = new CloudVM(id, name, userid, tenantid, networks);
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
        //l.add(IStorageSourceService.class);
        l.add(IRestApiService.class);
        return l;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
        //this.storageSource = context.getServiceImpl(IStorageSourceService.class);
		// read properties from config
        GlobalConfig config = GlobalConfig.getInstance();
		String iaasHost = config.iaasHost;
		if(context.getConfigParams(this).get("authUrl")!=null)
			authUrl= context.getConfigParams(this).get("authUrl").replace("[iaashost]", iaasHost);
		if(context.getConfigParams(this).get("quantumUrl")!=null)
			quantumUrl= context.getConfigParams(this).get("quantumUrl").replace("[iaashost]", iaasHost);
		if(context.getConfigParams(this).get("novaUrl")!=null)
			novaUrl= context.getConfigParams(this).get("novaUrl").replace("[iaashost]", iaasHost);
//		String authUrl "http://controller.research.intra.sds.com:35357/v2.0";
//		String quantumUrl="http://controller.research.intra.sds.com:9696/v2.0";
//		String quantumUrl="http://172.16.0.10:9696/v2.0";
//		String novaUrl="http://controller.research.intra.sds.com:8774/v2";
		if(context.getConfigParams(this).get("cloudAgentCommandUrl")!=null)
			cloudAgentCommandUrl = context.getConfigParams(this).get("cloudAgentCommandUrl").replace("[iaashost]", iaasHost);
		
        this.tenantMap = new HashMap<String, CloudTenant>();
        this.userMap = new HashMap<String, CloudUser>(); 
        this.subnetMap = new HashMap<String, CloudSubnet>(); 
        this.vmMap = new HashMap<String, CloudVM>(); 
        this.portMapping = new HashMap<String, CloudPort>(); 
        this.tenantMacIPMapping = new HashMap<String, Map<String, String>>();
        this.tenantIPMacMapping = new HashMap<String, Map<String, String>>();
        this.routerMap = new HashMap<String, CloudRouter>();
        this.networkMap = new HashMap<String,CloudNetwork>();
        
        this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
        
        if(context != null)
        	this.restApi = context.getServiceImpl(IRestApiService.class);
		getMacIPMapping();
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
        restApi.addRestletRoutable(new CloudAgentRoutable());
		//this.storageSource.setTablePrimaryKeyName(this.tenantTableName, this.tenantPrimaryKeyName);
		//this.storageSource.setTablePrimaryKeyName(this.userTableName, this.userPrimaryKeyName);
		//this.storageSource.setTablePrimaryKeyName(this.subnetTableName, this.subnetPrimaryKeyName);
        
        //add event listener
        eventManager.addEventListener(EventType.REQUEST_KNOWLEDGE, this);
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
		OpenstackClient client = new OpenstackClient();
		client.init(null);
		client.getRouters();
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

	/**
	 * get 
	 */
	@Override
	public Map<String, CloudPort> getPorts() {
		return this.getMacIPMapping();
	}

	@Override
	public void processEvent(Event e) {
		// TODO handle with cloud info retrieving request
		if(e.type!=EventType.REQUEST_KNOWLEDGE){
			log.debug(" cloud agent does not know how to deal with event of {} type",e.type);
			return;
		}
		KnowledgeType entityType = ((RequestEventArgs)e.args).entityType;	
		InfoRetrievingThread thread = new InfoRetrievingThread(entityType);
		new Thread(thread).start();
		
	}

	/**
	 * retrieve info async
	 * @author Administrator
	 *
	 */
	class InfoRetrievingThread implements Runnable{
		public KnowledgeType entityType;
		public InfoRetrievingThread(KnowledgeType infoType){
			this.entityType = infoType;
		}
		@Override
		public void run() {
			// TODO 
			Object infoObj = null;
			log.debug(" start retrieving cloud info, type:{}",entityType);
			if (entityType == KnowledgeType.CLOUD_PORT)
				infoObj = getPorts();
			else if (entityType == KnowledgeType.CLOUD_ROUTER)
				infoObj = getRouters();
			else if (entityType == KnowledgeType.CLOUD_VM)
				infoObj = getVMs();
			else if (entityType == KnowledgeType.CLOUD_SUBNET)
				infoObj = getSubnets();
			else if (entityType == KnowledgeType.CLOUD_TENANT)
				infoObj = getTenants();
			else if (entityType == KnowledgeType.CLOUD_USER)
				infoObj = getUsers();
			else if (entityType == KnowledgeType.CLOUD_NETWORK)
				infoObj = getNetworks();
			else{
				return;
			}
			log.debug(" get cloud info of {} type",entityType);
			Event e = new Event(EventType.RETRIEVED_KNOWLEDGE, null,this,new KnowledgeEventArgs(entityType,infoObj));
			eventManager.addEvent(e);
		}
	}
	
	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		
	}

	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		
	}
}
