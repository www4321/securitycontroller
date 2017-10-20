package com.sds.securitycontroller.TaskManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;


public class MyTask implements IDBObject {
	private String id;
	private String type;
	private String host;
	protected static Map<String, Method> dbFieldMapping;
	protected static Logger log = LoggerFactory.getLogger(MyTask.class);
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public  MyTask(String id,String type,String host) {
		this.id=id;
		this.type=type;
		this.host=host;
	}
	public  MyTask(String type,String host) {
		this.type=type;
		this.host=host;
	}
	public  MyTask() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public Map<String, Object> getDBElements() {
		// TODO Auto-generated method stub
    	Map<String,Object> map = new HashMap<String,Object>();    	
    	map.put("id",			this.getId());
    	map.put("type",		this.getType());
    	map.put("host",			this.getHost());	
    	
    	return map;
	}
	@Override
	public Object getFieldValueByKey(String key) {
		// TODO Auto-generated method stub
		if(dbFieldMapping == null){
			dbFieldMapping = new HashMap<String, Method>();
		    Class<? extends MyTask> cla=this.getClass();		    
			try {
				dbFieldMapping.put("id", 			cla.getDeclaredMethod("id"));
				dbFieldMapping.put("type", 			cla.getDeclaredMethod("type"));
				dbFieldMapping.put("host", 			cla.getDeclaredMethod("host"));	
			
				
				 
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
		// TODO Auto-generated method stub
		return new MyTask(
				resultSet.getString("type"),
				resultSet.getString("host")

													   		
		    	);
	}



}
