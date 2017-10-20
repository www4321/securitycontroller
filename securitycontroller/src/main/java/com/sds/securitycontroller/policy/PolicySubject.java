package com.sds.securitycontroller.policy;

import java.io.Serializable;

public class PolicySubject implements Serializable{

	private static final long serialVersionUID = 1L;
	protected String policySubjectId;
	protected PolicySubjectType policySubjectType;
	public PolicySubject(){
		super();
	}
	/**
	 * 
	 * @param policySubjectId 主体唯一标识，根据注册信息获取
	 * @param policySubjectType 主题类型，枚举类型
	 */
	public PolicySubject(String policySubjectId,
			PolicySubjectType policySubjectType) {
		super();
		this.policySubjectId = policySubjectId;
		this.policySubjectType = policySubjectType;
	}
	public String getPolicySubjectId() {
		return policySubjectId;
	}
	public void setPolicySubjectId(String policySubjectId) {
		this.policySubjectId = policySubjectId;
	}
	public PolicySubjectType getPolicySubjectType() {
		return policySubjectType;
	}
	public void setPolicySubjectType(PolicySubjectType policySubjectType) {
		this.policySubjectType = policySubjectType;
	}
	public enum PolicySubjectType {
		ADS_APP,
		IDS_APP,
		SECURITY_CONTROLLER, 
		NETWORK_CONTROLLER, 
		POLICY_RESOLVER,//more detailed than sc
		SECURITY_DEVICE, 
		NETWORK_DEVICE,
		CLOUD_TENANT,
		CLOUD_VM,
		SWITCH_PORT,
		FLOW,
		UNDEFINED
	};
}
