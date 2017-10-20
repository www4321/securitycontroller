/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.policy;

//策略类型
public enum PolicyActionType {
	REDIRECT_FLOW,
    //ie. used in waf case, proxy IP1:port1 to IP2:port2
    PROXY,
	CREATE_FLOW,
	DELETE_FLOW,
	DROP_FLOW,
	ALLOW_FLOW,
	BIND_MAC_IP,
	BLOCK_VM,
	CLEAN_FLOW,
	// adjust reputation
	ADJUST_REPUTATION,
	
	// BYOD
	BYOD_INIT,
	BYOD_ALLOW,

	//delete policy
	DELETE, 
	
	// restore policies
	RESTORE_REDIRECT_FLOW,
	RESTORE_BYOD_INIT, 
	RESTORE_BYOD_ALLOW, 
	
	GLOBAL_ABNORMAL_FLOW_DETECTED,
	
	UNKNOWN, 
	HTTP
	/*//WAF
	WAF_REVERSE_PROXY_CREATE_WEBSITE,//反向代理
	WAF_POLICY_TEMPLATE,//策略模版
	WAF_WHITE_LIST_CREATE_EXCEPTION,//例外：白名单
	//WVSS
	WVSS_CREATE_TASK*/
}
