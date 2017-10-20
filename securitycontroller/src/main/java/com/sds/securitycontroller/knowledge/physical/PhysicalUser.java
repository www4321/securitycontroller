package com.sds.securitycontroller.knowledge.physical;

import java.util.ArrayList;
import java.util.List;

import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.common.User;

public class PhysicalUser extends User{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5078268553275564772L;
	// related entities include: Network device, flow, 
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.PHYSICAL_DEVICE};
	//,KnowledgeType.NETWORK_DEVICE,KnowledgeType.NETWORK_FLOW};
	String status;	

	protected List<String> bindingMacList = new ArrayList<String>();
	
	
	public PhysicalUser(String name, boolean enabled, String tenantId,String status) {
		super(name, enabled, tenantId);
		type=KnowledgeType.PHYSICAL_USER;
		this.status=status;
		super.initAffiliates(affiliatedEntityTypes);
		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);
		this.attributeMap.put(KnowledgeEntityAttribute.STATUS, status);
	}

	
	public void addBindingMac(String mac){
		if(!bindingMacList.contains(mac))
			bindingMacList.add(mac);
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


	public List<String> getBindingMacList() {
		return bindingMacList;
	}
}
