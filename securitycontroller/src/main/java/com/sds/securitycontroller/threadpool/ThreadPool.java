/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.threadpool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;

public class ThreadPool implements IThreadPoolService, ISecurityControllerModule {
    protected ScheduledExecutorService executor = null;
    protected IRegistryManagementService serviceRegistry;
    
    // IThreadPoolService

    @Override
    public ScheduledExecutorService getScheduledExecutor() {
        return executor;
    }
    
    // ISecurityControllerModule
    
    @Override
    public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
        Collection<Class<? extends ISecurityControllerService>> l = 
                new ArrayList<Class<? extends ISecurityControllerService>>();
        l.add(IThreadPoolService.class);
        return l;
    }

    @Override
    public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService>
            getServiceImpls() {
        Map<Class<? extends ISecurityControllerService>,
            ISecurityControllerService> m = 
                new HashMap<Class<? extends ISecurityControllerService>,
                    ISecurityControllerService>();
        m.put(IThreadPoolService.class, this);
        // We are the class that implements the service
        return m;
    }

    @Override
    public Collection<Class<? extends ISecurityControllerService>>
            getModuleDependencies() {
        // No dependencies
        return null;
    }

    @Override
    public void init(SecurityControllerModuleContext context)
                                 throws SecurityControllerModuleException {
        executor = Executors.newScheduledThreadPool(15);
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
    }

    @Override
    public void startUp(SecurityControllerModuleContext context) {
        serviceRegistry.registerService("", this);
    }
}
