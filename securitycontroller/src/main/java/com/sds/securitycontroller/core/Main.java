/** 
*    Copyright 2014 SDS.   
*	 STRATEGY RESEARCH DEPT. 
**/ 
package com.sds.securitycontroller.core;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.module.ISecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.module.SecurityControllerModuleLoader;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.utils.CmdLineSettings;

/**
 * Host for the Floodlight main method
 * @author alexreimers
 */
public class Main {

    /**
     * Main method to load configuration and modules
     * @param args
     * @throws FloodlightModuleException 
     */
    public static void main(String[] args) throws SecurityControllerModuleException {
        // Setup logger
        System.setProperty("org.restlet.engine.loggerFacadeClass", 
                "org.restlet.ext.slf4j.Slf4jLoggerFacade");
        
        GlobalConfig config = GlobalConfig.getInstance();
        config.loadConfig("config/global.config");
        
        CmdLineSettings settings = new CmdLineSettings();
        CmdLineParser parser = new CmdLineParser(settings);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            parser.printUsage(System.out);
            System.exit(1);
        }
        
        // Load modules
        SecurityControllerModuleLoader loader = new SecurityControllerModuleLoader();
        ISecurityControllerModuleContext moduleContext = loader.loadModulesFromConfig(settings.getModuleFile());
        //xpn test
        /*System.out.println("all modules \n");
        Collection<ISecurityControllerModule> allModules = moduleContext.getAllModules();
        for (ISecurityControllerModule iSecurityControllerModule : allModules) {
			System.out.println(iSecurityControllerModule.getClass());
			Map<String, String> configParams = moduleContext.getConfigParams(iSecurityControllerModule);
			Set<String> keys = configParams.keySet();
			System.out.println("configParams: \n");
			for (String string : keys) {
				System.out.println(iSecurityControllerModule.getClass()+string+"="+configParams.get(string));
			}
			
		}
        System.out.println("all services \n");
        Collection<Class<? extends ISecurityControllerService>> allServices = moduleContext.getAllServices();
        for (Class<? extends ISecurityControllerService> class1 : allServices) {
			System.out.println(class1.getCanonicalName());
		}*/
        
        
        //Add built-in event listeners
        //IEventManagerService eventManager = EventManager.getInstance();
        //eventManager.initBuiltinListeners(moduleContext);
        
        
        // Run REST server
        IRestApiService restApi = moduleContext.getServiceImpl(IRestApiService.class);        
        restApi.run();
        // Run the main security controller module
        IEventManagerService scheduler = moduleContext.getServiceImpl(IEventManagerService.class);
        
        ISecurityControllerProviderService controller =
                moduleContext.getServiceImpl(ISecurityControllerProviderService.class);
        controller.setScheduler(scheduler);
        // This call blocks, it has to be the last line in the main
        controller.run();
    }
}
