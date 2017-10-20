package com.sds.securitycontroller.access.manager;

import java.util.HashMap;

public class AccessControlContext {

	private HashMap<String, Object> context = null;

	public AccessControlContext() {
		this.context = new HashMap<String, Object>();
	}

	public void addContext(String key, Object value) {
		this.context.put(key, value);
	}

	public Object getContext(String key) {
		return this.context.get(key);
	}

}
