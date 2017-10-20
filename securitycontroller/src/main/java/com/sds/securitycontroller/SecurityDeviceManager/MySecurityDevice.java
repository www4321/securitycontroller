package com.sds.securitycontroller.SecurityDeviceManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class MySecurityDevice  {
	private String engineId;
	private String ip;
	private String name;
	private boolean isalive;
	private String type;
	private String factory;
	private String disk;
	private String memory;
	private String engineLocation;
	private float load;
	private double get_speedmax;
	private double send_speedmax;
	private int cpu_usagey;
	private int memory_usage;
	private int disk_usage;
	private int get_speed;
	private int send_speed;

	protected static Map<String, Method> dbFieldMapping;
	private String userName;
	private String passwd;

	public MySecurityDevice(String ip){
		this.ip=ip;
	}
	
//	public ScanDevice(String ip,String username,String passwd){
//		this.engineId=ip;
//		this.userName=username;
//		this.passwd=passwd;
//	}

	public MySecurityDevice(String name,String ip,String type){
		this.name=name;
		this.ip=ip;
		this.type=type;

	}
	
	public float getLoad() {
		return load;
	}

	public void setLoad(float load) {
		this.load = load;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEngineId() {
		return engineId;
	}
	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}
	public String getFactory() {
		return factory;
	}
	public void setFactory(String factory) {
		this.factory = factory;
	}
	public String getEngineLocation() {
		return engineLocation;
	}
	public void setEngineLocation(String engineLocation) {
		this.engineLocation = engineLocation;
	}
	public double getGet_speedmax() {
		return get_speedmax;
	}
	public void setGet_speedmax(float get_speedmax) {
		this.get_speedmax = get_speedmax;
	}
	public double getSend_speedmax() {
		return send_speedmax;
	}
	public void setSend_speedmax(float send_speedmax) {
		this.send_speedmax = send_speedmax;
	}
	public int getCpu_usagey() {
		return cpu_usagey;
	}
	public void setCpu_usagey(int cpu_usagey) {
		this.cpu_usagey = cpu_usagey;
	}
	public int getMemory_usage() {
		return memory_usage;
	}
	public void setMemory_usage(int memory_usage) {
		this.memory_usage = memory_usage;
	}
	public int getDisk_usage() {
		return disk_usage;
	}
	public void setDisk_usage(int disk_usage) {
		this.disk_usage = disk_usage;
	}
	public int getGet_speed() {
		return get_speed;
	}
	public void setGet_speed(int get_speed) {
		this.get_speed = get_speed;
	}
	public int getSend_speed() {
		return send_speed;
	}
	public void setSend_speed(int send_speed) {
		this.send_speed = send_speed;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisk() {
		return disk;
	}

	public void setDisk(String disk) {
		this.disk = disk;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public Map<String, Object> getDBElements() {
		// TODO Auto-generated method stub
    	Map<String,Object> map = new HashMap<String,Object>();    	
 
    	map.put("type",		this.getType());
    	map.put("engineLocation",			this.getEngineLocation());	
    	map.put("get_speedmax",		this.getSend_speedmax());
    	map.put("send_speedmax",			this.getSend_speedmax());
    	map.put("cpu_usagey",		this.getCpu_usagey());
    	map.put("memory_usage",		this.getMemory_usage());
    	map.put("disk_usage",			this.getDisk_usage());
    	map.put("get_speed",			this.getSend_speed());
    	map.put("send_speed",		this.getSend_speed());
    	map.put("ip",		this.getIp());
    	map.put("disk",		this.getDisk());
    	map.put("memory",		this.getMemory());
 

    	return map;
	}
	public Object getFieldValueByKey(String key) {
		// TODO Auto-generated method stub
		if(dbFieldMapping == null){
			dbFieldMapping = new HashMap<String, Method>();
		    Class<? extends MySecurityDevice> cla=this.getClass();		    
			try {
				dbFieldMapping.put("engineId", 			cla.getDeclaredMethod("engineId"));
				dbFieldMapping.put("type", 			cla.getDeclaredMethod("type"));
				dbFieldMapping.put("engineLocation", 			cla.getDeclaredMethod("engineLocation"));	
				dbFieldMapping.put("get_speedmax", 		cla.getDeclaredMethod("get_speedmax"));				
				dbFieldMapping.put("send_speedmax", 			cla.getDeclaredMethod("send_speedmax"));
				dbFieldMapping.put("cpu_usagey", 		cla.getDeclaredMethod("cpu_usagey"));
				dbFieldMapping.put("memory_usage", 		cla.getDeclaredMethod("memory_usage"));
				dbFieldMapping.put("disk_usage", 			cla.getDeclaredMethod("disk_usage"));
				dbFieldMapping.put("get_speed", 			cla.getDeclaredMethod("get_speed"));
				dbFieldMapping.put("send_speed", 		cla.getDeclaredMethod("send_speed"));
				dbFieldMapping.put("myOrdeList", 		cla.getDeclaredMethod("myOrdeList"));
				dbFieldMapping.put("name", 		cla.getDeclaredMethod("name"));
				dbFieldMapping.put("disk", 		cla.getDeclaredMethod("disk"));
				dbFieldMapping.put("memory", 		cla.getDeclaredMethod("memory"));
	
				
				
				 
			} catch (NoSuchMethodException | SecurityException e) {
			    //log.error("getFieldValueByKeys error: "+e.getMessage());
				return null;
			}
		}
		Method m = dbFieldMapping.get(key);		    
		try { 
			return m.invoke(this, new Object[0]);
		}catch(Exception e){
			//log.error("getFieldValueByKeys error: "+e.getMessage());
			return null;
		}
	}
//	@Override
//	public IDBObject mapRow(IAbstractResultSet resultSet) {
//		// TODO Auto-generated method stub
//		return new ScanDevice(
//				resultSet.getString("engineId"),
//				resultSet.getString("type"),	
//				resultSet.getString("factory"),
//				resultSet.getString("engineLocation"),	
//				resultSet.getString("get_speedmax"),
//				resultSet.getString("send_speedmax"),				
//				resultSet.getString("cpu_usagey"),
//				resultSet.getString("memory_usage"),				
//				resultSet.getString("disk_usage"),
//				resultSet.getString("get_speed"),
//				resultSet.getString("send_speed"),			
//				resultSet.getString("myOrdeList")													   		
//		    	);
//	}
//	public String toString() {
//	    return 
//	            "{"+
//	    		"id='" +id+ '\''+","
//	            +"TargetURL='" +targetURL+ '\''+","
//	            +"scan_depth='" +scan_depth+ '\''+","
//	            +"max_page='" +maxpage+ '\''+","
//	            +"TimeSpace='" +timeSpace+ '\''+"\r"
//	            +"parallel_count='" +parallel_count+'\''+","
//	            +"factory='" +factory+ '\''+","
//	    		+"startedTime='" +startedTime+ '\''+","
//	    		+"finishedTime='" +finishedTime+ '\''+","
//	    		+"status='" +status+ '\''+","
//	            +"TargetURL='" +targetURL+ '\''+","
//	            +"type='" +type+ '\''+
//	            '}';
//	}

}
