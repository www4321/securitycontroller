/** 
 *    Copyright 2014 BUPT. 
 **/
package com.sds.securitycontroller.directory.registry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.directory.CommandEventArgs;
import com.sds.securitycontroller.directory.ModuleCommand;
import com.sds.securitycontroller.directory.ModuleCommandResponse;
import com.sds.securitycontroller.directory.ServiceEventArgs;
import com.sds.securitycontroller.directory.ServiceInfo;
import com.sds.securitycontroller.directory.ServiceInfo.ServiceStatus;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.event.manager.IRPCHandler;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IStorageSourceService;

public class RegistryManager implements IRegistryManagementService,
		ISecurityControllerModule, IEventListener {

	protected IRestApiService restApi;
	protected IEventManagerService eventManager;
	protected IRegistryManagementService serviceRegistry;

	protected static Logger log = LoggerFactory
			.getLogger(RegistryManager.class);
	SecurityControllerModuleContext context;
	protected Timer timer;
	protected int registerInterval = 10;

	// private Map<String, ServiceInfo> services = new HashMap<String,
	// ServiceInfo>();
	private Set<ServiceInfo> localNewServices = new HashSet<ServiceInfo>();
	private final String serviceNode = "/services";
	private final String commandNode = "/commands";
	ZookeeperHelper zkHelper;

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services = new ArrayList<Class<? extends ISecurityControllerService>>();
		services.add(IRegistryManagementService.class);
		return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IRegistryManagementService.class, this);
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
		this.context = context;
		Map<String, String> configOptions = context.getConfigParams(this);
		this.eventManager = context.getServiceImpl(IEventManagerService.class,
				this);
		restApi = context.getServiceImpl(IRestApiService.class);
		this.timer = new Timer();
		this.registerInterval = Integer.parseInt(configOptions
				.get("registerInterval"));
		this.serviceRegistry = context.getServiceImpl(
				IRegistryManagementService.class, this);
		log.info("BUPT registry manager initialized.");
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		this.eventManager.addEventListener(EventType.REG_SERVICE, this);
		this.eventManager.addEventListener(EventType.UNREG_SERVICE, this);
		this.eventManager.addEventListener(EventType.UNREG_COMMAND, this);

		zkHelper = ZookeeperHelper.getInstance();
		// create service entry
		try {
			zkHelper.writeZKNode(serviceNode, serviceNode,
					CreateMode.PERSISTENT);
			zkHelper.writeZKNode(commandNode, commandNode,
					CreateMode.PERSISTENT);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
			return;
		}

		// register REST interface
		RegistryManageRoutable r = new RegistryManageRoutable();
		restApi.addRestletRoutable(r);
		serviceRegistry.registerService(r.basePath(), this);
		log.info("BUPT registry manager started.");

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (localNewServices.size() == 0)
					return;
				ServiceInfo[] s = new ServiceInfo[localNewServices.size()];
				ServiceEventArgs args = new ServiceEventArgs(localNewServices
						.toArray(s));
				Event event = new Event(EventType.REG_SERVICE,
						RegistryManager.class, RegistryManager.class, args);
				eventManager.addBroadcastEvent(event);
				// clean
				localNewServices = new HashSet<ServiceInfo>();
			}
		}, 1000, registerInterval * 1000);
	}

	@Override
	public void regService(ServiceInfo service) {

		try {
			List<String> services = zkHelper.getZKChildren(serviceNode);
			for (String s : services) {
				if (s.equals(service.getId())) {
					return;
				}
			}
			String path = serviceNode + "/" + service.getId();
			log.info("New service " + service.getServiceName()
					+ " registed, path: " + path);
			zkHelper.writeZKNode(path, objTobytes(service),
					CreateMode.EPHEMERAL);
		} catch (KeeperException | InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	public ServiceInfo findService(String serviceName, String host) {
		String id = ServiceInfo.calulateId(serviceName, host);
		return findService(id);
	}

	@Override
	public List<ServiceInfo> getAllServices() {
		LinkedList<ServiceInfo> serviceInfos = new LinkedList<>();
		try {
			List<String> children = zkHelper.getZKChildren(serviceNode);
			for (String child : children) {
				String childPath = serviceNode + "/" + child;
				byte[] data = zkHelper.readZKNode(childPath);
				serviceInfos.add((ServiceInfo) bytesToObj(data));
			}
		} catch (KeeperException | InterruptedException
				| ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return serviceInfos;
	}

	@Override
	public ServiceInfo findService(String id) {
		try {
			byte[] bytes = zkHelper.readZKNode(serviceNode + "/" + id);
			if (bytes != null) {
				return (ServiceInfo) bytesToObj(bytes);
			} else {
				return null;
			}
		} catch (KeeperException | InterruptedException
				| ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] objTobytes(Object obj) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
		objStream.writeObject(obj);
		return byteStream.toByteArray();
	}

	public Object bytesToObj(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
		ObjectInputStream objStream = new ObjectInputStream(byteStream);
		return objStream.readObject();
	}

	@Override
	public void processEvent(Event e) {

		log.debug("received a '{}' event", e.type);
		if (e.type == EventType.REG_SERVICE) {
			try {
				ServiceEventArgs args = (ServiceEventArgs) (e.args);
				ServiceInfo[] remoteServices = args.services;
				for (ServiceInfo service : remoteServices) {
					regService(service);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (e.type == EventType.UNREG_SERVICE) {
			try {
				ServiceEventArgs args = (ServiceEventArgs) (e.args);
				String id = ServiceInfo.calulateId(args.serviceName, args.host);
				unregService(id);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (e.type == EventType.UNREG_COMMAND) {
			try {
				CommandEventArgs args = (CommandEventArgs) (e.args);
				unregCommand(args.module);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
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

	@Override
	public void unregService(String id) {
		try {
			zkHelper.deleteZKNode(serviceNode + "/" + id);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerService(String url,
			ISecurityControllerModule serviceModule) {
		String host = GlobalConfig.getInstance().hostName;
		ServiceInfo service = new ServiceInfo(serviceModule.getClass()
				.getCanonicalName(), host, url, ServiceStatus.LIVE);
		if (!localNewServices.contains(service))
			localNewServices.add(service);
	}

	@Override
	public void unregCommand(String module) {
		try {
			zkHelper.deleteZKNode(commandNode + "/" + module);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerCommand(ModuleCommand command,
			Class<? extends IRPCHandler> executor) {
		command.setModule(executor.getCanonicalName());
		try {
			String path = commandNode + "/" + command.getCommand();
			if (zkHelper.exist(path))
				return;

			log.info("New command " + command.getCommand()
					+ " registed, path: " + path);
			zkHelper.writeZKNode(path, objTobytes(command),
					CreateMode.EPHEMERAL);
		} catch (KeeperException | InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<ModuleCommand> getAllCommands() {
		LinkedList<ModuleCommand> commands = new LinkedList<>();
		try {
			List<String> children = zkHelper.getZKChildren(commandNode);
			for (String child : children) {
				String childPath = commandNode + "/" + child;
				byte[] data = zkHelper.readZKNode(childPath);
				commands.add((ModuleCommand) bytesToObj(data));
			}
		} catch (KeeperException | InterruptedException
				| ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return commands;
	}

	@Override
	public String executeCommand(ModuleCommandResponse resp) {
		try {
			String childPath = commandNode + "/" + resp.getCommand();
			byte[] data = zkHelper.readZKNode(childPath);
			if (data == null)
				return "command not found";
			ModuleCommand command = (ModuleCommand) bytesToObj(data);
			List<String> subcommands = command.getSubcommand();
			boolean found = false;
			for(String subcommand: subcommands){
				if(subcommand.equals(resp.getSubcommand()))
					found = true;
			}
			if(found == false)
				return "subcommand not found";
			Set<String> options = command.getOptions();
			for(Entry<String, String> entry: resp.getOptions().entrySet()){
				String opt = entry.getKey();
				if(!options.contains(opt)){
					return "unknown option: "+opt;
				}
			}
			
			String strExecutor = command.getModule();
			///ssd
			@SuppressWarnings("unchecked")
			Class<ISecurityControllerService> clsExecutor = (Class<ISecurityControllerService>) Class.forName(strExecutor);
			Object[] objs = new Object[1];
			objs[0] = resp;
			String result = (String)eventManager.makeRPCCall(clsExecutor, "executeCommand", objs);
			return result;
	        
			
		} catch (KeeperException | InterruptedException
				| ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return "error:"+e.toString();
		}
	}

}
