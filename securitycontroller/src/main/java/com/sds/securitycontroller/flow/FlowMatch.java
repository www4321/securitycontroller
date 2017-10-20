/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.flow;


import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.utils.Cypher;
import com.sds.securitycontroller.utils.IPAddress;

@Entity
public class FlowMatch 
implements java.io.Serializable{	
	private static final long serialVersionUID = -1513726977184263703L;
	static Logger log=LoggerFactory.getLogger(FlowMatch.class);
	@Id
    protected String id;
	public String getId() {
		if(this.id == null){
			String raw = this.getDataLayerDestination()+ this.getDataLayerSource()
					+this.getDataLayerType() +this.getDataLayerVirtualLan()
					+ this.getNetworkSource() + this.getNetworkDestination() + this.getNetworkProtocol()
					+ this.getTransportSource() + this.getTransportDestination()
					+ this.getInputPort() ;
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


	public void setId(String id) {
		this.id = id;
	}

	
	
 
	public String getMatch(){
		
		return  dataLayerSource+";"
				+getDataLayerDestination()+";"
				+networkSource+";"
				+networkDestination+";"
				+getTransportSource()+";"
				+getTransportDestination()+";"
				+getDataLayerType()+";"
				+getDataLayerVirtualLan()+";"
				+getDataLayerVirtualLan()+";"
				+getNetworkDestinationMaskLen()+";"
				+getNetworkProtocol()+";"
				+getNetworkSourceMaskLen()+";"
				+networkTypeOfService+";"
				+wildcards;
	}
	
    protected int wildcards;
    protected short inputPort;
    protected String dataLayerSource;
    private String dataLayerDestination;
    private int dataLayerVirtualLan;
    private int dataLayerVirtualLanPriorityCodePoint;
    private int dataLayerType;
    protected String networkTypeOfService;
    private int networkProtocol;
    protected String networkSource ;
    protected String networkDestination ;
    protected int networkSourceInt = -1;
    protected int networkDestinationInt = -1;
    private int transportSource;
    private int transportDestination;
    private int networkDestinationMaskLen;
    private int networkSourceMaskLen;
    private int queryPage=1;
    private int querySize=5;
	public int getQuerySize() {
		return querySize;
	}
	@Override
	public int hashCode() {   
		 int result=17;
		 result=result*31+getDataLayerDestination().hashCode();
		 result=result*31+dataLayerSource.hashCode();
		 result=result*31+getDataLayerType();
		 result=result*31+getDataLayerVirtualLan();
		 result=result*31+networkDestination.hashCode();
		 result=result*31+getNetworkProtocol();
		 result=result*31+networkSource.hashCode();
		 result=result*31+getTransportDestination();
		 result=result*31+getTransportSource();
		 return result; 
	}
	
	
	@Override
	public boolean equals(Object obj)
   {
   if(obj instanceof FlowMatch)
   {
    if(this.getDataLayerDestination().equals(((FlowMatch)obj).getDataLayerDestination())&&this.dataLayerSource.equals(((FlowMatch)obj).getDataLayerSource())
    		&&this.getDataLayerType()==((FlowMatch)obj).getDataLayerType()&&this.getDataLayerVirtualLan()==((FlowMatch)obj).getDataLayerVirtualLan()&&this.networkDestination.equals(((FlowMatch)obj).getNetworkDestination())
    		&&this.getNetworkProtocol()==((FlowMatch)obj).getNetworkProtocol()&&this.networkSource.equals(((FlowMatch)obj).getNetworkSource())
    		&&this.getTransportDestination()==((FlowMatch)obj).getTransportDestination()&&this.getTransportSource()==((FlowMatch)obj).getTransportSource())
       return true;
   }
   return false;
   }

	public void setQuerySize(int querySize) {
		this.querySize = querySize;
	}


	public int getQueryPage() {
		return queryPage;
	}


	public void setQueryPage(int queryPage) {
		this.queryPage = queryPage;
	}


	@Override
    public String toString(){
        StringBuilder sBuilder = new StringBuilder();
        return addToBuilder(sBuilder).toString();
    }
    public StringBuilder addToBuilder(StringBuilder sBuilder){
    	Set<String> set = new HashSet<String>();
    	set.add("wildcards");
    	//set.add("inputPort");
    	set.add("dataLayerSource");
    	set.add("dataLayerDestination");
    	set.add("dataLayerVirtualLan");
    	set.add("dataLayerVirtualLanPriorityCodePoint");
    	set.add("dataLayerType");
    	set.add("networkTypeOfService");
    	set.add("networkProtocol");
    	set.add("networkSource");
    	set.add("networkDestination");
    	//set.add("networkSourceInt");
    //	set.add("networkDestinationInt");
    	set.add("transportSource");
    	set.add("transportDestination");
    	set.add("networkDestinationMaskLen");
    	set.add("networkSourceMaskLen");
    	return addToBuilder(set, sBuilder);
    }
    
    public StringBuilder addToBuilder(Set<String> fields, StringBuilder sBuilder){
    	if(fields.contains("wildcards"))
	    	sBuilder.append("wildcards:"+wildcards);
    //	if(fields.contains("inputPort"))
	 //   	sBuilder.append(",inport:"+inputPort);
    	if(fields.contains("dataLayerSource"))
	    	sBuilder.append(",smac:"+(dataLayerSource));
    	if(fields.contains("dataLayerDestination"))
	    	sBuilder.append(",dmac:"+(getDataLayerDestination()));
    	if(fields.contains("dataLayerVirtualLan"))
	    	sBuilder.append(",vlan:"+getDataLayerVirtualLan());
    	if(fields.contains("dataLayerVirtualLanPriorityCodePoint"))
	    	sBuilder.append(",vlanPriorityCodePoint:"+getDataLayerVirtualLan());
    	if(fields.contains("dataLayerType"))
    		sBuilder.append(",l2type:"+getDataLayerType());
    	if(fields.contains("networkTypeOfService"))
	    	sBuilder.append(",tos:"+networkTypeOfService);
    	if(fields.contains("networkProtocol"))
	    	sBuilder.append(",l3type:"+getNetworkProtocol());
	    if(fields.contains("networkSource") && networkSource!=null)
	    	sBuilder.append(",sip:"+networkSource);
	 //   if(fields.contains("networkSourceInt") && networkSourceInt>0)
	//    	sBuilder.append(",sip:"+convertIPInt(networkSourceInt));
	    if(fields.contains("networkDestination") && networkDestination!=null)
	    	sBuilder.append(",dip:"+networkDestination);
	   // if(fields.contains("networkDestinationInt") && networkDestinationInt>0)
	 //   	sBuilder.append(",dip:"+convertIPInt(networkDestinationInt));
	    if(fields.contains("networkSourceMaskLen"))
	    	sBuilder.append(",smask:"+getNetworkSourceMaskLen());
	    if(fields.contains("networkDestinationMaskLen"))
	    	sBuilder.append(",dmask:"+getNetworkDestinationMaskLen());
	    if(fields.contains("transportSource"))
	    	sBuilder.append(",sport:"+getTransportSource());
	    if(fields.contains("transportDestination"))	
	    	sBuilder.append(",dport:"+getTransportDestination());
    	return sBuilder;
}
    protected String convertIPInt(int ip){
        int x1 = ((ip>>> 24) & 0xff);
        int x2 = (byte)((ip>>> 16) & 0xff);
        int x3 = (byte)((ip>>>  8) & 0xff);
        int x4 = (byte)((ip ) & 0xff);
        return x1+"."+x2+"."+x3+"."+x4;
    }

    protected String convertMacString(String mac){
        byte[] bs = mac.getBytes();
        StringBuilder sb = new StringBuilder();
        for(byte x : bs){
            int low = x & 0xF;  
            int high = (x >> 8) & 0xF;  
            sb.append(Character.forDigit(high, 16));  
            sb.append(Character.forDigit(low, 16));
            sb.append(':');
        }
        return sb.toString();
    }

	public int getwildcards() {
		return wildcards;
	}


	public void setwildcards(int wildcards) {
		this.wildcards = wildcards;
	}

	public static FlowMatch resolveFlowMatchFromJsonNode(JsonNode node){
		try {
			FlowMatch flowMatch = new FlowMatch();
			// resolve flow match
			String src_mac = node.path("dataLayerSource").asText();
			String dst_mac = node.path("dataLayerDestination").asText();
			
			String src_ip = null;
			String dst_ip = null;
			int curPage=node.path("curPage").asInt();
			int size=node.path("size").asInt();
			
			try {
				if(node.path("networkSource").isInt()){
					IPAddress ip=new IPAddress(node.path("networkSource").asInt());
					src_ip=ip.toString();
				}
				else
					src_ip = node.path("networkSource").asText();
			} catch (Exception e) {
				log.error("invalid src IP format!\n{}",e.getMessage());
				src_ip=null;
			}
			try {
				if(node.path("networkDestination").isInt()){
					IPAddress ip=new IPAddress(node.path("networkDestination").asInt());
					dst_ip=ip.toString();
				}
				else
					dst_ip = node.path("networkDestination").asText();
			} catch (Exception e) {
				log.error("invalid dest IP format!\n{}",e.getMessage());
				dst_ip=null;
			}


			//if null
			if(src_mac.equals("null"))
				src_mac = null;
			if(dst_mac.equals("null"))
				dst_mac = null;
			if(src_ip.equals("null"))
				src_ip = null;
			if(dst_ip.equals("null"))
				dst_ip = null;
			
			
			int src_port = node.path("transportSource").asInt();
			int dst_port = node.path("transportDestination").asInt();
			int proto = node.path("networkProtocol").asInt();
			
			flowMatch.setDataLayerSource(src_mac);
			flowMatch.setDataLayerDestination(dst_mac);
			flowMatch.setNetworkSource(src_ip);
			flowMatch.setNetworkDestination(dst_ip);
			flowMatch.setTransportSource((short)src_port);
			flowMatch.setTransportDestination((short)dst_port);
			flowMatch.setNetworkProtocol(proto);
			flowMatch.setQueryPage(curPage);//分页
			flowMatch.setQuerySize(size);
			return flowMatch;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	


	/**
	 * 
	 * @param objMatch
	 * @param refMatch
	 * @return true if objMatch contains refMatch
	 */
	public static boolean isFlowMatchesRelated(FlowMatch refMatch ,FlowMatch objMatch){
		
		if( !objMatch.dataLayerDestination.isEmpty() && 
			!objMatch.dataLayerDestination.equals(refMatch.dataLayerDestination) )
			return false;
		if( !objMatch.dataLayerSource.isEmpty() && 
				!objMatch.dataLayerSource.equals(refMatch.dataLayerSource) )
			return false;
		
		if( !objMatch.networkDestination.isEmpty() && 
				!objMatch.networkDestination.equals(refMatch.networkDestination) )
			return false;
		if( !objMatch.networkSource.isEmpty() && 
				!objMatch.networkSource.equals(refMatch.networkSource) )
			return false;

		if( objMatch.getTransportDestination()!=0 && 
				objMatch.getTransportDestination() != refMatch.getTransportDestination() )
			return false;
		if( objMatch.getTransportSource()!=0 && 
				objMatch.getTransportSource() != refMatch.getTransportSource() )
			return false;
		
		if( objMatch.getNetworkProtocol()!=0 && 
				objMatch.getNetworkProtocol() != refMatch.getNetworkProtocol() )
			return false;
		
		return true;
	}
	
	/***
	 * 
	 * @param matchA
	 * @param matchB
	 * @return 1 if matchA contains matchB, 2 if matchA == matchB, 3 if matchB contains matchA, 0 if not all above 
	 */
	public static int analyseTwoFlowMatchRelation(FlowMatch matchA,FlowMatch matchB){
		int stat = 2;//2: equal, 1: a contains b, 3: b contains a;
		// inputPort
		if( matchA.getInputPort()!=matchB.getInputPort() ){
			return 0;
		}
		// dataLayerDestination
		if( matchA.getDataLayerDestination()==null ){
			if(matchB.getDataLayerDestination()!=null){
				if(stat>2)return 0;
				stat = 1;
			}
		}
		else{
			if(matchB.getDataLayerDestination()==null){
				if(stat<2)return 0;
				stat = 3;
			}
			else if(! matchA.getDataLayerDestination().equals(matchB.getDataLayerDestination()))
				return 0;
		}
		// dataLayerSource
		if( matchA.getDataLayerSource()==null ){
			if(matchB.getDataLayerSource()!=null){
				if(stat>2)return 0;
				stat = 1;
			}
		}
		else{
			if(matchB.getDataLayerSource()==null){
				if(stat<2)return 0;
				stat = 3;
			}
			else if(! matchA.getDataLayerSource().equals(matchB.getDataLayerSource()))
				return 0;
		}
		// networkDestination
		if( matchA.getNetworkDestination()==null ){
			if(matchB.getNetworkDestination()!=null){
				if(stat>2)return 0;
				stat = 1;
			}
		}
		else{
			if(matchB.getNetworkDestination()==null){
				if(stat<2)return 0;
				stat = 3;
			}
			else if(! matchA.getNetworkDestination().equals(matchB.getNetworkDestination()))
				return 0;
		}
		// networkSource
		if( matchA.getNetworkSource()==null ){
			if(matchB.getNetworkSource()!=null){
				if(stat>2)return 0;
				stat = 1;
			}
		}
		else{
			if(matchB.getNetworkSource()==null){
				if(stat<2)return 0;
				stat = 3;
			}
			else if(! matchA.getNetworkSource().equals(matchB.getNetworkSource()))
				return 0;
		}
		// getNetworkProtocol
		if( matchA.getNetworkProtocol()!=matchB.getNetworkProtocol() ){
			if(matchA.getNetworkProtocol()==0 && stat > 2 )return 0;
			else if(matchB.getNetworkProtocol()==0 && stat < 2 )return 0;
		}
		// transportDestination
		if( matchA.getTransportDestination()!=matchB.getTransportDestination() ){
			if(matchA.getTransportDestination()==0 && stat > 2 )return 0;
			else if(matchB.getTransportDestination()==0 && stat < 2 )return 0;
		}
		// transportSource
		if( matchA.getTransportSource()!=matchB.getTransportSource() ){
			if(matchA.getTransportSource()==0 && stat > 2 )return 0;
			else if(matchB.getTransportSource()==0 && stat < 2 )return 0;
		} 
		return stat;
	}

	public short getInputPort() {
		return inputPort;
	}


	public void setInputPort(short inputPort) {
		this.inputPort = inputPort;
	}


	public String getDataLayerSource() {
		return dataLayerSource;
	}


	public void setDataLayerSource(String dataLayerSource) {
		this.dataLayerSource = dataLayerSource;
	}


	public String getDataLayerDestination() {
		return dataLayerDestination;
	}


	public void setDataLayerDestination(String dataLayerDestination) {
		this.dataLayerDestination = dataLayerDestination;
	}


	public int getDataLayerVirtualLan() {
		return dataLayerVirtualLan;
	}


	public void setDataLayerVirtualLan(int dataLayerVirtualLan) {
		this.dataLayerVirtualLan = dataLayerVirtualLan;
	}


	public int getDataLayerVirtualLanPriorityCodePoint() {
		return dataLayerVirtualLanPriorityCodePoint;
	}


	public void setDataLayerVirtualLanPriorityCodePoint(
			int dataLayerVirtualLanPriorityCodePoint) {
		this.dataLayerVirtualLanPriorityCodePoint = dataLayerVirtualLanPriorityCodePoint;
	}


	public int getDataLayerType() {
		return dataLayerType;
	}


	public void setDataLayerType(int dataLayerType) {
		this.dataLayerType = dataLayerType;
	}


	public String getNetworkTypeOfService() {
		return networkTypeOfService;
	}


	public void setNetworkTypeOfService(String networkTypeOfService) {
		this.networkTypeOfService = networkTypeOfService;
	}


	public int getNetworkProtocol() {
		return networkProtocol;
	}


	public void setNetworkProtocol(int networkProtocol) {
		this.networkProtocol = networkProtocol;
	}


	public String getNetworkSource() {
		return networkSource;
	}


	public void setNetworkSource(String networkSource) {
		this.networkSource = networkSource;
	}


	public String getNetworkDestination() {
		return networkDestination;
	}


	public void setNetworkDestination(String networkDestination) {
		this.networkDestination = networkDestination;
	}


	public int getNetworkSourceInt() {
		return networkSourceInt;
	}


	public void setNetworkSourceInt(int networkSourceInt) {
		this.networkSourceInt = networkSourceInt;
	}


	public int getNetworkDestinationInt() {
		return networkDestinationInt;
	}


	public void setNetworkDestinationInt(int networkDestinationInt) {
		this.networkDestinationInt = networkDestinationInt;
	}


	public int getTransportSource() {
		return transportSource;
	}


	public void setTransportSource(int transportSource) {
		this.transportSource = transportSource;
	}


	public int getTransportDestination() {
		return transportDestination;
	}


	public void setTransportDestination(int transportDestination) {
		this.transportDestination = transportDestination;
	}


	public int getNetworkDestinationMaskLen() {
		return networkDestinationMaskLen;
	}


	public void setNetworkDestinationMaskLen(int networkDestinationMaskLen) {
		this.networkDestinationMaskLen = networkDestinationMaskLen;
	}


	public int getNetworkSourceMaskLen() {
		return networkSourceMaskLen;
	}


	public void setNetworkSourceMaskLen(int networkSourceMaskLen) {
		this.networkSourceMaskLen = networkSourceMaskLen;
	}

	public Boolean isRedirect(){
		if(this.dataLayerDestination.equals("00:00:00:00:00:00")||this.dataLayerSource.equals("00:00:00:00:00:00"))
			return false;
		return true;
	}
	
}
