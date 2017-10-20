/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app.manager;

import java.util.List;

import com.sds.securitycontroller.event.ISubscriptionResult;

public class AppPushRequest {
	
	String subscribeUrl;
	String subscriptionId;
	String appId;

	List<ISubscriptionResult> data;
	List<?> rawData;
	public List<ISubscriptionResult> getData() {
		return data;
	}

	public List<?> getRawData() {
		return rawData;
	}

	public void setData(List<ISubscriptionResult> data) {
		this.data = data;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}
	
	public void setSubscribeUrl(String url) {
		this.subscribeUrl = url;
		
	}

	public String getSubscribeUrl() {
		return this.subscribeUrl;
	}
	
	public String getAppId(){
		return this.appId;
	}

	@SuppressWarnings("unchecked")
	public AppPushRequest(String url, String subscriptionId, List<?> results, String appId){
		this.appId = appId;
		this.subscribeUrl = url;
		this.subscriptionId = subscriptionId;
		if(results.size()>0){
			if(results.get(0) instanceof ISubscriptionResult)
				this.data = (List<ISubscriptionResult>)results;
			else
				this.rawData = results;
		}
	}
	

	//public AppPushRequest(String url, String subscriptionId, List<ISubscriptionResult> results, String appId){
		

}
