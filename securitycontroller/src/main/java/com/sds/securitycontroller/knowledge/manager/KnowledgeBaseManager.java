/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.manager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.device.manager.IDeviceManagementService;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.KnowledgeEventArgs;
import com.sds.securitycontroller.event.RequestEventArgs;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.flow.FlowEventArgs;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.knowledge.AuthenticationType;
import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.cloud.CloudNetwork;
import com.sds.securitycontroller.knowledge.cloud.CloudPort;
import com.sds.securitycontroller.knowledge.cloud.CloudRouter;
import com.sds.securitycontroller.knowledge.cloud.CloudSubnet;
import com.sds.securitycontroller.knowledge.cloud.CloudTenant;
import com.sds.securitycontroller.knowledge.cloud.CloudUser;
import com.sds.securitycontroller.knowledge.cloud.CloudVM;
import com.sds.securitycontroller.knowledge.cloud.LocalVlan;
import com.sds.securitycontroller.knowledge.cloud.LocalVnet;
import com.sds.securitycontroller.knowledge.cloud.VifPort;
import com.sds.securitycontroller.knowledge.cloud.CloudVM.AddressInfo;
import com.sds.securitycontroller.knowledge.cloud.CloudVM.NetworkInfo;
import com.sds.securitycontroller.knowledge.networkcontroller.AttachmentPoint;
import com.sds.securitycontroller.knowledge.networkcontroller.NetworkDeviceEntity;
import com.sds.securitycontroller.knowledge.networkcontroller.NetworkFlow;
import com.sds.securitycontroller.knowledge.networkcontroller.Switch;
import com.sds.securitycontroller.knowledge.security.SecurityDeviceEntity;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.utils.IPAddress;
import com.sds.securitycontroller.utils.MongoDBDriver;

