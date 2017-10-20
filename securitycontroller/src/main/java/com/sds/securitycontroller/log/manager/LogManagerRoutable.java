package com.sds.securitycontroller.log.manager;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class LogManagerRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/", LogManagerResource.class);
        router.attach("", LogManagerResource.class);
        router.attach("/{id}", LogManagerResource.class);
        router.attach("/{id}/", LogManagerResource.class);
        router.attach("/report/scan", LogScanReportResource.class);
        return router;
	}

	@Override
	public String basePath() {
    	return "/sc/logs";
	}

}
