/** 
*    Copyright 2014 BUPT.   
*	 STRATEGY RESEARCH DEPT. 
**/ 
/*
package com.sds.securitycontroller.command;

public class FlowCommand  implements java.io.Serializable {

	public FlowCommand() {
		super();
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;
	String flowname; // 流名，任意指定
	private short priority = 100;
	private short idleTimeout = 50;
	private short hardTimeout = 0;
	APInfo startPoint;	//重定向的起始点
	APInfo endPoint;	//重定向的终点
	MatchArguments matchArguments;	// 用于匹配的参数，五元组

	public FlowCommand(String flowname, short priority, APInfo startPoint,
			APInfo endPoint, MatchArguments matchArguments) {
		super();
		this.flowname = flowname;
		this.priority = priority;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.matchArguments = matchArguments;
	}

	public String getFlowname() {
		return flowname;
	}

	public void setFlowname(String flowname) {
		this.flowname = flowname;
	}

	public MatchArguments getArguments() {
		return matchArguments;
	}

	public void setArguments(MatchArguments arguments) {
		this.matchArguments = arguments;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(short priority) {
		this.priority = priority;
	}
	public APInfo getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(APInfo startPoint) {
		this.startPoint = startPoint;
	}

	public APInfo getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(APInfo endPoint) {
		this.endPoint = endPoint;
	}

	public short getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(short idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public short getHardTimeout() {
		return hardTimeout;
	}

	public void setHardTimeout(short hardTimeout) {
		this.hardTimeout = hardTimeout;
	}

	@Override
	public String toString() {
		return "FlowCommand [flowname=" + flowname + ", priority=" + priority
				+ ", startPoint=" + startPoint + ", endPoint=" + endPoint
				+ ", matchArguments=" + matchArguments + "]";
	}
}
** 
*    Copyright 2014 BUPT. 
**
*/ 
package com.sds.securitycontroller.command;

//import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


//import com.sds.securitycontroller.utils.Cypher;

public class RedirectFlowCommand extends FlowCommandBase implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RedirectFlowCommand() {
		super();
		// TODO Auto-generated constructor stub
	}

	MatchArguments matchArguments;	// 用于匹配的参数，五元组
	
	public List<RedirectDeviceInfo> devices = new ArrayList<RedirectDeviceInfo>();
	
//	public RedirectFlowCommand(String flowname, MatchArguments arguments , DstInfo dst_info) {
//		super();
//		this.commandName = flowname;
//		this.dst_info = dst_info;
//		this.matchArguments = arguments;	
//	}

	public RedirectFlowCommand(MatchArguments matchArguments,List<RedirectDeviceInfo> devices){
		super();
		super.matchArguments = matchArguments;
		this.devices = devices;
	}
//	public RedirectFlowCommand(String string, short defaultPriority, APInfo apInfo,
//			APInfo apInfo2, MatchArguments matchArguments2) {
//		// TODO Auto-generated constructor stub
//		
//	}

//	@Override
	
	

	public MatchArguments getArguments() {
		return matchArguments;
	}

	public void setArguments(MatchArguments arguments) {
		this.matchArguments = arguments;
	}

//	public short getPriority() {
//		return commandPriority;
//	}
//
//	public void setPriority(int priority) {
//		this.commandPriority = priority;
//	}

	@Override
	public String toString() {
		StringBuffer sBuff=new StringBuffer();
		sBuff.append("FlowCommand [flowname=").append(commandName)
			.append(", type=REDIRECT_FLOW")
			.append(", priority=").append(commandPriority);
		sBuff.append(", devices=[");
		for(RedirectDeviceInfo devInfo:devices){
			sBuff.append("{deviceid=").append(devInfo.deviceid)
			.append(", tag=").append(devInfo.tag)
			.append(", ingress={").append("mac=").append(devInfo.getIngress().getMac()).append(", ap=").append(devInfo.getIngress().getAp()).append("},")
			.append(", egress={").append("mac=").append(devInfo.getEgress().getMac()).append(", ap=").append(devInfo.getEgress().getAp()).append("}")
			.append("},");
		}
		sBuff.append("]");
		sBuff.append("]");
		return sBuff.toString();
//			.append(", direction=").append(direction)
//		return "FlowCommand [flowname=" + commandName + ", priority=" + commandPriority
//				+ ", direction=" + direction + ", dst_info=" + dst_info
//				+ ", matchArguments=" + matchArguments + ", id=" + id + "]";
	} 
}
