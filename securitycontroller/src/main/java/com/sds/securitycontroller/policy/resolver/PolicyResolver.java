/** 
 *    Copyright 2014 BUPT. 
 **/
package com.sds.securitycontroller.policy.resolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sds.securitycontroller.app.manager.IAppManagementService;
import com.sds.securitycontroller.command.ByodInitCommand;
import com.sds.securitycontroller.command.CommandAppliedEventArgs;
import com.sds.securitycontroller.command.CommandPushRequest;
import com.sds.securitycontroller.command.CommandPushResultEventArgs;
import com.sds.securitycontroller.command.CommandPusher;
import com.sds.securitycontroller.command.CommandRecord;
import com.sds.securitycontroller.command.DeviceCommand;
import com.sds.securitycontroller.command.FlowAction;
import com.sds.securitycontroller.command.FlowCommandBase;
import com.sds.securitycontroller.command.ICommandPushService;
import com.sds.securitycontroller.command.MatchArguments;
import com.sds.securitycontroller.command.ProxyCommand;
import com.sds.securitycontroller.command.RedirectDeviceInfo;
import com.sds.securitycontroller.command.RedirectDeviceInterface;
import com.sds.securitycontroller.command.RedirectFlowCommand;
import com.sds.securitycontroller.command.ResolvedCommand;
import com.sds.securitycontroller.command.RestoreCommand;
import com.sds.securitycontroller.command.SingleFlowCommand;
import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.device.BootDevice;
import com.sds.securitycontroller.device.Device;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.device.manager.IDeviceManagementService;
import com.sds.securitycontroller.directory.ModuleCommandResponse;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.cloud.CloudPort;
import com.sds.securitycontroller.knowledge.cloud.CloudVM;
import com.sds.securitycontroller.knowledge.cloud.CloudVM.AddressInfo;
import com.sds.securitycontroller.knowledge.cloud.CloudVM.NetworkInfo;
import com.sds.securitycontroller.knowledge.cloud.agent.ICloudAgentService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.packet.IPv4;
import com.sds.securitycontroller.policy.AllowFlowActionArgs;
import com.sds.securitycontroller.policy.AtomPolicy;
import com.sds.securitycontroller.policy.ByodInitActionArgs;
import com.sds.securitycontroller.policy.DevicePolicyObject;
import com.sds.securitycontroller.policy.DropFlowActionArgs;
import com.sds.securitycontroller.policy.FlowPolicyObject;
import com.sds.securitycontroller.policy.PolicyAction;
import com.sds.securitycontroller.policy.PolicyActionArgs;
import com.sds.securitycontroller.policy.PolicyActionType;
import com.sds.securitycontroller.policy.PolicyCommand;
import com.sds.securitycontroller.policy.PolicyInfo;
import com.sds.securitycontroller.policy.PolicyRecord;
import com.sds.securitycontroller.policy.PolicyStatus;
import com.sds.securitycontroller.policy.PolicySubject;
import com.sds.securitycontroller.policy.PolicySubject.PolicySubjectType;
import com.sds.securitycontroller.policy.RedirectFlowRoutingItem;
import com.sds.securitycontroller.policy.RedirectingFlowActionArgs;
import com.sds.securitycontroller.policy.TrafficPattern;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClause.QueryClauseItemType;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.utils.Cypher;
import com.sds.securitycontroller.utils.HTTPUtils;

