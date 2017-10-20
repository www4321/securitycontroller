package com.sds.securitycontroller.access.manager;

import com.esotericsoftware.minlog.Log;
import com.sds.securitycontroller.access.radac.RiskControlPolicy;
import com.sds.securitycontroller.common.IExpression;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public abstract class Policy implements IDBObject {

	private static final long serialVersionUID = -6079436286562882943L;

	public enum PolicyResult {
		ALLOW, DENY, DEFAULT,
	}
	
	public enum PolicyType{
		ACCESS,
		RISK
	}

	protected String id;

	public String getId() {
		return id;
	}
	
	protected PolicyType type;

	public PolicyType getType() {
		return type;
	}
	

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {

		String typeJson = resultSet.getString("type");
		PolicyType type = Enum.valueOf(PolicyType.class, typeJson);
		
		if(type == PolicyType.ACCESS)
			return new AccessControlPolicy(null, null, null, SubjectOperation.ACCESS, null).mapRow(resultSet);
		else if(type == PolicyType.RISK)
			return new RiskControlPolicy(null, null, null,null,true, true).mapRow(resultSet);
		else{
			Log.error("Unimplemented asset type: " + type);
			return null;
		}
	}
	
	// 主体属性集合，用于确定某操作的主体是否适用该策略
	protected IExpression subjectExpression;
	// 客体属性集合，用于确定某操作的客体是否适用该策略
	protected IExpression objectExpression;
	// 策略结果，允许、拒绝或默认行为
	protected PolicyResult result;

	public abstract String getAttributesString();

}
