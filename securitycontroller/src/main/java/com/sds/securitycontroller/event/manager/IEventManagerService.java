/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event.manager;

import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.module.ISecurityControllerModuleContext;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;

public interface IEventManagerService  extends ISecurityControllerService{
    public void addEventListener(EventType type, IEventListener listener);
    public void removeEventListener(EventType type, IEventListener listener);
    void addEvent(Event e);
    void addBroadcastEvent(Event e);
    public void initBuiltinListeners(ISecurityControllerModuleContext moduleContext );
    public void start();
	public void addConditionToListener(EventSubscriptionInfo condition);
	public void accessContext(SecurityControllerModuleContext context);
	
	public Object makeRPCCall(Class<?> serviceClass,String methodName,Object[] args);
	public boolean getStartedStatus();
	public void setStartedStatus(boolean status);
}
