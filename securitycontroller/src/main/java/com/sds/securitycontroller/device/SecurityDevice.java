/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.device;

import java.util.HashMap;
import java.util.Map;

public class SecurityDevice extends Device  implements java.io.Serializable{


	private static final long serialVersionUID = -5408460646989902909L;
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public SecurityDevice(String name) {
		super(name);
	}


	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
}
