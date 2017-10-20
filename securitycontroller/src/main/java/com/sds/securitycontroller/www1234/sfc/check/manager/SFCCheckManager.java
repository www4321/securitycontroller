package com.sds.securitycontroller.www1234.sfc.check.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.flow.FlowEventArgs;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.flow.manager.IFlowPollingService;
import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuplePath;
import com.sds.securitycontroller.knowledge.globaltraffic.analyzer.IGlobalTrafficAnalyzeService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.www1234.sfc.SFCFlowInfo;
import com.sds.securitycontroller.www1234.sfc.SFCInstance;
import com.sds.securitycontroller.www1234.sfc.SFCSwitchPortInstance;
import com.sds.securitycontroller.www1234.sfc.TrafficPattern;
import com.sds.securitycontroller.www1234.sfc.manager.ISFCService;


public class SFCCheckManager implements ISFCCheckService, ISecurityControllerModule,IEventListener{
	
	protected static Logger log = LoggerFactory.getLogger(SFCCheckManager.class);
	protected IRestApiService restApi;
	protected ISFCService sfcManager;
	protected IEventManagerService eventManager;
	protected IFlowPollingService flowPollingService;
	//protected Map<SFCInstance,List<FlowInfo>> rulesMap;
	private RulesConflict rulesConflict;
	public HashMap<String, Set<FlowInfo>> flowrules = new HashMap<String, Set<FlowInfo>>();
	
	public boolean sfcCheckenable = false;
	public List<SFCInstance> sfcInstances =null; //SFCCheck 检验的服务链
	public Map<String,List<ConflictFlowInfo>> errorFolwMap;
	
	
	
	@Override
	public List<ConflictFlowInfo> getConflictRules(String sfc_id) {
		if(errorFolwMap.containsKey(sfc_id))
			return errorFolwMap.get(sfc_id);
		return null;
	}

	@Override
	public void init(SecurityControllerModuleContext context) throws SecurityControllerModuleException {
		this.restApi = context.getServiceImpl(IRestApiService.class);
		this.sfcManager = context.getServiceImpl(ISFCService.class);
		this.eventManager = context.getServiceImpl(IEventManagerService.class);
		this.flowPollingService = context.getServiceImpl(IFlowPollingService.class);
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		SFCCheckRoutable sfcCheckRoutable = new SFCCheckRoutable();
		restApi.addRestletRoutable(sfcCheckRoutable);
		log.info("SFC Check start up");
		//rulesMap = new HashMap<SFCInstance,List<FlowInfo>>();
		
		eventManager.addEventListener(EventType.RETRIEVED_FLOWLIST, this);
		sfcInstances = new ArrayList<SFCInstance>();
		errorFolwMap = new HashMap<String,List<ConflictFlowInfo>>();
		rulesConflict = new RulesConflict();
	}
	@Override
	public boolean sfcCheckbySwitchPort(String sfc_id){	
		List<FlowInfo> fList = flowPollingService.getAllFlows();
		flowrules = flowClassfication(fList);
		SFCSwitchPortInstance sfcSwitchPortInstance = sfcManager.getSFCSwitchPortInstancebyId(sfc_id);
		List<SFCFlowInfo> sfcfLists = getSFCFlowInfosBySwitchPortInstance(sfcSwitchPortInstance);		
		List<FlowInfo> fListInfo =new ArrayList<FlowInfo>();
		List<FlowInfo> conflict = null;
		List<ConflictFlowInfo> errorFlowInfo =new ArrayList<ConflictFlowInfo>();
		for(int i=0;i<sfcfLists.size();i++){
			fListInfo.clear();
			if(flowrules.containsKey(sfcfLists.get(i).getDpid()))
				fListInfo.addAll(flowrules.get(sfcfLists.get(i).getDpid()));
			conflict = rulesConflict.getConflictFlowRules(sfcfLists.get(i), fListInfo);
			if(conflict.size()!=0){
				errorFlowInfo.add(new ConflictFlowInfo(sfcfLists.get(i),conflict)); 
			}
		}
		errorFolwMap.put(sfc_id, errorFlowInfo);
		if(errorFolwMap.get(sfc_id).size()!=0)
			return true;
		return false;
	}
	
