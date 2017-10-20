package com.sds.securitycontroller.www1234.sfc.manager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;
import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuplePath;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.utils.http.HttpUtilsMethod;
import com.sds.securitycontroller.www1234.servicefunction.ServiceFunctionDevice;
import com.sds.securitycontroller.www1234.servicefunction.manager.IServiceFunctionService;
import com.sds.securitycontroller.www1234.sfc.SFCInstance;
import com.sds.securitycontroller.www1234.sfc.SFCSwitchPortInstance;
import com.sds.securitycontroller.www1234.sfc.TrafficPattern;
import com.sds.securitycontroller.www1234.sfc.ServiceFunction;

public class SFCManager implements ISFCService, ISecurityControllerModule {

	private String ncHost = null;
	private String url = "/wm/staticflowentrypusher/json";
	private String urlRoute = "/wm/topology/route/<src-dpid>/<src-port>/<dst-dpid>/<dst-port>/json";
	protected IRestApiService restApi;
	protected static Logger log = LoggerFactory.getLogger(SFCManager.class);
	protected IServiceFunctionService serviceFunctionManager;

	protected Map<String, List<String>> sfcflowMap;    //Store SFC id and flow rules name
	protected Map<String, List<NodePortTuplePath>> sfcpathMap;
	protected List<SFCInstance> sfcInstanceList ;
	
	protected List<SFCSwitchPortInstance> sfcSwitchPortInstanceList ;
	
	/* implement the methods of ISFCService. */
	@Override
	public List<SFCInstance> getSfcInstanceList(){
		return this.sfcInstanceList;
	}
	@Override
	public SFCSwitchPortInstance getSFCSwitchPortInstancebyId(String sfc_id) {
		for(int i=0;i<sfcSwitchPortInstanceList.size();i++){
			if(sfcSwitchPortInstanceList.get(i).getSfc_id().equals(sfc_id))
				return sfcSwitchPortInstanceList.get(i);
		}
		return null;
	}
	@Override
	public SFCInstance getSfcInstancebyId(String sfc_id){
		for(int i=0;i<sfcInstanceList.size();i++){
			if(sfcInstanceList.get(i).getSfc_id().equals(sfc_id))
				return sfcInstanceList.get(i);
		}
		return null;
	}
/**
 * 映射方法 1 : 根据NF所在的交换机和端口映射
 */
	
	@Override
	public void mapSFCbySwPorts(SFCSwitchPortInstance sfcSwitchPortInstance) {
		String sfc_id = sfcSwitchPortInstance.getSfc_id();
		if(sfcflowMap.containsKey(sfc_id)){
			log.info("service function chain id has been exisited.");
			return ;
		}
		String url = this.ncHost +this.url;
		List<NodePortTuplePath> path = getRoutebySwPort(sfcSwitchPortInstance);
		log.info("\n the sfc path has been selected. :"+path.toString());
		List<String> list= creatFlowRulesbySwPort(sfcSwitchPortInstance,path);
		sfcflowMap.put(sfcSwitchPortInstance.getSfc_id(), new ArrayList<String>());
		for(int i=0; i <list.size();i++){
			HttpUtilsMethod.httpPost(url, null, list.get(i));
			sfcflowMap.get(sfc_id).add(sfc_id+"_"+i);
		}
		sfcpathMap.put(sfc_id, path);

		sfcSwitchPortInstanceList.add(sfcSwitchPortInstance);
		
	}
	public List<String> creatFlowRulesbySwPort(SFCSwitchPortInstance sfcSwitchPortInstance,List<NodePortTuplePath> path){
		List<String> allFlowRules = new ArrayList<String>();
		Map<String,String> traffic  = sfcSwitchPortInstance.getTrafficPattern();
		traffic.put("switch", "dpid");
		traffic.put("name", "flowName");
		traffic.put("cookie", "0");
		traffic.put("priority", ""+sfcSwitchPortInstance.getPriority());
		traffic.put("ingress-port", "in_port");
		traffic.put("ether-type", "2048");
		traffic.put("active", "true");
		traffic.put("actions", "output=out_port");
		NodePortTuplePath SwitchPort = null;
		
		for(int i=0;i<path.size();i++){
			SwitchPort = path.get(i);
			String flow = JSONObject.toJSONString(traffic);
			flow = flow.replace("dpid", SwitchPort.getNodeId()).replace("in_port",""+SwitchPort.getIn_portId()).
					replace("out_port", ""+SwitchPort.getOut_portId()).replace("flowName", sfcSwitchPortInstance.getSfc_id()+"_"+i);
			allFlowRules.add(flow);
		}
		log.info("\n SFC flow rules has been created. : ");
		for(int i=0;i<allFlowRules.size();i++)
			System.out.println("flow rule "+(i+1)+": "+allFlowRules.get(i));
		log.info("\n the SFC flow rules has been pushed to SDN controller");
		return allFlowRules;
	}
	
