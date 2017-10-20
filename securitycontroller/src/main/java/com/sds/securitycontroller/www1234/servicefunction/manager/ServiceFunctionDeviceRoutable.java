package com.sds.securitycontroller.www1234.servicefunction.manager;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class ServiceFunctionDeviceRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
		router.attach("", ServiceFunctionDeviceResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/sc/sf";
	}

}
