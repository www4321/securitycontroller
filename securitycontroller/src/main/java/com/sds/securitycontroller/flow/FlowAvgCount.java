/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.flow;

import java.io.Serializable;
import java.util.Date;

public class FlowAvgCount implements Serializable {
	
	protected FlowInfo flowInfo;
	protected double avgPktCount;
	protected double avgByteCount;
	protected int timeInterval;
	protected int pktLearnedCount;
	protected Date createTime;
	protected Date refreshTime;
	/**
	 * 
	 */
	private static final long serialVersionUID = 3956187740694287991L;


	public FlowAvgCount(FlowInfo flowInfo, long avgPktCount, long avgByteCount,
			int timeInterval, Date createTime, Date refreshTime) {
		super();
		this.flowInfo = flowInfo;
		this.avgPktCount = avgPktCount;
		this.avgByteCount = avgByteCount;
		this.timeInterval = timeInterval;
		this.createTime = createTime;
		this.refreshTime = refreshTime;
	}
	public FlowAvgCount(FlowInfo flowInfo, long avgPktCount, long avgByteCount) {
		super();
		this.flowInfo = flowInfo;
		this.avgPktCount = avgPktCount;
		this.avgByteCount = avgByteCount;
	}
	public FlowAvgCount(){
		super();
	}
	public FlowAvgCount(FlowInfo fi){
		this.flowInfo = fi;
		this.avgByteCount = fi.getByteCount();
		this.avgPktCount = fi.getPacketCount();
		this.pktLearnedCount = 1;
		this.createTime = new Date();
		this.refreshTime = this.createTime;
	}

	public FlowInfo getFlowInfo() {
		return flowInfo;
	}
	public void setFlowInfo(FlowInfo flowInfo) {
		this.flowInfo = flowInfo;
	}
	public double getAvgPktCount() {
		return avgPktCount;
	}
	public double updateAvgPktCount(long pkts) {
		avgPktCount = (avgPktCount*pktLearnedCount + pkts)/(pktLearnedCount+1);
		return avgPktCount;
	}
	public double updateAvgByteCount(long bytes) {
		avgByteCount = (avgByteCount*pktLearnedCount + bytes)/(pktLearnedCount+1);
		return avgByteCount;
	}
	public void setAvgPktCount(long avgPktCount) {
		this.avgPktCount = avgPktCount;
	}
	public double getAvgByteCount() {
		return avgByteCount;
	}
	public void setAvgByteCount(long avgByteCount) {
		this.avgByteCount = avgByteCount;
	}
	public int getTimeInterval() {
		return timeInterval;
	}
	public void setTimeInterval(int timeInterval) {
		this.timeInterval = timeInterval;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getRefreshTime() {
		return refreshTime;
	}
	public void setRefreshTime(Date refreshTime) {
		this.refreshTime = refreshTime;
	}
	public int getPktLearnedCount() {
		return pktLearnedCount;
	}
	public void setPktLearnedCount(int pktLearnedCount) {
		this.pktLearnedCount = pktLearnedCount;
	}
	public int increasePktLearnedCount() {
		this.refreshTime = new Date();
		return pktLearnedCount>=65535?65535:++pktLearnedCount;
	}
	@Override
	public String toString() {
		return "FlowAvgCount [flowInfo="+"protocol:"+flowInfo.getnetworkProtocol()+" " + flowInfo.getnetworkSource()+":"+flowInfo.gettransportSource()+"->"+flowInfo.getnetworkDestination()+":"+flowInfo.gettransportDestination() + ", avgPktCount="
				+ avgPktCount + ", avgByteCount=" + avgByteCount
				+ ", timeInterval=" + timeInterval + ", pktLearnedCount="
				+ pktLearnedCount + ", createTime=" + createTime
				+ ", refreshTime=" + refreshTime + "]";
	}
	
}
