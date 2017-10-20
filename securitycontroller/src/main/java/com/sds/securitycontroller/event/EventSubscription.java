/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event;

public abstract class EventSubscription implements java.io.Serializable{

	private static final long serialVersionUID = 7382203527836708055L;

	public enum SubscribedValueCategory{
		OBJECT,
		RECORD,
		OBJLIST,
		PARENT,
		ROOT,
		SQL
	}
	

	public enum SubscriptionType{
		COMPOUND,
		OPERATOR,
		LIST,
		SCRIPT,
		// REPORT EVENT SUBSCRIPTION
		REPORT
	}
	
	

    public enum Operator { EQ, LT, LTE, GT, GTE, AND, OR, COUNT, ALL, NONE};
	
	public static String now = "NOW";
	

    protected Operator operator;
	protected SubscribedValueCategory subscribedValueCategory;// return db records or the event object? 

	protected SubscriptionType subscriptionType;
	
	public SubscriptionType getSubscriptionType() {
		return subscriptionType;
	}

	public void setSubscriptionType(SubscriptionType subscriptionType) {
		this.subscriptionType = subscriptionType;
	}

	public SubscribedValueCategory getSubscribedValueCategory() {
		return subscribedValueCategory;
	}
    
    public Operator getOperator() {
        return operator;
    }    
}
