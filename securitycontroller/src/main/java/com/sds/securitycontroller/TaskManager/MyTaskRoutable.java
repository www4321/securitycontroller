package com.sds.securitycontroller.TaskManager;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class MyTaskRoutable implements RestletRoutable,Runnable{

	@Override
	public Restlet getRestlet(Context context) {
		// TODO Auto-generated method stub

		//System.out.println("/sc/taskHandlerAPI已启动");
		Router router=new Router(context);
		router.attach("/scanner", MyScannerResource.class);
		router.attach("/firewall", MyFirewallResource.class);
		router.attach("/ids", MyIDSResource.class);
		router.attach("/waf", MyWafResource.class);
		router.attach("/getCPU", NorthReqResource.class);
		router.attach("/scanResult/{orderId}", MyTaskResultResource.class);
		router.attach("/scanResult", MyTaskResultResource.class);

		return router;
	}

	@Override
	public String basePath() {
		// TODO Auto-generated method stub
		return "/sc/taskHandler";
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
