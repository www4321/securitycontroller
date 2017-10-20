/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.storage.IStorageSourceService;

public class SecurityControllerModuleLoader {

	protected static Logger logger = 
            LoggerFactory.getLogger(SecurityControllerModuleLoader.class);

    protected static Map<Class<? extends ISecurityControllerService>,
                  Collection<ISecurityControllerModule>> serviceMap;
    protected static Map<ISecurityControllerModule,
                  Collection<Class<? extends 
                                   ISecurityControllerService>>> moduleServiceMap;
    protected static Map<String, ISecurityControllerModule> moduleNameMap;
    protected static Object lock = new Object();
    
    protected SecurityControllerModuleContext securityControllerModuleContext;
	
    public static final String COMPILED_CONF_FILE = 
            "securitycontroller.default.properties";
    public static final String SECURITYCONTROLLER_MODULES_KEY =
            "securitycontroller.modules";
    public static final String SECURITYCONTROLLER_GENERALPARAM_KEY =
            "securitycontroller.generalparam";
    
	public SecurityControllerModuleLoader() {
	    securityControllerModuleContext = new SecurityControllerModuleContext();
	}
	
	public static Map<String, ISecurityControllerModule> getmoduleNameMap(){
		return moduleNameMap;
	}
	
	public static ISecurityControllerModule getModule(String modulename){
		return moduleNameMap.get(modulename);
	}
	
	/**
	 * Finds all ISecurityControllerModule(s) in the classpath. It creates 3 Maps.
	 * serviceMap -> Maps a service to a module
	 * moduleServiceMap -> Maps a module to all the services it provides
	 * moduleNameMap -> Maps the string name to the module
	 * @throws SecurityControllerModuleException If two modules are specified in the configuration
	 * that provide the same service.
	 */
	protected static void findAllModules(Collection<String> mList) throws SecurityControllerModuleException {
	    synchronized (lock) {
	        if (serviceMap != null) return;
	        serviceMap =  new HashMap<Class<? extends ISecurityControllerService>, Collection<ISecurityControllerModule>>();
	        moduleServiceMap =  new HashMap<ISecurityControllerModule, Collection<Class<? extends ISecurityControllerService>>>();
	        moduleNameMap = new HashMap<String, ISecurityControllerModule>();
	        
	        // Get all the current modules in the classpath
	        ClassLoader cl = Thread.currentThread().getContextClassLoader();
	        ServiceLoader<ISecurityControllerModule> moduleLoader
	            = ServiceLoader.load(ISecurityControllerModule.class, cl);
	        // Iterate for each module, iterate through and add it's services
	        Iterator<ISecurityControllerModule> moduleIter = moduleLoader.iterator();
	        while (moduleIter.hasNext()) {
	        	ISecurityControllerModule m = null;
	        	try {
	        		m = moduleIter.next();
	        	} catch (ServiceConfigurationError sce) {
	        		sce.printStackTrace();
	        		logger.info("Could not load module, "+sce.getMessage());
	        		System.exit(-1);
	        	}
	            logger.info("Found module " + m.getClass().getName());

	            // Set up moduleNameMap
	            moduleNameMap.put(m.getClass().getCanonicalName(), m);

	            // Set up serviceMap
	            Collection<Class<? extends ISecurityControllerService>> modServices =
	                    m.getModuleServices();
	            if (modServices != null) {
	                moduleServiceMap.put(m, modServices);
	                for (Class<? extends ISecurityControllerService> s : modServices) {
	                    Collection<ISecurityControllerModule> mods = 
	                            serviceMap.get(s);
	                    if (mods == null) {
	                        mods = new ArrayList<ISecurityControllerModule>();
	                        serviceMap.put(s, mods);
	                    }
	                    mods.add(m);
	                    // Make sure they haven't specified duplicate modules in the config
	                    //ignore database services.
	                    if(m.getClass().getCanonicalName().contains("StorageSource"))
	                    	continue;
	                    int dupInConf = 0;
	                    for (ISecurityControllerModule cMod : mods) {
	                        if (mList.contains(cMod.getClass().getCanonicalName()))
	                            dupInConf += 1;
	                    }
	                    
	                    if (dupInConf > 1) {
	                        String duplicateMods = "";
                            for (ISecurityControllerModule mod : mods) {
                                duplicateMods += mod.getClass().getCanonicalName() + ", ";
                            }
	                        throw new SecurityControllerModuleException("ERROR! The configuraiton" +
	                                " file specifies more than one module that provides the service " +
	                                s.getCanonicalName() +". Please specify only ONE of the " +
	                                "following modules in the config file: " + duplicateMods);
	                    }
	                }
	            }
	        }
	    }
	}
	
