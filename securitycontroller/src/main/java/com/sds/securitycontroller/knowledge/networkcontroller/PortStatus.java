/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;


public class PortStatus  implements java.io.Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = 570881922805235169L;
	//	long portNumber;
	long receivePackets;
	long transmitPackets;
	long receiveBytes;
	long transmitBytes;
	long receiveDropped;
	long transmitDropped;
	long receiveErrors;
	long transmitErrors;
	long receiveFrameErrors;
	long receiveOverrunErrors;
	long receiveCRCErrors;
	long collisions;
	
	public PortStatus(long receivePackets, long transmitPackets, long receiveBytes, long transmitBytes, long receiveDropped, long transmitDropped, long receiveErrors, long transmitErrors, long receiveFrameErrors, long receiveOverrunErrors, long receiveCRCErrors, long collisions){
//		this.portNumber = portNumber;
		this.receivePackets = receivePackets;
		this.transmitPackets = transmitPackets;
		this.receiveBytes = receiveBytes;
		this.transmitBytes = transmitBytes;
		this.receiveDropped = receiveDropped;
		this.transmitDropped= transmitDropped;
		this.receiveErrors = receiveErrors;
		this.transmitErrors = transmitErrors;
		this.receiveFrameErrors = receiveFrameErrors;
		this.receiveOverrunErrors = receiveOverrunErrors;
		this.receiveCRCErrors = receiveCRCErrors;
		this.collisions = collisions;
	}
}
