/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.flow;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sds.securitycontroller.event.EventArgs;


public class FlowEventArgs extends EventArgs  implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3777570594371767791L;
	public Map<String, FlowInfo> flowMapping;
	
	public List<FlowInfo> flowList = null;
	
	//0115 
	long totalPacketCount;
	long totalByteCount;
	
	/*public FlowInfo flow;
	
	public FlowEventArgs(short src_inport, String src_mac, String dst_mac,
			int dl_type, String src_ip, String dst_ip, short src_port, short dst_port,
			int nw_proto, int counter){
		this.flow = new FlowInfo();
        this.flow.getMatch().setinputPort(src_inport);
        this.flow.getMatch().setdataLayerSource(src_mac);
        this.flow.getMatch().setdataLayerDestination(dst_mac);
        this.flow.getMatch().setdataLayerType(dl_type);
        this.flow.getMatch().setnetworkSource(src_ip);
        this.flow.getMatch().setnetworkDestination(dst_ip);
        this.flow.getMatch().setnetworkProtocol(nw_proto);
        this.flow.getMatch().settransportSource(src_port);
        this.flow.getMatch().settransportDestination(dst_port);
        this.flow.setByteCount(counter);
    }
    */
	public FlowEventArgs(List<FlowInfo> flowList){
		this.flowList = flowList;
		
	}
	
	public FlowEventArgs(Map<String, FlowInfo> flowMapping, Date time){
		this.flowMapping = flowMapping;
		this.time = time;
	}
	
	public Map<String, FlowInfo>  getFlowMapping(){
		return this.flowMapping;
	}

	
    public FlowInfo flowInfo;
    public FlowEventArgs(FlowInfo flowInfo, Date time){
        this.flowInfo = flowInfo;
        this.time = time;
    }

	public long getTotalPacketCount() {
		return totalPacketCount;
	}

	public void setTotalPacketCount(long totalPacketCount) {
		this.totalPacketCount = totalPacketCount;
	}

	public long getTotalByteCount() {
		return totalByteCount;
	}

	public void setTotalByteCount(long totalByteCount) {
		this.totalByteCount = totalByteCount;
	}

	@Override
	public String toString() {
		return "FlowEventArgs [flowMapping=" + flowMapping + ", flowList=" + flowList + ", totalPacketCount="
				+ totalPacketCount + ", totalByteCount=" + totalByteCount + ", flowInfo=" + flowInfo + "]";
	} 
	
	
}
