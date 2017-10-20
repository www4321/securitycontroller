/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.asset.manager;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class AssetManagerRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/flows", AssetManagerResource.class);
        return router;
	}

	@Override
	public String basePath() {
        return "/sc/assetmanager";
	}

}
