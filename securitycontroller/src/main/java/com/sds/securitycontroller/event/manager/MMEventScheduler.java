/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.command.ICommandPushService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.log.manager.ILogManagementService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerModuleContext;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.policy.resolver.IPolicyResolveService;

public class MMEventScheduler extends EventManager{

    protected static Logger log = LoggerFactory.getLogger(EventManager.class);

    protected BlockingQueue<Event> events;
    protected Event flowEvent = null;

    protected Map<EventType, Set<IEventListener>> eventListeners;

	private ISecurityControllerModuleContext context;
    
    public  MMEventScheduler(){  	
        this.events = new LinkedBlockingQueue<Event>();
        this.eventListeners = new HashMap<EventType, Set<IEventListener>>();
    }

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
        Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
        m.put(IEventManagerService.class, this);
        return m;
	}
	
	
    
    @Override
	public void addEventListener(EventType type, IEventListener listener){
    	if(!this.eventListeners.containsKey(type)){
    		this.eventListeners.put(type, new HashSet<IEventListener>());
    	}
    	this.eventListeners.get(type).add(listener);
    }
    

    @Override
	public void removeEventListener(EventType type, IEventListener listener){
    	if(this.eventListeners.containsKey(type)){
    		if(this.eventListeners.get(type).contains(listener))
    	    	this.eventListeners.get(type).remove(listener);
    		if(this.eventListeners.get(type).isEmpty())
    			this.eventListeners.remove(type);
    	}
    }
    
    //This method is used to add an event with its corresponding data to event queue
    @Override
	public void addEvent(Event e){
    	if(e.type == EventType.RETRIEVED_FLOW){
    		if(this.flowEvent != null){
    			this.events.remove(this.flowEvent);
    		}
			this.flowEvent = e;
    	}
		try {
			this.events.put(e);
		} catch (InterruptedException e1) {
			log.error("Failure adding update to queue", e1);
		}
    	
    }
    
    //This method is used to process an registered event from event queue
    public void processEvent(Event e){
    	EventType type = e.type;
    	if(!this.eventListeners.containsKey(type))
    		return;
    	Date now=new Date();
    	long delay=now.getTime()-e.args.getTime().getTime();
    	Set<IEventListener> listeners = this.eventListeners.get(type);
    	for(IEventListener listener : listeners){
        	log.info("Dispatching a {} event {} ms after its generation, to {}."
        			,e.type.toString(),delay,listener.getClass().toString());
        	listener.processEvent(e);
    	}
    }
    
    @Override
	public void start(){
    	while (true) {
            try {
                Event event = events.take();
                if(event.type == EventType.RETRIEVED_FLOW)
                	this.flowEvent = null;
                processEvent(event);
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                log.error("Exception in controller updates loop", e);
            }
        }
    }
    
    @Override
	public void initBuiltinListeners(ISecurityControllerModuleContext moduleContext ){
    	
        ICommandPushService commandPusher = moduleContext.getServiceImpl(ICommandPushService.class);
        if(commandPusher instanceof IEventListener)
        	addEventListener(EventType.PUSH_FLOW, (IEventListener)commandPusher);
        else
        	log.error(commandPusher+" does not implements IEventListener interface.");
        
        IPolicyResolveService policyResolver = moduleContext.getServiceImpl(IPolicyResolveService.class);
        if(policyResolver instanceof IEventListener)
        	addEventListener(EventType.RECEIVED_POLICY, (IEventListener)policyResolver);
        else
        	log.error(policyResolver+" does not implements IEventListener interface.");
        
        ILogManagementService logManager = moduleContext.getServiceImpl(ILogManagementService.class);
        if(logManager instanceof IEventListener)
        	addEventListener(EventType.RECEIVED_LOG,(IEventListener)logManager);
        else 
        	log.error(logManager+" does not implements IEventListener interface.");
    }

	@SuppressWarnings("unchecked")
	@Override
	public void addConditionToListener(EventSubscriptionInfo condition) {
		
		Class<? extends ISecurityControllerService> subscribedModuleClass = null;
		try {
			subscribedModuleClass = (Class<? extends ISecurityControllerService>) Class.forName(condition.getModule());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	ISecurityControllerModule subscribedModule = 
    			(ISecurityControllerModule) context.getServiceImpl(subscribedModuleClass);
    	
    	if(subscribedModule instanceof IEventListener){
    		IEventListener listener = (IEventListener)subscribedModule;
    		listener.processAddListenEventCondition(condition.getEventype(), condition);
    		this.addEventListener(condition.getEventype(), listener);
    	}        			
	}

	@Override
	public void accessContext(SecurityControllerModuleContext context) {
		this.context=context;
	}

	@Override
	public Object makeRPCCall(Class<?> serviceClass,String methodName,Object[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBroadcastEvent(Event e) {
		// TODO Auto-generated method stub
		
	}
}