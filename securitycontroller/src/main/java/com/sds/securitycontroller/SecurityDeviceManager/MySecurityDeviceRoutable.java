package com.sds.securitycontroller.SecurityDeviceManager;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import com.sds.securitycontroller.restserver.RestletRoutable;

public class MySecurityDeviceRoutable  implements RestletRoutable{

	@Override
	public Restlet getRestlet(Context context) {
		// TODO Auto-generated method stub
		//System.out.println("设备api已启动");
		Router router = new Router(context);
		router.attach("", MySecurityDeviceResource.class);
		router.attach("/register", MySecurityDeviceResource.class);
		router.attach("/loadInfo",DeviceRealtimeInfoResource.class);
		return router;
	}

	@Override
	public String basePath() {
		// TODO Auto-generated method stub
		return "/sc/securityDevice";
	}

}
