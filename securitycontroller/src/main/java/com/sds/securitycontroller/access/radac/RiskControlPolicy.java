package com.sds.securitycontroller.access.radac;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.access.manager.AccessControlPolicy;
import com.sds.securitycontroller.access.manager.Policy;
import com.sds.securitycontroller.access.radac.RiskStatus.RiskLevel;
import com.sds.securitycontroller.common.Entity;
import com.sds.securitycontroller.common.ExpressionUtils;
import com.sds.securitycontroller.common.IExpression;
import com.sds.securitycontroller.common.MatchResult;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class RiskControlPolicy extends Policy {
	// 基于风险的策略，用于匹配某个主体操作客体的风险是否满足该策略

	private static final long serialVersionUID = -8203634110276922734L;

	protected static Logger log = LoggerFactory.getLogger(AccessControlPolicy.class);

	// 满足策略的每一项风险值
	protected  Map<String, Integer> acceptedRisks;
	// 不满足风险要求时是否允许继续匹配访问规则
	protected  boolean allowOverride;
	// 满足风险要求后是否要求继续验证访问控制规则
	protected  boolean requireVerification;

	public boolean getAllowOverride() {
		return allowOverride;
	}

	public boolean getRequireVerification() {
		return requireVerification;
	}
	
	    
	public void setAcceptedRisks(Map<String, Integer> acceptedRisks) {
		this.acceptedRisks = acceptedRisks;
	}

	public void setAllowOverride(boolean allowOverride) {
		this.allowOverride = allowOverride;
	}

	public void setRequireVerification(boolean requireVerification) {
		this.requireVerification = requireVerification;
	}

	public RiskControlPolicy(String id, IExpression subjectExpression,
			IExpression objectExpression, PolicyResult result,
			boolean allowOverride, boolean requireVerification) {
		this(id, subjectExpression, objectExpression, result, allowOverride, requireVerification, new HashMap<String, Integer>());
	}
	

	public RiskControlPolicy(String id, IExpression subjectExpression,
			IExpression objectExpression, PolicyResult result,
			boolean allowOverride, boolean requireVerification, Map<String, Integer> acceptedRisks) {
		this.id = id;
		this.subjectExpression = subjectExpression;
		this.objectExpression = objectExpression;
		this.result = result;
		this.type = PolicyType.RISK;
		this.acceptedRisks = acceptedRisks;
		this.allowOverride = allowOverride;
		this.requireVerification = requireVerification;
		
	}

	public PolicyResult compliedWith(Entity subject,
			Entity object, RiskStatus risk) throws Exception {
		MatchResult r1 = subjectExpression.match(subject.getAttributes());
		// subject not found, the policy is not for this operation
		if (r1 == MatchResult.UNMATCH || r1 == MatchResult.NOT_FOUND)
			return PolicyResult.DEFAULT;
		MatchResult r2 = objectExpression.match(object.getAttributes());
		// object not found, the policy is not for this operation
		if (r2 == MatchResult.UNMATCH || r2 == MatchResult.NOT_FOUND)
			return PolicyResult.DEFAULT;

		// check if match the risk, if so, return the action
		if (risk.getSubjectRisk() != RiskLevel.UNSET
				&& this.acceptedRisks.containsKey("subject")) {
			if (this.acceptedRisks.get("subject") < risk.getSubjectRisk())
				return this.result;
		} else if (risk.getSubjectRisk() != RiskLevel.UNSET
				&& this.acceptedRisks.containsKey("object")) {
			if (this.acceptedRisks.get("object") < risk.getObjectRisk())
				return this.result;
		} else if (risk.getSubjectRisk() != RiskLevel.UNSET
				&& this.acceptedRisks.containsKey("env")) {
			if (this.acceptedRisks.get("env") < risk.getEnvtRisk())
				return this.result;
		} else if (risk.getSubjectRisk() != RiskLevel.UNSET
				&& this.acceptedRisks.containsKey("overall")) {
			if (this.acceptedRisks.get("overall") < risk.getOverallRisk())
				return this.result;
		}

		return PolicyResult.DEFAULT;
	}

	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.id);
		map.put("sbj_expr", this.subjectExpression.toString());
		map.put("obj_expr", this.objectExpression.toString());
		map.put("type", this.type.toString());
		map.put("attrs", this.getAttributesString());
		return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if(key.equals("id"))
			return this.id;
		else if(key.equals("sbj_expr"))
			return this.subjectExpression;
		else if(key.equals("sbj_expr"))
			return this.objectExpression;
		else if(key.equals("type"))
			return this.type;
		else if(key.equals("attrs")){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("allow_override", this.allowOverride);
			map.put("require_auth", this.requireVerification);
			map.put("accepted_risks", this.acceptedRisks);
			return map;
		}
		else
			return null;
	}

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {

		boolean _allowOverride = false;
		boolean _requireAuth = false;
		Map<String, Integer> _acceptedRisks = new HashMap<String, Integer>();
		
		String sbjExprJson = resultSet.getString("sbj_expr");
		String objExprJson = resultSet.getString("obj_expr");
		String attrsJson = resultSet.getString("attrs");
		String resultJson = resultSet.getString("result");
		String typeJson = resultSet.getString("type");
		PolicyResult result = Enum.valueOf(PolicyResult.class, resultJson);
		PolicyType type = Enum.valueOf(PolicyType.class, typeJson);

		if(type != PolicyType.RISK){
			log.error("Unmatch policy type "+ type + " for risk control policy");
			return null;
		}
		
		IExpression subjectExpr = ExpressionUtils.parseExpressiony(sbjExprJson);
		IExpression objectExpr = ExpressionUtils.parseExpressiony(objExprJson);
		if(subjectExpr == null || objectExpr == null)
			return null;
		
    	ObjectMapper mapper = new ObjectMapper();
		try{
			JsonNode root = mapper.readValue(attrsJson, JsonNode.class);			
			_allowOverride = root.path("allow_override").asBoolean();
			_requireAuth = root.path("require_auth").asBoolean();
			Iterator<Entry<String, JsonNode>> iter = root.path("accepted_risks").fields();
			while(iter.hasNext()){
				Entry<String, JsonNode> entry = iter.next();
				_acceptedRisks.put(entry.getKey(), entry.getValue().asInt());					
			}
				
		
		} catch (Exception e) {
            log.error("Error parse access policy: ", e);
            e.printStackTrace();      
        }		
		
		return new RiskControlPolicy(
				resultSet.getString("id"),
				subjectExpr,
				objectExpr,
				result,_allowOverride, _requireAuth, _acceptedRisks
				);
	}


	@Override
	public String getAttributesString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("allow_override: "+this.allowOverride);
		sb.append(",require_auth: "+this.requireVerification);
		sb.append(",accepted_risks: {");
		for(Entry<String, Integer> entry: this.acceptedRisks.entrySet()){
			sb.append(entry.getKey() + ":"+entry.getValue() + ",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("}}");
		return sb.toString();
	}

}
