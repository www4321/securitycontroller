package com.sds.securitycontroller.flow;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class FlowGlobal implements IDBObject, Serializable {

	public String id="";
	public String flowIds="";
	public String links="";
	public String createTime;
	public String lastTime;
	
	protected static Map<String, Method> dbFieldMapping;
	protected static Logger log = LoggerFactory.getLogger(FlowGlobal.class);
	
	public FlowGlobal(){
		
	}
	public FlowGlobal(String id, String flowIds, String links,
			String createTime, String lastTime) {
		super();
		this.id=id;
		this.flowIds=flowIds;
		this.links=links;
		this.createTime=createTime;
		this.lastTime=lastTime;
	}
	private static final long serialVersionUID = 1387844303406756151L;

	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.id);
		map.put("flowIds", this.flowIds);
		map.put("links", this.links);
		map.put("createTime", this.createTime);
		map.put("lastTime", this.lastTime);
		return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if (dbFieldMapping == null) {
			dbFieldMapping = new HashMap<String, Method>();
			Class<? extends FlowGlobal> cla = this.getClass();
			try {
				dbFieldMapping.put("id", cla.getDeclaredMethod("id"));
				dbFieldMapping.put("flowIds", cla.getDeclaredMethod("flowIds"));
				dbFieldMapping.put("links", cla.getDeclaredMethod("links"));
				dbFieldMapping.put("createTime", cla.getDeclaredMethod("createTime"));
				dbFieldMapping.put("lastTime", cla.getDeclaredMethod("lastTime"));
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
		String id=resultSet.getString("id")!=null?resultSet.getString("id"):"";
		String flowIds=resultSet.getString("flowIds")!=null?resultSet.getString("flowIds"):"";
		String links=resultSet.getString("links")!=null?resultSet.getString("links"):"";
		String createTime=resultSet.getString("createTime")!=null?resultSet.getString("createTime"):"";
		String lastTime=resultSet.getString("lastTime")!=null?resultSet.getString("lastTime"):"";
		
		return new FlowGlobal(id,flowIds,links,createTime,lastTime);
	}

}
