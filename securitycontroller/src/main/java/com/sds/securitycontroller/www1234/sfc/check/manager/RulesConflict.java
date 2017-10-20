package com.sds.securitycontroller.www1234.sfc.check.manager;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.flow.ActionType;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.www1234.sfc.SFCFlowInfo;

public class RulesConflict {
	protected static Logger log = LoggerFactory.getLogger(RulesConflict.class);
	public List<FlowInfo> getConflictFlowRules(SFCFlowInfo sfcInfo,List<FlowInfo> fListInfo){
		List<FlowInfo> fList = new ArrayList<FlowInfo>();
		for(int i=0;i<fListInfo.size();i++){
			if(conflict(sfcInfo,fListInfo.get(i))){
				fList.add(fListInfo.get(i));
			}
		}
		return fList;
		
	}
	public boolean conflict(SFCFlowInfo sfcInfo,FlowInfo flowInfo){
//		log.info("sfcInfo="+sfcInfo.toString());
//		log.info("flowInfo"+flowInfo.toString());
		if(supset(sfcInfo,flowInfo)){
			int out_port = 0;
			for(int k=0;k<flowInfo.getActions().size();k++){
				if(flowInfo.getActions().get(k).getType().equals(Enum.valueOf(ActionType.class,"OUTPUT")))
					out_port = flowInfo.getActions().get(k).getPort();
			}
			if(flowInfo.getPriority()>sfcInfo.getPriority()&&sfcInfo.getOut_port()!=out_port){
				//log.info("sfcInfo is supset of flowInfo");
				return true;
			}
				
		}
		else if(subset(sfcInfo,flowInfo)){
			int out_port = 0;
			for(int k=0;k<flowInfo.getActions().size();k++){
				if(flowInfo.getActions().get(k).getType().equals(Enum.valueOf(ActionType.class,"OUTPUT")))
					out_port = flowInfo.getActions().get(k).getPort();
			}
			if(flowInfo.getPriority()>sfcInfo.getPriority()&&sfcInfo.getOut_port()!=out_port){
				//log.info("sfcInfo is subset of flowInfo");
				return true;
			}
				
		}
		else if(overlap(sfcInfo,flowInfo)){
			int out_port = 0;
			for(int k=0;k<flowInfo.getActions().size();k++){
				if(flowInfo.getActions().get(k).getType().equals(Enum.valueOf(ActionType.class,"OUTPUT")))
					out_port = flowInfo.getActions().get(k).getPort();
			}
			if(flowInfo.getPriority()>sfcInfo.getPriority()&&sfcInfo.getOut_port()!=out_port){
				//log.info("sfcInfo is overlap with flowInfo");
				return true;
			}
				
		}
		return false;
	}
	
