/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event;

/**
 * New event type defines in here
 */
public enum EventType  implements java.io.Serializable{
	NEW_TASK,

	REG_SERVICE,
	UNREG_SERVICE,
	UNREG_COMMAND,
	ADD_DEVICE,
	REMOVE_DEVICE,
	UPDATE_DEVICE,
	APP_ADDED,
	APP_REMOVED,
	UPDATE_APP,
	RETRIEVE_FLOW, //to get the flow statistics
	RETRIEVED_FLOW, // the flow statistics is retrieved from the controller
	RETRIEVED_FLOWLIST,//the flow is retrieved and send in a form of flow list
	RECEIVED_POLICY,
	PUSH_FLOW,
	RECEIVED_LOG,
	RECEIVED_ALERT,
	ATTACK_WARNING,
	SCHEDULED_JOB,
	ADD_SUBSCRIPTION,// appended by wxt
	REMOVE_SUBSCRIPTION,// appended by wxt
	FILTER_CONTENT,
	REQUEST_KNOWLEDGE, // request to get knowledge
	RETRIEVED_KNOWLEDGE, // retrieved knowledge from another module
	//reputation
	MALICIOUS_FLOW_DETECTED,
	MALICIOUS_FLOW_RESTORED,
	MALICIOUS_VM_DETECTED,
	MALICIOUS_VM_RESTORED,
	TENANT_REPUTATION_UPDATE,
	USER_REPUTATION_UPDATE,
	
	//floodlight->sc
	NEW_FLOW,
	
	//logManager-> app or other module, add by rp
	NEW_REPORT_ITEM,
	
	//flow command applied
	FLOW_COMMAND_APPLIED,
	//xpn-test
	TEST,

	//新订单
	NEW_ORDER,
	//新的流命令
	FLOW_COMMAND,
	FLOW_FINISH

}
