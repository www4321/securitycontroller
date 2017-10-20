package com.sds.securitycontroller.command;

import java.util.Map;

/**
 * @author xpn
 * 设备策略命令
 */
public class DeviceCommand extends HttpCommandBase {

	private static final long serialVersionUID = 1L;

	public DeviceCommand() {
	}

	public DeviceCommand(String url, String method, Map<String, String> heads,
			String data) {
		super(url, method, heads, data);
	}

}
