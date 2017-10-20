package com.sds.securitycontroller.knowledge.physical.agent;

import java.util.Map;

import com.sds.securitycontroller.knowledge.physical.PhysicalUser;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IPhysicalAgentService extends ISecurityControllerService {
	Map<String,PhysicalUser> getAuthenticationUsers();
}
