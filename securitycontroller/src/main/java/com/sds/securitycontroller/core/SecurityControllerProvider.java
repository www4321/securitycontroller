/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sds.securitycontroller.core.internal.Controller;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.threadpool.IThreadPoolService;

public class SecurityControllerProvider implements ISecurityControllerModule /*,Serializable*/{

	Controller controller;
    
    @Override
    public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
        Collection<Class<? extends ISecurityControllerService>> services =
                new ArrayList<Class<? extends ISecurityControllerService>>(1);
        services.add(ISecurityControllerProviderService.class);
        return services;
    }

    @Override
    public Map<Class<? extends ISecurityControllerService>,
               ISecurityControllerService> getServiceImpls() {
        controller = new Controller();
        
        Map<Class<? extends ISecurityControllerService>,
            ISecurityControllerService> m = 
                new HashMap<Class<? extends ISecurityControllerService>,
                            ISecurityControllerService>();
        m.put(ISecurityControllerProviderService.class, controller);
        return m;
    }

    @Override
    public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
        Collection<Class<? extends ISecurityControllerService>> dependencies =
            new ArrayList<Class<? extends ISecurityControllerService>>(4);
        dependencies.add(IRestApiService.class);
        dependencies.add(IThreadPoolService.class);
        return dependencies;
    }

    @Override
    public void init(SecurityControllerModuleContext context) throws SecurityControllerModuleException {
       controller.setRestApiService(
           context.getServiceImpl(IRestApiService.class));
       controller.setThreadPoolService(
           context.getServiceImpl(IThreadPoolService.class));
       controller.init(context.getConfigParams(this));
    }

    @Override
    public void startUp(SecurityControllerModuleContext context) {
        controller.startupComponents();
    }
}
