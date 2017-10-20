/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;

import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;


public class SwitchPort  extends KnowledgeEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9149135309123677921L;
	int portNumber;
	String hardwareAddress;
	String name;
	int config;
	int state;
	int currentFeatures;
	int advertisedFeatures;
	int supportedFeatures;
	int peerFeatures;
	
	PortStatus status;
	
//	public CloudPort cloudPort;
	
//	int receivePackets;
//	int transmitPackets;
//	int receiveBytes;
//	int transmitBytes;
//	int receiveDropped;
//	int transmitDropped;
//	int receiveErrors;
//	int transmitErrors;
//	int receiveFrameErrors;
//	int receiveOverrunErrors;
//	int receiveCRCErrors;
//	int collisions;
	
	public SwitchPort(int portNumber,String hardwareAddress,String name,int config,
			int state,int currentFeatures,int advertisedFeatures,int supportedFeatures,
			int peerFeatures
//			,int receivePackets, int transmitPackets
//			, int receiveBytes,
//			int transmitBytes, int receiveDropped, int transmitDropped, int receiveErrors,
//			int transmitErrors, int receiveFrameErrors, int receiveOverrunErrors,
//			int receiveCRCErrors, int collisions
			,PortStatus status){
		this.type = KnowledgeType.NETWORK_SWITCH_PORT;
		this.portNumber = portNumber;
		this.hardwareAddress = hardwareAddress;
		this.name = name;
		this.config = config;
		this.state = state;
		this.currentFeatures = currentFeatures;
		this.advertisedFeatures = advertisedFeatures;
		this.supportedFeatures = supportedFeatures;
		this.peerFeatures = peerFeatures;
		this.status = status;
		

		this.attributeMap.put(KnowledgeEntityAttribute.ID, portNumber);
		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);
		this.attributeMap.put(KnowledgeEntityAttribute.HARDWARE_ADDRESS, hardwareAddress);;
		this.attributeMap.put(KnowledgeEntityAttribute.STATUS, state);
		
		
//		this.receivePackets = receivePackets;
//		this.transmitPackets = transmitPackets;
//		this.receiveBytes = receiveBytes;
//		this.transmitBytes = transmitBytes;
//		this.receiveDropped = receiveDropped;
//		this.transmitDropped= transmitDropped;
//		this.receiveErrors = receiveErrors;
//		this.transmitErrors = transmitErrors;
//		this.receiveFrameErrors = receiveFrameErrors;
//		this.receiveOverrunErrors = receiveOverrunErrors;
//		this.receiveCRCErrors = receiveCRCErrors;
//		this.collisions = collisions;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public String getHardwareAddress() {
		return hardwareAddress;
	}

	public String getName() {
		return name;
	}

	public int getConfig() {
		return config;
	}

	public int getState() {
		return state;
	}

	public int getCurrentFeatures() {
		return currentFeatures;
	}

	public int getAdvertisedFeatures() {
		return advertisedFeatures;
	}

	public int getSupportedFeatures() {
		return supportedFeatures;
	}

	public int getPeerFeatures() {
		return peerFeatures;
	}

	public PortStatus getStatus() {
		return status;
	}
}
