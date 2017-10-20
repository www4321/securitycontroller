package com.sds.securitycontroller.policy;

public class RawPolicyObject extends PolicyObject {

	private static final long serialVersionUID = 1L;
	public RawPolicyObject() {
		super();
	}
	protected Object rawObject;
	
	public Object getRawObject() {
		return rawObject;
	}

	public void setRawObject(Object rawObject) {
		this.rawObject = rawObject;
	}
}
