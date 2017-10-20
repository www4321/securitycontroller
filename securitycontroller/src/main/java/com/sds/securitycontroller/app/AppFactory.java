/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app;

import java.util.HashMap;
import java.util.Map;
 
import com.fasterxml.jackson.databind.JsonNode;


public class AppFactory {
	
	public enum AppType {
		ADS,
		WAF,
		IDS,
		IPS,
		L3,
		DHCP,
		//TODO
		ANALYSER,
		UNKNONW
	}
	

	public enum AppCategory {
		SECURITY_APP,
		NETWORK_APP,
		UNKNOWN
	}

	protected static Map<AppType, AppCategory> deviceTypeMapping;

	protected static void initAppTypeMapping(){
		if(deviceTypeMapping != null)
			return;
		deviceTypeMapping = new HashMap<AppType, AppCategory>();
		deviceTypeMapping.put(AppType.ADS, AppCategory.SECURITY_APP);
		deviceTypeMapping.put(AppType.WAF, AppCategory.SECURITY_APP);
		deviceTypeMapping.put(AppType.IDS, AppCategory.SECURITY_APP);
		deviceTypeMapping.put(AppType.IPS, AppCategory.SECURITY_APP);
		deviceTypeMapping.put(AppType.ANALYSER, AppCategory.SECURITY_APP);
		
		deviceTypeMapping.put(AppType.L3, AppCategory.NETWORK_APP);
		deviceTypeMapping.put(AppType.DHCP, AppCategory.NETWORK_APP);
	}
	
	public static App createApp(App app){
		if(null == app || null == app.category){
			return null;
		}
		switch(app.category){
			case NETWORK_APP:
				return new NetworkApp(app);
			case SECURITY_APP:
				return new SecurityApp(app);
			default:
				return null;
		}
		
	}
	

	public static App createApp(JsonNode jn) throws Exception {		
		if(deviceTypeMapping == null){
			initAppTypeMapping();
		}
		
		if (!jn.has("guid")) {
			throw new Exception("App guid missing");
		}
		if(jn.path("guid").asText().isEmpty()){
			throw new Exception("App guid must be set");
		}
		if (!jn.has("hash")) {
			throw new Exception("App hash missing");
		}
		if (!jn.has("name")) {
			throw new Exception("App name missing");
		}
		if (!jn.has("version")) {
			throw new Exception("App version missing");
		}
		if (!jn.has("hash")) {
			throw new Exception("App hash missing");
		}
		if (!jn.has("host")) {
			throw new Exception("App host missing");
		}
		if (!jn.has("port")) {
			throw new Exception("App port missing");
		}
		if (!jn.has("root_url")) {
			throw new Exception("App root_url missing");
		}
		if (!jn.has("manage_url")) {
			throw new Exception("App manage_url missing");
		}
		if (!jn.has("protocol")) {
			throw new Exception("App protocol missing");
		}
		if (!jn.has("type")) {
			throw new Exception("App type missing");
		}
		
		String guid 		= jn.path("guid").asText();
		String hash 		= jn.path("hash").asText().toUpperCase();
		String name 		= jn.path("name").asText();		
		String version 		= jn.path("version").asText();
		String host 		= jn.path("host").asText();		
		int    port 		= jn.path("port").asInt();
		String root_url 	= jn.path("root_url").asText();
		String manage_url 	= jn.path("manage_url").asText();
		String protocol 	= jn.path("protocol").asText().toUpperCase();
		AppType type 		= AppType.valueOf(jn.path("type").asText().toUpperCase());
		AppCategory category = deviceTypeMapping.get(type);

		return createApp(guid,hash,name,version,host,port,root_url,manage_url,protocol,type, category);
	}
	
	public static App createApp(
			String guid,
			String hash,				
			String name,
			String version,
			String host,
			int    port,
			String root_url,
			String manage_url,
			String protocol,
			AppType type,
			AppCategory category	
			)throws Exception {

		if (category == AppCategory.NETWORK_APP) {
			return new NetworkApp(guid,hash,name,version,host,port,root_url,manage_url,protocol,type, category);
		} else if (category == AppCategory.SECURITY_APP) {
			return new SecurityApp(guid,hash,name,version,host,port,root_url,manage_url,protocol,type, category);
		} else
			throw new Exception("App type unknown:" + type);		
	}
}