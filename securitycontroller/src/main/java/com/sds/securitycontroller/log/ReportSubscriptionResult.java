package com.sds.securitycontroller.log;

import java.util.Map;

import com.sds.securitycontroller.event.ISubscriptionResult;

public class ReportSubscriptionResult implements ISubscriptionResult {
	public Map<?, ?> subscriptionContent;
	public String type;
	public String targetType;
	public String targetId;
	
	public ReportSubscriptionResult(Map<?, ?> content,String type,String targetType,String targetId){
		this.subscriptionContent = content;
		this.type = type;
		this.targetType = targetType;
		this.targetId = targetId;
	}

}
