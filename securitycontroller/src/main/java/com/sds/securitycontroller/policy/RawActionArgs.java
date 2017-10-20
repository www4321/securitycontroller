package com.sds.securitycontroller.policy;

public class RawActionArgs extends PolicyActionArgs {

	private static final long serialVersionUID = -6407241032895144071L;
	
	private Object data;
	
	public RawActionArgs() {
		super();
	}


	public void setData(Object data) {
		this.data = data;
	}
	

	public Object getData() {
		return this.data;
	}


}
