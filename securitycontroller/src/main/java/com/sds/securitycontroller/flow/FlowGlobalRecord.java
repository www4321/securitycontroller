package com.sds.securitycontroller.flow;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class FlowGlobalRecord implements Serializable, IDBObject {

	private static final long serialVersionUID = 1L;
	protected static Map<String, Method> dbFieldMapping;
	protected static Logger log = LoggerFactory.getLogger(FlowGlobal.class);
	
	public String globalFlowId;
	public String flowId;
	public String src_mac;
	public String dst_mac;
	public String src_ip;
	public String dst_ip;
	public String src_port;
	public String dst_port;
	public String pkg_count;
	public String byte_count;
	public String links;
	public String createTime;
	public String lastTime;
	

	public FlowGlobalRecord() {
		super();
	}

	public FlowGlobalRecord(String globalFlowId, String flowId, String src_mac,
			String dst_mac, String src_ip, String dst_ip, String src_port,
			String dst_port, String pkg_count, String byte_count, String links,
			String createTime, String lastTime) {
		super();
		this.globalFlowId = globalFlowId;
		this.flowId = flowId;
		this.src_mac = src_mac;
		this.dst_mac = dst_mac;
		this.src_ip = src_ip;
		this.dst_ip = dst_ip;
		this.src_port = src_port;
		this.dst_port = dst_port;
		this.pkg_count = pkg_count;
		this.byte_count = byte_count;
		this.links = links;
		this.createTime = createTime;
		this.lastTime = lastTime;
	}

	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("globalFlowId", this.globalFlowId);
		map.put("flowId", this.flowId);
		map.put("src_mac", this.src_mac);
		map.put("dst_mac", this.dst_mac);
		map.put("src_ip", this.src_ip);
		map.put("dst_ip", this.dst_ip);
		map.put("src_port", this.src_port);
		map.put("dst_port", this.dst_port);
		map.put("pkg_count", this.pkg_count);
		map.put("byte_count", this.byte_count);
		map.put("links", this.links);
		map.put("createTime", this.createTime);
		map.put("lastTime", this.lastTime);
		return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if (dbFieldMapping == null) {
			dbFieldMapping = new HashMap<String, Method>();
			Class<? extends FlowGlobalRecord> cla = this.getClass();
			try {
				dbFieldMapping.put("globalFlowId", cla.getDeclaredMethod("globalFlowId"));
				dbFieldMapping.put("flowId", cla.getDeclaredMethod("flowId"));
				dbFieldMapping.put("src_mac", cla.getDeclaredMethod("src_mac"));
				dbFieldMapping.put("dst_mac", cla.getDeclaredMethod("dst_mac"));
				dbFieldMapping.put("src_ip", cla.getDeclaredMethod("src_ip"));
				dbFieldMapping.put("dst_ip", cla.getDeclaredMethod("dst_ip"));
				dbFieldMapping.put("src_port", cla.getDeclaredMethod("src_port"));
				dbFieldMapping.put("dst_port", cla.getDeclaredMethod("dst_port"));
				dbFieldMapping.put("pkg_count", cla.getDeclaredMethod("pkg_count"));
				dbFieldMapping.put("byte_count", cla.getDeclaredMethod("byte_count"));
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
		String globalFlowId=resultSet.getString("globalFlowId")!=null?resultSet.getString("globalFlowId"):"";
		String flowId=resultSet.getString("globalFlowId")!=null?resultSet.getString("globalFlowId"):"";
		String src_mac=resultSet.getString("globalFlowId")!=null?resultSet.getString("globalFlowId"):"";
		String dst_mac=resultSet.getString("dst_mac")!=null?resultSet.getString("dst_mac"):"";
		String src_ip=resultSet.getString("src_ip")!=null?resultSet.getString("src_ip"):"";
		String dst_ip=resultSet.getString("dst_ip")!=null?resultSet.getString("dst_ip"):"";
		String src_port=resultSet.getString("src_port")!=null?resultSet.getString("src_port"):"";
		String dst_port=resultSet.getString("dst_port")!=null?resultSet.getString("dst_port"):"";
		String pkg_count=resultSet.getString("pkg_count")!=null?resultSet.getString("pkg_count"):"";
		String byte_count=resultSet.getString("byte_count")!=null?resultSet.getString("byte_count"):"";
		String links=resultSet.getString("links")!=null?resultSet.getString("links"):"";
		String createTime=resultSet.getString("createTime")!=null?resultSet.getString("createTime"):"";
		String lastTime=resultSet.getString("lastTime")!=null?resultSet.getString("lastTime"):""; 
		
		return new FlowGlobalRecord(globalFlowId, flowId, src_mac, dst_mac, src_ip, dst_ip, src_port, dst_port, pkg_count, byte_count, links, createTime, lastTime);
	}

}