	/**
	 * Loads the modules from a specified configuration file.
	 * @param fName The configuration file path
	 * @return An ISecurityControllerModuleContext with all the modules to be started
	 * @throws SecurityControllerModuleException
	 */
	public ISecurityControllerModuleContext loadModulesFromConfig(String fName) 
	        throws SecurityControllerModuleException {
	    Properties prop = new Properties();
	    
	    File f = new File(fName);
	    if (f.isFile()) {
            logger.info("Loading modules from file {}", fName);
            try {
                prop.load(new FileInputStream(fName));
            } catch (Exception e) {
                logger.error("Could not load module configuration file", e);
                System.exit(1);
            }
        } else {
            logger.info(fName);
            logger.info("Loading default modules");
            InputStream is = this.getClass().getClassLoader().
                                    getResourceAsStream(COMPILED_CONF_FILE);            
            try {
                prop.load(is);
            } catch (IOException e) {
                logger.error("Could not load default modules", e);
                System.exit(1);
            } catch (Exception e) {
	            logger.error("Could not load default configure file", e);
	            System.exit(1);
            }
        }
        
        String moduleList = prop.getProperty(SECURITYCONTROLLER_MODULES_KEY)
                                .replaceAll("\\s", "");
        Collection<String> configMods = new ArrayList<String>();
        configMods.addAll(Arrays.asList(moduleList.split(",")));
        return loadModulesFromList(configMods, prop);
	}
	
