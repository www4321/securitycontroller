/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app.manager;

import org.restlet.Context;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class AppManagerRoutable implements RestletRoutable{
	
	 /**
     * Create the Restlet router and bind to the proper resources.
     */
    @Override
    public Router getRestlet(Context context) {
        Router router = new Router(context);
        //register info
        router.attach("/info", AppManagerResource.class);        
        router.attach("/{id}/info", AppManagerResource.class);
        //realtime info
        router.attach("/realtimeinfo", AppRealtimeResource.class);
        router.attach("/{id}/realtimeinfo", AppRealtimeResource.class);
        //subscription app->sc
        router.attach("/{id}/subscription", AppSubscriptionManagerResource.class);
        router.attach("/{id}/subscription/", AppSubscriptionManagerResource.class);
        router.attach("/{id}/subscription/{subscribername}", AppSubscriptionManagerResource.class);
        router.attach("/{id}/subscription/{subscribername}/", AppSubscriptionManagerResource.class);
        
        //policy app->sc
        router.attach("/{id}/command", AppCommandResource.class);        
        //control
        router.attach("/{id}/status", AppControlResource.class);
        //package & version
        router.attach("/{id}/version", AppVersionResource.class);                
        router.attach("/{id}/package", AppPackageResource.class);
        router.attach("/package", AppPackageResource.class);
        //snapshot
        router.attach("/{id}/snapshot", AppSnapshotResource.class);
        router.attach("/snapshot", AppSnapshotResource.class);
        return router;
    }

    @Override
    public String basePath() {
    	return "/sc/apps";
    }
}
