package com.sds.securitycontroller.policy.resolver;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.restlet.data.Form;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.policy.AtomPolicy;
import com.sds.securitycontroller.policy.ByodInitActionArgs;
import com.sds.securitycontroller.policy.FlowPolicyObject;
import com.sds.securitycontroller.policy.PolicyAction;
import com.sds.securitycontroller.policy.PolicyActionArgs;
import com.sds.securitycontroller.policy.PolicyActionType;
import com.sds.securitycontroller.policy.PolicyInfo;
import com.sds.securitycontroller.policy.PolicyRecord;
import com.sds.securitycontroller.policy.PolicySubject;
import com.sds.securitycontroller.policy.PolicySubject.PolicySubjectType;
import com.sds.securitycontroller.policy.RedirectFlowRoutingItem;
import com.sds.securitycontroller.policy.RedirectingFlowActionArgs;
import com.sds.securitycontroller.policy.TrafficPattern;
import com.sds.securitycontroller.utils.DPID;

public class PolicyResource extends ServerResource implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7505297594337898550L;
	protected static Logger log = LoggerFactory.getLogger(PolicyResource.class);
	String policyId = null;
	IPolicyResolveService policyResolver = null;
	
	@Override
	public void doInit(){
		policyResolver = (IPolicyResolveService)getContext().getAttributes().
				get(IPolicyResolveService.class.getCanonicalName());
		if(getRequestAttributes().get("id")!=null)
			policyId = (String) getRequestAttributes().get("id");
	}
	
	@Get("json")
	public Object handleGetData(){
		PolicySubject policySubject = null;
		PolicyActionType policyActionType = null;
		
		Form queryParams = getRequest().getResourceRef().getQueryAsForm();
		if(queryParams.getFirstValue("subject") != null)
			policySubject =new PolicySubject("", PolicySubjectType.valueOf(queryParams.getFirstValue("subject").toUpperCase()));
		if(queryParams.getFirstValue("type") != null)
			policyActionType = PolicyActionType.valueOf(queryParams.getFirstValue("type").toUpperCase()); 		
		
		Collection<PolicyRecord> records = policyResolver.getPolicyRecords(policySubject, policyActionType);
		
		if(records==null){
			return "[]";
		}
		/*List<Object> objList=new ArrayList<Object>();
		for (PolicyRecord policyRecord : records) {
			ObjectMapper mapper = new ObjectMapper();
			Object object = policyRecord.policy.getObject().getType();
			String string;
			try {
				string = mapper.writeValueAsString(object);
				System.out.println(string);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}*/
		
		
    	JsonFactory jasonFactory = new JsonFactory();
        StringWriter writer = new StringWriter();
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);

        	generator.writeStartObject();
        	generator.writeStringField("status", "ok");
        	
        	generator.writeArrayFieldStart("policies");
        	for(PolicyRecord rec:records){
//        		generator.writeStartObject();
//        		generator.writeStringField("id", rec.getId());
//        		generator.writeStringField("appid", rec.policy.getAppID());
        		
//        		generator.writeStartObject();
        		generator.writeRawValue(AtomPolicy.serializePolicy(rec.policy));
//        		generator.writeEndObject();
//        		
//        		generator.writeEndObject();
        		
        		/*
        		generator.writeStartObject();
        		generator.writeStringField("id", rec.getId());
        		generator.writeStringField("appid", rec.policy.getAppID());
        		// "object":{
        		generator.writeObjectFieldStart("object");
//	        		"type":"VM",//策略的对象，对应原有策略的POLICY_SCALE参数，包括：FLOW,VM,USER,TENANT
        		generator.writeStringField("type", rec.policy.object.getType().toString());
//	        		"id":"xxx",//对象的ID（？FLOW是否有全局唯一ID？）
        		generator.writeStringField("id", rec.policy.object.getId());
//	        		"flow_match"://仅当type为FLOW时有效，根据3、4层的五元组匹配流
        		if(!rec.policy.object.resolvedFlowMatches.isEmpty()){
        			FlowMatch flowMatch1 = rec.policy.object.resolvedFlowMatches.get(0);
        			generator.writeObjectFieldStart("flow_match");
//	        		{
//	        			"dataLayerDestination":"fa:16:3e:4e:3c:20",
//	        			"dataLayerSource":"fa:16:3e:3a:fb:f2",
        			generator.writeStringField("dataLayerSource", flowMatch1.getdataLayerSource());
//	        			"dataLayerType":"0x0806", 	
//	        			"dataLayerVirtualLan":-1, 	
//	        			"dataLayerVirtualLanPriorityCodePoint":0, 	
//	        			"networkDestination":"80.0.0.3",
//	        			"networkDestinationMaskLen":32, 	
//	        			"networkProtocol":2, 	
//	        			"networkSource":"80.0.0.12", 	
//	        			"networkSourceMaskLen":32, 	
//	        			"transportDestination":0, 	
//	        			"transportSource":0
//	        		},
        			generator.writeEndObject();
        		}
//	        		"traffic_direction":"IN",	//仅当type不是FLOW的时候有效，值为IN或OUT，指明策略的作用对象是从目标主机（或目标用户/租户所拥有的主机）流进还是流出的流量
//        		"traffic_pattern":{
        		TrafficPattern trafficPattern = rec.policy.object.getTrafficPattern();
        		if(trafficPattern!=null){
        			generator.writeObjectFieldStart("traffic_pattern");
//        			//仅当type不是FLOW的时候有效，指明策略作用的目标主机（或目标用户/租户所拥有的主机）和流量特征
//        					"object_host":{
//        			//指定目标主机
//        						"ip_addr":””,
//        			//仅当type为VM时有效，指定目标主机的IP地址（支持掩码）
//        						"ignored_ip_list":[”192.168.19.1”,”192.168.19.2”]
//        			//仅当type为USER/TANENT/SUBNET时有效，当type为上述值时，默认目标主机为属于该用户/租户/子网的所有主机，配置ignored_ip_list表示排除一部分主机（支持掩码）
//        			},
//        			"object_hw_addr":{
        			generator.writeObjectFieldStart("object_hw_addr");
//        				"src_mac":"",
        			generator.writeStringField("src_mac", trafficPattern.getSrcMac());
//        				"dst_mac":""
//        			}
        			generator.writeEndObject();
//        			"traffic_details”:{ 
//        			//描述流量特征
//        			" direction":"IN",
//        			//流量方向，值为IN/OUT，指明策略的作用对象是从目标主机流进还是流出的流量
//        						"target_ip":"",
//        			//指定目标主机流进/流出的流量指向的IP（当direction=IN时，target是源，目标主机是目的，反之亦然），可缺省
//        						"service_port":"80,8080",
//        			//指定流量所属服务端口，可缺省，支持表达端口范围，如80,8080或1000-2000
//        						"service_details":""
//        			//服务细节，保留字段
//        			}
//        					"description":”匹配：流向属于User1用户的、除了IP为192.168.19.1和192.168.19.2的所有主机、且服务端口为80或8080的流量”
//        			}
            		generator.writeEndObject();
        		}
//	        	},
        		generator.writeEndObject();

        	
//        		generator.writeEndObject();
        		
//        		"action":{
        		generator.writeObjectFieldStart("action");
//        			//对目标的动作
//        			"type":"REDIRECT_FLOW",	
        		generator.writeStringField("type", rec.policy.action.getType().toString());
//	        		"argument":{
        		generator.writeObjectFieldStart("argument");
        		PolicyActionArgs args=rec.policy.action.getActionArgs();
        		//generator.writeObject(null);
//	        			//动作参数
//	        			"requirement":{
//	        			//执行动作的需求，如需要设备的能力等
//	        				
//	        			},	
//	        			"redirect_routing":
//	        			//仅当type为REDIRECT_FLOW时有效，详细定义流量重定向的操作
//	        			{
//	        				"hop_count":2,
//	        			//重定向的跳数
//	        				"description":"来自外网的流量，先通过WAF，再通过IPS，最后到达目标主机",
//	        				"routing":[
//	        			//设备连接顺序（按流量流经顺序）
//	        			{"sequence":1, 
//	        					"device_type":"WAF"
//	        			//设备类型，包括IPS/IDS/DPI/WAF/...，OBJDEV指的是在object里定义的目标设备（主机），OBJDEV可能位于路径的两端或中间
//	        					},
//	        				{"sequence":2,
//	        					"device_type":"IPS"
//	        				},
//	        				{"sequence":3,
//	        					"device_type":"OBJDEV"
//	        				}]
//	        			}
//	        		}
        			generator.writeEndObject();
//        		}
        		generator.writeEndObject();


//        		generator.writeStringField("objecttype", rec.policy.object.getType().toString());
//        		generator.writeStringField("actiontype", rec.policy.action.getType().toString());
        		
        		generator.writeEndObject();
        		*/
        	}
        	generator.writeEndArray();
        	
        	generator.writeEndObject();
        	
        	generator.close();
        	return writer.toString();
        	
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"status\" : \"error\", \"result\" : \"getting apps failed: "+e.getMessage()+"\"}"; 
		}
	}

	@Post
	public String handlePostdata(String fmJson) {
		log.info(" Processing a new policy:\n {}",fmJson);
		String status = "";
		String policyId = null;
		PolicyInfo policyInfo = null;
		try {
			policyInfo = decodeCompoundPolicyJson(fmJson);
			policyId = this.policyResolver.generateNewPolicy(policyInfo);
			status = "ok";
		} catch (IOException e) {
			log.error("Error parsing new command: " + fmJson, e);
			return "{\"status\" : \"error\", \"details\" : \""+e.toString()+". \"}";
		} catch (Exception e) {
			log.error("Error creating new command: ", e);
			return "{\"status\" : \"error\", \"details\" : \""+e.toString()+". \"}";
		}

		JsonFactory jasonFactory = new JsonFactory();
		StringWriter writer = new StringWriter();
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject();
			generator.writeStringField("status", status);
			generator.writeObjectFieldStart("result");
			generator.writeBooleanField("success", true);
			if(policyInfo != null)
				generator.writeStringField("policy_id", policyId);
			generator.writeEndObject();
			generator.writeEndObject();
			generator.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "{\"status\" : \"error\", \"details\" : \"json conversion failed. \"}";
		}

		return writer.toString();
	}


	@Delete
	public String handleDeleteData(String fmJson) {
		String status = "";
		if(policyId == null){
			log.error("Error deleting policy: no policy id provided");
			return "{\"status\" : \"error\", \"details\" : \"no policy id provided. \"}";
		}
		log.info(" Processing deleting a policy: " + policyId);
		try {
			this.policyResolver.deletePolicy(policyId);
			status = "ok";
		} catch (Exception e) {
			log.error("Error deleting policy: ", e);
			return "{\"status\" : \"error\", \"details\" : \""+e.toString()+". \"}";
		}

		JsonFactory jasonFactory = new JsonFactory();
		StringWriter writer = new StringWriter();
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject();
			generator.writeStringField("status", status);
			generator.writeObjectFieldStart("result");
			generator.writeBooleanField("success", true);
			generator.writeStringField("policy_id", policyId);
			generator.writeEndObject();
			generator.writeEndObject();
			generator.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "{\"status\" : \"error\", \"details\" : \"json conversion failed. \"}";
		}
		return writer.toString();
	}
	
	/**
	 * Method that decode post Json into com.sds.policy.Policy format
	 */
	public static PolicyInfo decodeCompoundPolicyJson(String fmJson) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(fmJson);

		String appID = rootNode.path("appid").asText();
		JsonNode policyNode = rootNode;//.path("policy");
		
		PolicyActionType atype = PolicyActionType.valueOf(policyNode.path("actiontype")
				.asText());
		short priority = (short) rootNode.path("priority").asInt();
		PolicySubject subject = new PolicySubject("",
				PolicySubjectType.valueOf(policyNode
				.path("subject").asText()));
		boolean negated = policyNode.path("negated").asBoolean();
		List<AtomPolicy> atomlist = new ArrayList<AtomPolicy>();
		
		int f = policyNode.path("force").asInt();
		boolean force = (f == 1) ?true: false;

		// resolve policy objects
		JsonNode objNodes = policyNode.path("objects");
		for(int i=0; i<objNodes.size();i++){
			JsonNode subobj = objNodes.get(i);
			FlowPolicyObject policyObject = new FlowPolicyObject();
			String id = subobj.path("id").asText();
			policyObject.setId(id);
			JsonNode flowMatchJsonNode = subobj.path("flow_match");
			FlowMatch flowMatch = null;
			// parse flow matches
			if(!flowMatchJsonNode.isMissingNode()){
				// resolve flow match
				String src_mac = (flowMatchJsonNode.path("src_mac").isMissingNode())?null:flowMatchJsonNode.path("src_mac").asText();
				String dst_mac = (flowMatchJsonNode.path("dst_mac").isMissingNode())?null:flowMatchJsonNode.path("dst_mac").asText();
				String src_ip = (flowMatchJsonNode.path("src_ip").isMissingNode())?null:flowMatchJsonNode.path("src_ip").asText();
				String dst_ip = (flowMatchJsonNode.path("dst_ip").isMissingNode())?null:flowMatchJsonNode.path("dst_ip").asText();
				int src_port = (flowMatchJsonNode.path("src_port").isMissingNode())?0:flowMatchJsonNode.path("src_port").asInt();
				int dst_port = (flowMatchJsonNode.path("dst_port").isMissingNode())?0:flowMatchJsonNode.path("dst_port").asInt();
				int proto =(flowMatchJsonNode.path("proto").isMissingNode())?0: flowMatchJsonNode.path("proto").asInt();
				flowMatch = new FlowMatch();
				
				flowMatch.setDataLayerSource(src_mac);
				flowMatch.setDataLayerDestination(dst_mac);
				flowMatch.setNetworkSource(src_ip);
				flowMatch.setNetworkDestination(dst_ip);
				flowMatch.setTransportSource((short)src_port);
				flowMatch.setTransportDestination((short)dst_port);
				flowMatch.setNetworkProtocol(proto);
			}
			
			JsonNode trafficPatternJsonNode = subobj.path("traffic_pattern");
			if(!trafficPatternJsonNode.isMissingNode()){
				TrafficPattern tp = new TrafficPattern();
				if(!trafficPatternJsonNode.path("src_mac").isMissingNode()){
					tp.setSrcMac( trafficPatternJsonNode.path("src_mac").asText());
				}
				if(!trafficPatternJsonNode.path("sw_port_name").isMissingNode()){
					tp.setInPortName( trafficPatternJsonNode.path("sw_port_name").asText());
				}
				policyObject.setTrafficPattern(tp);
			}
			
			if(flowMatch!=null)
				policyObject.getResolvedFlowMatches().add(flowMatch);
			KnowledgeType objectType = KnowledgeType.valueOf(subobj.path("type").asText());
			policyObject.setType(objectType);
			// resolve policy action
			JsonNode actionNode = policyNode.path("action");
			PolicyActionType actionType = PolicyActionType.valueOf( actionNode.path("type").asText() );
			// parse policy action args according to action type 
			JsonNode argsNode = actionNode.path("args");
			PolicyActionArgs actionArgs = null;
			if(!argsNode.isMissingNode()){
				// parse action requirements 
				switch(actionType){
					case REDIRECT_FLOW:
						actionArgs = new RedirectingFlowActionArgs();
						JsonNode redirectRoutingNode = argsNode.path("redirect_routing").path("routing");
						if(!redirectRoutingNode.isMissingNode()){
							for(JsonNode redirectRoutingPointNode:redirectRoutingNode){
								int sequence=redirectRoutingPointNode.path("sequence").asInt();
								String devType = redirectRoutingPointNode.path("device_type").asText();
								RedirectFlowRoutingItem routingItem = new RedirectFlowRoutingItem();
								routingItem.sequence=(short) sequence;
								routingItem.deviceType=DeviceType.valueOf(devType);
								((RedirectingFlowActionArgs)actionArgs).addRedirectRouting(routingItem);
							}
						}
						break;
					case BYOD_INIT:
						/*
							"dpid": 128983852086,
				            "inPort": 3,
				            "serverIp": "100.0.0.14",
				            "serverMac": "fa:16:3e:8f:fc:73",
				            "network": "111.0.0.0",
				            "mask": 24,
						 * */
						long dpid = -1;
						short inPort = -1;
						String serverIp = null;
						String serverMac = null;
						String network = null;
						short mask = 0;
						try {
							dpid = DPID.longValueOf(argsNode.path("dpid").asText());
							inPort = (short)argsNode.path("inPort").asInt();
							serverIp = argsNode.path("serverIp").asText();
							serverMac =argsNode.path("serverMac").asText();
							network = argsNode.path("network").asText();
							mask = (short)argsNode.path("mask").asInt();
						} catch (Exception e) {
							log.error(e.toString());
							return null;
						}
						
						actionArgs = new ByodInitActionArgs(dpid, inPort, serverIp,
								serverMac, network, mask);
						break;
					default:
						break;
				}
			}
			PolicyAction policyAction = new PolicyAction(actionType, actionArgs);
			
			AtomPolicy atom = new AtomPolicy(policyObject,policyAction);
			atom.setSubId(appID);
			atom.setPriority(priority);
			atomlist.add(atom);
		}
		
		AtomPolicy[] atomarray = new AtomPolicy[atomlist.size()];
		atomlist.toArray(atomarray);
		PolicyInfo policyInfo = new PolicyInfo(appID, atomarray, force,subject,negated,atype);
		policyInfo.setPriority(priority);
		log.info("SA have generated policy. Policy is: {}", policyInfo.toString());
		return policyInfo;
	}
}
