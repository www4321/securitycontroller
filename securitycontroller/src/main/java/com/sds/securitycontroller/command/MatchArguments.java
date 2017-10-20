/** 
 *    Copyright 2014 BUPT. 
 **/ 
package com.sds.securitycontroller.command;

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.packet.IPv4;
import com.sds.securitycontroller.utils.MACAddress;
import com.sds.securitycontroller.utils.openflow.HexString;

public class MatchArguments implements Serializable {
	private static final long serialVersionUID = 7876624570787349568L;
	final public static int OFPFW_ALL = ((1 << 22) - 1);
	public static final short VLAN_UNTAGGED = (short)0xffff;

	protected int wildcards;
	protected short inputPort;
//	protected byte[] dataLayerSource;
	protected String dataLayerSource;
//	protected byte[] dataLayerDestination;
	protected String dataLayerDestination;
	protected short dataLayerVirtualLan;
	protected byte dataLayerVirtualLanPriorityCodePoint;
	protected short dataLayerType;
	protected byte networkTypeOfService;
	protected byte networkProtocol;
	protected int networkSource;
	protected int networkDestination;
	protected short transportSource;
	protected short transportDestination;

	public MatchArguments() {
		this.wildcards = OFPFW_ALL;
		this.dataLayerDestination = "00:00:00:00:00:00";//new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0,0x0 };   dst_mac
		this.dataLayerSource = "00:00:00:00:00:00";//new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 };       src_mac
		this.dataLayerVirtualLan = VLAN_UNTAGGED;
		this.dataLayerVirtualLanPriorityCodePoint = 0;
		this.dataLayerType = 0;
		this.inputPort = 0;
		this.networkProtocol = 0;
		this.networkTypeOfService = 0;
		this.networkSource = 0;                     //src_ip
		this.networkDestination = 0;                //dst_ip
		this.transportDestination = 0;              //dst_port 
		this.transportSource = 0;                   //src_port
	}
	
	public MatchArguments(FlowMatch flowMatch) throws NumberFormatException,IllegalArgumentException{
		this.wildcards = flowMatch.getwildcards();
		this.dataLayerDestination = (flowMatch.getDataLayerDestination()==null ||flowMatch.getDataLayerDestination().isEmpty() )?"00:00:00:00:00:00":
				MACAddress.valueOf(flowMatch.getDataLayerDestination()).toString();
//				(flowMatch.getdataLayerDestination()==null ||flowMatch.getdataLayerDestination().isEmpty())?
//				new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 }:
//				MACAddress.valueOf(flowMatch.getdataLayerDestination()).toBytes();//TODO this correct??
		
//				new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0,0x0 };
		this.dataLayerSource = (flowMatch.getDataLayerSource()==null ||flowMatch.getDataLayerSource().isEmpty())?"00:00:00:00:00:00":
				MACAddress.valueOf(flowMatch.getDataLayerSource()).toString(); 
//				(flowMatch.getdataLayerSource()==null ||flowMatch.getdataLayerSource().isEmpty())?
//				new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 }:
//				MACAddress.valueOf(flowMatch.getdataLayerSource()).toBytes(); // new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 };
		
		this.dataLayerVirtualLan = (short) flowMatch.getDataLayerVirtualLan();//VLAN_UNTAGGED;
		this.dataLayerVirtualLanPriorityCodePoint = (byte) flowMatch.getDataLayerVirtualLanPriorityCodePoint();// 0;
		this.dataLayerType = (short) flowMatch.getDataLayerType();//0;
		this.inputPort = flowMatch.getInputPort();//0;
		this.networkProtocol = (byte) flowMatch.getNetworkProtocol();//0;
		this.networkTypeOfService =  (flowMatch.getNetworkTypeOfService()==null || flowMatch.getNetworkTypeOfService().isEmpty())?0:
				( (byte) Integer.parseInt(flowMatch.getNetworkTypeOfService()) );//0; //TODO correct???
		this.networkSource = (flowMatch.getNetworkSource()==null || flowMatch.getNetworkSource().isEmpty())?0:
				( IPv4.toIPv4Address( flowMatch.getNetworkSource() ));//0;
		this.networkDestination = (flowMatch.getNetworkDestination()==null ||flowMatch.getNetworkDestination().isEmpty() )?0:
			(IPv4.toIPv4Address( flowMatch.getNetworkDestination()));//0;
		this.transportDestination = (short) flowMatch.getTransportDestination();//0;
		this.transportSource =  (short) flowMatch.getTransportSource();//0;
	}