	public List<SFCFlowInfo> getSFCFlowInfosBySwitchPortInstance(SFCSwitchPortInstance sfcSwitchPortInstance){
		List<SFCFlowInfo> sfcfLists = new ArrayList<SFCFlowInfo>();
		List<NodePortTuplePath> sfcPath = sfcManager.getRouteById(sfcSwitchPortInstance.getSfc_id());
		
		Map<String, String> traffic = sfcSwitchPortInstance.getTrafficPattern();
		String src[] = traffic.get("src-ip").split("/");
		String networkSource =null;
		int networkSourceMaskLen=-1;
		if(src.length==2){
			networkSource = src[0];
			networkSourceMaskLen = Integer.parseInt(src[1]);
		}else{
			networkSource = src[0];
			networkSourceMaskLen = 32;
		}
		
		String src1[] = traffic.get("dst-ip").split("/");
		String networkDestination =null;
		int networkDestinationMaskLen=-1;
		if(src1.length==2){
			networkDestination = src1[0];
			networkDestinationMaskLen = Integer.parseInt(src1[1]);
		}else{
			networkDestination = src1[0];
			networkDestinationMaskLen = 32;
		}
		for(int i=0;i<sfcPath.size();i++){
			String src_mac = traffic.containsKey("src-mac")?traffic.get("src-mac"):"00:00:00:00:00:00";
			String dst_mac = traffic.containsKey("dst-mac")?traffic.get("dst-mac"):"00:00:00:00:00:00";
			String src_port = traffic.containsKey("src-port")?traffic.get("src-port"):"0";
			String dst_port = traffic.containsKey("dst-port")?traffic.get("dst-port"):"0";
			String protocol = traffic.get("protocol");
			int proto = 0;
	        switch(protocol){
			case "0x01":
				proto = 0x01;break;
			case "0x06":
				proto = 0x06;break;
			case "0x11":
				proto = 0x11;break;
			default: break;
			}
	        
			sfcfLists.add(new SFCFlowInfo(sfcPath.get(i).getNodeId(),sfcPath.get(i).getIn_portId(),src_mac,dst_mac,
					networkSource,networkDestination,networkDestinationMaskLen,networkSourceMaskLen,Integer.parseInt(src_port),Integer.parseInt(dst_port),
					proto,sfcSwitchPortInstance.getPriority(),sfcPath.get(i).getOut_portId()));
		}	
		return sfcfLists;
	}
	
	
	@Override
	public boolean sfcCheck(SFCInstance sfcInstance) {
		List<SFCFlowInfo> sfcfLists = getSFCFlowInfos(sfcInstance);
		List<FlowInfo> fListInfo =new ArrayList<FlowInfo>();
		List<FlowInfo> errorFlowInfo =new ArrayList<FlowInfo>();
		for(int i=0;i<sfcfLists.size();i++){
			fListInfo.clear();
			if(flowrules.containsKey(sfcfLists.get(i).getDpid()))
				fListInfo.addAll(flowrules.get(sfcfLists.get(i).getDpid()));
			errorFlowInfo.addAll(rulesConflict.getConflictFlowRules(sfcfLists.get(i), fListInfo));
		}
		//SFC check
//		log.info("\n");
//		log.info("\n the key information of the SFC policy:"+"SFC policy ID:"+sfcInstance.getSfc_id()+"\n NFs of SFC policy:"+sfcInstance.getSfc()+
//				"\n the flow pattern is :"+sfcInstance.getTrafficPattern().toString());
//		for(int i=0;i<errorFlowInfo.size();i++){
//			log.info("\n \n conflict rules:"+errorFlowInfo.get(i)+"\n");
//		}
			
		
		
		return false;
	}
	public List<SFCFlowInfo> getSFCFlowInfos(SFCInstance sfcInstance){
		List<SFCFlowInfo> sfcfLists = new ArrayList<SFCFlowInfo>();
		List<NodePortTuplePath> sfcPath = sfcManager.getRouteById(sfcInstance.getSfc_id());
		//log.info("SFC Path:"+sfcPath.toString());
		TrafficPattern traffic = sfcInstance.getTrafficPattern();
		String src[] = traffic.getNetworkSource().split("/");
		String networkSource =null;
		int networkSourceMaskLen=-1;
		if(src.length==2){
			networkSource = src[0];
			networkSourceMaskLen = Integer.parseInt(src[1]);
		}else{
			networkSource = src[0];
			networkSourceMaskLen = 32;
		}
		
		String src1[] = traffic.getNetworkDestination().split("/");
		String networkDestination =null;
		int networkDestinationMaskLen=-1;
		if(src1.length==2){
			networkDestination = src1[0];
			networkDestinationMaskLen = Integer.parseInt(src1[1]);
		}else{
			networkDestination = src1[0];
			networkDestinationMaskLen = 32;
		}
		for(int i=0;i<sfcPath.size();i++){
			sfcfLists.add(new SFCFlowInfo(sfcPath.get(i).getNodeId(),sfcPath.get(i).getIn_portId(),traffic.getDataLayerSource(),traffic.getDataLayerDestination(),
					networkSource,networkDestination,networkDestinationMaskLen,networkSourceMaskLen,traffic.getTransportSource(),traffic.getTransportDestination(),
					traffic.getNetworkProtocol(),sfcInstance.getPriority(),sfcPath.get(i).getOut_portId()));
		}
		return sfcfLists;
	}
	
