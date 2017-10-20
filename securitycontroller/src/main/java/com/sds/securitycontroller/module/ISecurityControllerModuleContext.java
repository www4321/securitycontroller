/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.module;

import java.util.Collection;
import java.util.Map;


public interface ISecurityControllerModuleContext {


    /**
     * Retrieves a casted version of a module from the registry.
     * @param name The ISecurityControllerService object type
     * @return The ISecurityControllerService
     * @throws SecurityControllerModuleException If the module was not found 
     * or a ClassCastException was encountered.
     */
    public <T extends ISecurityControllerService> T getServiceImpl(Class<T> service);
	public <T extends ISecurityControllerService> T getServiceImpl(Class<T> service, Object caller);
    public <T extends ISecurityControllerService> T getServiceImpl(Class<T> service, Object caller, String identifier);
    
    /**
     * Returns all loaded services
     * @return A collection of service classes that have been loaded
     */
    public Collection<Class<? extends ISecurityControllerService>> getAllServices();
    
    /**
     * Returns all loaded modules
     * @return All SecurityController modules that are going to be loaded
     */
    public Collection<ISecurityControllerModule> getAllModules();
    
    /**
     * Gets module specific configuration parameters.
     * @param module The module to get the configuration parameters for
     * @return A key, value map of the configuration options
     */
    public Map<String, String> getConfigParams(ISecurityControllerModule module);
}

