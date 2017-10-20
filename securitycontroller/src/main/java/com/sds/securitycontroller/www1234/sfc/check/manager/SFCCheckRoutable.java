package com.sds.securitycontroller.www1234.sfc.check.manager;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class SFCCheckRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
		router.attach("/{sfcId}", SFCCheckResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/sc/checksfc";
	}

}
