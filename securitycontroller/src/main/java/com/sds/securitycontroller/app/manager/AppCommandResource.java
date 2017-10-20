/** 
*    Copyright 2014 BUPT 
**/ 
package com.sds.securitycontroller.app.manager;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.policy.AtomPolicy;
import com.sds.securitycontroller.policy.FlowPolicyObject;
import com.sds.securitycontroller.policy.PolicyAction;
import com.sds.securitycontroller.policy.PolicyActionType;
import com.sds.securitycontroller.policy.PolicyInfo;
import com.sds.securitycontroller.policy.PolicySubject;
import com.sds.securitycontroller.policy.PolicySubject.PolicySubjectType;
import com.sds.securitycontroller.policy.resolver.PolicyEventArgs;

public class AppCommandResource extends ServerResource implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;
	protected static Logger log = LoggerFactory.getLogger(AppSubscriptionManagerResource.class);
	String id = null;
	App app = null;
	IAppManagementService appmanager = null;
	IEventManagerService eventManager = null;
	

	@Override  
    public void doInit() {    
        appmanager = 
                (IAppManagementService)getContext().getAttributes().
                get(IAppManagementService.class.getCanonicalName());
        eventManager = 
                (IEventManagerService)getContext().getAttributes().
                get(IEventManagerService.class.getCanonicalName());
        id = (String) getRequestAttributes().get("id");
        if(id != null){
        	app = appmanager.getApp(id);
        }
    }
	@Post
	public String handlePostdata(String fmJson) {
		String status = "";

		try {
			PolicyInfo policyInfo = DecodeCommandJson(fmJson);
			PolicyEventArgs args = new PolicyEventArgs(policyInfo);
			eventManager.addEvent(new Event(EventType.RECEIVED_POLICY, null,
					this, args));
			// add an event...
			log.info("_____________A RECEIVED_POLICY event added.");
			status = "ok";
		} catch (IOException e) {
			log.error("Error parsing new command: " + fmJson, e);
			status = "error";
		} catch (Exception e) {
			log.error("Error creating new command: ", e);
			status = "error";
		}

		JsonFactory jasonFactory = new JsonFactory();
		StringWriter writer = new StringWriter();
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject();
			generator.writeStringField("status", status);
			generator.writeObjectFieldStart("result");
			generator.writeBooleanField("result", true);
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
	public static PolicyInfo DecodeCommandJson(String fmJson) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(fmJson);

		String appid = rootNode.path("appid").asText();

		JsonNode policyNode = rootNode;//.path("policy");
		PolicyActionType atype = PolicyActionType.valueOf(policyNode.path("actiontype")
				.asText());
		PolicySubject subject = new PolicySubject("",
				PolicySubjectType.valueOf(policyNode
				.path("subject").asText()));
		boolean negated = policyNode.path("negated").asBoolean();
		
		List<AtomPolicy> atomlist = new ArrayList<AtomPolicy>();
		
		// resolve policy objects
		JsonNode objNodes = policyNode.path("objects");
//		JsonNode objNode = objNodes.path("object");
		//Iterator<JsonNode> iter = objNode.iterator();
		for(int i=0; i<objNodes.size();i++){
		//while (iter.hasNext()) {
			JsonNode subobj = objNodes.get(i);
			
			JsonNode flowMatchJsonNode = subobj.path("flow_match");
			// resolve flow match
			String src_mac = flowMatchJsonNode.path("src_mac").asText();
			String dst_mac = flowMatchJsonNode.path("dst_mac").asText();
			String src_ip = flowMatchJsonNode.path("src_ip").asText();
			String dst_ip = flowMatchJsonNode.path("dst_ip").asText();
			int src_port = flowMatchJsonNode.path("src_port").asInt();
			int dst_port = flowMatchJsonNode.path("dst_port").asInt();
			int proto = flowMatchJsonNode.path("proto").asInt();
			FlowMatch flowMatch = new FlowMatch();
			
			flowMatch.setDataLayerSource(src_mac);
			flowMatch.setDataLayerDestination(dst_mac);
			flowMatch.setNetworkSource(src_ip);
			flowMatch.setNetworkDestination(dst_ip);
			flowMatch.setTransportSource((short)src_port);
			flowMatch.setTransportDestination((short)dst_port);
			flowMatch.setNetworkProtocol(proto);
			
			FlowPolicyObject policyObject = new FlowPolicyObject();
			policyObject.getResolvedFlowMatches().add(flowMatch);
			
			// resolve policy action
			JsonNode actionNode = policyNode.path("action");
			PolicyActionType actionType = PolicyActionType.valueOf( actionNode.path("type").asText() );
			PolicyAction policyAction = new PolicyAction(actionType, null);
			
			AtomPolicy atom = new AtomPolicy(policyObject,policyAction);
			atomlist.add(atom);
		}
		
		AtomPolicy[] atomarray = new AtomPolicy[atomlist.size()];
		atomlist.toArray(atomarray);
		PolicyInfo info = new PolicyInfo(appid, atomarray, false);
		info.setSubject(subject);
		info.setActionType(atype);
		info.setNegated(negated);
		log.info("SC have generated policy: {}", info.toString());

		return info;
	}

}