	public Map<String,List<FlowInfo>> flowInfoToMap(Set<FlowInfo> flowInfos){
		Map<String,List<FlowInfo>> switchFlowMap = new HashMap<String,List<FlowInfo>>();
		FlowInfo flowRule = null;
		Iterator<FlowInfo> iterg= flowInfos.iterator();
		while(iterg.hasNext()){
			flowRule = iterg.next();
			if(!switchFlowMap.containsKey(flowRule.getDpid()))
				switchFlowMap.put(flowRule.getDpid(), new ArrayList<FlowInfo>());
			switchFlowMap.get(flowRule.getDpid()).add(flowRule);
		}
		//log Switch DPID and flow rules message.
		Set<String> key = switchFlowMap.keySet();
		Iterator<String> iter = key.iterator();
		List<FlowInfo> list =null;
		while(iter.hasNext()){
			list = switchFlowMap.get(iter.next());
			for(int i=0;i<list.size();i++)
				log.info("dpid={},in_put={},out_port={}",list.get(i).getDpid(),list.get(i).getinputPort(),list.get(i).getActions());
			log.info("............................");
		}
		return switchFlowMap;
	}
	public FlowMatch getFlowMatch(SFCInstance sfcInstance){
		FlowMatch flowMatch = new FlowMatch();
		TrafficPattern traffic = sfcInstance.getTrafficPattern();
		flowMatch.setDataLayerSource(traffic.getDataLayerSource());
		flowMatch.setDataLayerDestination(traffic.getDataLayerDestination());
		flowMatch.setNetworkSource(traffic.getNetworkSource());
		flowMatch.setNetworkDestination(traffic.getNetworkDestination());
		flowMatch.setTransportSource(traffic.getTransportSource());
		flowMatch.setTransportDestination(traffic.getTransportDestination());
		flowMatch.setDataLayerVirtualLan(0);
		flowMatch.setDataLayerType(2048);
		//具体表示什么协议，要查看openflow协议，待完成。。。。。。。。。。。。。。。
		switch(traffic.getNetworkProtocol()){
		case 0x01:
			flowMatch.setNetworkProtocol(0x01);break;
		case 0x06:
			flowMatch.setNetworkProtocol(0x06);break;
		case 0x11:
			flowMatch.setNetworkProtocol(0x11);break;
		default: break;
		}
		return flowMatch;
	}
	
	long result =0;
	long times =0;
	@Override
	public void processEvent(Event e) {		
		if(sfcCheckenable){

			FlowEventArgs fArgs = (FlowEventArgs)e.args;
			List<FlowInfo> fList = fArgs.flowList;

			log.info("flow rules num:"+fList.size());
	
			
//			for(int i=0;i<fList.size();i++)
//				log.info("flow info"+fList.get(i));
			flowrules = flowClassfication(fList);
			sfcInstances = 	sfcManager.getSfcInstanceList();
			//log.info("size:"+sfcInstances.size());
			for(int i=0;i<sfcInstances.size();i++){
				times =times+1;
				long start =new Date().getTime();
				sfcCheck(sfcInstances.get(i));
				long end =new Date().getTime();
				result = (end -start)+result;
				log.info("time is"+(end-start) +" ms" );
				log.info("times is "+ times);
				log.info("mean time is "+ ((double)result)/times+" ms");
			}
		}
			
	}
	
	

