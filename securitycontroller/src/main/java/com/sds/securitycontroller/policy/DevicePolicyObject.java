package com.sds.securitycontroller.policy;


public class DevicePolicyObject extends PolicyObject {

	private static final long serialVersionUID = 1L;
	//临时解决方案，待进一步修改
	protected Object deviceArgs;
	public Object getDeviceArgs() {
		return deviceArgs;
	}
	public void setDeviceArgs(Object deviceArgs) {
		this.deviceArgs = deviceArgs;
	}
	public DevicePolicyObject() {
		super();
	}
	@Override
	public Object getObject() {
		return this;
	}
}
