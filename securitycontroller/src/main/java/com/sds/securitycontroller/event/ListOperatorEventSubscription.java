/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event;




public class ListOperatorEventSubscription extends OperatorEventSubscription implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	public ListOperatorEventSubscription(String subscribedKey,
			Operator operator, Comparable<?> value,
			SubscribedValueCategory subscribedValueCategory,
			SubscriptionType subscriptionType, Class<?> valueType,
			EventSubscription child, Operator listOperator) {
		super(subscribedKey, operator, value, subscribedValueCategory,
				subscriptionType, valueType);
		this.subscription = child;
		this.listOperator = listOperator;
		this.subscriptionType = SubscriptionType.LIST;
		
	}

    private EventSubscription subscription;

	private Operator listOperator = Operator.ALL;
	
    	
	
    public Operator getListOperator() {
		return listOperator;
	}


	public void setListOperator(Operator listOperator) {
		this.listOperator = listOperator;
	}
	

	public EventSubscription getSubscription() {
		return subscription;
	}


	public void setSubscription(EventSubscription subscription) {
		this.subscription = subscription;
	}


    public boolean compareCountObj(int count, Object refv){
		return calcObj(count, (int)refv, this.operator);
    }


}