	public boolean supset(SFCFlowInfo sfcInfo,FlowInfo flowInfo){
		//boolean flag =false;
		if(!sfcInfo.getDpid().equals(flowInfo.getDpid()))
			return false;
		if(sfcInfo.getIn_port()!=0&&sfcInfo.getIn_port()!=flowInfo.getMatch().getInputPort())
			return false;
		if(!sfcInfo.getDataLayerSource().equals("00:00:00:00:00:00") && !sfcInfo.getDataLayerSource().equals(flowInfo.getMatch().getDataLayerSource()))
			return false;
		if(!sfcInfo.getDataLayerDestination().equals("00:00:00:00:00:00") && !sfcInfo.getDataLayerDestination().equals(flowInfo.getMatch().getDataLayerDestination()))
			return false;
		if(sfcInfo.getTransportSource()!=0 && sfcInfo.getTransportSource()!=flowInfo.getMatch().getTransportSource())
			return false;
		if(sfcInfo.getTransportDestination()!=0 && sfcInfo.getTransportDestination()!=flowInfo.getMatch().getTransportDestination())
			return false;
		if(sfcInfo.getNetworkProtocol()!=0 && sfcInfo.getNetworkProtocol()!=flowInfo.getMatch().getNetworkProtocol())
			return false;
		if(!sfcInfo.getNetworkSource().equals("0.0.0.0")&& !ipSupSet(sfcInfo.getNetworkSource(),sfcInfo.getNetworkSourceMaskLen(),flowInfo.getMatch().getNetworkSource(),flowInfo.getMatch().getNetworkSourceMaskLen()))
			return false;
		if(!sfcInfo.getNetworkDestination().equals("0.0.0.0")&& !ipSupSet(sfcInfo.getNetworkDestination(),sfcInfo.getNetworkDestinationMaskLen(),flowInfo.getMatch().getNetworkDestination(),flowInfo.getMatch().getNetworkDestinationMaskLen()))
			return false;
		return true;
	}
	//return true if sfcInfo is subset of flowInfo.
	public boolean subset(SFCFlowInfo sfcInfo,FlowInfo flowInfo){
		if(!sfcInfo.getDpid().equals(flowInfo.getDpid()))
			return false;
		if(flowInfo.getMatch().getInputPort()!=0 && flowInfo.getMatch().getInputPort()!= sfcInfo.getIn_port())
			return false;
		if(!flowInfo.getMatch().getDataLayerSource().equals("00:00:00:00:00:00")&& !flowInfo.getMatch().getDataLayerSource().equals(sfcInfo.getDataLayerSource()))
			return false;
		if(!flowInfo.getMatch().getDataLayerDestination().equals("00:00:00:00:00:00")&& !flowInfo.getMatch().getDataLayerDestination().equals(sfcInfo.getDataLayerDestination()))
			return false;
		if(flowInfo.getMatch().getTransportSource()!=0 && sfcInfo.getTransportSource()!=flowInfo.getMatch().getTransportSource())
			return false;
		if(flowInfo.getMatch().getTransportDestination()!=0 && sfcInfo.getTransportDestination()!=flowInfo.getMatch().getTransportDestination())
			return false;
		if(flowInfo.getMatch().getNetworkProtocol()!=0 && sfcInfo.getNetworkProtocol()!=flowInfo.getMatch().getNetworkProtocol())
			return false;
		if(!flowInfo.getMatch().getNetworkSource().equals("0.0.0.0")&& !ipSupSet(flowInfo.getMatch().getNetworkSource(),flowInfo.getMatch().getNetworkSourceMaskLen(),sfcInfo.getNetworkSource(),sfcInfo.getNetworkSourceMaskLen()))
			return false;
		if(!flowInfo.getMatch().getNetworkDestination().equals("0.0.0.0")&& !ipSupSet(flowInfo.getMatch().getNetworkDestination(),flowInfo.getMatch().getNetworkDestinationMaskLen(),sfcInfo.getNetworkDestination(),sfcInfo.getNetworkDestinationMaskLen()))
			return false;
		return true;
	}
	//return true if sfcInfo is overlap with flowInfo.
	public boolean overlap(SFCFlowInfo sfcInfo,FlowInfo flowInfo){
		//boolean flag = false;
		if(!sfcInfo.getDpid().equals(flowInfo.getDpid()))
			return false;
		if(supset(sfcInfo,flowInfo))
			return false;
		if(subset(sfcInfo,flowInfo))
			return false;
		if(flowInfo.getMatch().getInputPort()!=0 && sfcInfo.getIn_port()!=0 && flowInfo.getMatch().getInputPort()!= sfcInfo.getIn_port())
			return false;
		if(!flowInfo.getMatch().getDataLayerSource().equals("00:00:00:00:00:00") && !sfcInfo.getDataLayerSource().equals("00:00:00:00:00:00") 
				&& !flowInfo.getMatch().getDataLayerSource().equals(sfcInfo.getDataLayerSource()))
			return false;
		if(!flowInfo.getMatch().getDataLayerDestination().equals("00:00:00:00:00:00")&&!sfcInfo.getDataLayerDestination().equals("00:00:00:00:00:00") 
				&& !flowInfo.getMatch().getDataLayerDestination().equals(sfcInfo.getDataLayerDestination()))
			return false;
		if(flowInfo.getMatch().getTransportSource()!=0 && sfcInfo.getTransportSource()!=0 && 
				sfcInfo.getTransportSource()!=flowInfo.getMatch().getTransportSource())
			return false;
		if(flowInfo.getMatch().getTransportDestination()!=0 && sfcInfo.getTransportDestination()!=0 
				&& sfcInfo.getTransportDestination()!=flowInfo.getMatch().getTransportDestination())
			return false;
		if(sfcInfo.getNetworkProtocol()!=0 &&flowInfo.getMatch().getNetworkProtocol()!=0 
				&& sfcInfo.getNetworkProtocol()!=flowInfo.getMatch().getNetworkProtocol())
			return false;
		if(!sfcInfo.getNetworkSource().equals("0.0.0.0")&&!flowInfo.getMatch().getNetworkSource().equals("0.0.0.0")&& 
				!ipSupSet(sfcInfo.getNetworkSource(),sfcInfo.getNetworkSourceMaskLen(),flowInfo.getMatch().getNetworkSource(),flowInfo.getMatch().getNetworkSourceMaskLen())
				&&
				!ipSupSet(flowInfo.getMatch().getNetworkSource(),flowInfo.getMatch().getNetworkSourceMaskLen(),sfcInfo.getNetworkSource(),sfcInfo.getNetworkSourceMaskLen()))
			return false;
		if(!sfcInfo.getNetworkSource().equals("0.0.0.0")&&!flowInfo.getMatch().getNetworkSource().equals("0.0.0.0")&& 
				!ipSupSet(sfcInfo.getNetworkDestination(),sfcInfo.getNetworkDestinationMaskLen(),flowInfo.getMatch().getNetworkDestination(),flowInfo.getMatch().getNetworkDestinationMaskLen())
				&&
				!ipSupSet(flowInfo.getMatch().getNetworkDestination(),flowInfo.getMatch().getNetworkDestinationMaskLen(),sfcInfo.getNetworkDestination(),sfcInfo.getNetworkDestinationMaskLen()))
			return false;
		return true;
	}
	
	// return true if ip1 contain ip2.
	public static boolean ipSupSet(String ip1,int ip1Mask,String ip2,int ip2Mask){
		int ipOne = ipStrToInt(ip1);
		int ipSec = ipStrToInt(ip2);
		if(ip1Mask>ip2Mask)
			return false;
		if(((ipOne >> (32-ip1Mask)) ^ (ipSec >> (32-ip1Mask)))==0)
			return true;
		return false;
	}
	
	private static int ipStrToInt(String strip){
        String[] ip_str = strip.split("\\.");
        int ip = 0;
        ip += Integer.valueOf(ip_str[0]) << 24;
        ip += Integer.valueOf(ip_str[1]) << 16;
        ip += Integer.valueOf(ip_str[2]) << 8;
        ip += Integer.valueOf(ip_str[3]);       
        return ip;
        
    }
	public static void main(String[]args){
		System.out.println(ipSupSet("192.168.64.0",18,"192.168.69.0",20));
		System.out.println(ipSupSet("192.168.64.0",20,"192.168.69.0",20));
	}
}
