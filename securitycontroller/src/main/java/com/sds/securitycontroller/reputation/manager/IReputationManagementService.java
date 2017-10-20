/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.reputation.manager;

import com.sds.securitycontroller.log.Report;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.reputation.ReputationEntity;

public interface IReputationManagementService extends  ISecurityControllerService{
	ReputationEntity getReputationEntity(String id);
	public void calculateReputationByReport(Report report);
}
