package com.sds.securitycontroller.access.manager;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.access.radac.RiskStatus;
import com.sds.securitycontroller.common.Entity;
import com.sds.securitycontroller.common.ExpressionUtils;
import com.sds.securitycontroller.common.IExpression;
import com.sds.securitycontroller.common.MatchResult;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class AccessControlPolicy extends Policy {
	// 基于访问控制的策略，用于匹配某个主体操作客体的的行为是否满足该策略

	private static final long serialVersionUID = 5764845128070567695L;
	protected static Logger log = LoggerFactory.getLogger(AccessControlPolicy.class);
	// 主体的操作
	protected SubjectOperation subjectOperation;

	public AccessControlPolicy(){
    }

	public AccessControlPolicy(String id, IExpression subjectExpression,
			IExpression objectExpression, SubjectOperation subjectOeration,
			PolicyResult result) {
		this.id = id;
		this.subjectExpression = subjectExpression;
		this.objectExpression = objectExpression;
		this.subjectOperation = subjectOeration;
		this.type = PolicyType.ACCESS;
		this.result = result;
	}

	public PolicyResult compliedWith(Entity subject,
			Entity object, SubjectOperation operation, RiskStatus risk)
			throws Exception {
		MatchResult r1 = subjectExpression.match(subject.getAttributes());
		// subject not found, the policy is not for this operation
		if (r1 == MatchResult.UNMATCH || r1 == MatchResult.NOT_FOUND)
			return PolicyResult.DEFAULT;
		MatchResult r2 = objectExpression.match(object.getAttributes());
		// object not found, the policy is not for this operation
		if (r2 == MatchResult.UNMATCH || r2 == MatchResult.NOT_FOUND)
			return PolicyResult.DEFAULT;

		if (subjectOperation != operation)
			return PolicyResult.DEFAULT;

		// match all
		return this.result;
	}

	

	@Override
	public String getAttributesString(){
		return "{sbj_op : '"+ this.subjectOperation + "'}";
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
		else if(key.equals("type"))
			return this.type;
		else if(key.equals("sbj_expr"))
			return this.subjectExpression;
		else if(key.equals("sbj_expr"))
			return this.objectExpression;
		else if(key.equals("type"))
			return "ACCESS";
		else if(key.equals("attrs")){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("sbj_op", this.subjectOperation);
			return map;
		}
		else
			return null;
	}

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {
		SubjectOperation op = null;

		String typeJson = resultSet.getString("type");
		String sbjExprJson = resultSet.getString("sbj_expr");
		String objExprJson = resultSet.getString("obj_expr");
		String attrsJson = resultSet.getString("attrs");
		String resultJson = resultSet.getString("result");
		PolicyResult result = Enum.valueOf(PolicyResult.class, resultJson);
		PolicyType type = Enum.valueOf(PolicyType.class, typeJson);
		
		if(type != PolicyType.ACCESS){
			log.error("Unmatch policy type "+ type + " for access control policy");
			return null;
		}
		
		IExpression subjectExpr = ExpressionUtils.parseExpressiony(sbjExprJson);
		IExpression objectExpr = ExpressionUtils.parseExpressiony(objExprJson);
		if(subjectExpr == null || objectExpr == null)
			return null;
		
    	ObjectMapper mapper = new ObjectMapper();
		try{
			JsonNode root = mapper.readValue(attrsJson, JsonNode.class);
			op = Enum.valueOf(SubjectOperation.class, root.path("sbj_op").asText());
		} catch (Exception e) {
            log.error("Error parse access policy: ", e);
            e.printStackTrace();      
        }		
		
		return new AccessControlPolicy(
				resultSet.getString("id"),
				subjectExpr,
				objectExpr,
				op,
				result);
	}

}
