package com.sds.securitycontroller.knowledge.globaltraffic;
//import java.io.IOException;
//
//import java.util.ArrayList;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.knowledge.globaltraffic.analyzer.FlowIngeration;

//import flow.FlowInfo;
//import flow.FlowInfoJsonParser;
//import flow.CaculateFlowPath;

public class MatchPath implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6574446573921170575L;
	protected static Logger log = LoggerFactory.getLogger(MatchPath.class);
	LinkedList<FlowMatch> matchlist;
	LinkedList<NodePortTuple> pathlink;
	public long packetCount=0;
	public long byteCount=0;
	public String createTime = "";
	public String lastTime = "";
	public String finishTime = "";
	/*
	public String createTime = "";
	
	{
	    Date date = new Date();
	    long time = date.getTime();
	    SimpleDateFormat format =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        createTime = format.format(time);
	}
	*/
	
	public MatchPath(LinkedList<FlowMatch> matchlist,LinkedList<NodePortTuple> pathlink){
		this.matchlist = matchlist;
		this.pathlink = pathlink;
		

		
		for(FlowMatch fm:matchlist){
			Long[] trafficStats=FlowIngeration.trafficStatMap.get(fm.getMatch());
			if(trafficStats!=null){
				packetCount+=trafficStats[0];
				byteCount+=trafficStats[1];
			}
		}
	}
	public LinkedList<FlowMatch>  getmatchlist() {
		return matchlist;
	}
	public void setmatchlist(LinkedList<FlowMatch> matchlist) {
		this.matchlist = matchlist;
	}
	public LinkedList<NodePortTuple> getpathlink() {
		return pathlink;
	}
	public void setpathlink( LinkedList<NodePortTuple> pathlink) {
		this.pathlink = pathlink;
	}
	
	public String writeJsonString(){
		JsonFactory jasonFactory = new JsonFactory();
		StringWriter writer = new StringWriter();
		try{
         	JsonGenerator generator = jasonFactory
                     .createGenerator(writer);
         	generator.writeStartObject();
         	LinkedList<FlowMatch> matchlist = this.getmatchlist();
         	LinkedList<NodePortTuple> pathlink = this.getpathlink();
         	generator.writeStringField("createTime", this.createTime);
         	generator.writeStringField("finishTime", this.finishTime);
         	//generator.writeStringField("lastTime", this.lastTime);
         	generator.writeStringField("byteCount", Long.toString(this.byteCount));
         	generator.writeStringField("packageCount", Long.toString(this.packetCount));
            generator.writeArrayFieldStart("matchList");
            generator.writeStartObject();
            for (FlowMatch entity : matchlist){
            	generator.writeStringField("dataLayerSource", entity.getDataLayerSource());
            	generator.writeStringField("dataLayerDestination", entity.getDataLayerDestination());
            	generator.writeStringField("networkProtocol", Integer.toString(entity.getNetworkProtocol()));
            	generator.writeStringField("networkSource", entity.getNetworkSource());
            	generator.writeStringField("networkDestination", entity.getNetworkDestination());
            	generator.writeStringField("transportSource", Integer.toString(entity.getTransportSource()));
            	generator.writeStringField("transportDestination", Integer.toString(entity.getTransportDestination()));
            }
            generator.writeEndObject();
            generator.writeEndArray();

            generator.writeArrayFieldStart("linkPath");
            generator.writeStartObject();
            for (NodePortTuple entity : pathlink){
                generator.writeStringField("nodeId", entity.getNodeId());
                generator.writeStringField("portId", Integer.toString(entity.getPortId()));
            }
            generator.writeEndObject();
            generator.writeEndArray();
         	generator.writeEndObject();
         	generator.close();
         	}
		catch (IOException e) {
             log.error("json conversion failed: ", e.getMessage());
             return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "+e.getMessage()+" \"}"; 
             }
		catch (Exception e) {
        	 e.printStackTrace();
             log.error("getting app failed: ", e.getMessage());
             return "{\"status\" : \"error\", \"result\" : \"getting knowledge entity failed: "+e.getMessage()+"\"}"; 
         } 
		String resp = writer.toString();
		return resp;
	}
}