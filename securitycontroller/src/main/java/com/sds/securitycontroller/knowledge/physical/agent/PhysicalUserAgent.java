package com.sds.securitycontroller.knowledge.physical.agent;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.knowledge.cloud.agent.AuthRequestBean;
//import com.sds.securitycontroller.knowledge.cloud.agent.OpenstackClient.AuthRequestBean;
import com.sds.securitycontroller.knowledge.physical.PhysicalUser;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.utils.HTTPUtils;

public class PhysicalUserAgent implements IPhysicalAgentService,ISecurityControllerModule,IEventListener {

	String userMngmServiceHost = "http://admin.byod.research.intra.sds.com";
	String userMngmUri = "/sessions/";
	String token = null;
	Date tokenExpireTime = null;
	
	Map<String,PhysicalUser> userMap=new HashMap<String, PhysicalUser>();
	
	protected IEventManagerService eventManager;
	protected static Logger log = LoggerFactory.getLogger(PhysicalUser.class);
	String authUrl="http://controller.research.intra.sds.com:35357/v2.0";
	
	String userName="adminUser";///updated at 2013-11-14
	String password="sds";//"xd";
	String tenantName="admin";//updated at 2013-11-14
	String tenantId = null;
	
	@Override
	public Map<String,PhysicalUser> getAuthenticationUsers() {
		if(isTokenExpired())
			GetToken();
		
		String url = userMngmServiceHost + userMngmUri;
		if(token==null || userName==null){
			log.error("Failed to get token.");
			return null;
		}
		url += "?username="+userName+"&token="+token;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		String jsonResp = HTTPUtils.httpGet(url, headers);
		if(jsonResp == null) //Error
			return null;
		this.userMap.clear();
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			for(int i = 0;i<root.size();i++){
				JsonNode userBindingNode = root.get(i);
				String username = userBindingNode.path("username").asText();
				String status = userBindingNode.path("status").asText();
				String mac = userBindingNode.path("mac_address").asText();
				PhysicalUser user = null;
				if(userMap.get(username)==null){
					//add user
					user = new PhysicalUser(username,(status.equals("online")), "byodadmin",status);
					userMap.put(username, user);
				}
				else{
					// add binding mac
					user = userMap.get(username);
				}
				user.addBindingMac(mac);
				
			}
		} catch (IOException e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		}  
		return this.userMap;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services =
                new ArrayList<Class<? extends ISecurityControllerService>>(1);
        services.add(IPhysicalAgentService.class);
        return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
        // We are the class that implements the service
        m.put(IPhysicalAgentService.class, this);
        return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		return null;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		log.info("AuthenticationUserAgent initializing...");
		userMngmServiceHost = context.getConfigParams(this).get("userManagerHost");
		userMngmUri = context.getConfigParams(this).get("userManagerUri");
		this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		//add event listener
        eventManager.addEventListener(EventType.REQUEST_KNOWLEDGE, this);
	}

	@Override
	public void processEvent(Event e) {
		if(e.type!=EventType.REQUEST_KNOWLEDGE){
			log.debug(" user management agent does not know how to deal with event of {} type",e.type);
			return;
		}
		/*
		KnowledgeType entityType = ((RequestEventArgs)e.args).entityType;
		if(entityType==KnowledgeType.PHYSICAL_USER){
			log.debug(" user management agent is returning {} knowledge entity.",entityType);
			Object infoObj = (Object)getAuthenticationUsers();
			Event event = new Event(EventType.RETRIEVED_KNOWLEDGE, null,this,new KnowledgeEventArgs(entityType,infoObj));
			eventManager.addEvent(event);
			return;
		}*/		
	}

	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		
	}

	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		
	}
	
	private boolean GetToken() {
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
	
	public boolean isTokenExpired(){
		if(this.token == null || this.tokenExpireTime==null)
			return true;
		Date now = new Date();
		boolean useUTCTime = true;
		if(useUTCTime){
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
	
	public static void main(String [] args) throws SecurityControllerModuleException{
		PhysicalUserAgent aua = new PhysicalUserAgent();
		Object infoObj = aua.getAuthenticationUsers();
		System.out.println(infoObj);
	}
}
