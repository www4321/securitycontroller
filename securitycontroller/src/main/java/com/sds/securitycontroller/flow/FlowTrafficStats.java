
package com.sds.securitycontroller.flow;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class FlowTrafficStats implements IDBObject, Serializable {

	private static final long serialVersionUID = 2404603748238445694L;

	public FlowTrafficStats(){
		time=0;
	}
	
	
	public FlowTrafficStats(long time, String src_mac, String dst_mac,
			String src_ip, String dst_ip, String src_port, String dst_port,
			long pkg_count, long byte_count,String links) {
		super();
		this.time = time;
		this.src_mac = src_mac;
		this.dst_mac = dst_mac;
		this.src_ip = src_ip;
		this.dst_ip = dst_ip;
		this.src_port = src_port;
		this.dst_port = dst_port;
		this.links=links;
		this.pkg_count = pkg_count;
		this.byte_count = byte_count;
	}

	protected static Map<String, Method> dbFieldMapping;
	protected static Logger log = LoggerFactory.getLogger(FlowTrafficStats.class);
	
	public static long lastRecordTimestamp;
	static{
		lastRecordTimestamp=new Date().getTime();
	}
	
	public static final int MAX_TICKS=10;
	public static int ticks=0;
	//count ticks, when ticks<MAX_TICKS: accumulate pkt & byte counts, next tick+=1; when = MAX_TICKS: next ticks=0
	public long time=0;

	public String src_mac="";
	public String dst_mac="";
	public String src_ip="";
	public String dst_ip="";
	public String src_port="";
	public String dst_port="";
	public String links="";
	public long pkg_count=0;
	public long byte_count=0;


	public FlowTrafficStats(String match, long packetCount, long byteCount) {
		super();
		try {
			time=new Date().getTime();
			String[] paras=match.split(";");
			src_mac=paras[0];
			dst_mac=paras[1];
			src_ip=paras[2];
			dst_ip=paras[3];
			src_port=paras[4];
			dst_port=paras[5];
			links=paras[14];
		} catch (Exception e) {
//			e.printStackTrace();
		}
		this.pkg_count = packetCount;
		this.byte_count = byteCount;
	}
	
	public FlowTrafficStats(Date dateTime,String match, long packetCount, long byteCount) {
		super();
		time=dateTime.getTime();
		try {
			String[] paras=match.split(";");
			src_mac=paras[0];
			dst_mac=paras[1];
			src_ip=paras[2];
			dst_ip=paras[3];
			src_port=paras[4];
			dst_port=paras[5];
			links=paras[14];
			
		} catch (Exception e) {
//			e.printStackTrace();
		}
		this.pkg_count = packetCount;
		this.byte_count = byteCount;
	}

	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("time", this.time);
		map.put("src_mac", this.src_mac);
		map.put("dst_mac", this.dst_mac);
		map.put("src_ip", this.src_ip);
		map.put("dst_ip", this.dst_ip);
		map.put("src_port", this.src_port);
		map.put("dst_port", this.dst_port);
		map.put("pkg_count", this.pkg_count);
		map.put("byte_count", this.byte_count);
		map.put("links", this.links);
		return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if (dbFieldMapping == null) {
			dbFieldMapping = new HashMap<String, Method>();
			Class<? extends FlowTrafficStats> cla = this.getClass();
			try {
				dbFieldMapping.put("time", cla.getDeclaredMethod("time"));
				dbFieldMapping.put("src_mac", cla.getDeclaredMethod("src_mac"));
				dbFieldMapping.put("dst_mac", cla.getDeclaredMethod("dst_mac"));
				dbFieldMapping.put("src_ip", cla.getDeclaredMethod("src_ip"));
				dbFieldMapping.put("dst_ip", cla.getDeclaredMethod("dst_ip"));
				dbFieldMapping.put("src_port", cla.getDeclaredMethod("src_port"));
				dbFieldMapping.put("dst_port", cla.getDeclaredMethod("dst_port"));
				dbFieldMapping.put("pkg_count", cla.getDeclaredMethod("pkg_count"));
				dbFieldMapping.put("byte_count", cla.getDeclaredMethod("byte_count"));
				dbFieldMapping.put("links", cla.getDeclaredMethod("links"));
				
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
		long time =resultSet.getLong("time");
		String src_mac=resultSet.getString("src_mac")!=null?resultSet.getString("src_mac"):"";
		String dst_mac=resultSet.getString("dst_mac")!=null?resultSet.getString("dst_mac"):"";
		String src_ip=resultSet.getString("src_ip")!=null?resultSet.getString("src_ip"):"";
		String dst_ip=resultSet.getString("dst_ip")!=null?resultSet.getString("dst_ip"):"";
		String src_port=resultSet.getString("src_port")!=null?resultSet.getString("src_port"):"";
		String dst_port=resultSet.getString("dst_port")!=null?resultSet.getString("dst_port"):"";
		String links=resultSet.getString("links")!=null?resultSet.getString("links"):"";
		long pkg_count = resultSet.getLong("pkg_count");
		long byte_count = resultSet.getLong("byte_count");
		return new FlowTrafficStats(time, src_mac, dst_mac, src_ip, dst_ip, src_port, dst_port, pkg_count, byte_count,links);
	}
}
