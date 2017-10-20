/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.directory.registry;

import java.util.List;

import com.sds.securitycontroller.directory.ModuleCommand;
import com.sds.securitycontroller.directory.ModuleCommandResponse;
import com.sds.securitycontroller.directory.ServiceInfo;
import com.sds.securitycontroller.event.manager.IRPCHandler;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IRegistryManagementService extends  ISecurityControllerService{
	void registerService(String url, ISecurityControllerModule serviceModule);
	void regService(ServiceInfo service);
	void unregService(String id);
	ServiceInfo findService(String id);
	List<ServiceInfo> getAllServices();
	void registerCommand(ModuleCommand command, Class<? extends IRPCHandler> executor);
	void unregCommand(String module);
	String executeCommand(ModuleCommandResponse resp);
	List<ModuleCommand> getAllCommands();
			
}
