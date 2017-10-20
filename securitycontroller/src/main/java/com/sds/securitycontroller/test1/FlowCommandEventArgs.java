package com.sds.securitycontroller.test1;

import com.sds.securitycontroller.event.EventArgs;

public class FlowCommandEventArgs extends EventArgs {
	private String method;
	private String flowCommand;
	private String url;
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getFlowCommand() {
		return flowCommand;
	}
	public void setFlowCommand(String flowCommand) {
		this.flowCommand = flowCommand;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public FlowCommandEventArgs(String method, String url,String flowCommand) {
		super();
		this.method = method;
		this.flowCommand = flowCommand;
		this.url = url;
	}
	public FlowCommandEventArgs(){
		super();
	}
}
