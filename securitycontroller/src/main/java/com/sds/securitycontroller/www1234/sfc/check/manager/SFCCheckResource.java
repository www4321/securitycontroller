package com.sds.securitycontroller.www1234.sfc.check.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sds.securitycontroller.flow.ActionType;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.www1234.servicefunction.manager.IServiceFunctionService;
import com.sds.securitycontroller.www1234.sfc.SFCFlowInfo;
import com.sds.securitycontroller.www1234.sfc.manager.ISFCService;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SFCCheckResource extends ServerResource{
	protected IServiceFunctionService serviceFunctionService = null;
	protected ISFCCheckService SFCCheckManager = null;
	protected ISFCService sfcManager =null;
	protected static Logger log = LoggerFactory.getLogger(SFCCheckResource.class);
	protected String sfcid;
	
	@Override
	protected void doInit() throws ResourceException {
		this.SFCCheckManager = (ISFCCheckService)getContext().getAttributes().get(ISFCCheckService.class.getCanonicalName());
		this.sfcManager = (ISFCService)getContext().getAttributes().get(ISFCService.class.getCanonicalName());
		this.sfcid = (String) getRequestAttributes().get("sfcId");
	}
	
	
	//Rest API URL : http://10.102.26.44:8888/sc/checksfc/sfc-id-1
	/**
	 * {
	 * 	"sfc-id":"id1"
	 * }
	 * @param fmjson
	 * @return
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 * 
	 *     {
	 *       "sfc-id":"",
	 *       "result":[{
	 *       	"SFC-rule":{
	 *       		"in-port":"",
	 *          	"src-ip":""
	 *       	},
	 *          "conflict-rules":[
	 *          	{
	 *          		"in-port":"",
	 *          		"src-ip":""
	 *          	},
	 *          	{
	 *          		"in-port":"",
	 *          		"src-ip":""
	 *          	}]
	 *       },
	 *       {
	 *       	"SFC-rule":{
	 *       		"in_port":"",
	 *          	"src-ip":""
	 *       	},
	 *          "conflict-rules":[
	 *          	{
	 *          		"in-port":"",
	 *          		"src-ip":""
	 *          	},
	 *          	{
	 *          		"in-port":"",
	 *          		"src-ip":""
	 *          	}]
	 *       }]
	 *    }
	 */
	@SuppressWarnings("unchecked")
	@Get
	public Representation  sfcCheckenable(String fmjson) throws JsonProcessingException, IOException{
		boolean result = false;
        String sfcid = "sfc-id-1";
        sfcid = this.sfcid;
        result = SFCCheckManager.sfcCheckbySwitchPort(sfcid);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("sfc-id", sfcid);
        log.info("the SFC policy named "+sfcid+" will be checked");
        if(result == false){
        	log.info("SFC policy is enforcement correctly");
        	map.put("result", "no conflict flow rules");
        }else{
        	log.info("SFC policy is  not enforcement correctly");
        	List<ConflictFlowInfo> conflictFlowRules = SFCCheckManager.getConflictRules(sfcid);
        	map.put("result", parseConflictFlowInfo(conflictFlowRules));
        }
        log.info(JSONObject.fromObject(map).toString());
		return new JacksonRepresentation<JSONObject>(JSONObject.fromObject(map));
	}
	/**
	 *      [
	 *      {
	 *       	"SFC-rule":{
	 *       		"in-port":"",
	 *          	"src-ip":""
	 *       	},
	 *          "conflict-rules":[
	 *          	{
	 *          		"in-port":"",
	 *          		"src-ip":""
	 *          	},
	 *          	{
	 *          		"in_port":"",
	 *          		"src-ip":""
	 *          	}]
	 *       },
	 *       {
	 *       	"SFC-rule":{
	 *       		"in-port":"",
	 *          	"src-ip":""
	 *       	},
	 *          "conflict-rules":[
	 *          	{
	 *          		"in-port":"",
	 *          		"src-ip":""
	 *          	},
	 *          	{
	 *          		"in-port":"",
	 *          		"src-ip":""
	 *          	}]
	 *       }]
	 * 
	 * @param conflictFlowRules
	 * @return
	 */
	public JSONArray parseConflictFlowInfo(List<ConflictFlowInfo> conflictFlowRules){
		ConflictFlowInfo conflictFlowInfo = null;
		List<JSONObject> conflicts  =  new ArrayList<JSONObject>();
		Map<String,Object> conflict = new HashMap<String,Object>();
		SFCFlowInfo sfcFlowInfo = null;
		List<FlowInfo> flowInfos =null;
		for(int i=0 ; i < conflictFlowRules.size() ; i++ ){
			conflictFlowInfo = conflictFlowRules.get(i);
			sfcFlowInfo = conflictFlowInfo.getSfcFlowInfo();
			flowInfos = conflictFlowInfo.getConflictFlows();
			conflict.put("SFC-rule", sfcFlowInfo2JsonObject(sfcFlowInfo));
			conflict.put("conflict-rules", flowInfos2JsonArray(flowInfos));
			conflicts.add(JSONObject.fromObject(conflict));
		}	
		
		return JSONArray.fromObject(conflicts);
	}
	
	private JSONObject sfcFlowInfo2JsonObject(SFCFlowInfo sfcFlowInfo){
		Map<String,String> map = new HashMap<String,String>();
		if(!sfcFlowInfo.getDataLayerSource().equals("00:00:00:00:00:00"))
			map.put("src-mac", sfcFlowInfo.getDataLayerSource());
		if(!sfcFlowInfo.getDataLayerDestination().equals("00:00:00:00:00:00"))
			map.put("dst-mac", sfcFlowInfo.getDataLayerDestination());
		if(sfcFlowInfo.getTransportSource()!=0)
			map.put("src-port", ""+sfcFlowInfo.getTransportSource());
		if(sfcFlowInfo.getTransportDestination()!=0)
			map.put("dst-port", ""+sfcFlowInfo.getTransportDestination());
		if(sfcFlowInfo.getNetworkSourceMaskLen()==32)
			map.put("src-ip",sfcFlowInfo.getNetworkSource());
		else map.put("src-ip",sfcFlowInfo.getNetworkSource()+"/"+sfcFlowInfo.getNetworkSourceMaskLen());
		if(sfcFlowInfo.getNetworkDestinationMaskLen()==32)
			map.put("dst-ip",sfcFlowInfo.getNetworkDestination());
		else map.put("dst-ip",sfcFlowInfo.getNetworkDestination()+"/"+sfcFlowInfo.getNetworkDestinationMaskLen());
		map.put("protocol", ""+sfcFlowInfo.getNetworkProtocol());
		map.put("dpid", sfcFlowInfo.getDpid());
		map.put("in-port", ""+sfcFlowInfo.getIn_port());
		map.put("out-port", ""+sfcFlowInfo.getOut_port());
		map.put("priority", ""+sfcFlowInfo.getPriority());
		
		log.info("\n the flow info installed by SFC policy: "+JSONObject.fromObject(map));
		return JSONObject.fromObject(map);
	}
	private JSONArray flowInfos2JsonArray(List<FlowInfo> flowInfos){
		List<JSONObject> conflicts  =  new ArrayList<JSONObject>();
		FlowInfo flowInfo = null;
		for(int i=0;i<flowInfos.size();i++){
			flowInfo = flowInfos.get(i);
			conflicts.add(flowInfo2JSONObject(flowInfo));
		}
		log.info("\n the rules conflicting with the SFC flow rule. : "+JSONArray.fromObject(conflicts));
		return JSONArray.fromObject(conflicts);
	}
	private JSONObject flowInfo2JSONObject(FlowInfo flowInfo){
		Map<String,String> map = new HashMap<String,String>();
		FlowMatch flowMatch = flowInfo.getMatch();
		map.put("dpid", flowInfo.getDpid());
		map.put("priority", ""+flowInfo.getPriority());
		map.put("cookie", ""+flowInfo.getCookie());
		if(!flowMatch.getDataLayerSource().equals("00:00:00:00:00:00"))
			map.put("src-mac", flowMatch.getDataLayerSource());
		if(!flowMatch.getDataLayerDestination().equals("00:00:00:00:00:00"))
			map.put("dst-mac", flowMatch.getDataLayerDestination());
		if(flowMatch.getTransportSource()!=0)
			map.put("src-port", ""+flowMatch.getTransportSource());
		if(flowMatch.getTransportDestination()!=0)
			map.put("dst-port", ""+flowMatch.getTransportDestination());
		if(flowMatch.getNetworkSourceMaskLen()==32)
			map.put("src-ip",flowMatch.getNetworkSource());
		else map.put("src-ip",flowMatch.getNetworkSource()+"/"+flowMatch.getNetworkSourceMaskLen());
		if(flowMatch.getNetworkDestinationMaskLen()==32)
			map.put("dst-ip",flowMatch.getNetworkDestination());
		else map.put("dst-ip",flowMatch.getNetworkDestination()+"/"+flowMatch.getNetworkDestinationMaskLen());
		if(flowMatch.getNetworkProtocol()!=0)
			map.put("protocol", ""+flowMatch.getNetworkProtocol());
		if(flowMatch.getInputPort()!=0)
			map.put("in-port", ""+flowMatch.getInputPort());
		int out_port = 0;
		for(int k=0;k<flowInfo.getActions().size();k++){
			if(flowInfo.getActions().get(k).getType().equals(Enum.valueOf(ActionType.class,"OUTPUT")))
				out_port = flowInfo.getActions().get(k).getPort();
		}
		map.put("out-port", ""+out_port);
		//log.info("flowInfo2JSONObject: "+JSONObject.fromObject(map));
		return JSONObject.fromObject(map);
	}
}
