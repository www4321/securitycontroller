/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.flow.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.flow.ActionType;
import com.sds.securitycontroller.flow.FlowAction;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.flow.FlowMatch;

public class FlowInfoJsonParser {
	protected static Logger log = LoggerFactory.getLogger(FlowPollingService.class);
	
	/**
	 * Decode the json based on the given dpid and jParser. jParser should be initialized
	 * in polling.java.
	 * @param JsonNode rootNode
	 * @return HashMap<String, List<FlowBean>>
	 * @throws IOException 
	 */
	public static List<FlowInfo> DecodeFlowJson (JsonNode rootNode) throws IOException{
		
		//FlowBean List consists of one / many flowbean. A flowbean list mapps to unique dpid
		List<FlowInfo> fblist = new ArrayList<FlowInfo>();
		/**
		 * {
            "actions": [
                {
                    "port": 6,
                    "maxLength": -1,
                    "length": 8,
                    "type": "OUTPUT",
                    "lengthU": 8
                }
            ],
            "priority": 0,
            "cookie": 9007199254740992,
            "idleTimeout": 5,
            "hardTimeout": 0,
            "match": {
                "dataLayerDestination": "fa:16:3e:fb:b1:73",
                "dataLayerSource": "fa:16:3e:71:04:d5",
                "dataLayerType": "0x0000",
                "dataLayerVirtualLan": -1,
                "dataLayerVirtualLanPriorityCodePoint": 0,
                "inputPort": 1,
                "networkDestination": "0.0.0.0",
                "networkDestinationMaskLen": 0,
                "networkProtocol": 0,
                "networkSource": "0.0.0.0",
                "networkSourceMaskLen": 0,
                "networkTypeOfService": 0,
                "transportDestination": 0,
                "transportSource": 0,
                "wildcards": 2629872
            },
            "durationSeconds": 0,
            "durationNanoseconds": 987000000,
            "packetCount": 2,
            "byteCount": 720,
            "tableId": 0
        }
		 */
		
		Iterator<Entry<String, JsonNode>> iter = rootNode.fields();
		
		while(iter.hasNext()){
			//a switch
			Entry<String, JsonNode> entry= iter.next();
			String dpid = entry.getKey();	
			//log.info(dpid);
			JsonNode sw = entry.getValue(); 
					

			if(!sw.isArray())
				log.error("switch node not a array: "+ sw.toString());
			for(int j = 0; j<sw.size();j++){
				//a flow
				FlowInfo fb = new FlowInfo();
				JsonNode flow = sw.get(j);
				
				Iterator<JsonNode> actions = flow.path("actions").iterator();

				FlowMatch match = new FlowMatch();
				List<FlowAction> actionslist = new ArrayList<FlowAction>();
				while(actions.hasNext()){
					// a action
					JsonNode action = actions.next();
					FlowAction ac = new FlowAction();
					JsonNode type = action.path("type");
					ac.setType(Enum.valueOf(ActionType.class, type.asText()));
				//	log.info("type="+Enum.valueOf(ActionType.class, type.asText()));
					if (Enum.valueOf(ActionType.class, type.asText()).equals(Enum.valueOf(ActionType.class,"OUTPUT")))
				{
					JsonNode port=action.path("port");
					   	ac.setPort(port.asInt());
					//   	log.info(port.asInt());
					   
					    //int Outport=port.asInt();
						JsonNode maxLength = action.path("maxLength");
						ac.setMaxlength(maxLength.asInt());
						JsonNode length = action.path("length");
						ac.setLength((short)length.asInt());
						
						JsonNode lengthU = action.path("lengthU");
						ac.setLengthU(lengthU.asInt());
						 actionslist.add(ac);
				}
						if(Enum.valueOf(ActionType.class, type.asText()).equals(Enum.valueOf(ActionType.class,"SET_VLAN_PCP")))
						{
						JsonNode virtualLanPriorityCodePoint= action.path("virtualLanPriorityCodePoint");
					   	ac.setvirtualLanPriorityCodePoint(virtualLanPriorityCodePoint.asInt());  
						//log.info(virtualLanPriorityCodePoint.asInt());
						JsonNode length = action.path("length");
						ac.setLength((short)length.asInt());
						
						JsonNode lengthU = action.path("lengthU");
						ac.setLengthU(lengthU.asInt());
						 actionslist.add(ac); 
						}
						if(Enum.valueOf(ActionType.class, type.asText()).equals(Enum.valueOf(ActionType.class,"SET_VLAN_ID")))
						{
						JsonNode virtualLanIdentifier=  action.path("virtualLanIdentifier");
					   	ac.setvirtualLanIdentifier(virtualLanIdentifier.asInt());  
					   	JsonNode length = action.path("length");
						ac.setLength((short)length.asInt());
						//log.info(virtualLanIdentifier.asInt());
						JsonNode lengthU = action.path("lengthU");
						ac.setLengthU(lengthU.asInt());
						 actionslist.add(ac);	
						}
						if(Enum.valueOf(ActionType.class, type.asText()).equals(Enum.valueOf(ActionType.class,"SET_NW_SRC")))
						{
						JsonNode networkAddress= action.path("networkAddress");
					   	ac.setnetworkAddress(networkAddress.asText());  
					 	JsonNode length = action.path("length");
						ac.setLength((short)length.asInt());
						JsonNode lengthU = action.path("lengthU");
						ac.setLengthU(lengthU.asInt());
						 actionslist.add(ac);
						}
						if(Enum.valueOf(ActionType.class, type.asText()).equals(Enum.valueOf(ActionType.class,"SET_NW_DST")))
						{
						JsonNode networkAddress= action.path("networkAddress");
					   	ac.setnetworkAddress(networkAddress.asText());  
					 	JsonNode length = action.path("length");
						ac.setLength((short)length.asInt());
						JsonNode lengthU = action.path("lengthU");
						ac.setLengthU(lengthU.asInt());
						 actionslist.add(ac);
						}
						if(Enum.valueOf(ActionType.class, type.asText()).equals(Enum.valueOf(ActionType.class,"SET_DL_SRC")))
						{
						JsonNode dataLayerAddress= action.path("dataLayerAddress");
					   	ac.setdataLayerAddress(dataLayerAddress.asText());  
					   	JsonNode length = action.path("length");
						ac.setLength((short)length.asInt());
						JsonNode lengthU = action.path("lengthU");
						ac.setLengthU(lengthU.asInt());
						 actionslist.add(ac);
						}
						if(Enum.valueOf(ActionType.class, type.asText()).equals(Enum.valueOf(ActionType.class,"SET_DL_DST")))
						{
						JsonNode dataLayerAddress= action.path("dataLayerAddress");
					   	ac.setdataLayerAddress(dataLayerAddress.asText());  
					   	JsonNode length = action.path("length");
						ac.setLength((short)length.asInt());
						JsonNode lengthU = action.path("lengthU");
						ac.setLengthU(lengthU.asInt());
						 actionslist.add(ac);
						}
						if(Enum.valueOf(ActionType.class, type.asText()).equals(Enum.valueOf(ActionType.class,"SET_TP_SRC")))
						{
						JsonNode transportPort= action.path("transportPort");
					   	ac.settransportPort(transportPort.asInt());  
					  	JsonNode length = action.path("length");
						ac.setLength((short)length.asInt());
						JsonNode lengthU = action.path("lengthU");
						ac.setLengthU(lengthU.asInt());
						 actionslist.add(ac);
						}
						if(Enum.valueOf(ActionType.class, type.asText()).equals(Enum.valueOf(ActionType.class,"SET_TP_DST")))
						{
						JsonNode transportPort= action.path("transportPort");
					   	ac.settransportPort(transportPort.asInt());  
					  	JsonNode length = action.path("length");
						ac.setLength((short)length.asInt());
						JsonNode lengthU = action.path("lengthU");
						ac.setLengthU(lengthU.asInt());
						 actionslist.add(ac);
						}
				}
				
				fb.setDpid(dpid);
				fb.setActions(actionslist);
				
				
				JsonNode priority = flow.path("priority");
				fb.setPriority(priority.asLong());
//				log.info("priority="+priority.asLong());
				
				JsonNode cookie = flow.path("cookie");
				fb.setCookie(cookie.asLong());
//				log.info("cookie="+cookie.asLong());
				
				JsonNode idleTimeout = flow.path("idleTimeout");
				fb.setIdleTimeout(idleTimeout.asInt());
//				log.info("idleTimeout="+idleTimeout.asInt());
				
				JsonNode hardTimeout = flow.path("hardTimeout");
				fb.setHardTimeout(hardTimeout.asInt());
//				log.info("hardTimeout="+hardTimeout.asInt());
				
				JsonNode dataLayerDestination = flow.path("match").path("dataLayerDestination");
				match.setDataLayerDestination(dataLayerDestination.asText());
//				log.info("dataLayerDestination="+dataLayerDestination.asText());
				
				JsonNode dataLayerSource = flow.path("match").path("dataLayerSource");
				match.setDataLayerSource(dataLayerSource.asText());
//				log.info("dataLayerSource="+dataLayerSource.asText());
				
				JsonNode dataLayerType = flow.path("match").path("dataLayerType");
				if(dataLayerType.asText().startsWith("0x")){
					match.setDataLayerType(Integer.parseInt(dataLayerType.asText().substring(2), 16));
				}else {
					match.setDataLayerType(Integer.parseInt(dataLayerType.asText(), 16));
				}
//				log.info("dataLayerType="+dataLayerType.asText());
				
				JsonNode dataLayerVirtualLan = flow.path("match").path("dataLayerVirtualLan");
				match.setDataLayerVirtualLan(dataLayerVirtualLan.asInt());
//				log.info("dataLayerVirtualLan="+dataLayerVirtualLan.asInt());
				
				JsonNode dataLayerVirtualLanPriorityCodePoint = flow.path("match").path("dataLayerVirtualLanPriorityCodePoint");
				match.setDataLayerVirtualLanPriorityCodePoint(dataLayerVirtualLanPriorityCodePoint.asInt());
//				log.info("dataLayerVirtualLanPriorityCodePoint="+dataLayerVirtualLanPriorityCodePoint.asInt());
				
				JsonNode inputPort = flow.path("match").path("inputPort");
				match.setInputPort((short)inputPort.asInt());
//				log.info("inputPort="+inputPort.asInt());
				
				JsonNode networkDestination = flow.path("match").path("networkDestination");
				match.setNetworkDestination(networkDestination.asText());
//				log.info("networkDestination="+networkDestination.asText());
				
				JsonNode networkDestinationMaskLen = flow.path("match").path("networkDestinationMaskLen");
				match.setNetworkDestinationMaskLen(networkDestinationMaskLen.asInt());
//				log.info("networkDestinationMaskLen="+networkDestinationMaskLen.asInt());
				
				JsonNode networkProtocol = flow.path("match").path("networkProtocol");
				match.setNetworkProtocol(networkProtocol.asInt());
//				log.info("networkProtocol="+networkProtocol.asInt());
				
				JsonNode networkSource = flow.path("match").path("networkSource");
				match.setNetworkSource(networkSource.asText());
//				log.info("networkSource="+networkSource.asText());
				
				JsonNode networkSourceMaskLen = flow.path("match").path("networkSourceMaskLen");
				match.setNetworkSourceMaskLen(networkSourceMaskLen.asInt());
//				log.info("networkSourceMaskLen="+networkSourceMaskLen.asInt());
				
				JsonNode networkTypeOfService = flow.path("match").path("networkTypeOfService");
				match.setNetworkTypeOfService(networkTypeOfService.asText());
//				log.info("networkTypeOfService="+networkTypeOfService.asInt());
				
				JsonNode transportDestination = flow.path("match").path("transportDestination");
				match.setTransportDestination(transportDestination.shortValue());
//				log.info("transportDestination="+transportDestination.asInt());
				
				JsonNode transportSource = flow.path("match").path("transportSource");
				match.setTransportSource(transportSource.shortValue());
//				log.info("transportSource="+transportSource.asInt());
				
				JsonNode wildcards = flow.path("match").path("wildcards");
				match.setwildcards(wildcards.asInt());
//				log.info("wildcards="+wildcards.asInt());
				
				//add match to flowbean
				fb.setMatch(match);
				
				JsonNode durationSeconds = flow.path("durationSeconds");
				fb.setDurationSeconds(durationSeconds.asInt());
//				log.info("durationSeconds="+durationSeconds.asInt());
				
				JsonNode durationNanoseconds = flow.path("durationNanoseconds");
				fb.setDurationNanoseconds(durationNanoseconds.asInt());
//				log.info("durationNanoseconds="+durationNanoseconds.asInt());
				
				JsonNode packetCount = flow.path("packetCount");
				fb.setPacketCount(packetCount.asLong());
//				log.info("packetCount="+packetCount.asInt());
				
				JsonNode byteCount = flow.path("byteCount");
				fb.setByteCount(byteCount.asLong());
//				log.info("byteCount="+byteCount.asInt());
				
				JsonNode tableId = flow.path("tableId");
				fb.setTableId(tableId.asInt());
				
				
				//add flow to the flow list
				fblist.add(fb);
			//	if(j<20)
			//		log.debug("{}",fb);
			//	else if(j<10)
			//		log.debug("\t......");
				
			}	
		}
		return fblist;
	}	

}
