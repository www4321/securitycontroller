/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.flow;

//import flow.ActionType;


public class FlowAction 
implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8345972437821351772L;
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getMaxlength() {
		return maxLength;
	}
	public void setMaxlength(int maxLength) {
		this.maxLength = maxLength;
	}
	
	public short getLength() {
		return length;
	}
	public void setLength(short length) {
		this.length = length;
	}
	
	public ActionType getType() {
		return type;
	}
	public void setType(ActionType type) {
		this.type = type;
	}

	public int getLengthU() {
		return lengthU;
	}
	public void setLengthU(int lengthU) {
		this.lengthU = lengthU;
	}
	public int getvirtualLanIdentifier()
	{
	 return virtualLanIdentifier;
		
	}
	public void setvirtualLanIdentifier(int virtualLanIdentifier)
	{
	this.virtualLanIdentifier=virtualLanIdentifier;
	}

	public int getvirtualLanPriorityCodePoint()
	{
	 return virtualLanPriorityCodePoint;
		
	}
	public void setvirtualLanPriorityCodePoint(int virtualLanPriorityCodePoint)
	{
	this.virtualLanPriorityCodePoint=virtualLanPriorityCodePoint;
	}
	
	public String getnetworkAddress()
	{
	 return networkAddress;
		
	}
	public void setnetworkAddress(String networkAddress)
	{
	this.networkAddress=networkAddress;
	}
	
	public String getdataLayerAddress()
	{
	 return dataLayerAddress;
		
	}
	public void setdataLayerAddress(String dataLayerAddress)
	{
	this.dataLayerAddress=dataLayerAddress;
	}
	public int gettransportPort()
	{
	 return transportPort;
		
	}
	public void settransportPort(int transportPort)
	{
	this.transportPort=transportPort;
	}
	
	public int port;
	public int maxLength;
	public short length;
	public ActionType type;
	public int lengthU;
	public int virtualLanIdentifier;
	public int virtualLanPriorityCodePoint;
	public String networkAddress=null;
	public String dataLayerAddress=null;
	public int transportPort;
	@Override
	public String toString() {
		return "FlowAction [port=" + port + "]";
	}
	
	
}