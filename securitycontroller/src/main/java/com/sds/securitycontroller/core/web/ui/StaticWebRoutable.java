/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.core.web.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.restserver.RestletRoutable;

public class StaticWebRoutable implements RestletRoutable, ISecurityControllerModule {

	private IRestApiService restApi;
	
    @Override
    public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
        Collection<Class<? extends ISecurityControllerService>> l = 
                new ArrayList<Class<? extends ISecurityControllerService>>();
        l.add(IRestApiService.class);
        return l;
    }
    
    @Override
    public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
        return null;
    }
    
    @Override
    public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService>
            getServiceImpls() {
        return null;
    }

    @Override
    public void init(SecurityControllerModuleContext context)
                                             throws SecurityControllerModuleException {
        restApi = context.getServiceImpl(IRestApiService.class);
    }
    
    @Override
    public void startUp(SecurityControllerModuleContext context) {
        // Add our REST API
        restApi.addRestletRoutable(this);
        
    }

	@Override
	public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("", new Directory(context, "clap://classloader/web/"));
        context.setClientDispatcher(new Client(context, Protocol.CLAP));
        return router;
	}

	@Override
	public String basePath() {
		return "/ui/";
	}
}
