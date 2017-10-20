/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.command;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.policy.PolicyActionType;
import com.sds.securitycontroller.restserver.RestResponse;
import com.sds.securitycontroller.utils.HTTPUtils;
import com.sds.securitycontroller.utils.http.HTTPHelper;
import com.sds.securitycontroller.utils.http.HTTPHelperResult;

public class CommandPusher implements ICommandPushService,
		ISecurityControllerModule, IEventListener {
	protected static Logger log = LoggerFactory.getLogger(CommandPusher.class);

	private Map<String,CommandRecord> commandRecordMap = new HashMap<String,CommandRecord>();  	
	public Map<EventType, EventSubscriptionInfo> subscriptionInfos = new HashMap<EventType, EventSubscriptionInfo>();

	protected IEventManagerService eventManager = null;
    protected IRegistryManagementService serviceRegistry;
	
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(ICommandPushService.class);
		return l;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(ICommandPushService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		return l;
	}

	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		this.subscriptionInfos.put(type, condition);
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		//register PUSH_FLOW event
		this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
		this.eventManager.addEventListener(EventType.PUSH_FLOW, this);
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
		log.info("BUPT security controller command pusher initialized...");

	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
        serviceRegistry.registerService("", this);
		log.info("BUPT security controller command pusher started");
	}

	@Override
	public void processEvent(Event e) {
		EventType type = e.type;
		log.info(" [[Command Pusher]>>>>>>>> Start to process a PUSH_FLOW event.");
		//
//		if (!this.subscriptionInfos.containsKey(e.type)) {
//			log.error("{} does not know how to deal with {} events", this, type);
//		}
		if (type == EventType.PUSH_FLOW) {// get push flow command
			CommandPushResultEventArgs args = (CommandPushResultEventArgs) e.args;

			try{
				RestResponse resp = pushDataToNetworkController(args.commandPushResult);
				if (resp == null)
					return;
			}
			catch(Exception ex){
				log.error("Error when push data to network controller: "+ex.toString());
				return;
			}
			
		}
	}

	@Override
	public RestResponse pushDataToNetworkController(CommandPushRequest req) throws Exception{
		if(req.data.getType() == PolicyActionType.DELETE)
			return pushDataToNetworkController(req, "DELETE");
		else
			return pushDataToNetworkController(req, "POST");
	}		
	
	protected RestResponse pushDataToNetworkController(CommandPushRequest req, String method) throws Exception{
		String url = req.commandUrl;
		log.info("Pushing command data to net contorller at:{}",url);
		ResolvedCommand command = req.getData();

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		String jsonResp = null;
		if(method.equals("POST")){
			ObjectMapper mapper = new ObjectMapper();
			StringWriter writer = new StringWriter();
			String jsonReq = "";
			//需要仔细分析使用......................................................
			JsonGenerator gen;
			try {
				gen = new JsonFactory().createGenerator(writer);
				mapper.writeValue(gen, command);
				jsonReq = writer.toString();
				gen.close();
				writer.close();
			} catch (IOException e) {
				log.error("Error when convert REST request: {}", e.getMessage());
				return new RestResponse("error",
						"error when convert REST request: " + e.getMessage());
			}
			jsonResp = HTTPUtils.httpPost(url, jsonReq, headers);
		}
		else if (method.equals("DELETE"))
			jsonResp = HTTPUtils.httpDelete(url, headers);
		else
			return new RestResponse("error", "unsupported command http request type: "+ method);
		
		if (jsonResp == null) // Error
			return new RestResponse("error", "return null response");
		try {
			log.info("NC returns response:\n{}",jsonResp);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			String status = root.path("status").asText();
			String result = root.path("result").asText();
			RestResponse response = new RestResponse(status, result);
			updateCommandRecords(method, command);
			return response;
		} catch (IOException e) {
			if(jsonResp.contains("exists, abort.")){
				log.warn("add policy with the same id, abort.");
				return new RestResponse("abort", "add policy with the same id, abort.");
			}
			else if(jsonResp.isEmpty()){
				log.warn("response is empty");
				return new RestResponse("empty", "add policy with the same id, abort.");
			}
			log.error("error response: {}", e.getMessage());
			return new RestResponse("error", "error response: "
					+ e.getMessage());
		}
	}

	void updateCommandRecords(String method, ResolvedCommand resolvedCommand){
		if(method.equals("DELETE")){
			if(commandRecordMap.containsKey(resolvedCommand.getId()))
				commandRecordMap.remove(resolvedCommand.getId());			
		}
		else{
			for(FlowCommandBase flowCommand:resolvedCommand.commandlist){
				CommandRecord commandRecord = new CommandRecord(resolvedCommand.getId(),flowCommand);
				commandRecordMap.put(commandRecord.commandID, commandRecord);
			}
		}
	}
	
	void updateCommandAppliedStatus(RedirectFlowCommand flowCommand, boolean isApplied){
		String fcid =  flowCommand.generateId();
		CommandRecord commandRecord = commandRecordMap.get(fcid);
		if(commandRecord==null){
			log.error("update a command which is not recorded.");
			return;
		}
		commandRecord.isApplied = isApplied; 
		String policyID = commandRecord.commandSetID;
		CommandAppliedEventArgs args = new CommandAppliedEventArgs(flowCommand,policyID);
		this.eventManager.addEvent(new Event(EventType.FLOW_COMMAND_APPLIED,
				null, null, args));
	}
	
	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public String pushDataToDevice(CommandPushRequest req)
			throws Exception {
		
		HTTPHelperResult httpHelperResult=null;
		ResolvedCommand data = req.getData();
		List<DeviceCommand> response = (List<DeviceCommand>) data.getHttpCommandList();
		for (DeviceCommand deviceCommand : response) {
			String url=deviceCommand.getUrl();
			String method=deviceCommand.getMethod();
			String dataString=deviceCommand.getData();
			if("GET".equals(method))
				httpHelperResult=HTTPHelper.httpGet(url, new HashMap<String,String>());
			else{
				httpHelperResult=HTTPHelper.httpRequest(url, method, dataString, new HashMap<String,String>());
			}
			if(200==httpHelperResult.getCode()){
				continue;
			}else {
				break;
			}
		}
		return httpHelperResult.toJson();
	}
}
