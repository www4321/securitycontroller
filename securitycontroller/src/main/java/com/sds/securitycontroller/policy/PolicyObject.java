package com.sds.securitycontroller.policy;

import java.io.Serializable;

import com.sds.securitycontroller.knowledge.KnowledgeType;

public class PolicyObject implements Serializable {

	private static final long serialVersionUID = -117572678667266116L;
	KnowledgeType type;
	String id;
	Object object;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public KnowledgeType getType() {
		return type;
	}
	public void setType(KnowledgeType type) {
		this.type = type;
	}
	public Object getObject() {
		return object;
	}
	public PolicyObject(){		
	}
}
