package com.sds.securitycontroller.flow;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class FlowTrafficIPStats implements IDBObject, Serializable {

	public String id = null;
	public String time="now()";

	public int appid=-1;
	public int src_ip=-1;
	public int dst_ip=-1;
	
	public long pkg_count=0;
	public long byte_count=0;
	
	private static final long serialVersionUID = 2404603748238445664L;

	
	public FlowTrafficIPStats(String id, String time, int appid, int src_ip, int dst_ip, 
			long pkg_count, long byte_count) {
		super();
		this.id = id;
		this.time = time;
		this.appid = appid;
		this.src_ip = src_ip;
		this.dst_ip = dst_ip;
		this.pkg_count = pkg_count;
		this.byte_count = byte_count;
	}

	protected static Map<String, Method> dbFieldMapping;
	protected static Logger log = LoggerFactory.getLogger(FlowTrafficStats.class);
	


	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.id);
		map.put("time", this.time);
		map.put("appid", this.appid);
		map.put("src_ip", this.src_ip);
		map.put("dst_ip", this.dst_ip);
		map.put("pkg_count", this.pkg_count);
		map.put("byte_count", this.byte_count);
		return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if (dbFieldMapping == null) {
			dbFieldMapping = new HashMap<String, Method>();
			Class<? extends FlowTrafficIPStats> cla = this.getClass();
			try {
				dbFieldMapping.put("id", cla.getDeclaredMethod("id"));
				dbFieldMapping.put("time", cla.getDeclaredMethod("time"));
				dbFieldMapping.put("src_port", cla.getDeclaredMethod("appid"));
				dbFieldMapping.put("src_ip", cla.getDeclaredMethod("src_ip"));
				dbFieldMapping.put("dst_ip", cla.getDeclaredMethod("dst_ip"));
				dbFieldMapping.put("pkg_count", cla.getDeclaredMethod("pkg_count"));
				dbFieldMapping.put("byte_count", cla.getDeclaredMethod("byte_count"));
				
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
		String id =resultSet.getString("id");
		String time =resultSet.getString("time");
		int appid=resultSet.getInt("appid");
		int src_ip=resultSet.getInt("src_ip");
		int dst_ip=resultSet.getInt("dst_ip");
		long pkg_count = resultSet.getLong("pkg_count");
		long byte_count = resultSet.getLong("byte_count");
		return new FlowTrafficIPStats(id, time, appid, src_ip, dst_ip, pkg_count, byte_count);
	}
}