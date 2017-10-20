
package com.sds.securitycontroller.policy;

import java.io.Serializable;

import com.sds.securitycontroller.device.DeviceFactory.DeviceType;

public class RedirectFlowRoutingItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1625815682102559581L;
	public short sequence;
	public DeviceType deviceType;
}
