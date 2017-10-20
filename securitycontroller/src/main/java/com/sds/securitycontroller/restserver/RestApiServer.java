/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.restserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.service.StatusService;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;

public class RestApiServer
    implements ISecurityControllerModule, IRestApiService {
    protected static Logger logger = LoggerFactory.getLogger(RestApiServer.class);
    protected List<RestletRoutable> restlets;
    protected SecurityControllerModuleContext fmlContext;
    protected IRegistryManagementService serviceRegistry;
    protected int restPort = 8080;
    
    // ***********
    // Application
    // ***********
    
    protected class RestApplication extends Application {
        protected Context context;
        
        public RestApplication() {
            super(new Context());
            this.context = getContext();
        }
        
        @Override
        public Restlet createInboundRoot() {
            Router baseRouter = new Router(context);
            baseRouter.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
            for (RestletRoutable rr : restlets) {
                baseRouter.attach(rr.basePath(), rr.getRestlet(context));
            }

            Filter slashFilter = new Filter() {            
                @Override
                protected int beforeHandle(Request request, Response response) {
                    Reference ref = request.getResourceRef();
                    String originalPath = ref.getPath();
                    if (originalPath.contains("//"))
                    {
                        String newPath = originalPath.replaceAll("/+", "/");
                        ref.setPath(newPath);
                    }
                    //added by marvel to enable sc inform fetch
                    getMessageHeaders(response).add("Access-Control-Allow-Origin", "*");
                    return Filter.CONTINUE;
                }

            };
            slashFilter.setNext(baseRouter);
            
            return slashFilter;
        }
        

       @SuppressWarnings("unchecked")
       Series<Header> getMessageHeaders(Message message) {
           String HEADERS_KEY = "org.restlet.http.headers";
           ConcurrentMap<String, Object> attrs = message.getAttributes();
           Series<Header> headers = (Series<Header>) attrs.get(HEADERS_KEY);
           if (headers == null) {
               headers = new Series<Header>(Header.class);
               Series<Header> prev = (Series<Header>) 
                   attrs.putIfAbsent(HEADERS_KEY, headers);
               if (prev != null) { headers = prev; }
           }
           return headers;
       }
        public void run(SecurityControllerModuleContext fmlContext, int restPort) {
            setStatusService(new StatusService() {
                @Override
                public Representation getRepresentation(Status status,
                                                        Request request,
                                                        Response response) {
                    return new JacksonRepresentation<Status>(status);
                }                
            });
            
            // Add everything in the module context to the rest
            for (Class<? extends ISecurityControllerService> s : fmlContext.getAllServices()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Adding {} for service {} into context",
                                 s.getCanonicalName(), fmlContext.getServiceImpl(s));
                }
                context.getAttributes().put(s.getCanonicalName(), 
                                            fmlContext.getServiceImpl(s));
            }
            
            // Start listening for REST requests
            try {
                final Component component = new Component();
                component.getServers().add(Protocol.HTTP, restPort);
                component.getClients().add(Protocol.CLAP);
                component.getDefaultHost().attach(this);
                component.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    // ***************
    // IRestApiService
    // ***************
    
    @Override
    public void addRestletRoutable(RestletRoutable routable) {
        restlets.add(routable);
    }

    @Override
    public void run() {
        //if (logger.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            sb.append("REST API routables: ");
            for (RestletRoutable routable : restlets) {
                sb.append(routable.getClass().getSimpleName());
                sb.append(" (");
                sb.append(routable.basePath());
                sb.append("), ");
            }
            logger.debug(sb.toString());
        //}
        
        RestApplication restApp = new RestApplication();
        restApp.getMetadataService().setDefaultCharacterSet(CharacterSet.UTF_8);
        restApp.run(fmlContext, restPort);
    }
    
    // *****************
    // ISecurityControllerModule
    // *****************
    
    @Override
    public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
        Collection<Class<? extends ISecurityControllerService>> services =
                new ArrayList<Class<? extends ISecurityControllerService>>(1);
        services.add(IRestApiService.class);
        return services;
    }

    @Override
    public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService>
            getServiceImpls() {
        Map<Class<? extends ISecurityControllerService>,
        ISecurityControllerService> m = 
            new HashMap<Class<? extends ISecurityControllerService>,
                        ISecurityControllerService>();
        m.put(IRestApiService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
        // We don't have any
        return null;
    }

    @Override
    public void init(SecurityControllerModuleContext context)
            throws SecurityControllerModuleException {
        // This has to be done here since we don't know what order the
        // startUp methods will be called
        this.restlets = new ArrayList<RestletRoutable>();
        this.fmlContext = context;
        
        // read our config options
        Map<String, String> configOptions = context.getConfigParams(this);
        String port = configOptions.get("port");
        if (port != null) {
            restPort = Integer.parseInt(port);
        }
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
        logger.info("REST port set to {}", restPort);
    }

    @Override
    public void startUp(SecurityControllerModuleContext Context) {
        serviceRegistry.registerService("", this);
    }
}
