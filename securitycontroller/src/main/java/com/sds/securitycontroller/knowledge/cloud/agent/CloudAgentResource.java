/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.command.CommandPushRequest;
import com.sds.securitycontroller.command.CommandPushResultEventArgs;
import com.sds.securitycontroller.command.RedirectFlowCommand;
import com.sds.securitycontroller.command.ResolvedCommand;
import com.sds.securitycontroller.device.Device;
import com.sds.securitycontroller.device.DeviceFactory.DeviceCategory;
import com.sds.securitycontroller.device.manager.IDeviceManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.KnowledgeEventArgs;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.cloud.CloudPort;
import com.sds.securitycontroller.knowledge.cloud.LocalVlan;
import com.sds.securitycontroller.knowledge.cloud.LocalVnet;
import com.sds.securitycontroller.knowledge.cloud.VifPort;
import com.sds.securitycontroller.packet.IPv4;
import com.sds.securitycontroller.policy.PolicyActionType;
import com.sds.securitycontroller.threadpool.IThreadPoolService;

public class CloudAgentResource extends ServerResource {
	protected static Logger log = LoggerFactory.getLogger(CloudAgentResource.class);

	protected static HashMap<String,LocalVnet> lvm=new HashMap<String,LocalVnet>();
	private List<String> computeNodes=new ArrayList<String>();
	private String commandUrl = "http://nc.research.intra.sds.com:8081/wm/securitycontrolleragent/policyaction";

    IEventManagerService eventManager;
	
	

	@Override  
    public void doInit() { 
		computeNodes.add("compute1");
		computeNodes.add("node1");
		commandUrl = OpenstackClient.cloudAgentCommandUrl;
		eventManager = 
                (IEventManagerService)getContext().getAttributes().
                get(IEventManagerService.class.getCanonicalName());
        
	}
	
