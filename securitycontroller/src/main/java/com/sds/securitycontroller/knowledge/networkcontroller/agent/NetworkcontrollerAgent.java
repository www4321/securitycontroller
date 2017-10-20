/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller.agent;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.KnowledgeEventArgs;
import com.sds.securitycontroller.event.RequestEventArgs;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.networkcontroller.AttachmentPoint;
import com.sds.securitycontroller.knowledge.networkcontroller.FlowTable;
import com.sds.securitycontroller.knowledge.networkcontroller.NetworkController;
import com.sds.securitycontroller.knowledge.networkcontroller.NetworkDeviceEntity;
import com.sds.securitycontroller.knowledge.networkcontroller.PortStatus;
import com.sds.securitycontroller.knowledge.networkcontroller.Switch;
import com.sds.securitycontroller.knowledge.networkcontroller.SwitchPort;
import com.sds.securitycontroller.knowledge.networkcontroller.Topology;
import com.sds.securitycontroller.knowledge.networkcontroller.TopologyLink;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;

public class NetworkcontrollerAgent implements INetworkcontrollerAgentService,ISecurityControllerModule
	,IEventListener{

    protected IEventManagerService eventManager;
	protected static Logger log = LoggerFactory.getLogger(NetworkcontrollerAgent.class);
	
	Topology topology;
	NetworkController networkController;
	Map<String,Switch> switchMap;
	Map<String,NetworkDeviceEntity> deviceMap;
	Map<String,FlowTable> flowTableMap;
	Map<String,PortStatus> portStatusMap;
	
	String ncHost = "http://nc.research.intra.sds.com:8081";
	String ncNCSwitchesAPIUrl = "/wm/core/controller/switches/json"; 
	String ncSwitchStatAPIUrl = "/wm/core/switch/<switchId>/<statType>/json"; //statType:
	String ncTopologyAPIUrl = "/wm/topology/<target>/json";	//target:links/switchclusters
	String ncControllerAPIUrl = "/wm/core/<target>/json";	//target:memory/role
	String ncDeviceAPIUrl = "/wm/device/";
	 
	SimpleDateFormat sdf  =  new  SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS " ); 
	
	@Override
	public Topology getTopologyStatus() {
		// get topology links and switchclusters
		// get links
		String url = ncTopologyAPIUrl.replace("<target>", "links");
		JsonNode linksNode = httpGetJson(url);
		if (linksNode==null)
			return null;
		List<TopologyLink> topologyLinks = new ArrayList<TopologyLink>();
		try{
			for (int i=0;i<linksNode.size();i++){
				JsonNode linkNode = linksNode.get(i);
				String src_switch = linkNode.path("src-switch").asText();
				int src_port = linkNode.path("src-port").asInt();
				String dst_switch = linkNode.path("dst-switch").asText();
				int dst_port = linkNode.path("dst-port").asInt();
				String type = linkNode.path("type").asText();
				String direction = linkNode.path("direction").asText();
				TopologyLink link = new TopologyLink(src_switch, src_port, dst_switch, dst_port, type, direction);
				topologyLinks.add(link);
			}
		}
		catch (Exception e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		}  
		// get switchclusters
		url = ncTopologyAPIUrl.replace("<target>", "switchclusters");
		JsonNode swclRoot = httpGetJson(url);
		if (swclRoot == null)
			return null;
		Map<String,List<String>> switchClusters = new HashMap<String,List<String>>();
		try{
			Iterator<String> iter = swclRoot.fieldNames();
			while(iter.hasNext()){
				String key = iter.next();
				JsonNode clusterNode = swclRoot.path(key);
				List<String> swIDs = new ArrayList<String>();
				for (int j=0;j<clusterNode.size();j++){
					String swid = clusterNode.get(j).asText();
					swIDs.add(swid);
				}
				switchClusters.put(key, swIDs);
			}
			
//			for (int i=0;i<swclRoot.size();i++){
//				JsonNode clusterNode = swclRoot.get(0);//.get(i);
//				List<String> swIDs = new ArrayList<String>();
//				
//				for (int j=0;j<clusterNode.size();j++){
//					String swid = clusterNode.get(i).asText();
//					swIDs.add(swid);
//				}
//				SwitchCluster swcl = new SwitchCluster(swIDs);
//				switchClusters.add(swcl);
//			}
		}
		catch (Exception e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		}
		//update topology
		this.topology = new Topology(topologyLinks, switchClusters); 
		topology.setType(KnowledgeType.NETWORK_TOPOLOGY);
		return this.topology;
	}

	@Override
	public NetworkController getNetworkControllerStatus() {
		// get NC role info and memory stat
//		String ds = "2013-12-05T04:12:14.517Z";
//		try {
//			Date date1 = sdf.parse(ds);
//		} catch (ParseException e1) {
//			e1.printStackTrace();
//		} 
		//get NC role
		String url = this.ncControllerAPIUrl.replace("<target>", "role");
		JsonNode roleNode = httpGetJson(url);
		if (roleNode == null)
			return null;
		try {
			String role = roleNode.path("role").asText();
			String role_change_description = roleNode.path("change-description").asText();
			Date role_change_time = sdf.parse( (roleNode.path("change-date-time").asText().replace('T', ' ').replace('Z', ' ')) );
			//get NC memory stats
			url = this.ncControllerAPIUrl.replace("<target>", "memory");
			JsonNode memoryNode = httpGetJson(url);
			if (memoryNode == null)
				return null;
			long totalMemory = memoryNode.path("total").asLong();
			long freeMemory = memoryNode.path("freeMemory").asLong();
			this.networkController = new NetworkController(role, role_change_description, role_change_time, totalMemory, freeMemory);
			networkController.setType(KnowledgeType.NETWORK_CONTROLLER);
			return this.networkController;
		}
		catch (Exception e) {
			log.error("error parsing json response: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Map<String, Switch> getSwitches() {
		// get switches info and status
		// get switches info and each switch's status
		String url = this.ncNCSwitchesAPIUrl;
		JsonNode swsNode = httpGetJson(url);
		if (swsNode==null)
			return null;
		this.switchMap.clear();
		try {
			for(int i=0;i<swsNode.size();i++){
				JsonNode swNode = swsNode.get(i);
				String dpid = swNode.path("dpid").asText();
				String harole = swNode.path("harole").asText();
				long actions = swNode.path("actions").asLong();
				JsonNode attrNode = swNode.path("attributes");
				boolean supportsOfppFlood = attrNode.path("supportsOfppFlood").asBoolean();
				boolean supportsNxRole = attrNode.path("supportsNxRole").asBoolean();
				boolean supportsOfppTable = attrNode.path("supportsOfppTable").asBoolean();
				long FastWildcards = attrNode.path("FastWildcards").asLong();
				//get port info
				JsonNode portsNode = swNode.path("ports");
				List<SwitchPort> ports = new ArrayList<SwitchPort>();
				
				Map<Integer, PortStatus> portStatusMap =  getPortStatuses(dpid);
				
				for(int j=0;j<portsNode.size();j++){
					JsonNode portNode = portsNode.get(j);
					int portNumber = portNode.path("portNumber").asInt();
					String hardwareAddress = portNode.path("hardwareAddress").asText();
					String name = portNode.path("name").asText();
					int config = portNode.path("config").asInt();
					int state = portNode.path("state").asInt();
					int currentFeatures = portNode.path("currentFeatures").asInt();
					int advertisedFeatures = portNode.path("advertisedFeatures").asInt();
					int supportedFeatures = portNode.path("supportedFeatures").asInt();
					int peerFeatures = portNode.path("peerFeatures").asInt();
					PortStatus portStatus = portStatusMap.get( new Integer(portNumber) );
					
					SwitchPort portInfo = new SwitchPort(portNumber, hardwareAddress, name, config, state, currentFeatures, advertisedFeatures, supportedFeatures, peerFeatures,portStatus);
					ports.add(portInfo);
				}
				//get switch description
				int buffers = swNode.path("buffers").asInt();
				int capabilities = swNode.path("capabilities").asInt();
				String inetAddress = swNode.path("inetAddress").asText();
				long connectedSince = swNode.path("connectedSince").asLong();
				JsonNode descriptionNode = swNode.path("description");
				String software = descriptionNode.path("software").asText(); 
				String hardware = descriptionNode.path("hardware").asText(); 
				String manufacturer = descriptionNode.path("manufacturer").asText(); 
				String serialNum = descriptionNode.path("serialNum").asText(); 
				String datapath = descriptionNode.path("datapath").asText(); 
				Map<String, FlowTable> flowTableMap = this.getFlowTableMap(dpid);
				Switch switch1 = new Switch(dpid, harole, actions, supportsOfppFlood, supportsNxRole, FastWildcards, supportsOfppTable, ports, buffers, software, hardware, manufacturer, serialNum, datapath, capabilities, inetAddress, connectedSince, flowTableMap);
				switch1.setType(KnowledgeType.NETWORK_SWITCH);
				this.switchMap.put(dpid, switch1); 
			}
			return switchMap;
		} 
		catch (Exception e) {
			log.error("error parsing json response: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unused")
	@Override
	public Map<String,NetworkDeviceEntity> getDevices() {
		// get devices info
		JsonNode root = httpGetJson(ncDeviceAPIUrl);
		if (root==null)
			return null;
		deviceMap.clear();
		try{
			for(int i=0;i<root.size();i++){
				JsonNode devNode = root.get(i);
				String entityClass = devNode.path("entityClass").asText();
				JsonNode macAddsNode = devNode.path("mac");
//				List<String> macAddresses = new ArrayList<String>();
				String macAddress="";
				for(int j=0;j<macAddsNode.size();j++){
					macAddress = macAddsNode.get(j).asText();
					break;
				}
				String ipv4Address="";
				JsonNode ipv4AddsNode = devNode.path("ipv4");
				for(int j=0;j<ipv4AddsNode.size();j++){
					ipv4Address = ipv4AddsNode.get(j).asText();
					break;
				}
				List<String> vlans = new ArrayList<String>();
				JsonNode vlanNode = devNode.path("vlan");
//				for(int j=0;j<vlanNode.size();j++){
//					vlans.add(ipv4AddsNode.get(j));
//				}
				JsonNode attachmentPointsNode = devNode.path("attachmentPoint");
				AttachmentPoint attachmentPoint = null;
				for(int j=0;j<attachmentPointsNode.size();j++){
					JsonNode apNode = attachmentPointsNode.get(j);
					String switchDPID = apNode.path("switchDPID").asText();
					int port = apNode.path("port").asInt();
					String errorStatus = apNode.path("errorStatus").asText();
					attachmentPoint = new AttachmentPoint(switchDPID, port, errorStatus);
				}
				long lastSeen = devNode.path("lastSeen").asLong();
				String dhcpClientName = null;
				JsonNode dhcpNameNode = devNode.path("dhcpClientName");
				if(dhcpNameNode!=null)
					dhcpClientName = dhcpNameNode.asText();
				NetworkDeviceEntity device = new NetworkDeviceEntity(entityClass, macAddress, ipv4Address, vlans, attachmentPoint, lastSeen, dhcpClientName);
				device.setType(KnowledgeType.NETWORK_DEVICE);
				this.deviceMap.put(device.getMacAddress(),device);
			}
			return deviceMap;
		}
		catch (Exception e) {
			log.error("error parsing json response: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	Map<String, FlowTable> getFlowTableMap(String dpid){
		// get  flow tables info
		String url = this.ncSwitchStatAPIUrl.replace("<statType>", "table").replace("<switchId>", dpid);
		JsonNode root = httpGetJson(url);
		if(root==null)
			return null;
		Map<String, FlowTable> flowTableMap = new HashMap<String, FlowTable>();
		
		Iterator<Entry<String, JsonNode>> iter = root.fields();
		try {
			while(iter.hasNext()){
				JsonNode tablesJsonNode = iter.next().getValue();
				for(int i=0;i<tablesJsonNode.size();i++){
					JsonNode tableNode = tablesJsonNode.get(i);
					int tableId = tableNode.path("tableId").asInt();
					String name = tableNode.path("name").asText();
					long wildcards = tableNode.path("wildcards").asLong();
					long maximumEntries = tableNode.path("maximumEntries").asLong();
					int activeCount = tableNode.path("activeCount").asInt();
					long lookupCount = tableNode.path("lookupCount").asLong();
					long matchedCount = tableNode.path("matchedCount").asLong();
					int length = tableNode.path("length").asInt();
					FlowTable flowTable = new FlowTable(tableId, name, wildcards, maximumEntries, activeCount, lookupCount, matchedCount, length);
					flowTable.setType(KnowledgeType.NETWORK_FLOW_TABLE);
					flowTableMap.put(new Integer(tableId).toString(), flowTable);
				}
			}
			return flowTableMap;
		}
		catch (Exception e) {
			log.error("error parsing json response: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}

	Map<Integer, PortStatus> getPortStatuses(String dpid) {
		// return all ports status of one switch
		String url = this.ncSwitchStatAPIUrl.replace("<statType>", "port").replace("<switchId>", dpid);
		JsonNode root = httpGetJson(url);
		if(root==null)
			return null;
		Map<Integer, PortStatus> portStatusMap = new HashMap<Integer, PortStatus>();
		
		Iterator<Entry<String, JsonNode>> iter = root.fields();
		try {
			while(iter.hasNext()){
				JsonNode portsNode = iter.next().getValue();
				for(int i=0;i<portsNode.size();i++){
					JsonNode statusNode = portsNode.get(i);
					int portNumber = statusNode.path("portNumber").asInt();
					long receivePackets = statusNode.path("receivePackets").asLong();
					long transmitPackets = statusNode.path("transmitPackets").asLong();
					long receiveBytes = statusNode.path("receiveBytes").asLong();
					long transmitBytes = statusNode.path("transmitBytes").asLong();
					long receiveDropped = statusNode.path("receiveDropped").asLong();
					long transmitDropped = statusNode.path("transmitDropped").asLong();
					long receiveErrors = statusNode.path("receiveErrors").asLong();
					long transmitErrors = statusNode.path("transmitErrors").asLong();
					long receiveFrameErrors = statusNode.path("receiveFrameErrors").asLong();
					long receiveOverrunErrors = statusNode.path("receiveOverrunErrors").asLong();
					long receiveCRCErrors = statusNode.path("receiveCRCErrors").asLong();
					long collisions = statusNode.path("collisions").asLong();
					PortStatus portStatus = new PortStatus(receivePackets, transmitPackets, receiveBytes, transmitBytes, receiveDropped, transmitDropped, receiveErrors, transmitErrors, receiveFrameErrors, receiveOverrunErrors, receiveCRCErrors, collisions);
					portStatusMap.put(new Integer(portNumber), portStatus);
				}
			}
			return portStatusMap;
		}
		catch (Exception e) {
			log.error("error parsing json response: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		return null;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		return null;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		return null;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		// get 
        GlobalConfig config = GlobalConfig.getInstance();
		Map<String, String> configOptions = context.getConfigParams(this);
        
        String _ncHost = config.ncHost;        
        this.ncHost = configOptions.get("nchost").replace("[nchost]", _ncHost);
        
        switchMap = new HashMap<String,Switch>();
    	deviceMap = new HashMap<String,NetworkDeviceEntity>();
    	flowTableMap = new HashMap<String,FlowTable>();
    	portStatusMap = new HashMap<String,PortStatus>();
    	this.eventManager = context.getServiceImpl(IEventManagerService.class, this);

	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {	
		//add event listener
        eventManager.addEventListener(EventType.REQUEST_KNOWLEDGE, this);
	}
	

	/**
	 * request from url and get json
	 * @param url
	 * @return jsonnode
	 */
	JsonNode httpGetJson(String url){
		try{
			url = ncHost + url;
			ObjectMapper mapper = new ObjectMapper();
			URL datasource = new URL(url);
			JsonNode root = mapper.readTree(datasource);
			return root;
		}
		catch (JsonProcessingException e) {
			log.error(" Json process error when requesting from url:"+ url +", message: "+e.getMessage());
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			log.debug(" IO error when requesting from url:"+ url +", message: "+e.getMessage());
			return null;
		}	

	}

	@Override
	public void processEvent(Event e) {
		if(e.type!=EventType.REQUEST_KNOWLEDGE){
			log.debug(" cloud agent does not know how to deal with event of {} type",e.type);
			return;
		}
//		String infoType = ((RequestEventArgs)e.args).request;//  e.args.toString();
		KnowledgeType infoType = ((RequestEventArgs)e.args).entityType;
		InfoRetrievingThread thread = new InfoRetrievingThread(infoType);
		new Thread(thread).run();
	}
	
	/**
	 * retrieve info async
	 * @author Administrator
	 *
	 */
	class InfoRetrievingThread implements Runnable{
		public KnowledgeType entityType;
		public InfoRetrievingThread(KnowledgeType entityType){
			this.entityType = entityType;
		}
		@Override
		public void run() {
			Object infoObj = null;
			log.debug(" start retrieving network controller info, type:{}",entityType);
			if (entityType==KnowledgeType.NETWORK_DEVICE)
				infoObj = getDevices();
			else if (entityType==KnowledgeType.NETWORK_CONTROLLER){
				Map<String,KnowledgeEntity> devMap = new HashMap<String,KnowledgeEntity>();
				getNetworkControllerStatus();
				try{
					if(networkController!=null)
						devMap.put(networkController.getRole(), networkController);
					else
						log.error("Unable to connect network controller.");
				}
				catch(java.lang.NullPointerException e ){
					log.error("Unable to connect network controller");//.getLocalizedMessage().getCause());					
				}
				infoObj = devMap;
			}
				
			else if (entityType==KnowledgeType.NETWORK_SWITCH)
				infoObj = getSwitches();
			else if (entityType==KnowledgeType.NETWORK_TOPOLOGY){
				Map<String,KnowledgeEntity> tpMap = new HashMap<String,KnowledgeEntity>();
				getTopologyStatus();
				tpMap.put("nc-topology",topology);
				infoObj = tpMap;
			}
//				infoObj = (Object) getTopologyStatus();
			else{
				return;
			}
			log.debug(" get cloud knowledge of {} type",entityType);
			Event e = new Event(EventType.RETRIEVED_KNOWLEDGE, null,this,new KnowledgeEventArgs(entityType,infoObj));
			eventManager.addEvent(e);
		}
	}
	

	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		
	}

	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		
	}
	
	public static void main(String [] args){
		
		NetworkcontrollerAgent nca = new NetworkcontrollerAgent();
		try {
//			nca.devices = new ArrayList<Device>();
			nca.topology = null;
			nca.getTopologyStatus();
//			List<Device> devs = nca.getDevices();
		} catch (Exception e) {
			// handle exception
			e.printStackTrace();
		}
		return;
		
	}
}
