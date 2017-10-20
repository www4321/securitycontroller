/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.policy;

import com.sds.securitycontroller.policy.PolicySubject.PolicySubjectType;

public class PolicyInfo implements java.io.Serializable{

	private static final long serialVersionUID = 12312222L;
	public PolicyInfo(String subid, AtomPolicy[] policies,boolean force) {
		this.policies = policies;
		this.subId = subid;
		this.force = force;
	}
	/**
	 * 
	 * @param subid 主体表示
	 * @param policies 子策略组
	 * @param force 是否强制执行
	 * @param policySubject 策略主题
	 * @param negated //是否对匹配条件取非
	 * @param policyActionType 策略类型
	 */
	public PolicyInfo (String subid, AtomPolicy[] policies,boolean force,PolicySubject policySubject,
			boolean negated,PolicyActionType policyActionType) {
		this.policies = policies;
		this.subId = subid;
		this.force = force;
		this.subject=policySubject;
		this.negated=negated;
		this.actionType=policyActionType;
	}
	protected String subId;
	protected AtomPolicy[] policies;
	private PolicySubject subject;//采取策略的主体
	//是否对匹配条件取非
	private boolean negated; //negated op
	private PolicyActionType actionType;//策略类型
	protected boolean force;
	protected short priority;
	
	public short getPriority() {
		return priority;
	}

	public void setPriority(short priority) {
		this.priority = priority;
	}

	public PolicySubject getSubject() {
		return subject;
	}

	public void setSubject(PolicySubject subject) {
		this.subject = subject;
	}

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}
	public PolicyActionType getActionType() {
		return actionType;
	}
	public void setActionType(PolicyActionType actionType) {
		this.actionType = actionType;
	}
	public AtomPolicy[] getPolicies() {
		return policies;
	}

	public void setPolicies(AtomPolicy[] policies) {
		this.policies = policies;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public String getSubId() {
		return subId;
	}
	public void setSubId(String subId) {
		this.subId = subId;
	}
	public PolicySubjectType getPolicySubjectType() {
		return this.subject.getPolicySubjectType();
	}
}

