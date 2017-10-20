package com.sds.securitycontroller.test1;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class EventSendRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
		router.attach("", EventSendResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/sc/eventsendtest1";
	}

}
