package com.sds.securitycontroller.directory;

import com.sds.securitycontroller.event.EventArgs;

public class ServiceEventArgs extends EventArgs{
	private static final long serialVersionUID = 1945449982828560349L;
	public ServiceInfo[] services;
	
	public String serviceName;
	public String host;
	
	public ServiceEventArgs(ServiceInfo[] services){
		this.services = services;
	}
	
	public ServiceEventArgs(String serviceName, String host){
		this.serviceName = serviceName;
		this.host = host;
	}

}
