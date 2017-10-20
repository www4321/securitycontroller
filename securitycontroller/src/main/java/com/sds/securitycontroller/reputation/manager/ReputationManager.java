/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.reputation.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.cloud.CloudTenant;
import com.sds.securitycontroller.knowledge.cloud.CloudUser;
import com.sds.securitycontroller.knowledge.cloud.CloudVM;
import com.sds.securitycontroller.knowledge.manager.IKnowledgeBaseService;
import com.sds.securitycontroller.log.Report;
import com.sds.securitycontroller.log.Report.TargetType;
import com.sds.securitycontroller.log.ReportEventArgs;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.reputation.Reputation.ReputationLevel;
import com.sds.securitycontroller.reputation.ReputationEntity;
import com.sds.securitycontroller.reputation.ReputationEventArgs;
import com.sds.securitycontroller.reputation.determined.DeterminedReputationCaculator;
import com.sds.securitycontroller.reputation.ds.DSReputationCaculator;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IStorageSourceService;

public class ReputationManager implements IReputationManagementService, 
	ISecurityControllerModule, IEventListener {
	
	protected ConcurrentHashMap<String, ReputationEntity> reputationEntities;
    protected IStorageSourceService storageSource;

    public final String tableName = "reputations";
    private Map<String, String> tableColumns;
    protected final String primaryKeyName = "id";
    protected boolean useDB = false;
    
	protected static Logger log = LoggerFactory.getLogger(ReputationManager.class);
	protected IEventManagerService eventManager;
    protected IRestApiService restApi;
    protected IRegistryManagementService serviceRegistry;
    protected IReputationCalculator reputationCalculator;

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services = new ArrayList<Class<? extends ISecurityControllerService>>();
		services.add(IReputationManagementService.class);
		return services;
	}
	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = 
				new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IReputationManagementService.class, this);
		return m;
	}
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {		
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IStorageSourceService.class);
        l.add(IRestApiService.class);
        if(this.useDB)
        	l.add(IStorageSourceService.class);
		return l;
	}
	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
		this.reputationEntities = new ConcurrentHashMap <String, ReputationEntity> ();
        restApi = context.getServiceImpl(IRestApiService.class);
        if(this.useDB)
        	storageSource = context.getServiceImpl(IStorageSourceService.class, this);
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
		log.info("BUPT security controller reputation manager initialized.");
	}
	@Override
	public void startUp(SecurityControllerModuleContext context) {
		Map<String, String> configOptions = context.getConfigParams(this);
    	String strategy = configOptions.get("strategy");
        if(strategy == null)
            strategy = "ds";
        // register REST interface
        ReputationManagerRoutable r = new ReputationManagerRoutable();
        restApi.addRestletRoutable(r);
        serviceRegistry.registerService(r.basePath(), this);
        if(this.useDB){
        	if(strategy.equals("ds")){
        		tableColumns = new HashMap<String, String>() {
					private static final long serialVersionUID = 7759520520896391895L;
        			{
        				put("id", "VARCHAR(128)");
        				put("tableId", "Integer");
        			};
        		};
        	}
        	else if(strategy.equals("determined")){
        		tableColumns = new HashMap<String, String>() {
					private static final long serialVersionUID = 7759520510896391894L;

					{
        				put("id", "VARCHAR(128)");
        				put("tableId", "Integer");
        			};
        		};
        	}
        	else{
        		log.error("Unknown reputation strategy: "+ strategy);
        		System.exit(-1);
        	}
	        storageSource.createTable(tableName, tableColumns);
	        storageSource.setTablePrimaryKeyName(this.tableName, this.primaryKeyName);
        }
        
        if(strategy.equals("ds"))
        	this.reputationCalculator = DSReputationCaculator.getInstance();
        else if(strategy.equals("determined"))
            	this.reputationCalculator = DeterminedReputationCaculator.getInstance();
		log.info("BUPT security controller reputation manager started.");
	}
	@Override
	public ReputationEntity getReputationEntity(String id) {
		return reputationEntities.get(id);
	}

	@Override
	public void calculateReputationByReport(Report report){
		String id = report.getTargetId();
		ReputationEntity reputationEntity = null;
		if(!this.reputationEntities.containsKey(id)){
			reputationEntity = initReputationEntity(report.getTargetId(), report.getTargetType());
			this.reputationEntities.put(id, reputationEntity);
		}
		else
			reputationEntity = this.reputationEntities.get(id);
	}
	
	public boolean calculateEntityReputationByReport(ReputationEntity reputationEntity, Report report){
		ReputationLevel previousReputationLevel = reputationEntity.getReputationLevel();
		reputationEntity.updateReputation(report);
		ReputationLevel currentReputationLevel = reputationEntity.getReputationLevel();
		if(previousReputationLevel != currentReputationLevel)
			notifyReputationUpdate(reputationEntity, previousReputationLevel);
		
		return true;
	}
	
	public ReputationEntity initReputationEntity(String id, TargetType type){
		Object target = null;
		KnowledgeType knowledgeType = KnowledgeType.NETWORK_FLOW;
		if(type == TargetType.FLOW){
			target = getFlowEntity(id);
			knowledgeType = KnowledgeType.NETWORK_FLOW;
		}
		else if(type == TargetType.VM){
			target = getVMEntity(id);
			knowledgeType = KnowledgeType.CLOUD_VM;
		}
		else if(type == TargetType.USER){
			target = getUserEntity(id);
			knowledgeType = KnowledgeType.CLOUD_USER;
		}
		else if(type == TargetType.TENANT){
			target = getTenantEntity(id);
			knowledgeType = KnowledgeType.CLOUD_TENANT;
		}
		
		ReputationEntity reputationEntity = reputationCalculator.initReputationEntity(id, target, knowledgeType);
		return reputationEntity;		
	}
	
	

	//send a notification even old and new state are the same
	//private boolean sendUpdateEvenTrustNotChanged = false;	
	public void notifyReputationUpdate(ReputationEntity entity, ReputationLevel previousReputationLevel){
		ReputationLevel currentReputationLevel = entity.getReputationLevel();
		
		//所有信誉异常应该在这里计算，然后转到策略解析中进行控制
		if(currentReputationLevel== ReputationLevel.UNTRUSTED){//if an entity is not trusted anymore...
			if(entity.getType() == KnowledgeType.NETWORK_FLOW)
				notifyEntityReputationChanged(entity.getSourceObject(), EventType.MALICIOUS_FLOW_DETECTED);
			else if(entity.getType() == KnowledgeType.CLOUD_VM)
				notifyEntityReputationChanged(entity.getSourceObject(), EventType.MALICIOUS_VM_DETECTED);
		}
		if(currentReputationLevel == ReputationLevel.HIGHLY_TRUSTED){//if an entity is trusted again...
			if(entity.getType() == KnowledgeType.NETWORK_FLOW)
				notifyEntityReputationChanged(entity.getSourceObject(), EventType.MALICIOUS_FLOW_RESTORED);
			else if(entity.getType() == KnowledgeType.CLOUD_VM)
				notifyEntityReputationChanged(entity.getSourceObject(), EventType.MALICIOUS_VM_RESTORED);
		}
	}	
	
	
	private void notifyEntityReputationChanged(Object sourceObject, EventType eventType){
		//alert all other components which are interested 
		ReputationEventArgs args = new ReputationEventArgs(sourceObject);
		eventManager.addBroadcastEvent(new Event(eventType, null,
				this, args));
	}

	Object getFlowEntity(String id){
		Object[] args = {KnowledgeType.NETWORK_FLOW, KnowledgeEntityAttribute.ID, id};
		return queryEntity(args);
	}
	

	Object getVMEntity(String id){
		Object[] args = {KnowledgeType.CLOUD_VM, KnowledgeEntityAttribute.ID, id};
		return queryEntity(args);
	}
	

	Object getUserEntity(String id){
		Object[] args = {KnowledgeType.CLOUD_USER, KnowledgeEntityAttribute.ID, id};
		return queryEntity(args);
	}
	
	
	Object getTenantEntity(String id){
		Object[] args = {KnowledgeType.CLOUD_TENANT, KnowledgeEntityAttribute.ID, id};
		return queryEntity(args);
	}
	
	
	Object queryEntity(Object[] args){
		try{
			Class<IKnowledgeBaseService> serviceClass = com.sds.securitycontroller.knowledge.manager.IKnowledgeBaseService.class;	
			String methodName = "queryRelatedEntity";		
			Object result= eventManager.makeRPCCall(serviceClass, methodName, args);
			return result;
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		} 
	}
	
	
	CloudTenant getTenantByUser(CloudUser user){
		try{
			Class<IKnowledgeBaseService> serviceClass = com.sds.securitycontroller.knowledge.manager.IKnowledgeBaseService.class;	
			String methodName = "queryRelatedEntity";	
			Object[] args = {KnowledgeType.CLOUD_USER, KnowledgeEntityAttribute.ID, user.getId(), KnowledgeType.CLOUD_TENANT};	
			CloudTenant tenant= (CloudTenant) eventManager.makeRPCCall(serviceClass, methodName, args);
			 return tenant;		}

		catch (Exception e){
			e.printStackTrace();
			return null;
		} 
	}
	
	CloudUser getUserByVM(CloudVM vm){
		try{
			Class<IKnowledgeBaseService> serviceClass = com.sds.securitycontroller.knowledge.manager.IKnowledgeBaseService.class;	
			String methodName = "queryRelatedEntity";	
			Object[] args = {KnowledgeType.CLOUD_VM, KnowledgeEntityAttribute.ID, vm.getId(), KnowledgeType.CLOUD_USER};	
			CloudUser user= (CloudUser) eventManager.makeRPCCall(serviceClass, methodName, args);
			 return user;
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		} 
	}

	CloudVM getSrcVMByFlow(FlowInfo flow){
		try{
			Class<IKnowledgeBaseService> serviceClass = com.sds.securitycontroller.knowledge.manager.IKnowledgeBaseService.class;	
			String methodName = "queryEntity";

	    	Set<String> set = new HashSet<String>();
	    	set.add("dataLayerSource");
	    	set.add("dataLayerDestination");
	    	set.add("dataLayerVirtualLan");
	    	set.add("dataLayerType");
	    	set.add("networkTypeOfService");
	    	set.add("networkProtocol");
	    	set.add("networkSource");
	    	set.add("networkDestination");
	    	set.add("transportSource");
	    	set.add("transportDestination");
	    	set.add("networkDestinationMaskLen");
	    	set.add("networkSourceMaskLen");
	    	
			Object[] args = {KnowledgeType.NETWORK_FLOW, KnowledgeEntityAttribute.MATCH, flow.getMatch().toString(), KnowledgeType.CLOUD_VM};	
			CloudVM vm= (CloudVM) eventManager.makeRPCCall(serviceClass, methodName, args);
			return vm;
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	

	@Override
	public void processEvent(Event e) {
		EventType type = e.type;
		if(type == EventType.RECEIVED_ALERT){//get a report alert
			log.debug("start to process RECEIVED_REPORT event.");
			ReportEventArgs args = (ReportEventArgs)e.args;	
			Report report = args.report;
			calculateReputationByReport(report);
			//TODO execute 
			
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
}
