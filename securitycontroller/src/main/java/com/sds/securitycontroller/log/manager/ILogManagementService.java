/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.log.manager;

import java.util.List;
import java.util.Map;

import com.sds.securitycontroller.event.manager.IRPCHandler;
import com.sds.securitycontroller.log.Report;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface ILogManagementService extends ISecurityControllerService, IRPCHandler{
	boolean addReport(Report report);
	boolean addReport(Object entity);
	
	Report getReport(String id);
	//TODO is Report?
	List<Report> queryReport(Object query);
	List<Map<String,Object>> getScanReport(String map, String reduce);

}
