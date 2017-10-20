/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.event.manager.IEventManagerService;

public class DeviceFactory {

	public enum DeviceType {
		ADS,
		WAF,
		WVSS,
		IDS,
		IPS,
		L3,
		DHCP,
		BYOD,
		USER_DEVICE,	//VM
		UNKNOWN
	}
	

	public enum DeviceCategory {
		SECURITY_DEVICE,
		NETWORK_DEVICE,
		UNKNOWN
	}

	protected static Map<DeviceType, DeviceCategory> deviceTypeMapping;

	protected static void initDeviceTypeMapping(){
		if(deviceTypeMapping != null)
			return;
		deviceTypeMapping = new HashMap<DeviceType, DeviceCategory>();
		deviceTypeMapping.put(DeviceType.ADS, DeviceCategory.SECURITY_DEVICE);
		deviceTypeMapping.put(DeviceType.WAF, DeviceCategory.SECURITY_DEVICE);
		deviceTypeMapping.put(DeviceType.IDS, DeviceCategory.SECURITY_DEVICE);
		deviceTypeMapping.put(DeviceType.IPS, DeviceCategory.SECURITY_DEVICE);
		deviceTypeMapping.put(DeviceType.L3, DeviceCategory.NETWORK_DEVICE);
		deviceTypeMapping.put(DeviceType.DHCP, DeviceCategory.NETWORK_DEVICE);
	}

	public static Device createDevice(JsonNode jn, IEventManagerService eventManager) throws Exception{
		if(deviceTypeMapping == null){
			initDeviceTypeMapping();
		}
		
		if (!jn.has("name")) {
			throw new Exception("dev name missing");
		}
		if (!jn.has("hash")) {
			throw new Exception("dev hash missing");
		}		 
		if (!jn.has("api_ver")) {
			throw new Exception("dev api_ver missing");
		}
		if (!jn.has("rule_ver")) {
			throw new Exception("dev rule_ver missing");
		}
		if (!jn.has("host")) {
			throw new Exception("dev host missing");
		}
		if (!jn.has("port")) {
			throw new Exception("dev port missing");
		}
		if (!jn.has("protocol")) {
			throw new Exception("dev protocol missing");
		}
		if (!jn.has("root_url")) {
			throw new Exception("dev root_url missing");
		}
		if (!jn.has("manage_url")) {
			throw new Exception("dev manage_url missing");
		}		
		if (!jn.has("license")) {
			throw new Exception("dev license missing");
		}		 	
		if (!jn.has("type")) {
			throw new Exception("dev type missing");
		}
		if (!jn.has("mac_addrs")) {
			throw new Exception("App mac_addrs missing");
		}
		if (!jn.path("mac_addrs").isArray()) {
			throw new Exception("App mac_addrs missing");
		}		
		if (!jn.has("service")) {
			throw new Exception("App mac_addrs missing");
		}
		if (!jn.path("service").isArray()) {
			throw new Exception("App mac_addrs missing");
		}
		
		String name 		= jn.path("name").asText();	
		String hash 		= jn.path("hash").asText().toUpperCase();		 
		String api_ver 		= jn.path("api_ver").asText();
		String rule_ver		= jn.path("rule_ver").asText();
		String host 		= jn.path("host").asText();		
		int    port 		= jn.path("port").asInt();
		String protocol 	= jn.path("protocol").asText().toLowerCase();
		String root_url 	= jn.path("root_url").asText();
		String manage_url 	= jn.path("manage_url").asText();
		String license 		= jn.path("license").asText();			 
		DeviceType type 	= DeviceType.valueOf(jn.path("type").asText().toUpperCase());
		DeviceCategory category = deviceTypeMapping.get(type);		
		//macs
		Iterator<JsonNode> it=jn.path("mac_addrs").iterator();		
		ArrayList<String> mac_addrs = new ArrayList<String>();
		while(it.hasNext())
		{
			mac_addrs.add(it.next().asText());
		}
		//service
		it=jn.path("service").iterator();		
		ArrayList<String> service = new ArrayList<String>();
		while(it.hasNext())
		{
			service.add(it.next().asText());
		}
	
		//when register, we check if it's an vm, if yes, set the vmid
		Object[] rqArgs = {host.trim()};
		Object result = eventManager.makeRPCCall(com.sds.securitycontroller.cloud.manager.ICloudAgentService.class, "getvmidbyip", rqArgs);
		String vmid="";
		if(result != null){
			vmid = (String)result; 
		}
		
			
		return createDevice(vmid,name, hash, api_ver, rule_ver, host, port,
				protocol, root_url, manage_url,	license, type,
				category, mac_addrs, service);
	} 
	
	
	//23 fields
	public static Device createDevice(
			String vmid,
    		String name,   	
    		String hash,    		 
    		String api_ver,
    		String rule_ver,
    		String host,
    		int	port,
    		String protocol,
    		String root_url,  
    		String manage_url,
    		String license,
    		DeviceType type,
    		DeviceCategory category,
    		List<String> mac_addrs,
    		List<String> service
   		 ) throws Exception{
		return new Device(
				vmid,
				name,    	   	
    	   		hash, 
    	   		api_ver,
    	   		rule_ver,
    	   		host,
    	   		port,
    	   		protocol,
    	   		root_url,    	   	 
    	   		manage_url,
    	   		license,    			 
    	   		type,
    	   		category,
    	   		mac_addrs,
    	   		service);
	}
}
