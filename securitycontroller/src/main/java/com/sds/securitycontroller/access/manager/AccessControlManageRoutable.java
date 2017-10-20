/** 
*    Copyright 2014 BUPT 
**/ 
package com.sds.securitycontroller.access.manager;

import org.restlet.Context;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class AccessControlManageRoutable implements RestletRoutable{
	
	 /**
     * Create the Restlet router and bind to the proper resources.
     */
    @Override
    public Router getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/control", AccessControlManageResource.class);
        router.attach("/policies/{id}", AccessControlPolicyManageResource.class);
        router.attach("/policies/", AccessControlPolicyManageResource.class);
        return router;
    }

    @Override
    public String basePath() {
    	return "/sc/access";
    }
}