	@Override
	public List<NodePortTuplePath> getRoutebySwPort(SFCSwitchPortInstance sfcSwitchPortInstance) {
		if(sfcSwitchPortInstance == null)
			return null;
		List<NodePortTuplePath> listPath = new ArrayList<NodePortTuplePath>();
		List<NodePortTuple> sfc  = sfcSwitchPortInstance.getPath();		
		if(sfc!=null){
			for(int i=0;i<sfc.size()-1;i=i+2){
				listPath.addAll(getRouteFromNCHost(sfc.get(i),sfc.get(i+1)));
			}
		}
		return listPath;
	}
	
	/**
	 * 映射方法 2 : 根据NF的名称进行映射
	 */	
	
	@Override
	public void mapSFC(SFCInstance sfcInstance) {
		String sfc_id = sfcInstance.getSfc_id();
		if(sfcflowMap.containsKey(sfc_id)){
			log.info("service function chain id has been exisited.");
			return ;
		}
		String url = this.ncHost +this.url;
		List<NodePortTuplePath> path = getRoute(sfcInstance);
		log.info("\n the key information of the SFC policy:"+"SFC policy ID:"+sfcInstance.getSfc_id()+"\n NFs of SFC policy:"+sfcInstance.getSfc()+
				"\n the flow pattern is :"+sfcInstance.getTrafficPattern().toString()+"\n SFC path:{}",path.toString());
		
		List<String> list= creatFlowRules(sfcInstance,path);
		sfcflowMap.put(sfcInstance.getSfc_id(), new ArrayList<String>());
		for(int i=0; i <list.size();i++){
			HttpUtilsMethod.httpPost(url, null, list.get(i));
			sfcflowMap.get(sfc_id).add(sfc_id+"_"+i);
		}
		sfcpathMap.put(sfc_id, path);
		sfcInstanceList.add(sfcInstance);
	}
	public List<String> creatFlowRules(SFCInstance sfcInstance,List<NodePortTuplePath> path){
		List<String> allFlowRules = new ArrayList<String>();
		String flowRules = null;
//		flowRules = "{\"switch\":\"dpid\","
//				+ "\"name\":\"flowname\","
//				+ "\"cookie\":\"0\","
//				+ "\"priority\":\"flowpriority\","
//				+ "\"ingress-port\":\"in_port\","
//				+ "\"src-mac\":\"src_mac\","
//				+ "\"dst-mac\":\"dst_mac\","
//				+ "\"protocol\":\"flowprotocol\","
//				+"\"src-ip\":\"src_ip\","
//				+"\"dst-ip\":\"dst_ip\","
//				+"\"src-port\":\"src_port\","
//				+"\"vlan-id\":\"0\","
//				+"\"vlan-priority\":\"0\","      //the value of vlan-priority is :0-7
//				+"\"dst-port\":\"dst_port\","
//				+"\"ether-type\":\"2048\","
//				+ "\"active\":\"true\","
//				+ "\"actions\":\""+"output=out_port\"}";
		flowRules = "{\"switch\":\"dpid\","
				+ "\"name\":\"flowname\","
				+ "\"cookie\":\"0\","
				+ "\"priority\":\"flowpriority\","
				+ "\"ingress-port\":\"in_port\","
				+ "\"src-mac\":\"src_mac\","
				+ "\"dst-mac\":\"dst_mac\","
				+ "\"protocol\":\"flowprotocol\","
				+"\"src-ip\":\"src_ip\","
				+"\"dst-ip\":\"dst_ip\","
				+"\"src-port\":\"src_port\","
				+"\"dst-port\":\"dst_port\","
				+"\"ether-type\":\"2048\","
				+ "\"active\":\"true\","
				+ "\"actions\":\""+"output=out_port\"}";
		TrafficPattern traffic = sfcInstance.getTrafficPattern();
		//需要考虑src_mac,dst_mac为空的情形
		flowRules = flowRules.replace("src_mac",traffic.getDataLayerSource()).replace("dst_mac",traffic.getDataLayerDestination()).
				replace("src_ip", traffic.getNetworkSource()).replace("dst_ip",traffic.getNetworkDestination()).replace("src_port",""+traffic.getTransportSource())
				.replace("dst_port", ""+traffic.getTransportDestination()).replace("flowprotocol", ""+traffic.getNetworkProtocol()).replace("flowpriority", ""+sfcInstance.getPriority());
		NodePortTuplePath SwitchPort = null;
		for(int i=0;i<path.size();i++){
			SwitchPort = path.get(i);
			String flow = flowRules;
			flow = flow.replace("dpid", SwitchPort.getNodeId()).replace("in_port",""+SwitchPort.getIn_portId()).
					replace("out_port", ""+SwitchPort.getOut_portId()).replace("flowname", sfcInstance.getSfc_id()+"_"+i);
			allFlowRules.add(flow);
		}
		log.info("\n \n SFC flow rules:{}",allFlowRules.toString());
		return allFlowRules;
	}
	@Override
	public boolean deleteSFCById(String sfc_id) {
		String url = this.ncHost +this.url;
		if(!sfcflowMap.containsKey(sfc_id)){
			log.info("sfc id {} is not exisiting"+sfc_id);
			return false;
		}
		List<String> flowRulesName = sfcflowMap.get(sfc_id);		
		for(int i=0; i< flowRulesName.size();i++){
			String flowName="{\"name\":\""+flowRulesName.get(i)+"\"}";
			HttpUtilsMethod.httpDel(url, null,flowName);
		}
		sfcflowMap.remove(sfc_id);
		sfcpathMap.remove(sfc_id);
		return true;
	}

