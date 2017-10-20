package com.sds.securitycontroller.test1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;

public class EventSend implements IEventListener, ISecurityControllerModule ,IEventSendService{
	
	protected static Logger log = LoggerFactory.getLogger(EventSend.class);
	protected IEventManagerService eventManager;
	protected IRegistryManagementService serviceRegistry;
	protected IRestApiService restApi;
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services =
                new ArrayList<Class<? extends ISecurityControllerService>>(1);
        services.add(IEventSendService.class);
		return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		 Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
	        m.put(IEventSendService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		 Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		 l.add(IRestApiService.class);
	     return l;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
		this.restApi = context.getServiceImpl(IRestApiService.class);
		this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);
		
		
		log.info("BUPT security controller EventSend initialized.");

	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		EventSendRoutable r = new EventSendRoutable();
	     restApi.addRestletRoutable(r);
	     serviceRegistry.registerService(r.basePath(), this);
		 log.info("BUPT security controller EventSend started");

	}
	
	@Override
	public void sendEvent() {
		log.info("EventSend Module start to send an event to evnet scheduler...");
		String method="POST";
		String url="http://10.108.170.218:8080/wm/staticflowentrypusher/json";
		String flowcommand="{\"switch\":\"00:00:22:16:51:7c:b8:4a\","
				+ "\"name\":\"flow-mod-1\","
				+ "\"cookie\":\"0\","
				+ "\"priority\":\"32768\","
				+ "\"ingress-port\":\"1\","
				+ "\"active\":\"true\","
				+ "\"actions\":\"output=1\"}";
		FlowCommandEventArgs evertArgs =new FlowCommandEventArgs(method,url,flowcommand);
		log.info(flowcommand);
        eventManager.addEvent(new Event(EventType.FLOW_COMMAND,"'hello' comes from EventSend",this,evertArgs));//生成一个TEST类型的事件并将其它发送给调度器，“hello”充当事件简单参数
        
        
	}
	
	@Override
	public void processEvent(Event e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		// TODO Auto-generated method stub

	}
	
	
	
	//CommandPusher向网络控制器推送命令，使用参数测试
	
	public void testCommandPusher(){
		
		
		
		
		
		
		
		
		
		
	}
	
	
	
}
