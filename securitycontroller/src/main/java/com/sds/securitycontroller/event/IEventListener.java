/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event;


public abstract interface IEventListener {

    public void processEvent(Event e);
    public void addListenEventCondition(EventType type, EventSubscriptionInfo condition);
	void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition);    
}
