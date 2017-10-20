/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.flow;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sds.securitycontroller.event.ISubscriptionResult;
import com.sds.securitycontroller.storage.DateTimeUtils;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.utils.Cypher;

@Entity
public class FlowInfo //extends BasicDBObject 
					implements java.io.Serializable,
					IDBObject, ISubscriptionResult  {
	
	/*
	 * Identify the structure as belows
	 * {"actions":[{"port":1,"maxLength":32767,"length":8,"type":"OUTPUT","lengthU":8}],"priority":32767,"cookie":45035996273704960,"idleTimeout":0,"hardTimeout":0,"match":{"dataLayerDestination":"00:00:00:00:00:00","dataLayerSource":"fa:16:3e:2c:2d:5f","dataLayerType":"0x0800","dataLayerVirtualLan":0,"dataLayerVirtualLanPriorityCodePoint":0,"inputPort":13,"networkDestination":"0.0.0.0","networkDestinationMaskLen":0,"networkProtocol":0,"networkSource":"0.0.0.0","networkSourceMaskLen":0,"networkTypeOfService":0,"transportDestination":0,"transportSource":0,"wildcards":3678442},"durationSeconds":57,"durationNanoseconds":436000000,"packetCount":0,"byteCount":0,"tableId":0}],"00:00:32:2b:5f:f4:3f:4f":[{"actions":[{"port":2,"maxLength":32767,"length":8,"type":"OUTPUT","lengthU":8}],"priority":32767,"cookie":45035996273704960,"idleTimeout":0,"hardTimeout":0,"match":{"dataLayerDestination":"00:00:00:00:00:00","dataLayerSource":"fa:16:3e:2c:2d:5f","dataLayerType":"0x0800","dataLayerVirtualLan":0,"dataLayerVirtualLanPriorityCodePoint":0,"inputPort":1,"networkDestination":"0.0.0.0","networkDestinationMaskLen":0,"networkProtocol":0,"networkSource":"0.0.0.0","networkSourceMaskLen":0,"networkTypeOfService":0,"transportDestination":0,"transportSource":0,"wildcards":3678442},"durationSeconds":57,"durationNanoseconds":434000000,"packetCount":7,"byteCount":2314,"tableId":0}],"00:00:36:3a:2e:62:85:40":[{"actions":[{"dataLayerAddress":"52:54:00:a9:b8:b2","length":16,"type":"SET_DL_SRC","lengthU":16},{"port":-3,"maxLength":32767,"length":8,"type":"OUTPUT","lengthU":8}],"priority":32767,"cookie":45035996273704960,"idleTimeout":0,"hardTimeout":0,"match":{"dataLayerDestination":"00:00:00:00:00:00","dataLayerSource":"fa:16:3e:2c:2d:5f","dataLayerType":"0x0800","dataLayerVirtualLan":0,"dataLayerVirtualLanPriorityCodePoint":0,"inputPort":6,"networkDestination":"0.0.0.0","networkDestinationMaskLen":0,"networkProtocol":0,"networkSource":"0.0.0.0","networkSourceMaskLen":0,"networkTypeOfService":0,"transportDestination":0,"transportSource":0,"wildcards":3678442},"durationSeconds":57,"durationNanoseconds":473000000,"packetCount":0,"byteCount":0,"tableId":0}],"00:00:26:94:46:eb:4d:47":[],"00:00:b2:fb:54:ac:0c:49":[{"actions":[{"port":5,"maxLength":32767,"length":8,"type":"OUTPUT","lengthU":8}],"priority":32767,"cookie":45035996273704960,"idleTimeout":0,"hardTimeout":0,"match":{"dataLayerDestination":"00:00:00:00:00:00","dataLayerSource":"fa:16:3e:2c:2d:5f","dataLayerType":"0x0800","dataLayerVirtualLan":0,"dataLayerVirtualLanPriorityCodePoint":0,"inputPort":1,"networkDestination":"0.0.0.0","networkDestinationMaskLen":0,"networkProtocol":0,"networkSource":"0.0.0.0","networkSourceMaskLen":0,"networkTypeOfService":0,"transportDestination":0,"transportSource":0,"wildcards":3678442},"durationSeconds":57,"durationNanoseconds":473000000,"packetCount":0,"byteCount":0,"tableId":0}],"00:00:5e:ae:01:9f:1f:4c":[{"actions":[{"port":0,"maxLength":32767,"length":8,"type":"OUTPUT","lengthU":8}],"priority":32767,"cookie":45035996273704960,"idleTimeout":0,"hardTimeout":0,"match":{"dataLayerDestination":"00:00:00:00:00:00","dataLayerSource":"fa:16:3e:2c:2d:5f","dataLayerType":"0x0800","dataLayerVirtualLan":0,"dataLayerVirtualLanPriorityCodePoint":0,"inputPort":2,"networkDestination":"0.0.0.0","networkDestinationMaskLen":0,"networkProtocol":0,"networkSource":"0.0.0.0","networkSourceMaskLen":0,"networkTypeOfService":0,"transportDestination":0,"transportSource":0,"wildcards":3678442},"durationSeconds":57,"durationNanoseconds":453000000,"packetCount":6,"byteCount":1986,"tableId":0}
	 */
	private static final long serialVersionUID = -4781225958501354495L;

	public static int MINIMUM_LENGTH = 88;
	@Id
    protected String id;
    protected String dpid;
	protected short length = (short) MINIMUM_LENGTH;
    protected int tableId;
    protected FlowMatch match;
    protected int durationSeconds;
    protected int durationNanoseconds;
    protected long priority;
    protected int idleTimeout;
    protected int hardTimeout;
    protected long cookie;
    protected long packetCount;
    protected long byteCount;
    protected Date time;
    
    
    
    
//    protected long packetCountIncrement;
//    protected long byteCountIncrement;
    
    @Override
	public boolean equals(Object obj) {
		
		return this.match.equals(((FlowInfo)obj).match)&&this.dpid.equals(((FlowInfo)obj).dpid)
				&&this.priority==((FlowInfo)obj).priority&&this.idleTimeout==((FlowInfo)obj).idleTimeout&&this.hardTimeout==((FlowInfo)obj).hardTimeout
				&&this.cookie==((FlowInfo)obj).cookie;
	}



	@Override
	public int hashCode() {
		int result=17;
		 result=result*31+match.hashCode();
		 result=result*31+idleTimeout;
		 result=result*31+hardTimeout;
		 result=result*31+(int)priority;
		 result=result*31+(int)cookie;
		 result=result*31+dpid.hashCode();
		 return result;
	}



	public void setTime(Date time) {
		this.time = time;
	}

	protected List<FlowAction> actions;
    protected static Map<String, Method> dbFieldMapping;
    protected static Logger log = LoggerFactory.getLogger(FlowInfo.class);


    public FlowInfo(){			
	}
    
    
    	
	public FlowInfo(String id, int tableId, String src_mac, String dst_mac, 
			String src_ip, String dst_ip, int src_port, int dst_port,
			int protocol, long byte_count, long pkg_count, Date time){
		this.tableId = tableId;
		this.match = new FlowMatch();
		
		this.match.setDataLayerDestination(dst_mac);
		this.match.dataLayerSource = src_mac;
		this.match.networkSource = src_ip;
		this.match.networkDestination = dst_ip;
		this.match.setTransportSource(src_port);
		this.match.setTransportDestination(dst_port);
		this.match.setNetworkProtocol(protocol);
		this.byteCount = byte_count;
		this.packetCount = pkg_count;
		this.time = time;
	}

	public FlowInfo(String id, long packetCount, long byteCount) {
		super();
		this.id = id;
		this.packetCount = packetCount;
		this.byteCount = byteCount;
	}

	public FlowInfo(int tableId, String src_mac, String dst_mac, 
			String src_ip, String dst_ip, int src_port, int dst_port,
			int protocol, long byte_count, long pkg_count, Date time){		
		this(null, tableId, src_mac, dst_mac, src_ip, dst_ip, src_port, dst_port,
				protocol, byte_count, pkg_count, time);
		this.id = getId();
	}

	public Date getTime() {
		return time;
	}



	public String getId(){
		if(this.id == null){
			String raw = this.match.getDataLayerDestination()+ this.match.getDataLayerSource()
					+this.match.getDataLayerType() +this.match.getDataLayerVirtualLan()
					+ this.match.getNetworkSource() + this.match.getNetworkDestination() + this.match.getNetworkProtocol()
					+ this.match.getTransportSource() + this.match.getTransportDestination()
					+ this.match.getInputPort()+this.getDpid()+this.getActions().toString();
			byte[] rawbytes = null;
			try {
				rawbytes = raw.getBytes("UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			this.id = Cypher.getMD5(rawbytes);
		}
		return this.id;
	}
	
	
    public String getDpid() {
		return dpid;
	}

	public void setDpid(String dpid) {
		this.dpid = dpid;
	}
	
    /**
     * @return the tableId
     */
    public int getTableId() {
        return tableId;
    }

    /**
     * @param tableId the tableId to set
     */
    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    /**
     * @return the match
     */
    public FlowMatch getMatch() {
        return match;
    }

    /**
     * @param match the match to set
     */
    public void setMatch(FlowMatch match) {
        this.match = match;
    }

    /**
     * @return the durationSeconds
     */
    public int getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * @param durationSeconds the durationSeconds to set
     */
    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    /**
     * @return the durationNanoseconds
     */
    public int getDurationNanoseconds() {
        return durationNanoseconds;
    }

    /**
     * @param durationNanoseconds the durationNanoseconds to set
     */
    public void setDurationNanoseconds(int durationNanoseconds) {
        this.durationNanoseconds = durationNanoseconds;
    }

    /**
     * @return the priority
     */
    public long getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(long priority) {
        this.priority = priority;
    }

    /**
     * @return the idleTimeout
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * @param idleTimeout the idleTimeout to set
     */
    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * @return the hardTimeout
     */
    public int getHardTimeout() {
        return hardTimeout;
    }

    /**
     * @param hardTimeout the hardTimeout to set
     */
    public void setHardTimeout(int hardTimeout) {
        this.hardTimeout = hardTimeout;
    }

    /**
     * @return the cookie
     */
    public long getCookie() {
        return cookie;
    }

    /**
     * @param cookie the cookie to set
     */
    public void setCookie(long cookie) {
        this.cookie = cookie;
    }

    /**
     * @return the packetCount
     */
    public long getPacketCount() {
        return packetCount;
    }

    /**
     * @param packetCount the packetCount to set
     */
    public void setPacketCount(long packetCount) {
        this.packetCount = packetCount;
    }

    /**
     * @return the byteCount
     */
    public long getByteCount() {
        return byteCount;
    }

    /**
     * @param byteCount the byteCount to set
     */
    public void setByteCount(long byteCount) {
        this.byteCount = byteCount;
    }

    /**
     * @param length the length to set
     */
    public void setLength(short length) {
        this.length = length;
    }

    /**
     * @return the actions
     */
    public List<FlowAction> getActions() {
        return actions;
    }

    /**
     * @param actions the actions to set
     */
    public void setActions(List<FlowAction> actions) {
        this.actions = actions;
    }
    
    

	public String getdataLayerDestination() {
		return this.match.getDataLayerDestination();
	}
	
	public String getdataLayerSource() {
		return this.match.dataLayerSource;
	}
	
	public int getdataLayerType() {
		return this.match.getDataLayerType();
	}
	public String getnetworkDestination() {
		return this.match.networkDestination;
	}
	public String getnetworkSource() {
		return this.match.networkSource;
	}

	public int getdataLayerVirtualLan() {
		return this.match.getDataLayerVirtualLan();
	}

	public int getdataLayerVirtualLanPriorityCodePoint() {
		return this.match.getDataLayerVirtualLanPriorityCodePoint();
	}

	public short getinputPort() {
		return this.match.inputPort;
	}

	public int getnetworkDestinationMaskLen() {
		return this.match.getNetworkDestinationMaskLen();
	}

	public int getnetworkProtocol() {
		return this.match.getNetworkProtocol();
	}

	public int getnetworkSourceMaskLen() {
		return this.match.getNetworkSourceMaskLen();
	}

	public String getnetworkTypeOfService() {
		return this.match.networkTypeOfService;
	}
	
	public int gettransportDestination() {
		return this.match.getTransportDestination();
	}

	public int gettransportSource() {
		return this.match.getTransportSource();
	}

	public int getwildcards() {
		return this.match.wildcards;
	}	


	public int getnetworkDestinationInt() {
		return this.match.networkDestinationInt;
	}
	public int getnetworkSourceInt() {
		return this.match.networkSourceInt;
	}

	//string to object fields
	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {
		return new FlowInfo(
				resultSet.getString("id"),
				resultSet.getInt("tableId"),
				resultSet.getString("src_mac"),
				resultSet.getString("dst_mac"),
				resultSet.getString("src_ip"),
				resultSet.getString("dst_ip"),
				resultSet.getInt("src_port"),
				resultSet.getInt("dst_port"),
				resultSet.getInt("protocol"),
				resultSet.getLong("byte_count"),
				resultSet.getLong("pkg_count"),
				resultSet.getDate("time"));
	}

	//collect all db columns
	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", getId());
		map.put("tableId", this.getTableId());
		map.put("src_mac", this.getdataLayerSource());
		map.put("dst_mac", this.getdataLayerDestination());
		map.put("src_ip", this.getnetworkSource());
		map.put("dst_ip", this.getnetworkDestination());
		map.put("src_port", this.gettransportSource());
		map.put("dst_port", this.gettransportDestination());
		map.put("protocol", this.getnetworkProtocol());
		map.put("byte_count", this.getByteCount());
		map.put("pkg_count", this.getPacketCount());
		map.put("time", DateTimeUtils.convertDateTime(new Date()));
		return map;
	}

	//db column to object fields
	@Override
	public Object getFieldValueByKey(String key){
		if(dbFieldMapping == null){
			dbFieldMapping = new HashMap<String, Method>();
		    Class<? extends FlowInfo> cla=this.getClass();		    
			try {
				dbFieldMapping.put("id", cla.getDeclaredMethod("getId"));
				dbFieldMapping.put("tableId", cla.getDeclaredMethod("getTableId"));
				dbFieldMapping.put("src_mac", cla.getDeclaredMethod("getdataLayerSource"));
				dbFieldMapping.put("dst_mac", cla.getDeclaredMethod("getdataLayerDestination"));
				dbFieldMapping.put("src_ip", cla.getDeclaredMethod("getnetworkSource"));
				dbFieldMapping.put("dst_ip", cla.getDeclaredMethod("getnetworkDestination"));
				dbFieldMapping.put("src_port", cla.getDeclaredMethod("gettransportSource"));
				dbFieldMapping.put("dst_port", cla.getDeclaredMethod("gettransportDestination"));
				dbFieldMapping.put("protocol", cla.getDeclaredMethod("getnetworkProtocol"));
				dbFieldMapping.put("byte_count", cla.getDeclaredMethod("getByteCount"));
				dbFieldMapping.put("pkg_count", cla.getDeclaredMethod("getPacketCount"));
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
	public String toString(){
		return String.format("id=%s, dpip=%s,Priority=%s,\n in_port=%s,smac=%s, dmac=%s, sip=%s, sipMask=%s,\n dip=%s, dipMask=%s,sport=%s, dport=%s, protocol=%s, out_port=%s",
				this.getId(),this.getDpid(),this.getPriority(),
				this.getinputPort()==0?"*":this.getinputPort(),
				this.match.dataLayerSource.equals("00:00:00:00:00:00")?"*":this.match.dataLayerSource, 
				this.match.getDataLayerDestination().equals("00:00:00:00:00:00")?"*":this.match.getDataLayerDestination(),
				this.match.networkSource.equals("0.0.0.0")?"*":this.match.networkSource, this.getMatch().getNetworkSourceMaskLen(),
				this.match.networkDestination.equals("0.0.0.0")?"*":this.match.networkSource,this.getMatch().getNetworkDestinationMaskLen(),
				this.match.getTransportSource()==0?"*":this.match.getTransportSource(), 
				this.match.getTransportDestination()==0?"*":this.match.getTransportDestination(),
				this.match.getNetworkProtocol()==0?"*":this.match.getNetworkProtocol(), this.getActions());
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String toQueryString(){
		StringBuilder sb =new StringBuilder();
		if(match==null)
			return null;
		try {
            sb = match.addToBuilder(sb);
			for(FlowAction action: actions){
				sb.append("output: ").append(action.getPort()).append(";");
			}
			return sb.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
