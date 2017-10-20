/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.policy;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.command.ResolvedCommand;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.policy.PolicySubject.PolicySubjectType;

public class AtomPolicy implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	protected String id;
	protected String subId;
	protected short priority;
	protected PolicySubject subject;
	protected PolicyObject object;
	protected PolicyAction action;

	//策略的级别,每个Object通过name，aimtype，objectId标识了其维度，流向以及流的唯一标识。
	protected AppPolicyScale policyObjectScale;  //匹配对象的级别，如流、vm等
	protected String policyObjectId;            //匹配对象的编号，全局策略唯一的
	protected Class<?> policyObjectClass;       //匹配对象的类型

	protected ResolvedCommand resolvedCommand;
	
	public PolicyObject getObject() {
		return object;
	}

	public void setObject(PolicyObject object) {
		this.object = object;
	}

	public PolicyAction getAction() {
		return action;
	}

	public void setAction(PolicyAction action) {
		this.action = action;
	}

	public short getPriority() {
		return priority;
	}

	public void setPriority(short priority) {
		this.priority = priority;
	}

	public String getSubId() {
		return subId;
	}

	public void setSubId(String subId) {
		this.subId = subId;
	}

	public Class<?> getPolicyObjectClass() {
		return policyObjectClass;
	}

	public AppPolicyScale getPolicyObjectScale() {
		return policyObjectScale;
	}

	public void setPolicyObjectScale(AppPolicyScale policyObjectScale) {
		this.policyObjectScale = policyObjectScale;
	}

	public String getObjectId() {
		return policyObjectId;
	}

	public void setObjectId(String objectId) {
		this.policyObjectId = objectId;
	}

	public void setPolicyObjectClass(Class<?> valuetype) {
		this.policyObjectClass = valuetype;
	}

	public AtomPolicy(AppPolicyScale policyObjectType, String policyObjectId,
			Class<?> policyObjectClass) {
		this.policyObjectScale = policyObjectType;
		this.policyObjectId = policyObjectId; 
		this.policyObjectClass = policyObjectClass; 
	}
	
	public AtomPolicy(PolicyObject obj,PolicyAction action){
		this.object = obj;
		this.action = action;
//		this.action.atomPolicy = this;
	}

	@Override
	public String toString() {
		return "AtomDescription [name=" + policyObjectScale + ", objectId=" + policyObjectId
				+ ", valueType=" + policyObjectClass + "]";
	}

	public PolicySubject getSubject() {
		return subject;
	}

	public void setSubject(PolicySubject subject) {
		this.subject = subject;
	}
	
	
	/**
	 * Serialize json
	 * @param policy
	 * @return
	 */
	public static String serializePolicy(AtomPolicy policy){
		String str=null;
		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter();
		JsonGenerator gen;
		try {
			gen = new JsonFactory().createGenerator(writer);
			mapper.writeValue(gen, policy);
			str = writer.toString();
			gen.close();
			writer.close();
		} catch (IOException e) {
			//log.error("Error when convert REST request: {}", e.getMessage());
			e.printStackTrace();
			str= null;
		}
		return str;

	}

	/**
	 * 
	 * @param json
	 * @return
	 */
	public static AtomPolicy deserializeToAtomPolicy(String json){
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(json);
			//String appid = rootNode.path("appID").asText();
			JsonNode policyNode = rootNode;//.path("policy");
			PolicySubject subject=new PolicySubject("",PolicySubjectType.UNDEFINED);
			try {
				subject.setPolicySubjectType(PolicySubjectType.valueOf(policyNode.path("subject").asText()));
			} catch (IllegalArgumentException e) {
				subject.setPolicySubjectType(PolicySubjectType.UNDEFINED);
			}
			// resolve policy object
			JsonNode objNode = policyNode.path("object") ;//objNodes.get(i);
			// 20140508
			FlowPolicyObject policyObject = new FlowPolicyObject();
			if(!objNode.path("id").isMissingNode()){
				policyObject.id=objNode.path("id").asText();
			}
			JsonNode flowMatchJsonNode = objNode.path("flow_match");
			FlowMatch flowMatch = null;
			// parse flow matches
			if(!flowMatchJsonNode.isMissingNode()){
				// resolve flow match
				flowMatch = FlowMatch.resolveFlowMatchFromJsonNode(flowMatchJsonNode);
				if(flowMatch!=null)
					policyObject.resolvedFlowMatches.add(flowMatch);
			}
			// parse traffic pattern
			JsonNode trafficPatternJsonNode = objNode.path("traffic_pattern");
			if(!trafficPatternJsonNode.isMissingNode()){
				TrafficPattern tp = new TrafficPattern();
				if(!trafficPatternJsonNode.path("srcMac").isMissingNode()){
					tp.setSrcMac( trafficPatternJsonNode.path("srcMac").asText());
				}
				if(!trafficPatternJsonNode.path("inPortName").isMissingNode()){
					tp.setInPortName( trafficPatternJsonNode.path("inPortName").asText());
				}
				policyObject.setTrafficPattern(tp);
			}
			//parse resolved flow matches
			JsonNode resolvedMatchesJsonNode = objNode.path("resolvedFlowMatches");
			if(!resolvedMatchesJsonNode.isMissingNode()){
				Iterator<JsonNode> iter = resolvedMatchesJsonNode.iterator();
				JsonNode resolvedMatchNode=null;
				while(iter.hasNext()){
				 resolvedMatchNode=iter.next();
				 flowMatch = FlowMatch.resolveFlowMatchFromJsonNode(resolvedMatchNode);
				 if(flowMatch!=null)
						policyObject.resolvedFlowMatches.add(flowMatch);
				}
			}
			KnowledgeType objectType = KnowledgeType.UNDEFINED; 
			try {
				objectType=KnowledgeType.valueOf(objNode.path("type").asText());
			} catch (IllegalArgumentException e1) {
				objectType = KnowledgeType.UNDEFINED; 
			}
				
			policyObject.setType(objectType);
			// resolve policy action
			JsonNode actionNode = policyNode.path("action");
			PolicyActionType actionType = PolicyActionType.UNKNOWN; 
			try {
				actionType=PolicyActionType.valueOf( actionNode.path("type").asText() );
			} catch (IllegalArgumentException e1) {
				actionType = PolicyActionType.UNKNOWN; 
			}
			JsonNode actionArgsNode = actionNode.path("actionArgs");
			PolicyActionArgs actionArgs = null;
			
			if(!actionArgsNode.isMissingNode() && !actionArgsNode.isNull()){
				if(actionType==PolicyActionType.BYOD_INIT){
					try {
						long dpid = actionArgsNode.path("dpid").asLong();
						short inPort = (short)actionArgsNode.path("inPort").asInt();
						String serverIp = actionArgsNode.path("serverIp").asText();
						String serverMac =actionArgsNode.path("serverMac").asText();
						String network = actionArgsNode.path("network").asText();
						short mask = (short)actionArgsNode.path("mask").asInt();
						@SuppressWarnings("unused")
						boolean force = actionArgsNode.path("force").asBoolean();
						actionArgs = new ByodInitActionArgs(dpid, inPort, serverIp, serverMac, network, mask);
					} catch (Exception e) {
						e.printStackTrace();
						actionArgs = null;
					}
				}
			}
			PolicyAction policyAction = new PolicyAction(actionType, actionArgs);
			
			AtomPolicy atomPolicy = new AtomPolicy(policyObject,policyAction);
			atomPolicy.subject = subject;
			return atomPolicy;
	}
	catch (Exception e) {
		e.printStackTrace();
		return null;
	}
	}

	public ResolvedCommand getResolvedCommand() {
		return resolvedCommand;
	}

	public void setResolvedCommand(ResolvedCommand resolvedCommand) {
		this.resolvedCommand = resolvedCommand;
	}

}
