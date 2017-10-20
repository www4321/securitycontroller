/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.reputation.manager;

import org.restlet.Context;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class ReputationManagerRoutable implements RestletRoutable{
	
	 /**
     * Create the Restlet router and bind to the proper resources.
     */
    @Override
    public Router getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/{id}", ReputationResource.class);
        return router;
    }

    @Override
    public String basePath() {
    	return "/sc/reputation";
    }
}
