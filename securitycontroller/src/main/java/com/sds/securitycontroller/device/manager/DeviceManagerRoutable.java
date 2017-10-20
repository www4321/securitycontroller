/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.device.manager;

import org.restlet.Context;
import org.restlet.routing.Router;

import com.sds.securitycontroller.log.manager.LogManagerResource;
import com.sds.securitycontroller.restserver.RestletRoutable;


public class DeviceManagerRoutable  implements RestletRoutable {
	 /**
     * Create the Restlet router and bind to the proper resources.
     */
    @Override
    public Router getRestlet(Context context) {
        Router router = new Router(context);
        //System.out.println("liu ye ping sa diao1");
        router.attach("/", DeviceManagerResource.class);
        router.attach("", DeviceManagerResource.class);
        // device booting manager API
        router.attach("/boot/", DeviceBootManagerResource.class);
        router.attach("/boot/batch/", DeviceBatchBootManagerResource.class);
        router.attach("/boot/{id}", DeviceBootManagerResource.class);
        router.attach("/services/", DeviceBootServiceManagerResource.class);
        router.attach("/{id}", DeviceManagerResource.class);
        router.attach("/{id}/", DeviceManagerResource.class);
        router.attach("/{id}/suspiciousdata", LogManagerResource.class);
        
        
        return router;
    }

    @Override
    public String basePath() {
        return "/sc/devices";
    }
}