	@Override
	public List<NodePortTuplePath> getRoute(SFCInstance sfcInstance) {
		if(sfcInstance == null)
			return null;
		List<NodePortTuplePath> listPath = new ArrayList<NodePortTuplePath>();
		List<ServiceFunction> sfc  = sfcInstance.getSfc();
		
		NodePortTuple in_switch = sfcInstance.getStart();
		NodePortTuple out_switch =null;
		ServiceFunctionDevice sfcDevice =null;
		if(sfc!=null){
			for(int i=0;i<sfc.size();i++){
				sfcDevice = serviceFunctionManager.getSFDevice(sfc.get(i));
				out_switch = sfcDevice.getIn_switchPort();
				listPath.addAll(getRouteFromNCHost(in_switch,out_switch));
				in_switch = sfcDevice.getOut_switchPort();
			}
		}
		out_switch = sfcInstance.getEnd();
		listPath.addAll(getRouteFromNCHost(in_switch,out_switch));
		return listPath;
	}
	
	public List<NodePortTuplePath> getRouteFromNCHost(NodePortTuple switchPort1,NodePortTuple switchPort2){
		String url = ncHost+urlRoute;
		if (switchPort1 == null || switchPort2 == null) {
            log.info("cannot find startpoint = {} or endpoint = {}, abort!",switchPort1, switchPort2);
            return null;
        }
		url = url.replaceAll("<src-dpid>", switchPort1.getNodeId());
		url = url.replaceAll("<src-port>", ""+switchPort1.getPortId());
		url = url.replaceAll("<dst-dpid>", switchPort2.getNodeId());
		url = url.replaceAll("<dst-port>", ""+switchPort2.getPortId());
		ObjectMapper mapper = new ObjectMapper();
		List<NodePortTuple> path = new ArrayList<NodePortTuple>();
		URL datasource;
		try {
			datasource = new URL(url);
			JsonNode rootNode = mapper.readTree(datasource);
			Iterator<JsonNode> ite = rootNode.iterator();
			JsonNode node=null;
			while(ite.hasNext()){
				node = ite.next();
				path.add(new NodePortTuple(node.path("switch").asText(),Short.parseShort(node.path("port").asText())));	
			}			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 List<NodePortTuplePath> routePath = new ArrayList<NodePortTuplePath>();
	        for (int i = 0; i < path.size() - 1; i += 2) {
	        	routePath.add(new NodePortTuplePath(path.get(i).getNodeId(),path.get(i).getPortId(),path.get(i+1).getPortId()));
	        }
	    return routePath;
	}
	@Override
	public List<NodePortTuplePath> getRouteById(String sfc_id) {
		if(sfcpathMap.containsKey(sfc_id))
			return sfcpathMap.get(sfc_id);
		return null;
	}
	/* implement the methods of ISecurityControllerModule. */
	@Override
	public void init(SecurityControllerModuleContext context) throws SecurityControllerModuleException {
		this.restApi = context.getServiceImpl(IRestApiService.class);
		this.serviceFunctionManager = context.getServiceImpl(IServiceFunctionService.class);
		GlobalConfig config = GlobalConfig.getInstance();
		if(config.ncHost!=null)
			ncHost = config.ncHost;
		
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		SFCRoutable routable = new SFCRoutable();
		restApi.addRestletRoutable(routable);
		sfcflowMap = new HashMap<String, List<String>>();
		sfcpathMap = new HashMap<String, List<NodePortTuplePath>>();
		sfcInstanceList = new ArrayList<SFCInstance>();
		sfcSwitchPortInstanceList =new ArrayList<SFCSwitchPortInstance>();
	}
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services =
                new ArrayList<Class<? extends ISecurityControllerService>>(1);
        services.add(ISFCService.class);
		return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
        m.put(ISFCService.class, this);
        return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		 Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		 l.add(IRestApiService.class);
		 l.add(IServiceFunctionService.class);
	     return l;
	}

}
