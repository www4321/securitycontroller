/** 
 *    Copyright 2014 BUPT. 
 **/
package com.sds.securitycontroller.device;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.annotations.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.device.DeviceFactory.DeviceCategory;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class Device implements IDBObject {

	private static final long serialVersionUID = 4867369264367828804L;
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static Map<String, Method> dbFieldMapping;
	protected static Logger log = LoggerFactory.getLogger(Device.class);

	@Id
	protected String id = "";
	private String vmid = "";
	private String name = "";
	private String alias = "";
	private String hash = "";
	private String api_ver = "";
	private String rule_ver = "";
	private String ip = "";
	private int port = 0;
	private String protocol = "";
	private String root_url = "";
	private boolean enable = false;
	private long reg_time = 0;
	private String manage_url = "";
	private String license = "";
	private boolean busy = false;

	private DeviceType type = DeviceType.UNKNOWN;
	protected DeviceCategory category = DeviceCategory.UNKNOWN;
	private List<String> mac_addrs = new ArrayList<String>();
	private List<String> service = new ArrayList<String>();

	private boolean connected = false;
	protected List<String> attachedTaps;
	protected boolean attached = false;

	public Device(){		
	}
	
	public Device(String name) {
		this.name = name;
	}

	// 14 paras, for DeviceFactory.createdevice(), id, reg_time, enable are
	// ignored
	public Device(String vmid, String name, String hash, String api_ver,
			String rule_ver, String ip, int port, String protocol,
			String root_url, String manage_url, String license,
			DeviceType type, DeviceCategory category, List<String> mac_addrs,
			List<String> service) {
		this("", vmid, name, "", hash, api_ver, rule_ver, ip, port, protocol,
				root_url, true, System.currentTimeMillis() / 1000L,
				manage_url, license, false, type, category, Device
						.arrayToString(mac_addrs), Device
						.arrayToString(service));
	}

	// 19 paras, for DB constructor
	public Device(String id, String vmid, String name, String alias,
			String hash, String api_ver, String rule_ver, String ip, int port,
			String protocol, String root_url, boolean enable, long reg_time,
			String manage_url, String license, boolean busy, DeviceType type,
			DeviceCategory category, String mac_addrs, // here
			String service // here
	) {
		this.setId(id);
		this.setVmid(vmid);
		this.name = name;
		this.alias = alias;
		this.setHash(hash);
		this.setApi_ver(api_ver);
		this.setRule_ver(rule_ver);
		this.ip = ip;
		this.setPort(port);
		this.setProtocol(protocol);
		this.setRoot_url(root_url);
		this.setEnable(enable);
		this.setReg_time(reg_time);
		this.manage_url = manage_url;
		this.license = license;
		this.busy = busy;
		this.type = type;
		this.setCategory(category);
		this.setMac_addrs(mac_addrs);
		this.setService(service);
	}
	

    @Override
	@JsonIgnore 
    public Map<String,Object> getDBElements(){
    	Map<String,Object> map = new HashMap<String,Object>();    	
    	map.put("id",			this.getId());
    	map.put("vmid",		this.getVmid());
    	map.put("name",			this.name);	
    	map.put("alias",		this.alias);
    	map.put("hash",			this.getHash());
    	map.put("api_ver",		this.getApi_ver());
    	map.put("rule_ver",		this.getRule_ver());
    	map.put("ip",			this.ip);
    	map.put("port",			this.getPort());
    	map.put("protocol",		this.getProtocol());
    	map.put("root_url",		this.getRoot_url());
    	map.put("enable",		this.getEnable());
    	map.put("reg_time",		this.getReg_time());
    	map.put("manage_url",	this.manage_url); 
    	map.put("license",		this.license);
    	map.put("busy",			this.busy);
    	map.put("type",			this.type.toString());
    	map.put("category",		this.getCategory().toString());
    	map.put("mac_addrs",	this.getMacString());    	
    	map.put("service",	this.getServiceString()); 
		map.put("enable", this.enable);
    	return map;
    }

	//db column to object fields
	@Override
	@JsonIgnore 
	public Object getFieldValueByKey(String key){
		if(dbFieldMapping == null){
			dbFieldMapping = new HashMap<String, Method>();
		    Class<? extends Device> cla=this.getClass();		    
			try {
				dbFieldMapping.put("id", 			cla.getDeclaredMethod("id"));
				dbFieldMapping.put("vmid", 			cla.getDeclaredMethod("vmid"));
				dbFieldMapping.put("name", 			cla.getDeclaredMethod("name"));	
				dbFieldMapping.put("alias", 		cla.getDeclaredMethod("alias"));				
				dbFieldMapping.put("hash", 			cla.getDeclaredMethod("hash"));
				dbFieldMapping.put("api_ver", 		cla.getDeclaredMethod("api_ver"));
				dbFieldMapping.put("rule_ver", 		cla.getDeclaredMethod("rule_ver"));
				dbFieldMapping.put("ip", 			cla.getDeclaredMethod("ip"));
				dbFieldMapping.put("port", 			cla.getDeclaredMethod("port"));
				dbFieldMapping.put("protocol", 		cla.getDeclaredMethod("protocol"));
				dbFieldMapping.put("root_url", 		cla.getDeclaredMethod("root_url"));
				dbFieldMapping.put("enable", 		cla.getDeclaredMethod("enable"));
				dbFieldMapping.put("reg_time", 		cla.getDeclaredMethod("reg_time"));
				dbFieldMapping.put("manage_url", 	cla.getDeclaredMethod("manage_url"));
				dbFieldMapping.put("license", 		cla.getDeclaredMethod("license")); 
				dbFieldMapping.put("busy", 			cla.getDeclaredMethod("busy")); 
				dbFieldMapping.put("type", 			cla.getDeclaredMethod("type"));
				dbFieldMapping.put("category", 		cla.getDeclaredMethod("category"));
				dbFieldMapping.put("mac_addrs", 	cla.getDeclaredMethod("getMacString"));
				dbFieldMapping.put("service",	 	cla.getDeclaredMethod("getServiceString"));
				
				 
			} catch (NoSuchMethodException | SecurityException e) {
			    log.error("getFieldValueByKeys error: "+e.getMessage());
				return null;
			}
		}
		Method m = dbFieldMapping.get(key);		    
		try { 
			return m.invoke(this, new Object[0]);
		}catch(Exception e){
			log.error("getFieldValueByKeys error: "+e.getMessage());
			return null;
		}
	}
	

	//string to object fields
	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {
		return new Device(
				resultSet.getString("id"),
				resultSet.getString("vmid"),
				resultSet.getString("name"),	
				resultSet.getString("alias"),
				resultSet.getString("hash"),
				resultSet.getString("api_ver"),
				resultSet.getString("rule_ver"),
				resultSet.getString("ip"),
				resultSet.getInt("port"),
				resultSet.getString("protocol"),
				resultSet.getString("root_url"),
				resultSet.getBoolean("enable"),
		   		resultSet.getInt("reg_time"),
		   		resultSet.getString("manage_url"),
		   		resultSet.getString("license"),
		   		resultSet.getBoolean("busy"),
		   		DeviceType.valueOf(resultSet.getString("type")),
		   		DeviceCategory.valueOf(resultSet.getString("category")),
		   		resultSet.getString("mac_addrs"),
		   		resultSet.getString("service")
		    	);
	} 
	
	

	/*
	 * 对本设备所连接哪个 交换机端口(tap)的获取，不应该从设备上取得，而应该某hapervisor之类的管理器取得
	 */
	@JsonIgnore 
	public List<String> getAttachedTaps(){
		return this.attachedTaps;
		
	}
	

	public void setAttachedTaps(List<String> list){
		this.attachedTaps = list;
		
	}
	
	/*
	 * cpu,mem,disk should not be changed
	 */
	public void updateInfo(JsonNode jn) {
		if(jn.has("alias")){
			this.alias = jn.path("alias").asText();
		}
		if(jn.has("api_ver")){
			this.setApi_ver(jn.path("api_ver").asText());
		}
		if(jn.has("rule_ver")){
			this.setRule_ver(jn.path("rule_ver").asText());
		}
		if(jn.has("hash")){
			this.setHash(jn.path("hash").asText());
		}
		if(jn.has("ip")){
			this.ip = jn.path("ip").asText();
		}
		if(jn.has("port")){
			this.setPort(jn.path("port").asInt());
		}
		if(jn.has("root_url")){
			this.setRoot_url(jn.path("root_url").asText());
		}
		if(jn.has("protocol")){
			this.setProtocol(jn.path("protocol").asText());
		} 
		if(jn.has("manage_url")){
			this.manage_url = jn.path("manage_url").asText();
		} 
		if(jn.has("license")){
			this.license = jn.path("license").asText();
		}
	}
	
	 //fields
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
	
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
 	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public String getApi_ver() {
		return api_ver;
	}
	public void setApi_ver(String api_ver) {
		this.api_ver = api_ver;
	}
    public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		if (0 == this.port ) {
			if("HTTPS" == this.getProtocol()){
				return 443;
			}else{
				return 80;
			}
		}
		return this.port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getRoot_url() {
		return root_url;
	}
	public void setRoot_url(String root_url) {
		if(null != root_url){
			this.root_url=root_url.endsWith("/")?root_url:root_url+"/";
		}
	}

	public boolean getEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public long getReg_time() {
		return reg_time;
	}
	public void setReg_time(long reg_time) {
		this.reg_time = reg_time;
	}
	public String getManage_url() {
		return manage_url;
	}
	public void setManage_url(String manage_url) {
		this.manage_url = manage_url;
	}
	
	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}		
	public DeviceType getType() {
		return type;
	} 
	public void setType(DeviceType type) {
		this.type = type;
	}
	public DeviceCategory getCategory() {
		return category;
	}
	public void setCategory(DeviceCategory category) {
		this.category = category;
	}
	
	//service
	public List<String> getService() {
		return service;
	}
	public void setService(List<String> service) {
		this.service = service;
	}
	public void setService(String service) {	
		if(null == service || service.isEmpty()){
			this.service.clear();
		}
		String[] macs = service.split(",");		
		for(int i=0; i<macs.length; i++){
			this.service.add(macs[i]);
		}
	}
	@JsonIgnore 
	public String getServiceString() {
		String str="";
		for(String s:this.service){
			str+=s+",";
		}
		if(str.isEmpty()){
			return str;
		}
		//remove the last ','
		return str.substring(0,str.length()-1);		
	}
	
	//macs
	public List<String> getMac_addrs() {
		return this.mac_addrs;
	}
	public void setMac_addrs(List<String> mac_addrs) {
		this.mac_addrs = mac_addrs;				
	}		
	public void setMac_addrs(String mac_addrs) {	
		if(null == mac_addrs || mac_addrs.isEmpty()){
			this.mac_addrs.clear();
		}
		String[] macs = mac_addrs.split(",");		
		for(int i=0; i<macs.length; i++){
			this.mac_addrs.add(macs[i]);
		}
	}
	
	@JsonIgnore 
	public String getMacString() {
		String str="";
		for(String s:this.mac_addrs){
			str+=s+",";
		}
		if(str.isEmpty()){
			return str;
		}
		//remove the last ','
		return str.substring(0,str.length()-1);		
	} 
	
	//utilities
	public static String arrayToString(List<String> list){
		String str="";
		for(String s:list){
			str+=s+",";
		}
		if(str.isEmpty()){
			return str;
		}
		return str.substring(0,str.length()-1);	
	}
	public String getVmid() {
		return this.vmid;
	}
	public void setVmid(String vmid) {
		this.vmid = vmid;
	}
	public boolean isBusy() {
		return busy;
	}
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	public String getRule_ver() {
		return rule_ver;
	}
	public void setRule_ver(String rule_ver) {
		this.rule_ver = rule_ver;
	}
		

	/**
	 * Check if the device is still active;
	 * 
	 * @return whether the device is still active
	 */
	public boolean isEnable() {
		return this.enable;
	}

	public boolean isAttached() {
		return this.attached;
	}

	public void setAttached(boolean attached) {
		this.attached = attached;
	}



	/**
	 * Check if the device is still connected; Only call while holding
	 * processMessageLock
	 * 
	 * @return whether the device is still disconnected
	 */
	public boolean isConnected() {
		return this.connected;
	}

	/**
	 * Set whether the device is connected Only call while holding
	 * modifySwitchLock
	 * 
	 * @param connected
	 *            whether the device is connected
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}


	@Override
	public String toString() {
		return this.category + "\t" + this.id;
	}

}
