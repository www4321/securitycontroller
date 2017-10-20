package com.sds.securitycontroller.cloud;

import java.util.HashMap;
import java.util.Map;

public class CloudTenant {
	
	private Map<String, CloudUser> users;

	private String id;
	private String name;
	
	public CloudTenant(String id, String name){
		this.setId(id);
		this.setName(name);
		this.users = new HashMap<String, CloudUser>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addUser(CloudUser user){
		this.users.put(user.id, user);
	}
	

	public CloudUser findUser(String userId){
		return this.users.get(userId);
	}
}
