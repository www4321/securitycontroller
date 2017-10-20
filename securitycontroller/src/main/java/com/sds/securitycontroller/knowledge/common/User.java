package com.sds.securitycontroller.knowledge.common;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;

public class User extends KnowledgeEntity {
	public User(String name, boolean enabled, String tenantId) {
		super();
		this.name = name;
		this.enabled = enabled;
		this.tenantId = tenantId;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -1586583819030290781L;
	String name;
	boolean enabled;
	String tenantId;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
}
