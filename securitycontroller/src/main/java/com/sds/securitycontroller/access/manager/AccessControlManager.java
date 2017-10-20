/** 
 *    Copyright 2014 BUPT.   
 *	  
 **/
package com.sds.securitycontroller.access.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.access.radac.RAdACManager;
import com.sds.securitycontroller.common.Entity;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;

public class AccessControlManager implements IAccessControlManagementService,
		ISecurityControllerModule {

	protected IRestApiService restApi;
	protected IEventManagerService eventScheduler;
	protected IStorageSourceService storageSource;
	protected IRegistryManagementService serviceRegistry;
	protected String strategy = "radac";
	protected static Logger log = LoggerFactory
			.getLogger(AccessControlManager.class);
	protected SecurityControllerModuleContext moduleContext;
	protected List<Policy> policies;


	String tableName = "acls";
	protected final String primaryKeyName = "id";
	private Map<String, String> tableColumns = new HashMap<String, String>(){
		private static final long serialVersionUID = 12222111L;
		{
			put("id", "VARCHAR(128)");
			put("sbjexpr", "VARCHAR(1024)");
			put("objexpr", "VARCHAR(1024)");
			put("attrs", "VARCHAR(4096)");
			put("result", "VARCHAR(20)");
			put("type", "VARCHAR(20)");
		}
	};
	
	
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services = new ArrayList<Class<? extends ISecurityControllerService>>();
		services.add(IAccessControlManagementService.class);
		return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IAccessControlManagementService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IStorageSourceService.class);
		l.add(IRestApiService.class);
		l.add(IStorageSourceService.class);
		return l;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		this.eventScheduler = context.getServiceImpl(
				IEventManagerService.class, this);
		this.restApi = context.getServiceImpl(IRestApiService.class, this);
		this.storageSource = context.getServiceImpl(
				IStorageSourceService.class, this);
		this.serviceRegistry = context.getServiceImpl(
				IRegistryManagementService.class, this);
		this.moduleContext = context;
		this.policies = new ArrayList<Policy>();
		log.info("BUPT security controller Access Control Manager initialized.");
	}

	@Override
	@SuppressWarnings("unchecked")
	public void startUp(SecurityControllerModuleContext context) {
		// register REST interface
		AccessControlManageRoutable r = new AccessControlManageRoutable();
		restApi.addRestletRoutable(r);
		Map<String, String> configOptions = context.getConfigParams(this);
		this.strategy = configOptions.get("strategy");
		
		serviceRegistry.registerService(r.basePath(), this);
		storageSource.createTable(tableName, tableColumns);
		storageSource.setTablePrimaryKeyName(this.tableName,
				this.primaryKeyName);

		// load policies
		QueryClause query = new QueryClause("type", "ACCESS", this.tableName, null, null);
		List<AccessControlPolicy> accessPolicies= (List<AccessControlPolicy>)storageSource.executeQuery(query, AccessControlPolicy.class);
		this.policies.addAll(accessPolicies);
		
		query = new QueryClause("type", "RISK", this.tableName, null, null);
		List<AccessControlPolicy> riskPolicies= (List<AccessControlPolicy>)storageSource.executeQuery(query, AccessControlPolicy.class);
		this.policies.addAll(riskPolicies);
		
		log.info("BUPT security controller Access Control Manager started.");
	}

	@Override
	public boolean allowAccess(Entity subject, Entity object,
			SubjectOperation operation, AccessControlContext context) {
		if (this.strategy.equals("radac")) {
			RAdACManager manager = RAdACManager.getInstance(this.moduleContext,
					this.policies);
			try {
				return manager.allowAccess(subject, object, operation, context);

			} catch (Exception e) {
				log.error("allow access calculation error: {}", e.toString());
			}
		}
		return false;
	}

	@Override
	public Policy getACLPolicy(String policyId){
		Policy policy = (Policy)this.storageSource.getEntity(tableName, policyId, Policy.class);
		return policy;
	}
	

	@Override
	public List<Policy> getACLPolicies(){
		@SuppressWarnings("unchecked")
		List<Policy> policies = (List<Policy>)this.storageSource.executeQuery(new QueryClause(this.tableName), Policy.class);
		return policies;
	}
	

	@Override
	public boolean updateACLPolicy(Policy policy){
		int n = this.storageSource.updateOrInsertEntity(this.tableName, policy);
		return (n>0)?true:false;
	}
	
	
	@Override
	public boolean addACLPolicy(Policy policy){
		int n = this.storageSource.insertEntity(this.tableName, policy);
		return (n>0)?true:false;
	}
	

	@Override
	public boolean removeACLPolicy(String policyId){
		int n = this.storageSource.deleteEntity(this.tableName, policyId);
		return (n>0)?true:false;
	}
}