	public void fromJson(JsonNode matchArgumentsNode){
		if(matchArgumentsNode != null){
			if(matchArgumentsNode.hasNonNull("wildcards"))
				this.setWildcards(matchArgumentsNode.path("wildcards").asInt());
			if(matchArgumentsNode.hasNonNull("inputport"))
				this.setInputPort((short) matchArgumentsNode.path("inputport").asInt());
			if(matchArgumentsNode.hasNonNull("dataLayerSource"))
				this.setDataLayerSource(HexString.fromHexString(matchArgumentsNode.path("dataLayerSource").asText()).toString());
			if(matchArgumentsNode.hasNonNull("dataLayerDestination"))
				this.setDataLayerDestination(HexString.fromHexString(matchArgumentsNode.path("dataLayerDestination").asText()).toString());
			if(matchArgumentsNode.hasNonNull("dataLayerVirtualLan"))
				this.setDataLayerVirtualLan((short) matchArgumentsNode.path("dataLayerVirtualLan").asInt());
			if(matchArgumentsNode.hasNonNull("dataLayerVirtualLanPriorityCodePoint"))
				this.setDataLayerVirtualLanPriorityCodePoint((byte) matchArgumentsNode.path("dataLayerVirtualLanPriorityCodePoint").asInt());
			if(matchArgumentsNode.hasNonNull("dataLayerType"))
				this.setDataLayerType((short) matchArgumentsNode.path("dataLayerType").asInt());
			if(matchArgumentsNode.hasNonNull("networkTypeOfService"))
				this.setNetworkTypeOfService((byte) matchArgumentsNode.path("networkTypeOfService").asInt());
			if(matchArgumentsNode.hasNonNull("networkProtocol"))
				this.setNetworkProtocol((byte) matchArgumentsNode.path("networkProtocol").asInt());
			if(matchArgumentsNode.hasNonNull("networkSource"))
				this.setNetworkSource(IPv4.toIPv4Address(matchArgumentsNode.path("networkSource").asText()));
			if(matchArgumentsNode.hasNonNull("networkDestination"))
				this.setNetworkDestination(IPv4.toIPv4Address(matchArgumentsNode.path("networkDestination").asText()));
			if(matchArgumentsNode.hasNonNull("transportSource"))
				this.setTransportSource((short) matchArgumentsNode.path("transportSource").asInt());
			if(matchArgumentsNode.hasNonNull("transportDestination"))
				this.setTransportDestination((short) matchArgumentsNode.path("transportDestination").asInt());
		}

	}

	public int getWildcards() {
		return wildcards;
	}

	public void setWildcards(int wildcards) {
		this.wildcards = wildcards;
	}

	public short getInputPort() {
		return inputPort;
	}

	public void setInputPort(short inputPort) {
		this.inputPort = inputPort;
	}

	public String getDataLayerSource() {
		return dataLayerSource;
	}

	public void setDataLayerSource(String dataLayerSource) {
		this.dataLayerSource = dataLayerSource;
	}

	public String getDataLayerDestination() {
		return dataLayerDestination;
	}

	public void setDataLayerDestination(String dataLayerDestination) {
		this.dataLayerDestination = dataLayerDestination;
	}

	public short getDataLayerVirtualLan() {
		return dataLayerVirtualLan;
	}

	public void setDataLayerVirtualLan(short dataLayerVirtualLan) {
		this.dataLayerVirtualLan = dataLayerVirtualLan;
	}

