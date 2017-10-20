/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class FlowTable extends KnowledgeEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 504371132347265170L;
	int tableId;
	String name;
	long wildcards;
	long maximumEntries;
	int activeCount;
	long lookupCount;
	long matchedCount;
	int length;
	
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.NETWORK_FLOW};

	public FlowTable(int tableId, String name, long wildcards, 
			long maximumEntries, int activeCount, long lookupCount, 
			long matchedCount, int length){
		this.type = KnowledgeType.NETWORK_FLOW_TABLE;
		this.tableId = tableId;
		this.name = name;
		this.wildcards = wildcards;
		this.maximumEntries = maximumEntries;
		this.activeCount = activeCount;
		this.lookupCount = lookupCount;
		this.matchedCount = matchedCount;
		this.length = length;
		super.initAffiliates(affiliatedEntityTypes);
		
		this.attributeMap.put(KnowledgeEntityAttribute.ID, tableId);
		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);

	}


	public int getTableId() {
		return tableId;
	}


	public String getName() {
		return name;
	}


	public long getWildcards() {
		return wildcards;
	}


	public long getMaximumEntries() {
		return maximumEntries;
	}


	public int getActiveCount() {
		return activeCount;
	}


	public long getLookupCount() {
		return lookupCount;
	}


	public long getMatchedCount() {
		return matchedCount;
	}


	public int getLength() {
		return length;
	}
}
