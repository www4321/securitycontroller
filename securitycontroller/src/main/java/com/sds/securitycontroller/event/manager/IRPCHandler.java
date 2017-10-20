/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event.manager;

import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IRPCHandler extends ISecurityControllerService {
	/**
	 * Handler
	 * @param methodName
	 * @param args
	 * @return
	 */
	Object handleRPCRequest(String methodName,Object[] args);

}
