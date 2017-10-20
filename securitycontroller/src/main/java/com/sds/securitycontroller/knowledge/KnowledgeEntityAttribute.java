/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge;

public enum KnowledgeEntityAttribute {
	ID,
	NAME,
	DESCRIPTION,
	STATUS,	// return Map<String,String>  
	HARDWARE_ADDRESS,
	DL_LAYER_ADDRESS,
	IP_ADDRESS,
	PORT,
	UPDATE_TIME,
	STATISTIC_INFO,	//flow table statistic count
	MATCH,
	TYPE,
	EXTERNAL_GATEWAY // external gateway id of a router
}
