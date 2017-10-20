/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.directory.registry;

import org.restlet.Context;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class RegistryManageRoutable implements RestletRoutable{
	
	 /**
     * Create the Restlet router and bind to the proper resources.
     */
    @Override
    public Router getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/services", RegistryServiceManageResource.class);
        router.attach("/services/{id}", RegistryServiceManageResource.class);
        router.attach("/commands", RegistryCommandManageResource.class);
        router.attach("/commands/{command}", RegistryCommandManageResource.class);
        return router;
    }

    @Override
    public String basePath() {
    	return "/sc/directory";
    }
}