	@Post
	public String handlePosts(String fmJson) throws JsonProcessingException, IOException{
		log.info("_____________{} received.",fmJson);
		LocalVlan lvmItem= new LocalVlan();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode=mapper.readTree(fmJson);
		JsonNode uuidNode=rootNode.path("net_uuid");
		lvmItem.setNetUuid(uuidNode.asText());
		lvmItem.setSegmentationId(rootNode.path("segmentation_id").asText());
		lvmItem.setVlan(rootNode.path("vlan").asInt());
		lvmItem.setHost(rootNode.path("host").asText());
		lvmItem.setNetworkType(rootNode.path("network_type").asText());
		lvmItem.setPhysicalNetwork(rootNode.path("physical_network").asText());

		String action=(rootNode.path("action").asText());
		String targetVif=(rootNode.path("target_vif").asText());

		JsonNode vifNode=rootNode.path("vif_ports");
		Iterator<JsonNode> vifList = vifNode.elements();
		while (vifList.hasNext()) {
			JsonNode vifPortNode = vifList.next();
			VifPort vifPort = new VifPort();
			vifPort.setVifId(vifPortNode.path("vif_id").asText());
			vifPort.setOfport(vifPortNode.path("ofport").asInt());
			vifPort.setVifMac(vifPortNode.path("vif_mac").asText());
			vifPort.setBrName(vifPortNode.path("br_name").asText());
			vifPort.setPortName(vifPortNode.path("port_name").asText());
			lvmItem.getVifPorts().put(vifPort.getVifId(), vifPort);
			lvmItem.setType(KnowledgeType.CLOUD_VLAN);
			log.debug(lvmItem.toString());
		}
		//data parsing completed 
		String returnCode="";
		if (lvm.containsKey(lvmItem.getNetUuid())){
			if(lvm.get(lvmItem.getNetUuid()).getLocalVlanMap().containsKey(lvmItem.getHost())){
				// network has vlan map for this host
				if(action.equals("DELETE") && (lvmItem.getVifPorts()==null || lvmItem.getVifPorts().isEmpty() )){
					//remove host's vlan map
					lvm.get(lvmItem.getNetUuid()).getLocalVlanMap().remove(lvmItem.getHost());
					if(lvm.get(lvmItem.getNetUuid()).getLocalVlanMap().isEmpty()){
						lvm.remove(lvmItem.getNetUuid());
						returnCode= "{\"status\" : \"network removed!\"}";
					}else
						returnCode= "{\"status\" : \"port removed!\"}";
				}else{
					lvm.get(lvmItem.getNetUuid()).getLocalVlanMap().get(lvmItem.getHost()).setVifPorts(lvmItem.getVifPorts());
					returnCode= "{\"status\" : \"network updated!\"}";
				}
			}else{
				lvm.get(lvmItem.getNetUuid()).getLocalVlanMap().put(lvmItem.getHost(), lvmItem);
				returnCode= "{\"status\" : \"host network added!\"}";
			}
		}else{
			HashMap<String, LocalVlan> hostMap = new HashMap<String,LocalVlan>();
			hostMap.put(lvmItem.getHost(),lvmItem);
			LocalVnet vnet = new LocalVnet(lvmItem.getNetUuid(), hostMap);
			vnet.setType(KnowledgeType.CLOUD_VLAN_MAP);
			lvm.put(lvmItem.getNetUuid(), vnet);
			
			//TODO: generate a ADD_NETWORK event.
			returnCode= "{\"status\" : \"network added!\"}";
			
			/*
			 * appended: send event to knowledge manager
			 *
			 */
			Object infoObj = lvm;
			KnowledgeType entityType = KnowledgeType.CLOUD_VLAN_MAP;
			Event e = new Event(EventType.RETRIEVED_KNOWLEDGE, null,this,new KnowledgeEventArgs(entityType,infoObj));
			this.eventManager.addEvent(e);
			
		}
		//update localVlanMap completed.

		//get the target vif object
		if(action.equals("ADD")){
			final VifPort tarVif = lvmItem.getVifPorts().get(targetVif);
			if(computeNodes.contains(lvmItem.getHost()) && tarVif.getBrName().equals("br-int")){
				IThreadPoolService tps = (IThreadPoolService) getContext().getAttributes().
						get(IThreadPoolService.class.getCanonicalName());
				tps.getScheduledExecutor().schedule(new Runnable(){

					@Override
					public void run() {
						bindVmIp(tarVif);
					}
					
				}, 5, TimeUnit.SECONDS);
//				bindVmIp(tarVif);
			}
		}

		//generate en PUSH_FLOW envent to redirect flow.

		//get a security device
		IDeviceManagementService deviceManager = (IDeviceManagementService) getContext().getAttributes().
				get(IDeviceManagementService.class.getCanonicalName());
		//Iterator<Device> iter = deviceManager.getConnectedDevices().iterator();
		Iterator<Device> iter = deviceManager.getDevs().iterator();
		@SuppressWarnings("unused")
		String dst_info=null;
		while(iter.hasNext()){
			Device dc = iter.next();
			if(dc.getCategory().equals(DeviceCategory.SECURITY_DEVICE))
				dst_info = dc.getAttachedTaps().get(0) +"%%"
						+dc.getMac_addrs().get(0) +"%%"
						+dc.getAttachedTaps().get(1) +"%%"
						+dc.getMac_addrs().get(1);
		}

		ResolvedCommand cr=new ResolvedCommand();
		cr.setType(PolicyActionType.REDIRECT_FLOW);
		List <RedirectFlowCommand> vmPolicies=new ArrayList<RedirectFlowCommand>();
		Iterator<Entry<String, LocalVlan>> iter1=lvm.get(lvmItem.getNetUuid()).getLocalVlanMap().entrySet().iterator();
		while (iter1.hasNext()){
			Entry<String, LocalVlan> entry1 = iter1.next();
			if (computeNodes.contains(entry1.getKey())){
				Iterator<Entry<String, VifPort>> vifIter = entry1.getValue().getVifPorts().entrySet().iterator();
				while(vifIter.hasNext()){
					vifIter.next();
//					Entry<String, VifPort> vifEntry = vifIter.next();
//					VifPort vif = vifEntry.getValue();
//					vmPolicies.add(new FlowCommand(vif.getVifId(),vif.getVifMac(),"SOURCE",dst_info));
				}
			}
		}
		cr.setCommandlist(vmPolicies);
		if(!vmPolicies.isEmpty()){
			CommandPushRequest commandPushResult = new CommandPushRequest(commandUrl , cr);			
			CommandPushResultEventArgs args1 = new CommandPushResultEventArgs(commandPushResult);
			this.eventManager.addEvent(
					new Event(EventType.PUSH_FLOW, null, this, args1));
		}
		return returnCode;
	}

	public void bindVmIp(VifPort vif){
		ICloudAgentService caService =
				(ICloudAgentService)getContext().getAttributes().
				get(ICloudAgentService.class.getCanonicalName());
		Map<String, CloudPort> macIPMapping = caService.getMacIPMapping();
		CloudPort cloudPort = macIPMapping.get(vif.getVifId());
		RedirectFlowCommand co = new RedirectFlowCommand(); 
//		co.setSrc_info(cloudPort.getMac());
//		co.setSrc_tag(vif.getPortName());
		co.setFlowName(cloudPort.getId()+"_binding");
		co.getArguments().setNetworkSource(IPv4.toIPv4Address( cloudPort.getIpaddr()) );
		List<RedirectFlowCommand> cl = new ArrayList<RedirectFlowCommand>();
		cl.add(co);
		ResolvedCommand cr = new ResolvedCommand();
		cr.setType(PolicyActionType.BIND_MAC_IP);
		cr.setCommandlist(cl);
		CommandPushRequest commandPushResult = new CommandPushRequest(commandUrl , cr);			
		CommandPushResultEventArgs args1 = new CommandPushResultEventArgs(commandPushResult);

		this.eventManager.addEvent(
				new Event(EventType.PUSH_FLOW, null, this, args1));
	}

}