	/**
	 * Loads modules (and their dependencies) specified in the list
	 * @param mList The array of fully qualified module names
	 * @param ignoreList The list of SecurityController services NOT to 
	 * load modules for. Used for unit testing.
	 * @return The ModuleContext containing all the loaded modules
	 * @throws SecurityControllerModuleException
	 */
	protected ISecurityControllerModuleContext loadModulesFromList(Collection<String> configMods, Properties prop, 
			Collection<ISecurityControllerService> ignoreList) throws SecurityControllerModuleException {
		logger.debug("Starting module loader");
		if (logger.isDebugEnabled() && ignoreList != null)
			logger.debug("Not loading module services " + ignoreList.toString());

        findAllModules(configMods);
        
        Collection<ISecurityControllerModule> moduleSet = new ArrayList<ISecurityControllerModule>();
        Map<Class<? extends ISecurityControllerService>, ISecurityControllerModule> moduleMap =
                new HashMap<Class<? extends ISecurityControllerService>,
                            ISecurityControllerModule>();

        Queue<String> moduleQ = new LinkedList<String>();
        // Add the explicitly configured modules to the q
        moduleQ.addAll(configMods);
        Set<String> modsVisited = new HashSet<String>();
        
        while (!moduleQ.isEmpty()) {
            String moduleName = moduleQ.remove();
            if (modsVisited.contains(moduleName))
                continue;
            modsVisited.add(moduleName);
            ISecurityControllerModule module = moduleNameMap.get(moduleName);
            if (module == null) {
                throw new SecurityControllerModuleException("Module " + 
                        moduleName + " not found");
            }
            // If the module provies a service that is in the
            // services ignorelist don't load it.
            if ((ignoreList != null) && (module.getModuleServices() != null)) {
            	for (ISecurityControllerService ifs : ignoreList) {
            		for (Class<?> intsIgnore : ifs.getClass().getInterfaces()) {
            			//System.out.println(intsIgnore.getName());
        				// Check that the interface extends ISecurityControllerService
        				//if (intsIgnore.isAssignableFrom(ISecurityControllerService.class)) {
            			//System.out.println(module.getClass().getName());
    					if (intsIgnore.isAssignableFrom(module.getClass())) {
    						// We now ignore loading this module.
    						logger.debug("Not loading module " + 
    									 module.getClass().getCanonicalName() +
    									 " because interface " +
    									 intsIgnore.getCanonicalName() +
    									 " is in the ignore list.");
    						
    						continue;
    					}
        				//}
            		}
            	}
            }
            
            // Add the module to be loaded
            addModule(moduleMap, moduleSet, module);
            // Add it's dep's to the queue
            Collection<Class<? extends ISecurityControllerService>> deps = 
                    module.getModuleDependencies();
            if (deps != null) {
                for (Class<? extends ISecurityControllerService> c : deps) {
                    ISecurityControllerModule m = moduleMap.get(c);
                    if (m == null) {
                        Collection<ISecurityControllerModule> mods = serviceMap.get(c);
                        // Make sure only one module is loaded
                        if ((mods == null) || (mods.size() == 0)) {
                            throw new SecurityControllerModuleException("ERROR! Could not " +
                                    "find an ISecurityControllerModule that provides service " +
                                    c.toString());
                        } else if (mods.size() == 1) {
                            ISecurityControllerModule mod = mods.iterator().next();
                            if (!modsVisited.contains(mod.getClass().getCanonicalName()))
                                moduleQ.add(mod.getClass().getCanonicalName());
                        } else {
                            boolean found = false;
                            for (ISecurityControllerModule moduleDep : mods) {
                                if (configMods.contains(moduleDep.getClass().getCanonicalName())) {
                                    // Module will be loaded, we can continue
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                String duplicateMods = "";
                                for (ISecurityControllerModule mod : mods) {
                                    duplicateMods += mod.getClass().getCanonicalName() + ", ";
                                }
                                throw new SecurityControllerModuleException("ERROR! Found more " + 
                                    "than one (" + mods.size() + ") ISecurityControllerModules that provides " +
                                    "service " + c.toString() + 
                                    ". Please specify one of the following modules in the config: " + 
                                    duplicateMods);
                            }
                        }
                    }
                }
            }
        }
        
        securityControllerModuleContext.setModuleSet(moduleSet);
        parseConfigParameters(prop);
        initModules(moduleSet);
        startupModules(moduleSet);
        
        return securityControllerModuleContext;
	}
	
	/**
	 * Loads modules (and their dependencies) specified in the list.
	 * @param configMods The collection of fully qualified module names to load.
	 * @param prop The list of properties that are configuration options.
	 * @return The ModuleContext containing all the loaded modules.
	 * @throws SecurityControllerModuleException
	 */
	public ISecurityControllerModuleContext loadModulesFromList(Collection<String> configMods, Properties prop) 
            throws SecurityControllerModuleException {
		return loadModulesFromList(configMods, prop, null);
    }
	
	/**
	 * Add a module to the set of modules to load and register its services
	 * @param moduleMap the module map
	 * @param moduleSet the module set
	 * @param module the module to add
	 */
	protected void addModule(Map<Class<? extends ISecurityControllerService>, 
                                           ISecurityControllerModule> moduleMap,
                            Collection<ISecurityControllerModule> moduleSet,
                            ISecurityControllerModule module) {
        if (!moduleSet.contains(module)) {
            Collection<Class<? extends ISecurityControllerService>> servs =
                    moduleServiceMap.get(module);
            if (servs != null) {
                for (Class<? extends ISecurityControllerService> c : servs)
                    moduleMap.put(c, module);
            }
            moduleSet.add(module);
        }
	}

    /**
     * Allocate  service implementations and then init all the modules
     * @param moduleSet The set of modules to call their init function on
     * @throws SecurityControllerModuleException If a module can not properly be loaded
     */
    protected void initModules(Collection<ISecurityControllerModule> moduleSet) 
                                           throws SecurityControllerModuleException {
        for (ISecurityControllerModule module : moduleSet) {            
            // Get the module's service instance(s)
            Map<Class<? extends ISecurityControllerService>, 
                ISecurityControllerService> simpls = module.getServiceImpls();

            // add its services to the context
            if (simpls != null) {
                for (Entry<Class<? extends ISecurityControllerService>, 
                        ISecurityControllerService> s : simpls.entrySet()) {
                    if (logger.isDebugEnabled()) {
                        logger.info("Setting " + s.getValue() + 
                                     "  as provider for " + 
                                     s.getKey().getCanonicalName());
                    }
                    if(s.getValue() instanceof IStorageSourceService){
                        securityControllerModuleContext.addService(s.getKey(),
                                s.getValue());                    	
                    }
                    else if (securityControllerModuleContext.getServiceImpl(s.getKey(), this) == null) {
                        securityControllerModuleContext.addService(s.getKey(),
                                                           s.getValue());
                    } else {
                        throw new SecurityControllerModuleException("Cannot set "
                                                            + s.getValue()
                                                            + " as the provider for "
                                                            + s.getKey().getCanonicalName()
                                                            + " because "
                                                            + securityControllerModuleContext.getServiceImpl(s.getKey(), null)
                                                            + " already provides it");
                    }
                }
            }
        }
        
        for (ISecurityControllerModule module : moduleSet) {
            // init the module
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing " + 
                             module.getClass().getCanonicalName());
            }
            module.init(securityControllerModuleContext);
        }
    }
    
    /**
     * Call each loaded module's startup method
     * @param moduleSet the module set to start up
     */
    protected void startupModules(Collection<ISecurityControllerModule> moduleSet) {
        for (ISecurityControllerModule m : moduleSet) {
            if (logger.isDebugEnabled()) {
                logger.debug("Starting " + m.getClass().getCanonicalName());
            }
            m.startUp(securityControllerModuleContext);
        }
    }
    
    /**
     * Parses configuration parameters for each module
     * @param prop The properties file to use
     */
    protected void parseConfigParameters(Properties prop) {
    	if (prop == null) return;
    	
        Enumeration<?> e = prop.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            // Ignore module list key
            if (key.equals(SECURITYCONTROLLER_MODULES_KEY)) {
                continue;
            }
            // add general config params
            if (key.contains(SECURITYCONTROLLER_GENERALPARAM_KEY)){
            	String configValue = null;  
                String configKey = key.substring(key.lastIndexOf(".") + 1);
                // Check to see if it's overridden on the command line
                String systemKey = System.getProperty(key);
                if (systemKey != null) {
                    configValue = systemKey;
                } else {
                    configValue = prop.getProperty(key);
                }
                securityControllerModuleContext.addGeneralConfigParam(
                		SECURITYCONTROLLER_GENERALPARAM_KEY, configKey, configValue);
                continue;
            }
            
            String configValue = null;
            int lastPeriod = key.lastIndexOf(".");
            String moduleName = key.substring(0, lastPeriod);
            String configKey = key.substring(lastPeriod + 1);
            // Check to see if it's overridden on the command line
            String systemKey = System.getProperty(key);
            if (systemKey != null) {
                configValue = systemKey;
            } else {
                configValue = prop.getProperty(key);
            }
            
            ISecurityControllerModule mod = moduleNameMap.get(moduleName);
            if (mod == null) {
                logger.warn("Module {} not found or loaded. " +
                		    "Not adding configuration option {} = {}", 
                            new Object[]{moduleName, configKey, configValue});
            } else {
                securityControllerModuleContext.addConfigParam(mod, configKey, configValue);
            }
            
        }
    }
}
