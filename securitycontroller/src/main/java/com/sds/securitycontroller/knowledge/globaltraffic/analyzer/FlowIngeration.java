package com.sds.securitycontroller.knowledge.globaltraffic.analyzer;
//import flow.FlowInfo;
//import flow.CaculateFlowPath;
//import flow.FlowMatch;
//import flow.FlowAction;
//import flow.ActionType;
//import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//import java.io.IOException;
//
//import java.util.Map.Entry;
//import java.util.*;

import com.sds.securitycontroller.flow.ActionType;
import com.sds.securitycontroller.flow.FlowAction;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;
public class FlowIngeration {
	
	public static Map<String, Long[]> trafficStatMap = new HashMap<String, Long[]>(); 
			
	public void flowingeration (List<FlowInfo> flist)
	{
		 trafficStatMap.clear();
		 CaculateFlowPath.caculatepath.clear();
		 int i;
		 CaculateFlowPath ca=new CaculateFlowPath();
		 /**
			 * {
	            "actions": [
	              {
	                
	                    "port": 6,
	                    "maxLength": -1, ,
	                    "length": 8,
	                    "type": "OUTPUT",
	                    "lengthU": 8
	                }
	            ],
	            "priority": 0,
	            "cookie": 9007199254740992,
	            "idleTimeout": 5,
	            "hardTimeout": 0,
	            "match": {
	                "dataLayerDestination": "fa:16:3e:fb:b1:73\
	                "dataLayerType": "0x0000",
	                "dataLayerVirtualLan": -1,
	                "dataLayerVirtualLanPriorityCodePoint": 0,
	                "inputPort": 1,
	                "networkDestination": "0.0.0.0",
	                "networkDestinationMaskLen": 0,
	                "networkProtocol": 0,
	                "networkSource": "0.0.0.0",
	                "networkSourceMaskLen": 0,
	                "networkTypeOfService": 0,
	                "transportDestination": 0,
	                "transportSource": 0,
	                "wildcards": 2629872
	            },
	            "durationSeconds": 0,
	            "durationNanoseconds": 987000000,
	            "packetCount": 2,
	            "byteCount": 720,
	            "tableId": 0
	        }
			 */
		
		for (i=0;i<flist.size();i++)
		{
			FlowMatch match=new FlowMatch();
			FlowMatch match1=new FlowMatch();
			String Destination=flist.get(i).getdataLayerDestination();
			match.setDataLayerDestination(Destination);
			int Dtype=flist.get(i).getdataLayerType();
			match.setDataLayerType(Dtype);
			String Source=flist.get(i).getdataLayerSource();
			match.setDataLayerSource(Source);
			int  Vlan=flist.get(i).getdataLayerVirtualLan();
			match.setDataLayerVirtualLan(Vlan);
			int Vlanport=flist.get(i).getdataLayerVirtualLanPriorityCodePoint();
			match.setDataLayerVirtualLanPriorityCodePoint(Vlanport);
			String Ndestination=flist.get(i).getnetworkDestination();
			match.setNetworkDestination(Ndestination);
			String  Nsource=flist.get(i).getnetworkSource();
			match.setNetworkSource(Nsource);
			int Ndestinationmask=flist.get(i).getnetworkDestinationMaskLen();
			match.setNetworkDestinationMaskLen(Ndestinationmask);
			int Nprotocol=flist.get(i).getnetworkProtocol();
			match.setNetworkProtocol(Nprotocol);
			int Nsourcemask=flist.get(i).getnetworkSourceMaskLen();
			match.setNetworkSourceMaskLen(Nsourcemask);
			String Ntype=flist.get(i).getnetworkTypeOfService();
			match.setNetworkTypeOfService(Ntype);
			int Tdestination=flist.get(i).gettransportDestination();
			match.setTransportDestination(Tdestination);
			int Tsource=flist.get(i).gettransportSource();
			match.setTransportSource(Tsource);
			int wildcards=flist.get(i).getwildcards();
			match.setwildcards(wildcards);
			short inputPort=flist.get(i).getinputPort();
			match.setInputPort(inputPort);
			String dpid=flist.get(i).getDpid();
			int oport=0;
			
			
			if (flist.get(i).getActions()!=null)
			{
				int j;
				int k;
				List<FlowAction> actionslist = flist.get(i).getActions();
				
			//match1=match;
				for(k=0;k<actionslist.size();k++)
				{
					if(actionslist.get(k).getType().equals(Enum.valueOf(ActionType.class,"OUTPUT")))
					{
					 oport=actionslist.get(k).getPort();
					}
				if(actionslist.get(k).getType().equals(Enum.valueOf(ActionType.class,"SET_VLAN_ID"))||actionslist.get(k).getType().equals(Enum.valueOf(ActionType.class,"SET_VLAN_PCP"))||
					actionslist.get(k).getType().equals(Enum.valueOf(ActionType.class,"SET_NW_SRC"))||actionslist.get(k).getType().equals(Enum.valueOf(ActionType.class,"SET_NW_DST"))||actionslist.get(k).getType().equals(Enum.valueOf(ActionType.class,"SET_DL_SRC"))||
					actionslist.get(k).getType().equals(Enum.valueOf(ActionType.class,"SET_DL_DST"))||actionslist.get(k).getType().equals(Enum.valueOf(ActionType.class,"SET_TP_SRC"))||actionslist.get(k).getType().equals(Enum.valueOf(ActionType.class,"SET_TP_DST")))
					{
					match1.setDataLayerDestination(Destination);
					match1.setDataLayerSource(Source);
					match1.setDataLayerType(Dtype);
					match1.setDataLayerVirtualLan(Vlan);
					match1.setDataLayerVirtualLanPriorityCodePoint(Vlanport);
					match1.setNetworkDestination(Ndestination);
				    match1.setNetworkDestinationMaskLen(Ndestinationmask);
                    match1.setNetworkProtocol(Nprotocol);
			        match1.setNetworkSource(Nsource);
		            match1.setNetworkSourceMaskLen(Nsourcemask);
					match1.setNetworkTypeOfService(Ntype);
				    match1.setTransportDestination(Tdestination);
					match1.setTransportSource(Tsource);
					match1.setwildcards(wildcards);			
				// System.out.println(match1);
						for(j=0;j<actionslist.size();j++)
						{
							if(actionslist.get(j).getType().equals(Enum.valueOf(ActionType.class,"SET_VLAN_ID")))
							{
								//System.out.println("true");
								match1. setDataLayerVirtualLan(actionslist.get(j).getvirtualLanIdentifier());
							//	System.out.println(actionslist.get(i).getvirtualLanIdentifier());
								//System.out.println(match1.dataLayerVirtualLan);
								}
							if(actionslist.get(j).getType().equals(Enum.valueOf(ActionType.class,"SET_VLAN_PCP")))
							{
								//System.out.println("true");
								match1.setDataLayerVirtualLanPriorityCodePoint(actionslist.get(j).getvirtualLanPriorityCodePoint());
							//	System.out.println(actionslist.get(k).getvirtualLanPriorityCodePoint());
							//	System.out.println(match1.dataLayerVirtualLanPriorityCodePoint);
							}
							if(actionslist.get(j).getType().equals(Enum.valueOf(ActionType.class,"SET_NW_SRC")))
							{
								match1.setNetworkSource(actionslist.get(j).getnetworkAddress());
							//	System.out.println(match1.networkSource);
							}
							if(actionslist.get(j).getType().equals(Enum.valueOf(ActionType.class,"SET_NW_DST")))
							{
								match1.setNetworkDestination(actionslist.get(j).getnetworkAddress());
							}
							if(actionslist.get(j).getType().equals(Enum.valueOf(ActionType.class,"SET_DL_SRC")))
							{
								match1.setDataLayerSource(actionslist.get(j).getdataLayerAddress());
							}
							if(actionslist.get(j).getType().equals(Enum.valueOf(ActionType.class,"SET_DL_DST")))
							{
								match1.setDataLayerDestination(actionslist.get(j).getdataLayerAddress());
							}
							
							if(actionslist.get(j).getType().equals(Enum.valueOf(ActionType.class,"SET_TP_SRC")))
							{
								match1.setTransportSource(actionslist.get(j).gettransportPort());
							}
							if(actionslist.get(j).getType().equals(Enum.valueOf(ActionType.class,"SET_TP_DST")))
							{
								match1.setTransportDestination(actionslist.get(j).gettransportPort());
							}
							
							}
						break;
				}
		}
				  if(match1.getDataLayerDestination()!=null)
			      {
			    	  			    		  
			    	// System.out.println(match1);
					  CaculateFlowPath.modflow.add(match);
					  CaculateFlowPath.modflow.add(match1);
			        /*ca.modflow.add(match);
			        ca.modflow.add(match1);*/
			   //     System.out.println(modflow.get(0));
			    //    System.out.println(modflow.get(1));
			    //    System.out.println(modflow.size());
			      }
			}
			
			Long[] trafficStats = { flist.get(i).getPacketCount(), flist.get(i).getByteCount()};
			trafficStatMap.put(match.getMatch(), trafficStats);
		
			//ca.caculateflowpath (match,dpid,oport);
			NodePortTuple np=new NodePortTuple(dpid,oport);
			ca.caculateflowpath (match,np);
		}
	}

}
