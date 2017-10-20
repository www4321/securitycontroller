package com.sds.securitycontroller.policy.resolver;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class PolicyResourceRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
        router.attach("", PolicyResource.class);
        router.attach("/{id}", PolicyResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/sc/policy";
	}

}
