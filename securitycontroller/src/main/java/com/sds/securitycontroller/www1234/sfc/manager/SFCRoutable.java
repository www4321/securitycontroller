package com.sds.securitycontroller.www1234.sfc.manager;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class SFCRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
		router.attach("", SFCResource.class);
		router.attach("/swport", SFCSwitchPortResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/sc/functionchain";
	}

}