	@Override
	public List<SFCInstance> getSFCInstances(){
		return this.sfcInstances;
	}
	@Override
	public void SetSFCInstances(List<SFCInstance> sfcInstances){
		 this.sfcInstances = sfcInstances ;
	}
	@Override
	public void sfcCheckenable(){
		 this.sfcCheckenable = true ;
	}
	
	
	/**
	 * orderByPriority(),quickSort(),partition(): The three methods implement Quick Sort for List<FlowInfo> in a Switch by Priority.
	 * @param switchFlowMap
	 */
	public void orderByPriority(Map<String,List<FlowInfo>> switchFlowMap){
		Set<String> key = switchFlowMap.keySet();
		Iterator<String> iter = key.iterator();
		List<FlowInfo> list =null;
		String dpid =null;
		while(iter.hasNext()){
			dpid= iter.next();
			list = switchFlowMap.get(dpid);
			quickSort(list,0,list.size()-1);
			switchFlowMap.get(dpid).clear();
			switchFlowMap.get(dpid).addAll(list);
		}
	}
	public void quickSort(List<FlowInfo> list, int start, int end) {  
	    if(start >= end) return;         
	    int mid = partition(list, start, end);  	      
	    quickSort(list, start, mid-1); 
	    quickSort(list, mid+1, end); 	    
	}
	public int partition(List<FlowInfo> list,int start,int end){
		FlowInfo copy =null;
		FlowInfo midvalue = list.get(start);
		while(start<end){
			while(list.get(end).getPriority()<midvalue.getPriority() && end > start)
				end--;
			copy = list.get(end);
			list.remove(start);
			list.add(start, copy);
			while(list.get(start).getPriority()>midvalue.getPriority() && end > start)
				start++;
			copy = list.get(start);
			list.remove(end);
			list.add(end,copy);
		}
		list.remove(end);
		list.add(end,midvalue);
		return end;
	}
	//将flow rules存储到其对应的交换机中
	public HashMap<String, Set<FlowInfo>> flowClassfication(List<FlowInfo> fList){
		HashMap<String, Set<FlowInfo>> flowrules = new HashMap<String, Set<FlowInfo>>();
		FlowInfo flowRule = null;
		for(int i=0;i<fList.size();i++){
			flowRule = fList.get(i);
			if(!flowrules.containsKey(flowRule.getDpid()))
				flowrules.put(flowRule.getDpid(), new HashSet<FlowInfo>());
			flowrules.get(flowRule.getDpid()).add(flowRule);
		}
		//log Switch DPID and flow rules message.
//		Set<String> key = flowrules.keySet();
//		Iterator<String> iter = key.iterator();
//		Set<FlowInfo> set =null;
//		List<FlowInfo> list =new ArrayList<FlowInfo>();
//		while(iter.hasNext()){
//			set = flowrules.get(iter.next());
//			list.clear();
//			list.addAll(set);
//			for(int i=0;i<list.size();i++)
//				log.info("dpid={},in_put={},out_port={}",list.get(i).getDpid(),list.get(i).getinputPort(),list.get(i).getActions());
//			log.info("............................");
//		}
		return flowrules;
	}
	
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services =
                new ArrayList<Class<? extends ISecurityControllerService>>(1);
        services.add(ISFCCheckService.class);
		return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
        m.put(ISFCCheckService.class, this);
        return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		 l.add(IRestApiService.class);
		 l.add(ISFCService.class);
		 l.add(IGlobalTrafficAnalyzeService.class);
	     return l;
	}

	

	@Override
	public void addListenEventCondition(EventType type, EventSubscriptionInfo condition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAddListenEventCondition(EventType type, EventSubscriptionInfo condition) {
		// TODO Auto-generated method stub
		
	}
}
//
//@Override
//public boolean sfcCheck(SFCInstance sfcInstance) {
//	List<FlowInfo> rulesInfo = new ArrayList<FlowInfo>();
////	if(!sfcManager.getSfcInstanceList().contains(sfcInstance)){
////		log.info("SFC Instance does not exist.");
////		return false;
////	}
//		
//	List<NodePortTuplePath> sfcPath = sfcManager.getRouteById(sfcInstance.getSfc_id());
//	FlowMatch match = getFlowMatch(sfcInstance);
//	Map<String,List<FlowInfo>> switchFlowMap  = flowInfoToMap(flowrules.get(match));
//	NodePortTuplePath switchPortNode = null;
//	List<FlowInfo> flowInfos = null;
//	int out_port = 0;
//	for(int i=0;i<sfcPath.size();i++){
//		switchPortNode = sfcPath.get(i);
//		flowInfos = switchFlowMap.get(switchPortNode.getNodeId());
//		for(int j=0;j<flowInfos.size();j++){
//			for(int k=0;k<flowInfos.get(j).getActions().size();k++){
//				if(flowInfos.get(j).getActions().get(k).getType().equals(Enum.valueOf(ActionType.class,"OUTPUT")))
//					out_port = flowInfos.get(j).getActions().get(k).getPort();
//			}
//			if(flowInfos.get(j).getinputPort()!=switchPortNode.getIn_portId())
//				continue;
//			else if(out_port == switchPortNode.getOut_portId())
//					log.info("corret flow rule:{}",flowInfos.get(j).toString());
//			else log.info("incorret flow rule:{}",flowInfos.get(j).toString());
//		}
//	}
//
//	this.rulesMap.put(sfcInstance, rulesInfo); 
//	return false;
//}
