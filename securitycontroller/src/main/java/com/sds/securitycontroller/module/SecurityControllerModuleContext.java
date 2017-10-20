/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The service registry for an ISecurityControllerProvider.
 * @author alexreimers
 */
public class SecurityControllerModuleContext implements ISecurityControllerModuleContext/*, Serializable */{

	protected Map<Class<? extends ISecurityControllerService>, List<ISecurityControllerService>> serviceMap;
	protected Map<Class<? extends ISecurityControllerModule>, Map<String, String>> configParams;
	protected Map<String, Map<String, String>> generalConfigParams;//2014-03-19
	
	protected Collection<ISecurityControllerModule> moduleSet;

    protected static Logger logger = LoggerFactory.getLogger(SecurityControllerModuleContext.class);
	
	/**
	 * Creates the ModuleContext for use with this ISecurityControllerProvider.
	 * This will be used as a module registry for all ISecurityControllerModule(s).
	 */
	public SecurityControllerModuleContext() {
		serviceMap = 
		        new HashMap<Class<? extends ISecurityControllerService>,
		        List<ISecurityControllerService>>();
		configParams =
		        new HashMap<Class<? extends ISecurityControllerModule>,
		                        Map<String, String>>();
		generalConfigParams = new HashMap<String, Map<String, String>>();
	}
	
	/**
	 * Adds a ISecurityControllerModule for this Context.
	 * @param clazz the service class
	 * @param service The ISecurityControllerService to add to the registry
	 */
	public void addService(Class<? extends ISecurityControllerService> clazz, 
	                       ISecurityControllerService service) {
		List<ISecurityControllerService> services = serviceMap.get(clazz); 
		if(services == null){
			services = new ArrayList<ISecurityControllerService>();
			serviceMap.put(clazz, services);
		}
		services.add(service);
	}

    @Override
	public <T extends ISecurityControllerService> T getServiceImpl(Class<T> service){
    	return getServiceImpl(service, null);
    }
	
    @Override
	public <T extends ISecurityControllerService> T getServiceImpl(Class<T> service, Object caller){
    	if(service.toString().indexOf("Storage")>=0)
    		return getServiceImpl(service, caller, "dbdriver");
    	else
    		return getServiceImpl(service, caller, null);
    }
    
	@SuppressWarnings("unchecked")
    @Override
	public <T extends ISecurityControllerService> T getServiceImpl(Class<T> service,
			Object caller, String identifier){
		List<ISecurityControllerService> services = serviceMap.get(service);
		if(services == null)
			return null;
		
	    	    
	    if(services.size()>1){
		    //multiple implementations..
	    	//对于一个接口有多个实现，返回用户定义的模块
	    	if(caller instanceof SecurityControllerModuleLoader){
	    		//first loading
	    		return null;
	    	}
	    	else{
		    	ISecurityControllerModule callerModule = (caller instanceof ISecurityControllerModule)?(ISecurityControllerModule)caller:null;

		    	if(callerModule != null){
				    Map<String, String> configs = getConfigParams(callerModule);
				    if(configs.containsKey(identifier)){
				    	for(ISecurityControllerService imservice: services){
				    		if(imservice.getClass().getCanonicalName().equals(configs.get(identifier))){
				    			return (T)imservice;
				    		}
				    	}
				    }
		    	}
	
			    ISecurityControllerService defaultService = services.get(0);
			    logger.warn("Cannot find dbdriver for "
		                + caller + ", use default storage driver: "+defaultService.getClass().getCanonicalName());
			    return (T)defaultService;
	    	}
		    
	    }
	    else{
	    	//单个实现，返回一个
			return (T)services.get(0);
	    }
	}
	
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getAllServices() {
	    return serviceMap.keySet();
	}
	
	@Override
	public Collection<ISecurityControllerModule> getAllModules() {
	    return moduleSet;
	}
	
	public void setModuleSet(Collection<ISecurityControllerModule> modSet) {
	    this.moduleSet = modSet;
	}
	
	/**
	 * Gets the configuration parameter map for a module
	 * @param module The module to get the configuration map for, usually yourself
	 * @return A map containing all the configuration parameters for the module, may be empty
	 */
	@Override
	public Map<String, String> getConfigParams(ISecurityControllerModule module) {
	    Map<String, String> retMap = configParams.get(module.getClass());
	    if (retMap == null) {
	        // Return an empty map if none exists so the module does not
	        // need to null check the map
	        retMap = new HashMap<String, String>();
	        configParams.put(module.getClass(), retMap);
	    }

	    // also add any configuration parameters for superclasses, but
	    // only if more specific configuration does not override it
	    for (Class<? extends ISecurityControllerModule> c : configParams.keySet()) {
	        if (c.isInstance(module)) {
	            for (Map.Entry<String, String> ent : configParams.get(c).entrySet()) {
	                if (!retMap.containsKey(ent.getKey())) {
	                    retMap.put(ent.getKey(), ent.getValue());
	                }
	            }
	        }
	    }

	    return retMap;
	}
	
	public Map<String, String> getGeneralConfigParams(String namespace){
		Map<String, String> retMap = generalConfigParams.get(namespace);
		if (retMap == null) {
	        // Return an empty map if none exists so the module does not
	        // need to null check the map
	        retMap = new HashMap<String, String>();
	        generalConfigParams.put(namespace, retMap);
	    }
		return retMap;
	}
	
	/**
	 * Adds a configuration parameter for a module
	 * @param mod The fully qualified module name to add the parameter to
	 * @param key The configuration parameter key
	 * @param value The configuration parameter value
	 */
	public void addConfigParam(ISecurityControllerModule mod, String key, String value) {
	    Map<String, String> moduleParams = configParams.get(mod.getClass());
	    if (moduleParams == null) {
	        moduleParams = new HashMap<String, String>();
	        configParams.put(mod.getClass(), moduleParams);
	    }
	    moduleParams.put(key, value);
	}
	
	public void addGeneralConfigParam(String namespace,String key,String value){
		Map<String, String> retMap= generalConfigParams.get(namespace);
		if(retMap==null){
			retMap = new HashMap<String,String>();
			generalConfigParams.put(namespace, retMap);
		}
		retMap.put(key, value);
	}
 }
