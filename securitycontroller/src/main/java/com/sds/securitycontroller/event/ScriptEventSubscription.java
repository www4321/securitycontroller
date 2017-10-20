/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event;

public class ScriptEventSubscription extends EventSubscription implements java.io.Serializable {

	private static final long serialVersionUID = 6921913388680434340L;
	private String subscriptionScript;
	public String getSubscriptionScript(){
		return subscriptionScript;
	}

	public void setSubscriptionScript(String script){
		subscriptionScript = script;
	}
	
	public ScriptEventSubscription(String script){
		this.subscriptionType = SubscriptionType.SCRIPT;
		subscriptionScript = script;
	}

}