public class PolicyResolver implements IPolicyResolveService,
		ISecurityControllerModule, IEventListener {
	protected static Logger log = LoggerFactory.getLogger(PolicyResolver.class);
	protected IRestApiService restApi;
	protected IEventManagerService eventManager;
	protected IDeviceManagementService deviceManager;
	protected ICloudAgentService cloudService;
	protected IAppManagementService appManager;
	protected IStorageSourceService storageSource;
	protected ICommandPushService commandPusher;
	protected IRegistryManagementService serviceRegistry;

	static boolean localCommandPusher = true;

	static String tableName = "policy";

	protected short defaultPriority = 100;
	protected static String ncCommandUrl = "http://nc.research.intra.sds.com:8081/wm/securitycontrolleragent/policyaction";
	public Map<EventType, EventSubscriptionInfo> subscriptionInfos = new HashMap<EventType, EventSubscriptionInfo>();


	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IPolicyResolveService.class);
		return l;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IPolicyResolveService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {

		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IStorageSourceService.class);
		return l;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {

		GlobalConfig config = GlobalConfig.getInstance();
		ncCommandUrl = (context.getConfigParams(this)
				.get("commandUrl")).replace("[nchost]", config.ncHost);
		localCommandPusher = Boolean.valueOf((context
				.getConfigParams(this).get("localCommandPusher")));
		this.eventManager = context.getServiceImpl(IEventManagerService.class,
				this);
		this.deviceManager = context
				.getServiceImpl(IDeviceManagementService.class);
		this.appManager = context.getServiceImpl(IAppManagementService.class);
		this.cloudService = context.getServiceImpl(ICloudAgentService.class);
		this.storageSource = context.getServiceImpl(
				IStorageSourceService.class, this);
		this.restApi = context.getServiceImpl(IRestApiService.class, this);
		if (localCommandPusher) {
			commandPusher = context.getServiceImpl(ICommandPushService.class);
			if (commandPusher == null) {
				log.error("[FATAL] local command pusher not started. System will now exit. Please either change 'localCommandPusher' "
						+ "to 'false' in config files or start Command Pusher on this machine. ");
				System.exit(1);
			}
		}

		eventManager.addEventListener(EventType.RECEIVED_POLICY, this);
		this.serviceRegistry = context.getServiceImpl(
				IRegistryManagementService.class, this);
		log.info("BUPT security controller policy resolver initialized.");
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {

		Map<String, String> columns = new HashMap<String, String>();
		columns.put("id", "VARCHAR(50)");
		columns.put("appid", "VARCHAR(50)");
		columns.put("generated_time", "INTEGER");
		columns.put("status", "VARCHAR(20)");
		columns.put("action_type", "VARCHAR(50)");
		columns.put("command_ids", "TEXT");
		columns.put("serialized_policy", "TEXT");
		columns.put("description", "TEXT");

		storageSource.createTable(tableName, columns);
		storageSource.setTablePrimaryKeyName(tableName, "id");

		// register REST interface
		PolicyResourceRoutable r = new PolicyResourceRoutable();
		restApi.addRestletRoutable(r);
		serviceRegistry.registerService(r.basePath(), this);
		serviceRegistry.registerCommand(PolicyCommand.getCommand(), IPolicyResolveService.class);

		new Thread(new SyncPolicyWithControllerThread()).start();

		log.info("BUPT security controller policy resolver started.");

	}

	@Override
	public void processEvent(Event e) {
		EventType type = e.type;
		log.info("Policy resolver start to process RECEIVED_POLICY event.");

		try {
			if (type == EventType.RECEIVED_POLICY) {// get a policy
				PolicyEventArgs args = (PolicyEventArgs) e.args;
				PolicyInfo policyInfo = args.policyInfo;
				generateNewPolicy(policyInfo);
			}
			// detected flow command applied (notified by NC agent)
			else if (type == EventType.FLOW_COMMAND_APPLIED) {
				CommandAppliedEventArgs eArgs = (CommandAppliedEventArgs) e.args;
				PolicyRecord policyRecord = (PolicyRecord) this.storageSource
						.getEntity(tableName, eArgs.policyID,
								PolicyRecord.class);
				String commandID = eArgs.flowCommand.generateId();
				CommandRecord commandRecord = policyRecord.commandRecordMap
						.get(commandID);
				commandRecord.isApplied = true;
				boolean policyCommandAllApplied = true;
				for (CommandRecord cm : policyRecord.commandRecordMap.values()) {
					if (!cm.isApplied) {
						policyCommandAllApplied = false;
						break;
					}
				}
				if (policyCommandAllApplied)
					policyRecord.policyStatus = PolicyStatus.APPLIED;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("error while process policy args! {}", ex.getMessage());
		}
	}

	@Override
	public String generateNewPolicy(PolicyInfo policyInfo)
			throws Exception {
		// validate APP
		AtomPolicy[] atomPolicies = policyInfo.getPolicies();
		String subId=policyInfo.getSubId();
		for (AtomPolicy atomPolicy : atomPolicies) {
			atomPolicy.setSubId(subId);
			if (!atomPolicy.getAction().getType().toString().contains("BYOD")) {
				// resolve policy object
				validateAndResolvePolicyObject((FlowPolicyObject)atomPolicy.getObject());
			}
			// check whether the policy is conflicted with others
			if (isPolicyConflicted(atomPolicy)) {
				throw new Exception("conflicted atom policy");
			}
			// switch: policy subject:
			// if subject is Network Controller:
			if (policyInfo.getPolicySubjectType() == PolicySubjectType.NETWORK_CONTROLLER) {
				if (atomPolicy.getAction().getActionArgs() != null){
					if(policyInfo.getPriority()<=0){
						atomPolicy.getAction().getActionArgs().setPriority((short)1);
					}else {
						atomPolicy.getAction().getActionArgs().setPriority(
								policyInfo.getPriority());
					}

					// atomPolicy.action.getActionArgs().setPriority((short)100);//changed
					// by xpn
				}
				ResolvedCommand resolvedCommand = generateNetworkControllerCommand(atomPolicy);
				if(resolvedCommand == null)
					return null;
				//policyInfo.setId(resolvedCommand.getId());

				PolicyRecord policyRecord = new PolicyRecord(
						resolvedCommand.getId(), subId, atomPolicy);

				// store in DB
				if (resolvedCommand != null) {
					atomPolicy.setResolvedCommand(resolvedCommand);

					for (FlowCommandBase command : resolvedCommand
							.getCommandlist()) {
						policyRecord.getCommandIdList().add(
								command.generateId());
					}
					storageSource.insertEntity(tableName, policyRecord);
				}

				if (resolvedCommand != null) {
					pushFlow(resolvedCommand);
				}

				// convert policy to policy record
				if (this.storageSource.getEntity(tableName, resolvedCommand.getId(),
						PolicyRecord.class) != null)
					return resolvedCommand.getId();

				return resolvedCommand.getId();
			}
			//if subject is sec dev:
			else if(policyInfo.getPolicySubjectType()==PolicySubjectType.SECURITY_DEVICE){
				return generateSecurityDevicePolicy(atomPolicy);
			}
			//if subject is SC:
			else if(policyInfo.getPolicySubjectType()==PolicySubjectType.SECURITY_CONTROLLER){
				processSecurityControllerPolicy(atomPolicy);

			}
		}
		return null;
	}
	/***
	 * 处理安全设备WAF等的策略解析
	 * @param atomPolicy
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String generateSecurityDevicePolicy(AtomPolicy atomPolicy) {
		if(!(atomPolicy.getObject() instanceof DevicePolicyObject))
			return "";
		if(!(atomPolicy.getAction().getType()!=PolicyActionType.HTTP))
			return "";
		List<DeviceCommand> httpCommandList=(List<DeviceCommand>) atomPolicy.getResolvedCommand().getHttpCommandList();
		ResolvedCommand resolverCommand=new ResolvedCommand();
		resolverCommand.setHttpCommandList(httpCommandList);
		CommandPushRequest req=new CommandPushRequest("", resolverCommand);
		String response = "";
		try {
			response = commandPusher.pushDataToDevice(req);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;		
	}

	ResolvedCommand generateNetworkControllerCommand(AtomPolicy atomPolicy) {
		ResolvedCommand resolvedCommand = null;
		if(!(atomPolicy.getObject() instanceof FlowPolicyObject))
			return null;
		FlowPolicyObject object=(FlowPolicyObject)atomPolicy.getObject();
		List<FlowMatch> resolvedFlowMatches = object.getResolvedFlowMatches();
		switch (atomPolicy.getAction().getType()) {
            case REDIRECT_FLOW:
                resolvedCommand = generateRedirectFlowCommand(
                		resolvedFlowMatches,
                        atomPolicy.getAction()
                                .getActionArgs());
                break;
            case CLEAN_FLOW:
                break;
            case DROP_FLOW:
                resolvedCommand = generateDropFlowCommand(
                		resolvedFlowMatches,
                        (DropFlowActionArgs) atomPolicy.getAction().getActionArgs());
                break;
            case ALLOW_FLOW:
                resolvedCommand = generateAllowFlowCommand(
                		resolvedFlowMatches,
                        (AllowFlowActionArgs) (atomPolicy.getAction().getActionArgs()));
                break;
            case BYOD_ALLOW:
                resolvedCommand = generateByodAllowFlowCommand(
                		resolvedFlowMatches,
                        (AllowFlowActionArgs) (atomPolicy.getAction().getActionArgs()));
                break;
            case BYOD_INIT:
                resolvedCommand = generateByodInitCommand(
                		resolvedFlowMatches,
                        atomPolicy.getAction().getActionArgs());
                break;
            case PROXY:
                resolvedCommand = generatePROXYCommand(atomPolicy);
                break;
            default:
                break;
        }
		return resolvedCommand;
	}

    private ResolvedCommand generatePROXYCommand(AtomPolicy atomPolicy) {
    	if(!(atomPolicy.getObject() instanceof FlowPolicyObject))
			return null;
		FlowPolicyObject object=(FlowPolicyObject)atomPolicy.getObject();
        JsonNode result = (JsonNode) object.getFlowArgs();
        ResolvedCommand resolvedCommand = new ResolvedCommand();
        resolvedCommand.setType(PolicyActionType.PROXY);
        resolvedCommand.setId("PROXY_COMMANDS_"+new Random().nextLong());
        List<ProxyCommand> proxyCommands = new ArrayList<>();
        resolvedCommand.setCommandlist(proxyCommands);
        ProxyCommand proxyCommand = new ProxyCommand();
        proxyCommands.add(proxyCommand);
        proxyCommand.setId("PROXY_COMMAND_"+new Random().nextLong());
        // set match
        MatchArguments match = new MatchArguments();
        proxyCommand.setMatchArguments(match);
        //ip only
        match.setDataLayerType((short) 2048);
        //tcp only
        match.setNetworkProtocol((byte) 6);

        String[] protected_host = result.get("protected_host").asText().split(":");
        String svrIp = protected_host[0];
        short svrPort = Short.parseShort(protected_host[1]);
        short workPort = Short.parseShort(protected_host[2]);
        long dpid = result.get("ifs").get(1).get("dpid").asLong();
        int portNum = result.get("ifs").get(1).get("port_num").asInt();
        String workMac = result.get("ifs").get(1).get("mac").asText();

        match.setNetworkDestination(IPv4.toIPv4Address(svrIp));
        match.setTransportDestination(svrPort);

        //set devices
        List<RedirectDeviceInfo> devices = new ArrayList<>();
        proxyCommand.setDevices(devices);

        RedirectDeviceInterface ingress = new RedirectDeviceInterface(null,dpid+":"+portNum);
        FlowAction inActions = new FlowAction();
        ingress.setActions(inActions);
        inActions.setDlDst(workMac);
        inActions.setNwDst(result.get("management_ip").asText());
        inActions.setTpDst(workPort);

        RedirectDeviceInterface egress = new RedirectDeviceInterface(null,dpid+":"+portNum);
        FlowAction eActions = new FlowAction();
        egress.setActions(eActions);
        eActions.setNwSrc(svrIp);
        eActions.setTpSrc(svrPort);

        RedirectDeviceInfo device = new RedirectDeviceInfo("Device" + new Random().nextLong(),
                0, ingress, egress);
        devices.add(device);

        //other settings
        proxyCommand.setCommandPriority((short) 1000);
        proxyCommand.setIdleTimeout(0);
        proxyCommand.setHardTimeout(0);
        proxyCommand.setInPort((short) result.get("gw_port").asInt());
        proxyCommand.setCommandName("proxy_command_" + new Random().nextInt());
        proxyCommand.setDpid(dpid);
        return resolvedCommand;

    }

    @SuppressWarnings("unchecked")
	void processSecurityControllerPolicy(AtomPolicy atomPolicy) {
    	if(!(atomPolicy.getObject() instanceof FlowPolicyObject))
    		return;
    	FlowPolicyObject object=(FlowPolicyObject)atomPolicy.getObject();
    	
		switch (atomPolicy.getAction().getType()) {
		case GLOBAL_ABNORMAL_FLOW_DETECTED:
			detectGlobalAbnormalFlows((List<FlowInfo>)object.getFlowArgs());
			break;
		default:
			break;
		}
		return;
	}
	
	
	@Override
	public void deletePolicy(String policyId) throws Exception {
		PolicyRecord policy = (PolicyRecord) this.storageSource.getEntity(
				tableName, policyId, PolicyRecord.class);
		if (policy == null)
			throw new Exception("Policy " + policyId + " not found");

		List<String> commandIds = policy.getCommandIdList();

		for (String commandId : commandIds) {
			ResolvedCommand resolvedCommand = new ResolvedCommand();
			resolvedCommand.setType(PolicyActionType.DELETE);
			pushFlow("/" + commandId, resolvedCommand);
			log.info("pushing command: " + "/" + commandId);
		}

		this.storageSource.deleteEntity(tableName, policyId);
		return;
	}

	boolean isPolicyConflicted(AtomPolicy atomPolicy) {
		@SuppressWarnings("unchecked")
		List<PolicyRecord> policyRecords = (List<PolicyRecord>) this.storageSource
				.executeQuery(tableName, "1=1", null, PolicyRecord.class);
		return isPolicyConflicted(atomPolicy, policyRecords);
	}

	/**
	 * 
	 * @param atomPolicy
	 *            待比较的policy
	 * @param policyRecords
	 *            数据库policy表中查询的记录，从而判断是否冲突
	 * @return
	 */
	boolean isPolicyConflicted(AtomPolicy atomPolicy,
			List<PolicyRecord> policyRecords) {
		for (PolicyRecord policyRecord : policyRecords) {
			// compare object
			boolean objectNotMatch = false;
			AtomPolicy refPolicy = policyRecord.policy;
			List<FlowMatch> objMatches = ((FlowPolicyObject)refPolicy.getObject()).getResolvedFlowMatches();
			Iterator<FlowMatch> iter = objMatches.iterator();
			while (iter.hasNext() && !objectNotMatch) {
				FlowMatch matchRef = iter.next();
				boolean flowNotMatch = true;
				for (FlowMatch matchObj : ((FlowPolicyObject)atomPolicy.
						getObject()).getResolvedFlowMatches()) {
					// stat 2: equal, 1: a contains b, 3: b contains a;
					int stat = FlowMatch.analyseTwoFlowMatchRelation(matchRef,
							matchObj);
					if (stat != 0) {
						flowNotMatch = false;
						break;
					}
				}
				if (flowNotMatch)
					objectNotMatch = true;
			}
			if (objectNotMatch)// object not match:
				continue;

			// we found two policies having matching flow matches, we then
			// compare actions
			PolicyAction actionRef = refPolicy.getAction();
			if (actionRef.getType() == atomPolicy.getAction().getType()
					&& actionRef.getActionArgs() != null
					&& actionRef.getActionArgs().getPriority() == atomPolicy
							.getPriority())
				// same action, then conflicted
				return true;
		}
		return false;
	}

	void pushFlow(ResolvedCommand commandResult) {
		pushFlow("", commandResult);
	}

	void pushFlow(String suffix, ResolvedCommand command) {
		if (command == null)
			return;
		CommandPushRequest commandPushResult = new CommandPushRequest(
				ncCommandUrl + suffix, command);
		try {
			if (localCommandPusher) {
				commandPusher.pushDataToNetworkController(commandPushResult);
				log.info("command pushed by local Command Pusher");
			} else {
				CommandPushResultEventArgs args1 = new CommandPushResultEventArgs(
						commandPushResult);
				this.eventManager.addEvent(new Event(EventType.PUSH_FLOW, null,
						this, args1));

				log.info("[[Policy resolver] command generated and send to Command Pusher.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("pushing command error: {}", e.getMessage());
		}
	}

	public void convertAtomPolicy(AtomPolicy atomPolicyDescription) {

	}

	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		this.subscriptionInfos.put(type, condition);
	}

	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
	}

	private String getVmMacByIP(String ipAddress) {
		if (ipAddress == null)
			return null;
		Object[] args = { KnowledgeType.CLOUD_PORT,
				KnowledgeEntityAttribute.IP_ADDRESS, ipAddress };
		CloudPort port = (CloudPort) eventManager
				.makeRPCCall(
						com.sds.securitycontroller.knowledge.manager.IKnowledgeBaseService.class,
						"queryEntity", args);
		if (port == null)
			return null;
		return port.getMac();
	}

	// resolve policy object into specific flow match pattern
	boolean validateAndResolvePolicyObject(FlowPolicyObject obj) {
		if(obj.getType() == null)
			return false;
		if (obj.getType() == KnowledgeType.NETWORK_FLOW) {
			if (obj.getFlowPattern() == null)
				return false;
			obj.getResolvedFlowMatches().add(obj.getFlowPattern());
			return true;
		}

		CloudVM vm = null;
		String vmIP = null;
		TrafficPattern trafficPattern = obj.getTrafficPattern();
		try {
			// find VM
			if (obj.getType() == KnowledgeType.CLOUD_VM) {
				// first find VM by id:
				if (obj.getId() != null) {
					Object[] args = { KnowledgeType.CLOUD_VM,
							KnowledgeEntityAttribute.ID, obj.getId() };
					vm = (CloudVM) eventManager
							.makeRPCCall(
									com.sds.securitycontroller.knowledge.manager.IKnowledgeBaseService.class,
									"queryEntity", args);
					if (vm != null) {
						vmIP = (String) vm.attributeMap
								.get(KnowledgeEntityAttribute.IP_ADDRESS);
					}
				}
				// if finding by ID failed: find VM by IP and valid whether IP
				// exists
				if (vm == null && trafficPattern != null) {
					Object[] args = { KnowledgeType.CLOUD_VM,
							KnowledgeEntityAttribute.IP_ADDRESS,
							trafficPattern.getHostIP() };
					vm = (CloudVM) eventManager
							.makeRPCCall(
									com.sds.securitycontroller.knowledge.manager.IKnowledgeBaseService.class,
									"queryEntity", args);
					if (vm == null)
						throw new Exception("Invalid VM IP!");
					vmIP = trafficPattern.getHostIP();
				}
				// generate flow match and add
				FlowMatch flowMatch = new FlowMatch();
				// set src & dst ip pattern
				if (trafficPattern.getTrafficDirection().equals("OUT")) {
					// direction:IN, object->target
					flowMatch.setNetworkDestination(vmIP);
					if (trafficPattern.getTargetIP() != null)
						flowMatch
								.setNetworkSource(trafficPattern.getTargetIP());
				}
				// 0512: for dropping all flows from sw port
				else if (obj.getType() == KnowledgeType.NETWORK_SWITCH_PORT) {

				}

				else {
					// default direction:IN, target->object
					flowMatch.setNetworkSource(vmIP);
					if (trafficPattern.getTargetIP() != null)
						flowMatch.setNetworkDestination(trafficPattern
								.getTargetIP());
				}
				obj.getResolvedFlowMatches().add(flowMatch);
			}
			// find
			else if (obj.getType() == KnowledgeType.CLOUD_USER
					|| obj.getType() == KnowledgeType.CLOUD_SUBNET
					|| obj.getType() == KnowledgeType.CLOUD_TENANT) {
				KnowledgeEntity entity = null;
				// find user by id:
				if (obj.getId() == null)
					throw new Exception("Entity " + obj.getType()
							+ " has an invalid ID(" + obj.getId() + ")!");
				// find user/tenant/subnet's VMs
				Object[] args = { obj.getType(), KnowledgeEntityAttribute.ID,
						obj.getId() };
				entity = (KnowledgeEntity) eventManager
						.makeRPCCall(
								com.sds.securitycontroller.knowledge.manager.IKnowledgeBaseService.class,
								"queryRelatedEntity", args);
				if (entity == null)
					throw new Exception("Invalid user ID!");
				Map<String, KnowledgeEntity> vmMap = entity.affiliatedEntityListMap
						.get(KnowledgeType.CLOUD_VM);
				if (vmMap == null) {
					log.warn("Entity {} (ID={}) has no affliated VMs.",
							obj.getType(), obj.getId());
					return true;
				}
				for (KnowledgeEntity ntt : vmMap.values()) {
					vm = (CloudVM) ntt;
					vmIP = (String) vm.attributeMap
							.get(KnowledgeEntityAttribute.IP_ADDRESS);
					// judge if ip is in ignore list
					if (trafficPattern.getIgnoredIPList() == null
							|| trafficPattern.getIgnoredIPList().contains(vmIP)) {
						FlowMatch flowMatch = new FlowMatch();
						// set src & dst ip pattern
						if (trafficPattern.getTrafficDirection().equals("OUT")) {
							// direction:IN, object->target
							flowMatch.setNetworkDestination(vmIP);
							if (trafficPattern.getTargetIP() != null)
								flowMatch.setNetworkSource(trafficPattern
										.getTargetIP());
						} else {
							// default direction:IN, target->object
							flowMatch.setNetworkSource(vmIP);
							if (trafficPattern.getTargetIP() != null)
								flowMatch.setNetworkDestination(trafficPattern
										.getTargetIP());
						}
						obj.getResolvedFlowMatches().add(flowMatch);
					}
				}
			} else if (obj.getType() == KnowledgeType.NETWORK_SWITCH_PORT) {
				//
				if (trafficPattern.getSrcMac() != null) {
					FlowMatch flowMatch = new FlowMatch();
					flowMatch.setDataLayerSource(trafficPattern.getSrcMac());
					obj.getResolvedFlowMatches().add(flowMatch);
				}
				if (trafficPattern.getInPortName() != null) {
					FlowMatch flowMatch = new FlowMatch();
					short inPort = Integer.valueOf(
							trafficPattern.getInPortName()).shortValue();
					flowMatch.setInputPort(Integer.valueOf(
							trafficPattern.getInPortName()).shortValue());
					// TODO manually set src and dst mac
					if (inPort == 3) {
						flowMatch.setDataLayerSource("52:54:00:a9:b8:b8");
						flowMatch.setDataLayerDestination("52:54:00:a9:b8:b9");
					}
					obj.getResolvedFlowMatches().add(flowMatch);
				}
			}
			// flow command resolve
			else if (obj.getType() == KnowledgeType.NETWORK_FLOW_COMMAND) {
				log.info("213421415231rwefwg3qc 4rc412cx4123cx41123xc41`xc44xc2"
						+ obj.getId());
			} else {
				log.warn("Not supported policy object '{}'!", obj.getType());
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/***
	 * get device by device type and requirements
	 * 
	 * @type policy type
	 * @param requirement
	 * @param excludedDeviceIDs
	 *            : do not find device in this list
	 * @return
	 */
	Device getDeviceByPolicyAction(List<DeviceType> types,
			Set<String> excludedDeviceIDs) {

		// here we get security device via device manager
		Device targetDevice = null;
		List<Device> allDevices = null;
		if (deviceManager != null)
			// allDevices = deviceManager.getConnectedDevices();
			allDevices = deviceManager.getDevs();

		if (allDevices == null)
			allDevices = new ArrayList<Device>();

		// for byod server
		Device byodDevice = new Device("byod-server");
		byodDevice.setType(DeviceType.BYOD);
		List<String> macs = new ArrayList<String>();
		macs.add("mac1");
		macs.add("mac2");
		byodDevice.setMac_addrs(macs);
		List<String> taps = new ArrayList<String>();
		taps.add("tap1");
		taps.add("tap2");
		byodDevice.setAttachedTaps(taps);
		allDevices.add(byodDevice);


		// we choose a device automatically
		// by liuwenmao, here we need to get suitable security devices from
		// device manager, e.g. return all devices that can prevent DDoS
		// attacks.
		for (Device dc : allDevices) {
			if (excludedDeviceIDs != null
					&& excludedDeviceIDs.contains(dc.getId()))
				continue;
			if (types.contains(dc.getType())) {
				targetDevice = dc;
				break;
			}
		}

		return targetDevice;
	}

	/***
	 * get device by device type and requirements
	 * 
	 * @param type
	 *            : possible device types
	 * @param requirement
	 * @param excludedDeviceIDs
	 *            : do not find device in this list
	 * @return
	 */
	Device getDeviceByPolicyAction(PolicyActionType type,
			Set<String> excludedDeviceIDs) {
		List<DeviceType> deviceTypes = new ArrayList<DeviceType>();
		switch (type) {
		case REDIRECT_FLOW:
			// here we need to get suitable security devices from device
			// manager, return all devices that can prevent DDoS attacks.
			deviceTypes.add(DeviceType.IPS);
			deviceTypes.add(DeviceType.ADS);
			deviceTypes.add(DeviceType.USER_DEVICE);
			break;
		case CLEAN_FLOW:
			deviceTypes.add(DeviceType.ADS);
			deviceTypes.add(DeviceType.IPS);
			deviceTypes.add(DeviceType.BYOD);
			break;
		default:
			break;
		}
		return getDeviceByPolicyAction(deviceTypes, excludedDeviceIDs);
	}

	// generate different types of commands

	ResolvedCommand generateByodInitCommand(List<FlowMatch> flowMatchList,
			PolicyActionArgs args) {
		log.info("Start generating BYOD_INIT command");
		ByodInitActionArgs initArgs = (ByodInitActionArgs) args;

		ResolvedCommand resolvedCommand = new ResolvedCommand();
		resolvedCommand.setType(PolicyActionType.BYOD_INIT);
		List<ByodInitCommand> vmCommands = new ArrayList<ByodInitCommand>();

		if (initArgs == null) {
			log.error("Arguments empty.");
			return null;
		}

		long dpid = initArgs.getDpid();// added by xpn
		short inPort = initArgs.getInPort();
		String serverIp = initArgs.getServerIp();
		String serverMac = initArgs.getServerMac();
		String network = initArgs.getNetwork();
		short mask = initArgs.getMask();
		boolean force = initArgs.isForce();

		ByodInitCommand command = new ByodInitCommand(dpid, inPort, serverIp,
				serverMac, network, mask);
		command.setCommandPriority((short) 2);
		MatchArguments matchArguments = new MatchArguments(flowMatchList.get(0));
		command.setMatchArguments(matchArguments);
		command.generateId();

		vmCommands.add(command);
		resolvedCommand.setCommandlist(vmCommands);
		String idstr = inPort + serverIp + serverMac + network + mask;
		resolvedCommand.setId(Cypher.getMD5(new String[] {
				resolvedCommand.getType().toString(), idstr }));
		resolvedCommand.setForce(force);
		log.info("Generated command {}: {}", resolvedCommand.getId(),
				resolvedCommand);
		return resolvedCommand;
	}

	public class Tuple<X, Y> {
		public final X x;
		public final Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}
	}

	ResolvedCommand generateByodAllowFlowCommand(List<FlowMatch> flowMatchList,
			AllowFlowActionArgs args) {
		log.info("Start generating BYOD_ALLOW command");

		ResolvedCommand resolvedCommand = new ResolvedCommand();
		resolvedCommand.setType(PolicyActionType.BYOD_ALLOW);
		List<SingleFlowCommand> commandList = new ArrayList<SingleFlowCommand>();

		String idstr = "";
		for (FlowMatch flowMatch : flowMatchList) {

			// 首先找到所有允许转发的交换机入口
			Set<Tuple<Long, Short>> allowedSWPorts = new HashSet<Tuple<Long, Short>>();
			List<QueryClauseItem> allItems = new ArrayList<QueryClauseItem>();
			allItems.add(new QueryClauseItem("action_type", "BYOD_INIT",
					QueryClauseItem.OpType.EQ));
			QueryClause qc = new QueryClause(allItems, QueryClauseItemType.OR,
					tableName, null, null);
			@SuppressWarnings("unchecked")
			List<PolicyRecord> policies = (List<PolicyRecord>) this.storageSource
					.executeQuery(qc, PolicyRecord.class);
			for (PolicyRecord policy : policies) {
				AtomPolicy ap = policy.policy;
				ByodInitActionArgs byodArgs = (ByodInitActionArgs) ap.getAction()
						.getActionArgs();
				allowedSWPorts.add(new Tuple<Long, Short>(byodArgs.getDpid(),
						byodArgs.getInPort()));
			}

			// 然后针对这些入口，添加允许的策略
			for (Tuple<Long, Short> entry : allowedSWPorts) {
				SingleFlowCommand sfCommand = new SingleFlowCommand();

				// set match arguments
				MatchArguments matchArguments = new MatchArguments(flowMatch);
				sfCommand.setMatchArguments(matchArguments);
				sfCommand.setDpid(entry.x);
				// set inport name
				sfCommand.setInPort(entry.y);
				// set priority
				sfCommand.setCommandPriority((short) 5);

				// set other stuff
				int hardTimeOut = (args == null) ? 0 : args.getHardTimeout();
				int idleTimeout = (args == null) ? 0 : args.getIdleTimeout();
				sfCommand.setHardTimeout(hardTimeOut);
				sfCommand.setIdleTimeout(idleTimeout);
				sfCommand.generateId();

				commandList.add(sfCommand);
			}
			idstr += flowMatch.getDataLayerSource();
		}
		resolvedCommand.setId(Cypher.getMD5(new String[] {
				resolvedCommand.getType().toString(), idstr }));
		resolvedCommand.setCommandlist(commandList);
		return resolvedCommand;
	}

	ResolvedCommand generateAllowFlowCommand(List<FlowMatch> flowMatchList,
			AllowFlowActionArgs args) {
		ResolvedCommand resolvedCommand = new ResolvedCommand();
		resolvedCommand.setType(PolicyActionType.ALLOW_FLOW);
		List<SingleFlowCommand> commandList = new ArrayList<SingleFlowCommand>();

		String idstr = "";
		for (FlowMatch flowMatch : flowMatchList) {
			SingleFlowCommand sfCommand = new SingleFlowCommand();

			// set match arguments
			MatchArguments matchArguments = new MatchArguments(flowMatch);
			sfCommand.setMatchArguments(matchArguments);
			sfCommand.setDpid(args.getDpid());
			// set priority
			sfCommand.setCommandPriority(args.getPriority());

			// set inport name
			sfCommand.setInPort(args.getInPort());

			// set other stuff
			int hardTimeOut = (args == null) ? 0 : args.getHardTimeout();
			int idleTimeout = (args == null) ? 0 : args.getIdleTimeout();
			sfCommand.setHardTimeout(hardTimeOut);
			sfCommand.setIdleTimeout(idleTimeout);

			// generate command id
			sfCommand.generateId();

			commandList.add(sfCommand);

			idstr += flowMatch.getDataLayerSource();
		}
		resolvedCommand.setId(Cypher.getMD5(new String[] {
				resolvedCommand.getType().toString(), idstr }));
		resolvedCommand.setCommandlist(commandList);
		return resolvedCommand;
	}

	ResolvedCommand generateDropFlowCommand(List<FlowMatch> flowMatchList,
			DropFlowActionArgs args) {
		ResolvedCommand resolvedCommand = new ResolvedCommand();
		resolvedCommand.setType(PolicyActionType.DROP_FLOW);
		List<SingleFlowCommand> commandList = new ArrayList<SingleFlowCommand>();

		for (FlowMatch flowMatch : flowMatchList) {
			SingleFlowCommand sfCommand = new SingleFlowCommand();

			// set match arguments
			MatchArguments matchArguments = new MatchArguments(flowMatch);
			sfCommand.setMatchArguments(matchArguments);
			sfCommand.setDpid(args.getDpid());

			// set priority
			sfCommand.setCommandPriority(args.getPriority());
			sfCommand.setInPort(args.getInPort());
			// set other stuff
			int hardTimeOut = (args == null) ? 0 : args.getHardTimeout();
			int idleTimeout = (args == null) ? 0 : args.getIdleTimeout();
			sfCommand.setHardTimeout(hardTimeOut);
			sfCommand.setIdleTimeout(idleTimeout);

			sfCommand.generateId();

			commandList.add(sfCommand);
		}
		resolvedCommand.setCommandlist(commandList);
		return resolvedCommand;
	}

	// clean flow command
	ResolvedCommand generateCleanFlowCommand(FlowMatch flowMatch,
			PolicyActionArgs args) {

		List<RedirectFlowCommand> vmCommands = new ArrayList<RedirectFlowCommand>();
		try {
			String IP = flowMatch.getNetworkDestination();
			String objDeviceMac = getVmMacByIP(IP);
			MatchArguments starterMatchArguments = new MatchArguments(null,
					objDeviceMac); // first match point(from/to the object
									// device)
			Device dev = getDeviceByPolicyAction(PolicyActionType.CLEAN_FLOW, null);
			if (dev == null)
				throw new Exception(" Failed to find device");
			List<RedirectDeviceInfo> deviceList = new ArrayList<RedirectDeviceInfo>();
			// set tag=1 ,only one device
			deviceList.add(new RedirectDeviceInfo(dev.getId(), 1,
					new RedirectDeviceInterface(dev.getMac_addrs().get(0), dev
							.getAttachedTaps().get(0)),
					new RedirectDeviceInterface(dev.getMac_addrs().get(1), dev
							.getAttachedTaps().get(1))));
			RedirectFlowCommand command = new RedirectFlowCommand(
					starterMatchArguments, deviceList);

			//
			// enterAPInfo = new APInfo(objDeviceMac,null,true,false,false);
			// exitAPInfo = new
			// APInfo(dev.getAttachedTaps()[0],dev.getMacAddresses()[0],false,false,false);
			//
			// short priority = (short) (args.priority + (short)1 );
			//
			// RedirectFlowCommand toDevFlowCommand = new
			// RedirectFlowCommand("flow_match="+flowMatch.toString()+";priority="+priority+";sequence=1",
			// priority, //
			// enterAPInfo,
			// exitAPInfo,
			// starterMatchArguments
			// );
			// priority = (short) (args.priority + (short)2 );
			// RedirectFlowCommand fromDevFlowCommand = new
			// RedirectFlowCommand("flow_match="+flowMatch.toString()+";priority="+priority+";sequence=2",
			// priority, //
			// enterAPInfo,
			// exitAPInfo,
			// starterMatchArguments
			// );
			// vmCommands.add(toDevFlowCommand);
			// vmCommands.add(fromDevFlowCommand);
			// resolvedCommand.setCommandlist(vmCommands);

			vmCommands.add(command);

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		ResolvedCommand resolvedCommand = new ResolvedCommand();
		resolvedCommand.setType(PolicyActionType.CLEAN_FLOW);
		resolvedCommand.setCommandlist(vmCommands);
		return resolvedCommand;
	}

	/***
	 * 
	 * @param flowMatch
	 * @param args
	 * @param requirement
	 * @return
	 */
	ResolvedCommand generateRedirectFlowCommand(List<FlowMatch> flowMatchList,
			PolicyActionArgs args) {
		ResolvedCommand resolvedCommand = new ResolvedCommand();
		resolvedCommand.setType(PolicyActionType.REDIRECT_FLOW);
		List<RedirectFlowCommand> vmCommands = new ArrayList<RedirectFlowCommand>();
		MatchArguments starterMatchArguments = null;// first match point(from/to
													// the object device)
		Set<String> addedDeviceIDs = new HashSet<String>();// record added

		List<DeviceType> deviceTypeRange;
		String IP = null;
		String objDeviceMac = null;
		String prevAttachPointMac = null;

		for (FlowMatch flowMatch : flowMatchList) {
			RedirectFlowCommand fc = null;
			try {
				if (flowMatch.getDataLayerSource() != null
						&& flowMatch.getDataLayerDestination() != null) {
					starterMatchArguments = new MatchArguments(
							flowMatch.getDataLayerSource(),
							flowMatch.getDataLayerDestination());
				} else {
					if (flowMatch.getNetworkDestination() != null) {
						// match flows into the object device(IN)
						IP = flowMatch.getNetworkDestination();
						objDeviceMac = getVmMacByIP(IP);
						starterMatchArguments = new MatchArguments(null,
								objDeviceMac);
					} else if (flowMatch.getNetworkSource() != null) {
						// match flows from the object device(OUT)
						IP = flowMatch.getNetworkSource();
						objDeviceMac = getVmMacByIP(IP);
						starterMatchArguments = new MatchArguments(
								prevAttachPointMac, null);
					}
				}

				RedirectingFlowActionArgs redArgs = (RedirectingFlowActionArgs) args;
				// forming device linking route by traversing redirect routing
				for (RedirectFlowRoutingItem item : redArgs
						.getRedirectFlowRoutingItems()) {
					if (item.deviceType == DeviceType.USER_DEVICE) {
						// redirect traffic from security device to object
						// device
						prevAttachPointMac = objDeviceMac;
					} else {
						// redirect traffic from object device to security
						// device find device
						deviceTypeRange = new ArrayList<DeviceType>();
						deviceTypeRange.add(item.deviceType);
						Device dev = getDeviceByPolicyAction(deviceTypeRange, addedDeviceIDs);
						if (dev == null)
							throw new Exception(" Failed to find device");
						addedDeviceIDs.add(dev.getId());
						prevAttachPointMac = dev.getMac_addrs().get(1);

						List<RedirectDeviceInfo> deviceList = new ArrayList<RedirectDeviceInfo>();
						// set tag=1 ,only one device
						deviceList
								.add(new RedirectDeviceInfo(dev.getId(), 1,
										new RedirectDeviceInterface(dev
												.getMac_addrs().get(0), dev
												.getAttachedTaps().get(0)),
										new RedirectDeviceInterface(dev
												.getMac_addrs().get(1), dev
												.getAttachedTaps().get(1))));
						fc = new RedirectFlowCommand(starterMatchArguments,
								deviceList);
						// generate id
						fc.generateId();
					}
					short priority = (short) (args.getPriority() + item.sequence);
					fc.setCommandPriority(priority);

					vmCommands.add(fc);
				}

			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
				return null;
			}
		}
		resolvedCommand.setCommandlist(vmCommands);
		log.info("Generate command is: {}", resolvedCommand);
		return resolvedCommand;
	}

	/**
	 * 
	 * @param policyID
	 * @return
	 */
	ResolvedCommand generateRestoreCommand(PolicyActionType type,
			String policyID, PolicyActionArgs args) {
		if (policyID == null)
			return null;
		// get policy record by id
		PolicyRecord record = getPolicyRecordByID(policyID);
		if (record == null)
			return null;
		ResolvedCommand resolvedCommand = new ResolvedCommand();
		resolvedCommand.setType(type);// PolicyActionType.RESTORE_BYOD_ALLOW);
		List<RestoreCommand> vmCommands = new ArrayList<RestoreCommand>();
		for (String commandID : record.getCommandIdList()) {
			RestoreCommand command = new RestoreCommand(commandID);
			vmCommands.add(command);
		}
		resolvedCommand.setCommandlist(vmCommands);
		String idstr = "res" + policyID;
		resolvedCommand.setId(idstr);
		resolvedCommand.setForce(args.isForce());
		log.info("Generate command {} is: {}", resolvedCommand.getId(),
				resolvedCommand);

		return resolvedCommand;
	}

	ResolvedCommand generateMirrorFlowCommand(List<FlowMatch> flowMatchList,
			PolicyActionArgs args) {
		// TODO
		return null;
	}

	PolicyRecord getPolicyRecordByID(String policyID) {
		return (PolicyRecord) this.storageSource.getEntity(tableName, policyID,
				PolicyRecord.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<PolicyRecord> getPolicyRecords(
			PolicySubject policySubject, PolicyActionType policyActionType) {
		List<QueryClauseItem> allItems = new ArrayList<QueryClauseItem>();
		if (policyActionType != null)
			allItems.add(new QueryClauseItem("action_type", policyActionType,
					QueryClauseItem.OpType.EQ));
		try {
			QueryClause qc = new QueryClause(allItems, QueryClauseItemType.AND,
					tableName, null, null);// new QueryClause(this.tableName);
			List<PolicyRecord> policyRecords = (List<PolicyRecord>) storageSource
					.executeQuery(qc, PolicyRecord.class);
			return policyRecords;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * synchronize policy
	 */
	void syncPolicyWithNetworkController() {
		Map<String, String> ncByodCommandPolicyMap = new HashMap<String, String>();
		// get BYOD policies from NC API
		String baseUrl = "http://nc.research.intra.sds.com:8081/wm/securitycontrolleragent/policyaction";
		String[] typesStrings = { "byod-allow", "byod-init" };
		String url = "";
		log.debug("sync Policy With Network Controller");

		for (String typeStr : typesStrings) {
			url = baseUrl + "?type=" + typeStr;
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");
			String jsonResp = HTTPUtils.httpGet(url, headers);
			if (jsonResp == null) {
				log.error("unable to connect network controller, synchronizing active command failed.");
				return;
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			JsonNode rootNode;
			try {
				rootNode = mapper.readValue(jsonResp, JsonNode.class);
				for (int i = 0; i < rootNode.size(); i++) {
					// get command id
					JsonNode commandNode = rootNode.get(i);
					String commandId = commandNode.path("id").asText();
					String policyId = commandNode.path("policyId").asText();
					if (commandId != null) {
						ncByodCommandPolicyMap.put(commandId, policyId);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		List<QueryClauseItem> allItems = new ArrayList<QueryClauseItem>();
		String[] types = new String[] { "BYOD_INIT", "BYOD_ALLOW" };
		for (String type : types) {
			allItems.add(new QueryClauseItem("action_type", type,
					QueryClauseItem.OpType.EQ));
		}

		QueryClause qc = new QueryClause(allItems, QueryClauseItemType.OR,
				tableName, null, null);// new QueryClause(this.tableName);
		@SuppressWarnings("unchecked")
		List<PolicyRecord> byodPolicies = (List<PolicyRecord>) storageSource
				.executeQuery(qc, PolicyRecord.class);
		Map<String, PolicyAction> policyActionMap = new HashMap<String, PolicyAction>();
		// for deleting identical policies
		Map<String, PolicyRecord> policyMap = new HashMap<String, PolicyRecord>();
		// for checking policy existence
		Map<String, PolicyRecord> allPolicyMap = new HashMap<String, PolicyRecord>();

		for (PolicyRecord policy : byodPolicies) {
			policyActionMap.put(policy.getId(), policy.policy.getAction());
			policyMap.put(policy.getId(), policy);
			allPolicyMap.put(policy.getId(), policy);
		}

		Map<String, PolicyRecord> addingPolicies = new HashMap<String, PolicyRecord>();
		Map<String, PolicyRecord> deletingPolicies = new HashMap<String, PolicyRecord>();

		for (Entry<String, String> entry : ncByodCommandPolicyMap.entrySet()) {
			String commandId = entry.getKey();
			String policyId = entry.getValue();

			if (!allPolicyMap.containsKey(policyId)) {
				// orphan policy from fl,delete
				// orphanPolicy.getCommandIdList().add(commandId);
				PolicyRecord orphanPolicy = null;
				if (deletingPolicies.containsKey(policyId))
					orphanPolicy = deletingPolicies.get(policyId);
				else
					orphanPolicy = new PolicyRecord(null, null, null);
				orphanPolicy.getCommandIdList().add(commandId);
				deletingPolicies.put(policyId, orphanPolicy);
				continue;
			} else {
				// same policy, ignore
				if (policyMap.containsKey(policyId))
					policyMap.remove(policyId);
			}
		}

		// checking the remaining elements in policyMap, i.e. new SC policies
		for (Entry<String, PolicyRecord> entry : policyMap.entrySet()) {
			String policyId = entry.getKey();
			PolicyRecord policy = entry.getValue();
			addingPolicies.put(policyId, policy);
		}

		for (PolicyRecord policy : addingPolicies.values()) {
			ResolvedCommand resolvedCommand = generateNetworkControllerCommand(policy
					.getPolicy());
			if (resolvedCommand == null)
				return;
			pushFlow(resolvedCommand);
		}

		// deleting
		for (PolicyRecord policy : deletingPolicies.values()) {
			List<String> commandIds = policy.getCommandIdList();
			for (String commandId : commandIds) {
				ResolvedCommand resolvedCommand = new ResolvedCommand();
				resolvedCommand.setType(PolicyActionType.DELETE);
				resolvedCommand.setId(commandId);
				pushFlow("/" + commandId, resolvedCommand);
				log.info("delete command: " + "/" + commandId);
			}
		}
		// pushFlow(orphanPolicy.getResolvedCommand());
	}

	class SyncPolicyWithControllerThread implements Runnable {

		public SyncPolicyWithControllerThread() {
		}

		@Override
		public void run() {
			while (true) {
				syncPolicyWithNetworkController();
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} // every 10 minutes
			}
		}
	}

	public static void getCloudMap(ICloudAgentService cloudService,
			HashMap<String, List<String>> tenantMap,
			HashMap<String, List<String>> userMap,
			HashMap<String, List<String>> vmMap,
			HashMap<String, String> vmTenantMapping) {
		Map<String, CloudVM> vmap = cloudService.getVMs();
		Iterator<Entry<String, CloudVM>> iter = vmap.entrySet().iterator();
		while (iter.hasNext()) { // per vm
			Entry<String, CloudVM> entry = iter.next();
			CloudVM cvm = entry.getValue();
			String vmId = cvm.getId();
			String userId = cvm.getUserId();
			String tenantId = cvm.getTenantId();
			vmTenantMapping.put(vmId, tenantId);
			Map<String, NetworkInfo> nInfos = cvm.getNetworks();
			Iterator<Entry<String, NetworkInfo>> netIter = nInfos.entrySet()
					.iterator();
			ArrayList<String> cache = new ArrayList<String>();
			while (netIter.hasNext()) { // per network
				Entry<String, NetworkInfo> netEntry = netIter.next();
				List<AddressInfo> addrs = netEntry.getValue().getAddresses();
				for (AddressInfo i : addrs) {
					cache.add(i.getAddr());
				}
			}
			if (cache.size() > 0 && vmId != null) {
				vmMap.put(vmId, cache);
				if (userMap.size() == 0 || !userMap.containsKey(userId)) {
					List<String> tmpList = new ArrayList<String>();
					tmpList.add(vmId);
					userMap.put(userId, tmpList);
				} else {
					List<String> tmpList = new ArrayList<String>();
					tmpList.addAll(userMap.get(userId));
					tmpList.add(vmId);
					userMap.put(userId, tmpList);
				}
				if (tenantMap.size() == 0 || !tenantMap.containsKey(tenantId)) {
					List<String> tmpList = new ArrayList<String>();
					tmpList.add(vmId);
					tenantMap.put(tenantId, tmpList);
				} else {
					List<String> tmpList = new ArrayList<String>();
					tmpList.addAll(tenantMap.get(tenantId));
					tmpList.add(vmId);
					tenantMap.put(tenantId, tmpList);
				}
			}

		}
	}

	@SuppressWarnings("unchecked")
	void detectGlobalAbnormalFlows(List<FlowInfo> abnormalFlows) {
		if(abnormalFlows == null || abnormalFlows.size()<=0){
			return;
		}
		BootDevice bootDevice =deviceManager.getBootDevice(DeviceType.IPS);
		if(bootDevice==null){
			log.warn("IPS device is not found");
			return;
		}
		JSONObject outData=new JSONObject();
		JSONArray commandlist=new JSONArray();
		outData.put("commandlist", commandlist);
		outData.put("type", "REDIRECT_FLOW");
		outData.put("id", Math.random() + "");

		for (FlowInfo flowInfo : abnormalFlows) {
			if(!flowInfo.getMatch().isRedirect()){
				continue;
			}
			JSONObject com = new JSONObject();
			String idstr = flowInfo.getinputPort() + flowInfo.getDpid()
					+ flowInfo.getnetworkDestination()
					+ flowInfo.getnetworkDestination()
					+ flowInfo.getnetworkProtocol();
			String cmdId = Cypher.getMD5(new String[] {
					PolicyActionType.REDIRECT_FLOW.toString(), idstr });

			com.put("id", cmdId);
			com.put("commandPriority", flowInfo.getPriority());
			// com.put("commandName", "USERac33682274c74a5fb3663677a0794fd5_0");
			com.put("commandName", cmdId);
			com.put("hardTimeout", flowInfo.getHardTimeout());
			com.put("idleTimeout", 5000);
			// com.put("idleTimeout", flowInfo.getIdleTimeout());

			JSONObject matchArg = new JSONObject();
			matchArg.put("wildcards", flowInfo.getwildcards());
			matchArg.put("inputPort", flowInfo.getinputPort());
			matchArg.put("dataLayerSource", flowInfo.getdataLayerSource());
			matchArg.put("dataLayerDestination",
					flowInfo.getdataLayerDestination());
			if (flowInfo.getdataLayerVirtualLan() != -1) {
				matchArg.put("dataLayerVirtualLan",
						flowInfo.getdataLayerVirtualLan());
			}
			if (flowInfo.getdataLayerVirtualLanPriorityCodePoint() != 0) {
				matchArg.put("dataLayerVirtualLanPriorityCodePoint",
						flowInfo.getdataLayerVirtualLanPriorityCodePoint());
			}
			matchArg.put("dataLayerType", flowInfo.getdataLayerType());
			matchArg.put(
					"networkTypeOfService",
					(flowInfo.getnetworkTypeOfService().equals("") ? 0
							: Integer.parseInt(flowInfo
									.getnetworkTypeOfService())));
			if (flowInfo.getnetworkSourceInt() != -1) {
				matchArg.put("networkSource", flowInfo.getnetworkSourceInt());
			}
			if (flowInfo.getnetworkDestinationInt() != -1) {
				matchArg.put("networkDestination",
						flowInfo.getnetworkDestinationInt());
			}
			if (flowInfo.gettransportSource() != 0) {
				matchArg.put("transportSource", flowInfo.gettransportSource());
			}
			if (flowInfo.gettransportDestination() != 0) {
				matchArg.put("transportDestination",
						flowInfo.gettransportDestination());
			}

			JSONArray devices = new JSONArray();
			com.put("devices", devices);
			JSONObject device = new JSONObject();
			device.put("deviceid", "ips1");
			device.put("tag", 1);
			JSONObject ingress = new JSONObject();
			// ingress.put("mac", "52:54:00:a9:b8:b8");//to be mod
			
			bootDevice.getAttribute("egress");
			ingress.put("ap", bootDevice.getAttribute("ingress").toString());
			//ingress.put("ap", "244592929826626:8");
			JSONObject egress = new JSONObject();
			// egress.put("mac", "52:54:00:a9:b8:b9");
			egress.put("ap", bootDevice.getAttribute("egress").toString());
			//egress.put("ap", "244592929826626:9");
			device.put("ingress", ingress);
			device.put("egress", egress);
			com.put("matchArguments", matchArg);

			devices.add(device);
			commandlist.add(com);
		}
		String jsonReq = outData.toJSONString();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		String resp=null;
		try {
            log.info("POST to {}: \n{}", ncCommandUrl, jsonReq);
            resp = (HTTPUtils.httpPost(ncCommandUrl, jsonReq, headers));
            log.info("nc response: {}", resp);
        } catch (Exception e) {
			log.error("exception flow redirect to IPS :{}",e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getAllPolicies() {
		Set<String> policyIds = new HashSet<String>();
		// get existed policy id from nc and save to policyIds(set)
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		String policies = HTTPUtils.httpGet(ncCommandUrl, headers);
		if(policies == null)
			return policyIds;
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode root = mapper.readValue(policies, JsonNode.class);
			for (JsonNode jsonNode : root) {
				policyIds.add(jsonNode.get("id").asText());
			}

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return policyIds;
	}

	public static void main(String[] args){
		ICommandPushService commandPusher=new CommandPusher();
		ResolvedCommand data=new ResolvedCommand();
		List<DeviceCommand> httpCommandList=new LinkedList<DeviceCommand>();
		
		DeviceCommand http1=new DeviceCommand("http://cloudsensing.cn:9071/WIFPa/AllMWID.json","GET",new HashMap<String,String>(),"");
		DeviceCommand http2=new DeviceCommand("http://cloudsensing.cn:9071/WIFPa/AllMWID.xml","GET",new HashMap<String,String>(),"");
		httpCommandList.add(http1);
		httpCommandList.add(http2);
		data.setHttpCommandList(httpCommandList);
		CommandPushRequest req=new CommandPushRequest("", data);
		try {
			String result = commandPusher.pushDataToDevice(req);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(result);
			int code =root.path("code").asInt();
			String msgString=root.path("msg").asText();
			System.out.println("code="+code+";msg="+msgString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String executeCommand(ModuleCommandResponse moduleCommand) {
		String command = moduleCommand.getCommand();
		String subcommand = moduleCommand.getSubcommand();
		if(!command.equals("policy_resolver"))
			return "invalid command: "+command;
		if(subcommand.equals("get-commands")){
			return "get-commands ok";
		}
		else if(subcommand.equals("del-commands")){
			return "del-commands ok";
		}
		else
			return "invalid subcommand: "+subcommand;
	}

	@Override
	public Object handleRPCRequest(String methodName, Object[] args) {
		if(null==args ){
			return null;
		}
		if(methodName.toLowerCase().equals("executecommand")){
			ModuleCommandResponse moduleCommand = (ModuleCommandResponse)args[0];
			return this.executeCommand(moduleCommand);
		}
		else
			return "unknown method: "+methodName;
	}

}