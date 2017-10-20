/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns list of modules loaded by Floodlight.
 * @author Rob Sherwood
 */
public class ModuleLoaderResource extends ServerResource {
    protected static Logger log = 
            LoggerFactory.getLogger(ModuleLoaderResource.class);
    
    /**
     * Retrieves information about loaded modules.
     * @return Information about loaded modules.
     */
    @Get("json")
    public Map<String, Object> retrieve() {
    	return retrieveInternal(false);
    }
    
    /**
     * Retrieves all modules and their dependencies available
     * to Floodlight.
     * @param loadedOnly Whether to return all modules available or only the ones loaded.
     * @return Information about modules available or loaded.
     */
    public Map<String, Object> retrieveInternal(boolean loadedOnly) {    
        Map<String, Object> model = new HashMap<String, Object>();

        Set<String> loadedModules = new HashSet<String>();
        for (Object val : getContext().getAttributes().values()) {
        	if ((val instanceof ISecurityControllerModule) || (val instanceof ISecurityControllerService)) {
        		String serviceImpl = val.getClass().getCanonicalName();
        		loadedModules.add(serviceImpl);
        		// log.debug("Tracking serviceImpl " + serviceImpl);
        	}
        }

        for (String moduleName : 
        	SecurityControllerModuleLoader.moduleNameMap.keySet() ) {
        	Map<String,Object> moduleInfo = new HashMap<String, Object>();

        	ISecurityControllerModule module = 
        			SecurityControllerModuleLoader.moduleNameMap.get(
        						moduleName);
        		
        	Collection<Class<? extends ISecurityControllerService>> deps = 
        			module.getModuleDependencies();
        	if ( deps == null)
            	deps = new HashSet<Class<? extends ISecurityControllerService>>();
        	Map<String,Object> depsMap = new HashMap<String, Object> ();
        	for (Class<? extends ISecurityControllerService> service : deps) {
        		Object serviceImpl = getContext().getAttributes().get(service.getCanonicalName());
        		if (serviceImpl != null)
        			depsMap.put(service.getCanonicalName(), serviceImpl.getClass().getCanonicalName());
        		else
        			depsMap.put(service.getCanonicalName(), "<unresolved>");

        	}
            moduleInfo.put("depends", depsMap);
        	
            Collection<Class<? extends ISecurityControllerService>> provides = 
            		module.getModuleServices();
        	if ( provides == null)
            	provides = new HashSet<Class<? extends ISecurityControllerService>>();
        	Map<String,Object> providesMap = new HashMap<String,Object>();
        	for (Class<? extends ISecurityControllerService> service : provides) {
        		providesMap.put(service.getCanonicalName(), module.getServiceImpls().get(service).getClass().getCanonicalName());
        	}
        	moduleInfo.put("provides", providesMap);            		

    		moduleInfo.put("loaded", false);	// not loaded, by default

        	// check if this module is loaded directly
        	if (loadedModules.contains(module.getClass().getCanonicalName())) {
        		moduleInfo.put("loaded", true);  			
        	} else {
        		// if not, then maybe one of the services it exports is loaded
        		for (Class<? extends ISecurityControllerService> service : provides) {
        			String modString = module.getServiceImpls().get(service).getClass().getCanonicalName();
        			if (loadedModules.contains(modString))
                		moduleInfo.put("loaded", true);
        			/* else 
        				log.debug("ServiceImpl not loaded " + modString); */
        		}
        	}

        	if ((Boolean)moduleInfo.get("loaded")|| !loadedOnly )
        		model.put(moduleName, moduleInfo);
        }            
        return model;
    }
}
