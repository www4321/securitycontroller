package com.sds.securitycontroller.common;

import java.util.HashMap;
import java.util.Map;

import com.sds.securitycontroller.knowledge.KnowledgeType;

public class Entity {
	String id;
	Map<String, Object> attrs;
	
	public Entity(String id){
		this.id = id;
		this.attrs = new HashMap<String, Object>();
	}

	public String getId() {
		return id;
	}
	
	
	public Object getAttribute(String attr){
		return attrs.get(attr);
	}
	

	public Map<String, Object> getAttributes(){
		return this.attrs;
	}
	
	@Override
	public String toString(){
		return this.id;
	}
	
	KnowledgeType type;

	public KnowledgeType getType() {
		return type;
	}

	public void setType(KnowledgeType type) {
		this.type = type;
	} 

}
