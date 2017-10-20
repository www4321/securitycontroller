package com.sds.securitycontroller.cloud;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class VirtualMachine  implements IDBObject, Serializable{ 
	private static final long serialVersionUID = -7698600892410797224L;
	protected static Logger log = LoggerFactory.getLogger(VirtualMachine.class);
	protected static Map<String, Method> dbFieldMapping;   
	
 
	
	
	private String vmid=null;
	private String type=null;
	private String ip=null; 
	private String status=null;
	private String msg=null;
	private int cfg_counter=0;
	
	//constructor
	public VirtualMachine(){
		this.vmid="";
		this.type="";
		this.ip="";
		this.status="";
		this.msg="init";
		this.cfg_counter=0;
	}
	public VirtualMachine(String vmid, String type, String ip, String status, String msg, int ip_conf_times){
		this.setVmid(vmid);
		this.setType(type);
		this.setIp(ip); 
		this.setStatus(status);
		this.setMsg(msg);
		this.setCfg_counter(ip_conf_times);
	}
		
	@Override
    @JsonIgnore 
	public Map<String, Object> getDBElements() {
		Map<String,Object> map = new HashMap<String,Object>();
    	map.put("vmid", 		this.getVmid());
    	map.put("type", 		this.getType());
    	map.put("ip", 			this.getIp()); 
    	map.put("status", 		this.getStatus());
    	map.put("msg", 			this.getMsg());
    	map.put("cfg_counter", 	this.getCfg_counter());
    	return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if(dbFieldMapping == null){
			dbFieldMapping = new HashMap<String, Method>();
		    Class<? extends VirtualMachine> cla=this.getClass();		    
			try {
				dbFieldMapping.put("vmid", 			cla.getDeclaredMethod("vmid"));
				dbFieldMapping.put("type", 			cla.getDeclaredMethod("type"));
				dbFieldMapping.put("ip", 			cla.getDeclaredMethod("ip")); 
				dbFieldMapping.put("status", 		cla.getDeclaredMethod("status"));
				dbFieldMapping.put("msg", 			cla.getDeclaredMethod("msg"));
				dbFieldMapping.put("cfg_counter",   cla.getDeclaredMethod("cfg_counter")); 				 
												
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

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {
		return new VirtualMachine(
				resultSet.getString("vmid"),
				resultSet.getString("type"),
				resultSet.getString("ip"), 
				resultSet.getString("status"),
				resultSet.getString("msg"),
				resultSet.getInt("cfg_counter"));		 
	}

	public String getVmid() {
		return vmid;
	}

	public void setVmid(String vmid) {
		this.vmid = vmid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getStatus() {
		return status;
	}

	public void setCfg_counter(int cfg_counter) {
		this.cfg_counter = cfg_counter;
	}
	 
	public void setStatus(String status) {
		this.status = status;
	}

	public int getCfg_counter() {
		return cfg_counter;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
