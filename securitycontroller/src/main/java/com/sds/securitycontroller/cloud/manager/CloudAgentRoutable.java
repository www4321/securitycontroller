package com.sds.securitycontroller.cloud.manager;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class CloudAgentRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/list/{item}", CloudAgentResource.class);
        router.attach("/{id}", CloudAgentResource.class);
        router.attach("/vms", CloudManagerResource.class);
        return router;
	}

	@Override
	public String basePath() {
        return "/sc/cloudagent";
	}

}
