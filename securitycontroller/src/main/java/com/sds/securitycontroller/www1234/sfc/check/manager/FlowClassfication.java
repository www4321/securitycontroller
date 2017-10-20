package com.sds.securitycontroller.www1234.sfc.check.manager;

//import flow.FlowInfo;
//import flow.CaculateFlowPath;
//import flow.FlowMatch;
//import flow.FlowAction;
//import flow.ActionType;
//import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;

import java.util.Set;

import com.sds.securitycontroller.flow.ActionType;
import com.sds.securitycontroller.flow.FlowAction;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.flow.FlowMatch;
public class FlowClassfication {
	 
	//www1234 add a member variable to store Flow Rules at a same Match. 2017/5/26
	public HashMap<FlowMatch, Set<FlowInfo>> flowClassfication (List<FlowInfo> flist){
		HashMap<FlowMatch, Set<FlowInfo>> flowrules = new HashMap<FlowMatch, Set<FlowInfo>>();
		for (int i=0;i<flist.size();i++){
			FlowMatch match=new FlowMatch();
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
			if (flist.get(i).getActions()!=null){
				int k;
				List<FlowAction> actionslist = flist.get(i).getActions();
				for(k=0;k<actionslist.size();k++){
					if(actionslist.get(k).getType().equals(Enum.valueOf(ActionType.class,"OUTPUT"))){
					 oport=actionslist.get(k).getPort();
					}				
				}
		     }
			
			//www1234 add
			if(!flowrules.containsKey(match))
				flowrules.put(match, new HashSet<FlowInfo>());
			flowrules.get(match).add(flist.get(i));
		}
		return flowrules;
	}

}
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