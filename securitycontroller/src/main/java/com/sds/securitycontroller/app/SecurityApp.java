/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app;

import com.sds.securitycontroller.app.AppFactory.AppCategory;
import com.sds.securitycontroller.app.AppFactory.AppType;

public class SecurityApp extends App implements java.io.Serializable{

	private static final long serialVersionUID = -540421817141285070L;


	public SecurityApp(String guid, String hash, String name, String version,
			String host, int port, String root_url, String manage_url,
			String protocol, AppType type, AppCategory category) {
		super(
				"", // here
				guid, hash,
				true,// here
				name,
				"", // here
				version, host, port, root_url, manage_url, protocol,
				System.currentTimeMillis() / 1000L,// here
				type, category);
	}
	
	public SecurityApp(App app){
		super(app);
	}
}