public class KnowledgeBaseManager implements IKnowledgeBaseService,
		ISecurityControllerModule, IEventListener {

	protected static Logger log = LoggerFactory.getLogger(KnowledgeBaseManager.class);
	protected IEventManagerService eventManager;
    protected IRegistryManagementService serviceRegistry;
	
	static String mapCreatedMark = UUID.randomUUID().toString();
	
	protected AuthenticationType authenticationType = AuthenticationType.CLOUD;
	
	KnowledgeType[] entityTypes = {KnowledgeType.SECURITY_DEVICE,KnowledgeType.CLOUD_NETWORK,KnowledgeType.CLOUD_PORT,KnowledgeType.CLOUD_ROUTER,
			KnowledgeType.CLOUD_SUBNET,KnowledgeType.CLOUD_TENANT,KnowledgeType.CLOUD_USER,KnowledgeType.CLOUD_VM,
			KnowledgeType.NETWORK_CONTROLLER,KnowledgeType.NETWORK_DEVICE,KnowledgeType.NETWORK_SWITCH,KnowledgeType.NETWORK_TOPOLOGY,
			KnowledgeType.PHYSICAL_USER
			}; 
	
	
	public Map<KnowledgeType, Map<String, KnowledgeEntity>> knowledgeTypedEntityMap
        	= new HashMap<KnowledgeType, Map<String, KnowledgeEntity>>();

	Map<KnowledgeType, Boolean> retrievedEntityFlagsMap = new HashMap<KnowledgeType, Boolean>();
	
    protected IRestApiService restApi;

	Timer timer;
	final int POLLING_TASK_INTERVAL = 180;
	public Boolean mappingCreated = false;
	@Override
	public Object handleRPCRequest(String methodName,Object[] args){
		Object output = null;
		log.info(" into RPC handler");
		while(!mappingCreated){
			try{
				Thread.sleep(1000);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		System.err.println("create map status:"+mappingCreated);
		try {
			if( methodName.equals("queryrelatedentity") &&  args.length==4 ){
		    	//query
		    	KnowledgeType inputEntityType = (KnowledgeType)args[0];
		    	KnowledgeEntityAttribute inputAttrType = (KnowledgeEntityAttribute)args[1];
		    	String inputAttrValue = (String)args[2];
		    	KnowledgeType outputEntityType = (KnowledgeType)args[3];
		    	output= queryRelatedEntity(inputEntityType,
		    			inputAttrType,inputAttrValue, 
		    			outputEntityType);
		    }
		    else if( methodName.equals("queryentity") && args.length ==3){
		    	KnowledgeType inputEntityType = (KnowledgeType)args[0];
		    	KnowledgeEntityAttribute inputAttrType = (KnowledgeEntityAttribute)args[1];
		    	String inputAttrValue = (String)args[2];
		    	output= queryEntity(inputEntityType,
		    			inputAttrType,inputAttrValue);
		    }
		    else if( methodName.equals("isentityrelated") && args.length ==6){
		    	KnowledgeType typeA = (KnowledgeType)args[0];
		    	KnowledgeEntityAttribute attrTypeA = (KnowledgeEntityAttribute)args[1];
		    	String attrValueA = (String)args[2];
		    	KnowledgeType typeB = (KnowledgeType)args[3];
		    	KnowledgeEntityAttribute attrTypeB = (KnowledgeEntityAttribute)args[4];
		    	String attrValueB = (String)args[5];
		    	output= isEntitiesRelated(typeA, attrTypeA, attrValueA, 
		    			typeB, attrTypeB, attrValueB);
		    }
		    else if( methodName.equals("retrieveentitylist") && args.length ==1){
		    	KnowledgeType inputEntityType = (KnowledgeType)args[0];
		    	output= retrieveEntityList(inputEntityType);
		    }
		    else{
		    	log.error(" args error!");
		    }
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			output = null;
		}
		return output;
	}
	
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		 Collection<Class<? extends ISecurityControllerService>> services =
	                new ArrayList<Class<? extends ISecurityControllerService>>(1);
	        services.add(IKnowledgeBaseService.class);
	        return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
        // We are the class that implements the service
        m.put(IKnowledgeBaseService.class, this);
        return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
        //l.add(IStorageSourceService.class);
        l.add(IKnowledgeBaseService.class);
        l.add(IDeviceManagementService.class);
        return l;
	}
	
	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
        if(context != null)
            this.restApi = context.getServiceImpl(IRestApiService.class);

	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
        log.info("BUPT Knowledge Base initialized.");
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		KnowledgeBaseRoutable r = new KnowledgeBaseRoutable();
        restApi.addRestletRoutable(r);
        serviceRegistry.registerService(r.basePath(), this);
		eventManager.addEventListener(EventType.RETRIEVED_KNOWLEDGE, this);
		//polling knowledge
		timer = new Timer();
		timer.schedule(new TimerTask() {
				@Override
				public void run() {
					synchronized (mappingCreated) 
					{
						mappingCreated = false;
						// requestForKnowledge & create entity mapping
						requestForKnowledge();
						createMapping();
					}
				}
			}, 1000, POLLING_TASK_INTERVAL*1000);	
        log.info("BUPT Knowledge Base started...");

		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		KnowledgeBaseManager kbm = new KnowledgeBaseManager();
//		NetworkFlow flow = (NetworkFlow) kbm._queryEntity(KnowledgeType.FLOW,
//				KnowledgeEntityAttribute.MATCH, "match.dataLayerDestination=00:00:00:00:00:00;match.networkSource=0.0.0.0");
	}
	
	boolean hasRetrievedAllInfo(){
//		boolean retrievedAllInfo = true;
		for(int i=0;i<entityTypes.length;i++){
			if( this.knowledgeTypedEntityMap.get(entityTypes[i]) == null || retrievedEntityFlagsMap.get(entityTypes[i])==false)
				return false;
		}
		return true;
	}
	
	void createMapping(){
		//wait until retrieved all info
		int retryLimit = 10,idx=0;
		while(!hasRetrievedAllInfo() && idx++<retryLimit){
			try{
				Thread.sleep(1000);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		try{
			// based on ports, map other related entities 
			if( knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_PORT)!=null){
				for (KnowledgeEntity entityPort: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_PORT).values()){
					CloudPort port = (CloudPort)entityPort;
					// map to VM (by device id)
					if (port.isStateUp()){
						// active
						if( knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_VM)!=null){
							for(KnowledgeEntity entity: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_VM).values()){
								CloudVM vm = (CloudVM)entity;
								//using fixed_ip to do map 
								for(NetworkInfo nwif:vm.getNetworks().values()){
									String fixed_ip = "";
									for(AddressInfo adInfo: nwif.getAddresses()){
										if(adInfo.getIpsType().equals("fixed") ){
											fixed_ip = adInfo.getAddr();
											break;
										}
									}
									if(fixed_ip.equals(port.getIpaddr())){
										entityPort.relatedEntityMap.put(KnowledgeType.CLOUD_VM,entity);
										entity.relatedEntityMap.put(KnowledgeType.CLOUD_PORT,entityPort);
										break;
									}
								}
							}
						}
						else{
							log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_VM);
							break;
						}
					}
					// map to subnet
					String sbnID = port.getSubnet();
					if( knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_SUBNET)!=null){
							for(KnowledgeEntity entity: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_SUBNET).values()){
							CloudSubnet subnet = (CloudSubnet)entity;
							if (subnet.getId().equals(sbnID)){
								port.relatedEntityMap.put(KnowledgeType.CLOUD_SUBNET,subnet);
								subnet.affiliatedEntityListMap.get(KnowledgeType.CLOUD_PORT).put(port.getId(),port);
								break;
							}
						}
					}
					else
						log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_SUBNET);
					// map to tenant
					String tenantID = port.getTenantId();
					if( knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_TENANT)!=null){
						for(KnowledgeEntity entity: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_TENANT).values()){
							CloudTenant tenant = (CloudTenant)entity;
							if (tenant.getId().equals(tenantID)){
								port.relatedEntityMap.put(KnowledgeType.CLOUD_TENANT,tenant);
								tenant.affiliatedEntityListMap.get(KnowledgeType.CLOUD_PORT).put(port.getId(),port);
								break;
							}
						}
					}
					else
						log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_TENANT);
					// map to network 
					String nwID = port.getNetworkId();
					if( knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_NETWORK)!=null){
						for(KnowledgeEntity entity: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_NETWORK).values()){
							CloudNetwork nw = (CloudNetwork)entity;
							if (nw.getId().equals(nwID)){
//								port.network = nw;
								port.relatedEntityMap.put(KnowledgeType.CLOUD_NETWORK,nw);
								nw.affiliatedEntityListMap.get(KnowledgeType.CLOUD_PORT).put(port.getId(),port);
								break;
							}
						}
					}
					else
						log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_NETWORK);
					// map cloud port to network device
					String mac = port.getMac();
					if(knowledgeTypedEntityMap.get(KnowledgeType.NETWORK_DEVICE)!=null){
						for(KnowledgeEntity entity: knowledgeTypedEntityMap.get(KnowledgeType.NETWORK_DEVICE).values()){
							NetworkDeviceEntity netDevice = (NetworkDeviceEntity)entity;
							if(netDevice.getMacAddress().equals(mac)){
								port.relatedEntityMap.put(KnowledgeType.NETWORK_DEVICE,netDevice);
								netDevice.relatedEntityMap.put(KnowledgeType.CLOUD_PORT,port);
							}
						}
					}
					else{
						log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.NETWORK_DEVICE);
						break;
					}
				}
			}
			else
				log.warn("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_PORT);
			
			if(knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_VM)!=null){
				//map VM
				for(KnowledgeEntity entityVM: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_VM).values()){
					CloudVM vm = (CloudVM)entityVM;
					//map to user
					CloudUser user = (CloudUser) knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_USER).get(vm.getUserId());
					vm.relatedEntityMap.put(KnowledgeType.CLOUD_USER,user);
					user.affiliatedEntityListMap.get(KnowledgeType.CLOUD_VM).put(vm.getId(), vm);
					//map to subnet,network according to port
					CloudPort cloudPort = (CloudPort) vm.relatedEntityMap.get(KnowledgeType.CLOUD_PORT);
					if(cloudPort!=null){
						//network
						CloudNetwork cloudNetwork = (CloudNetwork) cloudPort.relatedEntityMap.get(KnowledgeType.CLOUD_NETWORK);
						if(cloudNetwork!=null){
							vm.relatedEntityMap.put(KnowledgeType.CLOUD_NETWORK, cloudNetwork);
							cloudNetwork.affiliatedEntityListMap.get(KnowledgeType.CLOUD_VM).put(vm.getId(),vm);
						}
						//subnet
						CloudSubnet subnet = (CloudSubnet) cloudPort.relatedEntityMap.get(KnowledgeType.CLOUD_SUBNET);
						if(subnet!=null){
							vm.relatedEntityMap.put(KnowledgeType.CLOUD_SUBNET, subnet);
							subnet.affiliatedEntityListMap.get(KnowledgeType.CLOUD_VM).put(vm.getId(), vm);
						}
					}
					//map to tenant
					CloudTenant tenant = (CloudTenant) knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_TENANT).get(vm.getTenantId());
					vm.relatedEntityMap.put(KnowledgeType.CLOUD_TENANT, tenant);
					tenant.affiliatedEntityListMap.get(KnowledgeType.CLOUD_VM).put(vm.getId(), vm);
				}
			}
			else
				log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_VM);

			if(knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_SUBNET)!=null){
				//map subnet
				for(KnowledgeEntity entitySubnet: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_SUBNET).values()){
					CloudSubnet subnet = (CloudSubnet)entitySubnet;
					// map to user
					for(KnowledgeEntity entity: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_USER).values()){
						CloudUser user = (CloudUser)entity;
						if(user.getSubnets().containsKey(subnet.getId())){
							user.relatedEntityMap.put(KnowledgeType.CLOUD_SUBNET,subnet);
							subnet.affiliatedEntityListMap.get(KnowledgeType.CLOUD_USER).put(user.getId(), user);
						}
					}
					// to nw 
					CloudNetwork network = (CloudNetwork)knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_NETWORK).get(subnet.getNetwork_id());
					subnet.relatedEntityMap.put(KnowledgeType.CLOUD_NETWORK,network);
					network.affiliatedEntityListMap.get(KnowledgeType.CLOUD_SUBNET).put(subnet.getId(), subnet);
					// to tenant
					CloudTenant tenant = (CloudTenant)knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_TENANT).get(subnet.getTenantId());
					if(tenant!=null){
						subnet.relatedEntityMap.put(KnowledgeType.CLOUD_TENANT,tenant);
						tenant.affiliatedEntityListMap.get(KnowledgeType.CLOUD_SUBNET).put(subnet.getId(), subnet);
					}
				}
			}
			else
				log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_SUBNET);

			if(knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_NETWORK)!=null){

				//map network
				for(KnowledgeEntity entityNetwork: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_NETWORK).values()){
					CloudNetwork network = (CloudNetwork)entityNetwork;
					// to tenant
					CloudTenant tenant = (CloudTenant)knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_TENANT).get(network.getTenant_id());
					if(tenant!=null){
						network.relatedEntityMap.put(KnowledgeType.CLOUD_TENANT,tenant);
						tenant.affiliatedEntityListMap.get(KnowledgeType.CLOUD_SUBNET).put(network.getId(), network);
					}
				}
			}
			else
				log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_NETWORK);
			
			if(knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_ROUTER)!=null){
				// map router to port and to network
				for(KnowledgeEntity entityRouter: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_ROUTER).values()){
					// according to cloud_router's port id list: mapping ports
					CloudRouter router=(CloudRouter)entityRouter;
					if(router.getPortIdList()!=null){
						for(String portID:router.getPortIdList()){
							//get port by id:
							CloudPort port= (CloudPort)knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_PORT).get(portID);
							if(port!=null){
								router.affiliatedEntityListMap.get(KnowledgeType.CLOUD_PORT).put(portID, port);
								//add network of the port to router's affiliated entities
								CloudNetwork cloudNetwork=(CloudNetwork) port.relatedEntityMap.get(KnowledgeType.CLOUD_NETWORK);
								if(cloudNetwork!=null){
									router.affiliatedEntityListMap.get(KnowledgeType.CLOUD_NETWORK).put(cloudNetwork.getId(), cloudNetwork);
									cloudNetwork.relatedEntityMap.put(KnowledgeType.CLOUD_ROUTER, router);
								}
							}
						}
					}
					// map to network according to external gateway info
					String networkID=router.getExternalGatewayInfo().getNetwork_id();
					if(networkID!=null && !networkID.isEmpty()){
						CloudNetwork cloudNetwork=(CloudNetwork)knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_NETWORK).get(networkID);
						if(cloudNetwork!=null){
							router.affiliatedEntityListMap.get(KnowledgeType.CLOUD_NETWORK).put(cloudNetwork.getId(), cloudNetwork);
							//cloudNetwork.affiliatedEntityListMap.get(KnowledgeType.CLOUD_ROUTER).put(router.getId(), router);
						}
					}
					// to tenant
					CloudTenant tenant = (CloudTenant)knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_TENANT).get(router.getTenant_id());
					if(tenant!=null){
						router.relatedEntityMap.put(KnowledgeType.CLOUD_TENANT,tenant);
						tenant.affiliatedEntityListMap.get(KnowledgeType.CLOUD_SUBNET).put(router.getId(), router);
					}
				}
			}
			else
				log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_ROUTER);
			
			
			if(knowledgeTypedEntityMap.get(KnowledgeType.NETWORK_DEVICE)!=null){
				// map network devices
				for(KnowledgeEntity entityDev: knowledgeTypedEntityMap.get(KnowledgeType.NETWORK_DEVICE).values()){
					NetworkDeviceEntity device = (NetworkDeviceEntity)entityDev;
					// to VM (according to port)
					CloudPort port = (CloudPort) device.relatedEntityMap.get(KnowledgeType.CLOUD_PORT);
					CloudVM vm = null;
					if(port!=null){
						vm = (CloudVM) port.relatedEntityMap.get(KnowledgeType.CLOUD_VM);
						if(vm!=null){
							vm.relatedEntityMap.put(KnowledgeType.NETWORK_DEVICE,device);
							device.relatedEntityMap.put(KnowledgeType.CLOUD_VM,vm);
						}
					}
					// to switch
					AttachmentPoint ap = device.getAttachmentPoint();
					if(ap==null)
						continue;
					String swDPID = ap.getSwitchDPID();
					Switch sw = (Switch) knowledgeTypedEntityMap.get(KnowledgeType.NETWORK_SWITCH).get(swDPID);
					device.relatedEntityMap.put(KnowledgeType.NETWORK_SWITCH,sw);
//					device.attachedSwitches.put( swDPID, this.switchMap.get(swDPID) );
					// vm -> dev -> switch
					if(vm!=null && sw!=null){
						sw.affiliatedEntityListMap.get(KnowledgeType.NETWORK_DEVICE).put(device.getMacAddress(), device);
						vm.relatedEntityMap.put(KnowledgeType.NETWORK_SWITCH,sw);
						sw.affiliatedEntityListMap.get(KnowledgeType.CLOUD_VM).put(vm.getId(),vm);
					}
				}
			}
			else
				log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.NETWORK_DEVICE);
			
