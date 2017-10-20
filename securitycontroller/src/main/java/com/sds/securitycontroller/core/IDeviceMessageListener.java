/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.core;

import com.sds.securitycontroller.core.message.DeviceMessage;
import com.sds.securitycontroller.core.remotenode.RemoteNode;
import com.sds.securitycontroller.protocol.DeviceMessageType;


public interface IDeviceMessageListener extends IListener<DeviceMessageType> {
	  public Command receive(RemoteNode node, DeviceMessage msg, SecurityControllerContext cntx);

}
