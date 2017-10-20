/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event;

import java.io.Serializable;

public class Event implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4179441214260457694L;
	public EventType type;
	public Object subject;
	public long time;
	public String sender;
	public EventArgs args;
	
	public Event(EventType type, Object subject){
		this(type, subject, null, null, System.currentTimeMillis());
	}
	
	public Event(EventType type, Object subject, Object sender, EventArgs args){
		this(type, subject, sender, args, System.currentTimeMillis());
	}

	public Event(EventType type, Object subject, Object sender, EventArgs args, long time){
		this.type = type;
		this.subject = subject;
		this.sender = sender.getClass().getCanonicalName();
		this.args = args;
		this.time = time;
	}
	
	
}
