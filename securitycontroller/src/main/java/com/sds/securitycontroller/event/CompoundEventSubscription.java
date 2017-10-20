/** 
*    Copyright 2014 BUPT. 
**/ 
/**
*    Copyright 2011, Big Switch Networks, Inc. 
*    Originally created by David Erickson, Stanford University
* 
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

package com.sds.securitycontroller.event;


/** Predicate class to handle AND and OR combinations of a number
 * of child predicates. The result of the logical combination of the
 * child predicates can also be negated to support a NOT operation.
 * 
 * @author rob
 *
 */
public class CompoundEventSubscription extends EventSubscription implements java.io.Serializable{
        
    /**
	 * 
	 */
	private static final long serialVersionUID = 9154846399061772973L;
	public void setNegated(boolean negated) {
		this.negated = negated;
	}


	public void setOperationValueType(Class<?> operationValueType) {
		this.operationValueType = operationValueType;
	}

	private boolean negated;
    private EventSubscription[] subscriptionList;

	private Class<?> operationValueType;// the type of operation value
	public Class<?> getOperationValueType() {
		return operationValueType;
	}

    public void setSubscriptionList(EventSubscription[] subscriptionList) {
		this.subscriptionList = subscriptionList;
	}
    
    public CompoundEventSubscription(Operator operator, boolean negated, Class<?> operationValueType, 
    		SubscribedValueCategory subscribedValueCategory, SubscriptionType subscriptionType, 
    		EventSubscription... subscriptionList) {
        this.operator = operator;
        this.negated = negated;
        this.operationValueType = operationValueType;
        this.subscribedValueCategory = subscribedValueCategory;
        this.subscriptionType = subscriptionType;
        this.subscriptionList = subscriptionList;
		this.subscriptionType = SubscriptionType.COMPOUND;
    }
    
    
    public boolean isNegated() {
        return negated;
    }
    
    public EventSubscription[] getSubscriptionList() {
        return subscriptionList;
    }

}
