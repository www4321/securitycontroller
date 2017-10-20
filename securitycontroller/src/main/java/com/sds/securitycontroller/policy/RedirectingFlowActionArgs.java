package com.sds.securitycontroller.policy;

import java.io.Serializable;
import java.util.LinkedList;

public class RedirectingFlowActionArgs extends PolicyActionArgs implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4672077321064809580L;
	int hopCount;//machine count contains in the routing
	LinkedList<RedirectFlowRoutingItem> redirectRouting = new LinkedList<RedirectFlowRoutingItem>();
	public LinkedList<RedirectFlowRoutingItem> getRedirectFlowRoutingItems(){
		return redirectRouting;
	}
	public void addRedirectRouting(RedirectFlowRoutingItem item){
		redirectRouting.add(item);
	}
	public RedirectingFlowActionArgs() {
		super();
		// TODO Auto-generated constructor stub
	} 
	public RedirectingFlowActionArgs(PolicyAction action,
			LinkedList<RedirectFlowRoutingItem> redirectFlowRouting) {
		super();
		this.redirectRouting = redirectFlowRouting; 
	}

}