//			//  NC Switch 
//			for(KnowledgeEntity entitySW: knowledgeTypedEntityMap.get(KnowledgeType.SWITCH).values()){
//				Switch sw = (Switch)entitySW;
//				for(SwitchPort swPort :sw.ports){
//					sw.affiliatedEntityListMap.get(KnowledgeType.SWITCH_PORT).put(swPort.getId(),swPort);
//				}
//			}
			if(knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_USER)!=null){
				// cloud user
				for(KnowledgeEntity entityUser: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_USER).values()){
					CloudUser user = (CloudUser)entityUser;
					//to tenant
					if(!( user.getTenantId() ==null || user.getTenantId().equals(""))){
						CloudTenant tenant = (CloudTenant) knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_TENANT).get(user.getTenantId());
						user.relatedEntityMap.put(KnowledgeType.CLOUD_TENANT,tenant);
						tenant.affiliatedEntityListMap.get(KnowledgeType.CLOUD_USER).put(user.getId(), user);
					}
					//to subnet
//					CloudSubnet subnet = (CloudSubnet) knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_SUBNET).get();
					for(KnowledgeEntity entity:user.getSubnets().values()){
						CloudSubnet subnet = (CloudSubnet)entity;
						user.affiliatedEntityListMap.get(KnowledgeType.CLOUD_SUBNET).put(subnet.getId(), subnet);
						subnet.affiliatedEntityListMap.get(KnowledgeType.CLOUD_USER).put(user.getId(), user);
					};
				}
			}
			else
				log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_USER);
			
			//20140701: physical user
			/*
			for(KnowledgeEntity entityUser: knowledgeTypedEntityMap.get(KnowledgeType.PHYSICAL_USER).values()){
				PhysicalUser user=(PhysicalUser)entityUser;
				for(String mac: user.getBindingMacList()){
					PhysicalDevice physicalDevice=new PhysicalDevice(mac,null);
					user.affiliatedEntityListMap.get(KnowledgeType.PHYSICAL_DEVICE).put(mac, physicalDevice);
				}
			}*/
			if(knowledgeTypedEntityMap.get(KnowledgeType.SECURITY_DEVICE)!=null){
				// security device to subnet
				for(KnowledgeEntity entitySecDev: knowledgeTypedEntityMap.get(KnowledgeType.SECURITY_DEVICE).values()){
					if(knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_SUBNET)!=null){
						//for each subnet, check cidr
						for(KnowledgeEntity entitySubnet: knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_SUBNET).values()){
							try {
								
								CloudSubnet subnet = (CloudSubnet) entitySubnet;
								String[] cidrSegs = subnet.getCidr().split("/");
								String subnetMask=cidrSegs[0];
								int masklen=Integer.valueOf(cidrSegs[1]);
								String devIP=((SecurityDeviceEntity)entitySecDev).getIp();
								if(IPAddress.isIpInSubnet(subnetMask, devIP, masklen)){
									entitySecDev.relatedEntityMap.put(KnowledgeType.CLOUD_SUBNET,subnet);
									subnet.affiliatedEntityListMap.get(KnowledgeType.SECURITY_DEVICE).
										put(entitySecDev.getId(),entitySecDev);
								}
							} catch (Exception e) {
//								e.printStackTrace();
								log.error(e.getMessage());
							}
						}
					}
					else{
						log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.CLOUD_SUBNET);
						break;
					}
				}
			}
			else
				log.debug("No '{}' entities found when creating entity relation mapping",KnowledgeType.SECURITY_DEVICE);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		mappingCreated = true;
		return;
	}

	 
	void processVnetMapping(Map<String, KnowledgeEntity> vnetMap){
		for(KnowledgeEntity entity:vnetMap.values()){
			try{
				LocalVnet vnet = (LocalVnet)entity;
				
				CloudNetwork nw = (CloudNetwork) knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_NETWORK)
						.get(vnet.getNet_Uuid());
				//map vlan's vifports to cloud ports
				for(LocalVlan vlan:vnet.getLocalVlanMap().values()){
					//attach vlan_map to network
					if(nw!=null){
						if(nw.affiliatedEntityListMap.get(KnowledgeType.CLOUD_VLAN) ==null)
							nw.affiliatedEntityListMap.put(KnowledgeType.CLOUD_VLAN,new HashMap<String, KnowledgeEntity>());
						nw.affiliatedEntityListMap.get(KnowledgeType.CLOUD_VLAN).put(vlan.getHost(), vlan);
					}
					for(VifPort vifPort:vlan.getVifPorts().values()){
						CloudPort port = (CloudPort) knowledgeTypedEntityMap.get(KnowledgeType.CLOUD_PORT).get(vifPort.getVifMac());
						if(port!=null){
							vifPort.relatedEntityMap.put(KnowledgeType.CLOUD_PORT, port);
							port.relatedEntityMap.put(KnowledgeType.CLOUD_VIFPORT, vifPort);
							break;
						}
					}
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void processEvent(Event e) {
		// Auto-generated method stub
		try{
			switch(e.type){
			case RETRIEVED_KNOWLEDGE:
				KnowledgeEventArgs args = (KnowledgeEventArgs)e.args;
				KnowledgeType knowledgeType = args.entityType;
				Map<String, KnowledgeEntity> knowledgeEntityMap = (Map<String, KnowledgeEntity>)(args.knowledgeObj);
				switch(knowledgeType){
				case NETWORK_FLOW:
				case NETWORK_FLOW_TABLE:
					break;
				case CLOUD_VLAN_MAP:
					processVnetMapping(knowledgeEntityMap);
					break;
				default:
					knowledgeTypedEntityMap.put(knowledgeType,knowledgeEntityMap);
					retrievedEntityFlagsMap.put(knowledgeType, true);
					break;
				}
				log.debug("[{}] type knowledge entity retrieved.",knowledgeType);
				break;
			case RETRIEVED_FLOW:
				//store flows
				FlowEventArgs flowArgs = (FlowEventArgs)e.args;
				if(flowArgs.flowMapping.size()==0)
					break;
				MongoDBDriver mongo = MongoDBDriver.getInstance();
				List<Object> fiList = new ArrayList<Object>();
				for(FlowInfo flowInfo:flowArgs.flowMapping.values()){
					fiList.add(flowInfo);
				}
				log.debug("insert {} flow infos into DB",fiList.size());
				mongo.insertDBOList(fiList,FlowInfo.class);//insertMap(flowArgs.flowMapping);
				mongo.destory();
				break;
			default:
				log.error("knowledge base does not handle {} event.",e.type);
				return;
			}
			
			if(e.type!=EventType.RETRIEVED_KNOWLEDGE)
				return;	
		}
		catch(Exception err){
			log.error(" error occurred when retrieving knowledge: {}:",err);
		}
		return;
	}

	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
	}

	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
	}
	
	void requestForKnowledge(){
		//send polling knowledge requests 
		for(int i=0;i<entityTypes.length;i++){
			retrievedEntityFlagsMap.put(entityTypes[i], false);
			KnowledgeType entityType = entityTypes[i];
			Event e = new Event(EventType.REQUEST_KNOWLEDGE,null,this,new RequestEventArgs(entityType)) ;
			this.eventManager.addEvent(e);
		}
		return;
	}

	@Override
	public KnowledgeEntity queryRelatedEntity(KnowledgeType inputEntityType,
			KnowledgeEntityAttribute inputAttrType,String inputAttrValue, 
			KnowledgeType outputEntityType) {
		// get input instance && query its related entity
		//wait until retrieved
		
		KnowledgeEntity input = null;
		KnowledgeEntity output = null;
		try{
			//get input instance
			input = _queryEntity(inputEntityType,inputAttrType, inputAttrValue);
			if(input==null){
				log.error("cannot find {} entity whose {} is {}",inputEntityType,inputAttrType,inputAttrValue);
				return null;
			}
			//query its related entity
			output = input.relatedEntityMap.get(outputEntityType);
			if(output==null){
				log.error(" Entity {} ({}={}) has no related entity of {} type ",inputEntityType,inputAttrType,inputAttrValue,outputEntityType);
			}
			return output; 
		}
		catch(Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	KnowledgeEntity _queryEntity(KnowledgeType inputEntityType,
			KnowledgeEntityAttribute inputAttrType, String inputAttrValue){
		KnowledgeEntity input =null;
		//if is match query, retrieve from db:
		if(inputAttrType==KnowledgeEntityAttribute.MATCH){
			try {
				//trim spaces
				inputAttrValue=inputAttrValue.replace(" ", "");
				//now only support flow query
				if(inputEntityType!=KnowledgeType.NETWORK_FLOW){
					log.error("now only support flow query");
					return null;
				}
				String[] queryStrings = inputAttrValue.split(";");
				Map<String, Object> queryConditionMap = new HashMap<String, Object>(); 
				for(String query:queryStrings){
					String[] keyValuePair = query.split("=");
					String key = keyValuePair[0];
					String value = keyValuePair[1];
					queryConditionMap.put(key, value);
				}
				MongoDBDriver mongodb = MongoDBDriver.getInstance();
				FlowInfo fi = (FlowInfo) mongodb.findOneByCompoundConditions(queryConditionMap,FlowInfo.class);
				return new NetworkFlow(fi);
			}
			catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
				return null;
			}
		}
		
		while(!mappingCreated){
			try{
				Thread.sleep(1000);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		try{
			//get input instance
			Map<String, KnowledgeEntity> list = this.knowledgeTypedEntityMap.get(inputEntityType);
			if(list==null)
				throw new Exception(" illegal query input entity type!");
			// for some unique entities, inputEntityType and inputAttrValue can be null, and returns the first entity.
			if(inputAttrType==null){
				Iterator<KnowledgeEntity> iter = list.values().iterator();
				//return the first one
				if(iter.hasNext())
					input=iter.next();
			}
			else{
				//query by ID = query by key
				if(inputAttrType==KnowledgeEntityAttribute.ID){
					input = list.get(inputAttrValue);
				}
				else {
					for(KnowledgeEntity entity:list.values()){
						String str = (String) entity.attributeMap.get(inputAttrType);
						if(str.contains(inputAttrValue)){
							input = entity;
							break;
						}
					}
				}
			}
		}
		catch(Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		return input;
	}

	FlowInfo getFlowInfoFromQuery(String key,String value){
		return null;
	}
	
	@Override
	public KnowledgeEntity queryEntity(KnowledgeType inputEntityType,
			KnowledgeEntityAttribute inputAttrType, String inputAttrValue) {
		switch(inputEntityType){
		case NETWORK_FLOW:
			return null;
		default:
			return _queryEntity(inputEntityType,inputAttrType, inputAttrValue);
		}
	}
	@Override
	public boolean isEntitiesRelated(KnowledgeType typeA,
			KnowledgeEntityAttribute attrTypeA, String attrValueA,
			KnowledgeType typeB, KnowledgeEntityAttribute attrTypeB,
			String attrValueB) {
		KnowledgeEntity entityA = _queryEntity(typeA,attrTypeA, attrValueA);
		KnowledgeEntity entityB = _queryEntity(typeB,attrTypeB, attrValueB);
		if(entityA==null||entityB==null)
			return false;
		try{
			return knowledgeTypedEntityMap.get(entityA.getType())
					 .get(entityA.attributeMap.get(KnowledgeEntityAttribute.ID))
					 .relatedEntityMap.containsKey(entityB.getType());
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Map<String,KnowledgeEntity> retrieveEntityList(KnowledgeType queryEntityType) {
		while(!mappingCreated){
			try{
				Thread.sleep(1000);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		Map<String,KnowledgeEntity> ret =  this.knowledgeTypedEntityMap.get(queryEntityType);
		return ret;
	}

	@Override
	public Map<String, KnowledgeEntity> queryAffliatedEntity(
			KnowledgeType inputEntityType,
			KnowledgeEntityAttribute inputAttrType, String inputAttrValue,
			KnowledgeType outputEntityType) {
		// get input instance && query its related entity
				//wait until retrieved
				
				KnowledgeEntity input = null;
				Map<String, KnowledgeEntity> output = null;
				try{
					//get input instance
					input = _queryEntity(inputEntityType,inputAttrType, inputAttrValue);
					if(input==null){
						log.error("cannot find {} entity whose {} is {}",inputEntityType,inputAttrType,inputAttrValue);
						return null;
					}
					//query its affiliated entities
					output = input.affiliatedEntityListMap.get(outputEntityType);
					if(output==null){
						log.error(" Entity {} ({}={}) has no affiliated entity of {} type ",inputEntityType,inputAttrType,inputAttrValue,outputEntityType);
					}
					return output; 
				}
				catch(Exception e){
					log.error(e.getMessage());
					e.printStackTrace();
					return null;
				}
	}
}
