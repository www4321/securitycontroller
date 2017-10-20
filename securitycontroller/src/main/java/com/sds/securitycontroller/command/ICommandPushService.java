/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.command;

import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.restserver.RestResponse;

public interface ICommandPushService extends ISecurityControllerService{
	RestResponse pushDataToNetworkController(CommandPushRequest req) throws Exception;
	String pushDataToDevice(CommandPushRequest req) throws Exception;
}
