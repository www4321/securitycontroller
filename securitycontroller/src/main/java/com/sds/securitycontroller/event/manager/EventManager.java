/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event.manager;

import java.util.ArrayList;
import java.util.Collection;

import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;

/**
 *  Events handled by the main loop
 */
public abstract class EventManager implements ISecurityControllerModule, IEventManagerService{

	private  SecurityControllerModuleContext context;
    protected IRegistryManagementService serviceRegistry;
	boolean hasStarted = false;

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		 Collection<Class<? extends ISecurityControllerService>> services =
	                new ArrayList<Class<? extends ISecurityControllerService>>(1);
	        services.add(IEventManagerService.class);
	        return services;
	}


	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		return null;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		this.context=context;
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
//        System.out.println("EventManager initialized. Service Registry: "+this.serviceRegistry);
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
        serviceRegistry.registerService("", this);
		accessContext(this.context);
	}
	

	@Override
	public void setStartedStatus(boolean status){
		hasStarted =  status;
	}
	@Override
	public boolean getStartedStatus() {
		return hasStarted;
	}
}
