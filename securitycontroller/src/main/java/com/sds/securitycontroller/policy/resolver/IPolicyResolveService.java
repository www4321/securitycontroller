/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.policy.resolver;

import java.util.Collection;
import java.util.Set;

import com.sds.securitycontroller.event.manager.IRPCHandler;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.policy.PolicyActionType;
import com.sds.securitycontroller.policy.PolicyInfo;
import com.sds.securitycontroller.policy.PolicyRecord;
import com.sds.securitycontroller.policy.PolicySubject;

public interface IPolicyResolveService extends ISecurityControllerService, IRPCHandler{
	
	Collection<PolicyRecord> getPolicyRecords(PolicySubject policySubject, PolicyActionType policyActionType ); 
	String generateNewPolicy(PolicyInfo policyInfo) throws Exception;
	void deletePolicy(String policyId) throws Exception;

	public Set<String> getAllPolicies();

}