	public byte getDataLayerVirtualLanPriorityCodePoint() {
		return dataLayerVirtualLanPriorityCodePoint;
	}

	public void setDataLayerVirtualLanPriorityCodePoint(
			byte dataLayerVirtualLanPriorityCodePoint) {
		this.dataLayerVirtualLanPriorityCodePoint = dataLayerVirtualLanPriorityCodePoint;
	}

	public short getDataLayerType() {
		return dataLayerType;
	}

	public void setDataLayerType(short dataLayerType) {
		this.dataLayerType = dataLayerType;
	}

	public byte getNetworkTypeOfService() {
		return networkTypeOfService;
	}

	public void setNetworkTypeOfService(byte networkTypeOfService) {
		this.networkTypeOfService = networkTypeOfService;
	}

	public byte getNetworkProtocol() {
		return networkProtocol;
	}

	public void setNetworkProtocol(byte networkProtocol) {
		this.networkProtocol = networkProtocol;
	}

	public int getNetworkSource() {
		return networkSource;
	}

	public void setNetworkSource(int networkSource) {
		this.networkSource = networkSource;
	}

	public int getNetworkDestination() {
		return networkDestination;
	}

	public void setNetworkDestination(int networkDestination) {
		this.networkDestination = networkDestination;
	}

	public short getTransportSource() {
		return transportSource;
	}

	public void setTransportSource(short transportSource) {
		this.transportSource = transportSource;
	}

	public short getTransportDestination() {
		return transportDestination;
	}

	public void setTransportDestination(short transportDestination) {
		this.transportDestination = transportDestination;
	}

	@Override
	public String toString() {
		String res = "";
		if(wildcards != OFPFW_ALL)res += "wildcards = " + wildcards+", ";
		if(inputPort != 0)res += "inputPort = " + inputPort+", ";
//		if(!Arrays.equals(dataLayerSource, new byte[]{0,0,0,0,0,0}))
//			res += "dataLayerSource = " + MACAddress.valueOf(dataLayerSource).toString()+", ";
//		if(!Arrays.equals(dataLayerDestination, new byte[]{0,0,0,0,0,0}))
//			res += "dataLayerDestination = " + MACAddress.valueOf(dataLayerDestination).toString()+", ";
		res += "dataLayerSource = " + dataLayerSource;
		res += "dataLayerDestination = " + dataLayerDestination;
		if(dataLayerVirtualLan != VLAN_UNTAGGED)res += "dataLayerVirtualLan = " + dataLayerVirtualLan+", ";
		if(dataLayerVirtualLanPriorityCodePoint != 0)res += "dataLayerVirtualLanPriorityCodePoint = " + dataLayerVirtualLanPriorityCodePoint+", ";
		if(dataLayerType != 0)res += "dataLayerType = " + dataLayerType+", ";
		if(networkTypeOfService != 0)res += "networkTypeOfService = " + networkTypeOfService+", ";
		if(networkProtocol != 0)res += "networkProtocol = " + networkProtocol+", ";
		if(networkSource != 0)res += "networkSource = " + networkSource+", ";
		if(networkDestination != 0)res += "networkDestination = " + networkDestination+", ";
		if(transportSource != 0)res += "transportSource = " + transportSource+", ";
		if(transportDestination != 0)res += "transportDestination = " + transportDestination+", ";		
		return res == "" ? "no field specified(wildcards all)":res;
	}
	//	public String dl_src = null;
	//	public String dl_dst = null;
	//	public int ether_type = 0x0800; //缺省为IP
	//	public int tp_src; //源端口
	//	public int tp_dst; //目的端口
	//	public String nw_src; //源网络地址
	//	public String nw_dst; //目的网络地址
	//	public String nw_proto; //网络协议
	public MatchArguments(String dl_src, String dl_dst) {
		super();
		this.dataLayerSource = MACAddress.valueOf(dl_src).toString();
		this.dataLayerDestination =  MACAddress.valueOf(dl_dst).toString();
	}
}
