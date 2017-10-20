/** 
*    Copyright 2014 BUPT
**/ 
package com.sds.securitycontroller.access.manager;

import java.util.List;

import com.sds.securitycontroller.common.Entity;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IAccessControlManagementService extends  ISecurityControllerService{
	
	public boolean allowAccess(Entity subject, Entity object, SubjectOperation operation, AccessControlContext context);
	public boolean addACLPolicy(Policy policy);
	boolean removeACLPolicy(String policyId);
	public Policy getACLPolicy(String policyId);
	public List<Policy> getACLPolicies();
	public boolean updateACLPolicy(Policy policy);			
}
