/** 
 *    Copyright 2014 BUPT. 
 **/
package com.sds.securitycontroller.flow.manager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.flow.FlowEventArgs;
import com.sds.securitycontroller.flow.FlowInfo;

import com.sds.securitycontroller.log.manager.ConsoleLogEventArgs;
import com.sds.securitycontroller.log.manager.ILogManagementService;
import com.sds.securitycontroller.log.manager.LogLevel;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.threadpool.IThreadPoolService;
import com.sds.securitycontroller.utils.SingletonTask;

public class FlowPollingService implements IFlowPollingService,
		ISecurityControllerModule {

	public String flowStatisticUrl = "http://nc.research.intra.sds.com:8081/wm/core/switch/[sw]/flow/json";
	public String switchUrl = "http://nc.research.intra.sds.com:8081/wm/core/controller/switches/json";
	public String ncHost = "http://nc.research.intra.sds.com:8081";
	String ncDeviceAPIUrl = "/wm/device/";//newly added
	protected int pollingTaskInterval = 10;
	protected Timer timer;
	boolean pollFromSeperateSW = false;// true;
	boolean saveFlowsToDB = false;
	protected static HashMap<String, String> IP_MAC_Tuple = new HashMap<String, String>();
	protected static HashMap<String, String> attachmentInfo = new HashMap<String, String>();
	protected static Logger log = LoggerFactory
			.getLogger(FlowPollingService.class);
	protected IThreadPoolService threadPool;
	protected ILogManagementService logService;
	protected IEventManagerService eventManager;
	protected SingletonTask detectTask;
	protected URL flowurl;
	protected List<String> switchDpidList = null;
	private List<IFlowSaver> flowSavers = new ArrayList<IFlowSaver>();

	static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

	String tableName = "flows";
	String globalFlowTableName="globalTraffic";
	protected final String primaryKeyName = "id";
	private Map<String, String> globalFlowColumns=new HashMap<String, String>(){
		private static final long serialVersionUID = 1L;
		{
			put("id", "VARCHAR(128)");
			put("tableId", "Integer");
			put("src_mac", "VARCHAR(20)");
			put("dst_mac", "VARCHAR(20)");
			put("src_ip", "VARCHAR(20)");
			put("dst_ip", "VARCHAR(20)");
			put("src_port", "Integer");
			put("dst_port", "Integer");
			put("protocol", "Integer");
			put("byte_count", "Integer");
			put("pkg_count", "Integer");
			put("link", "VARCHAR");
			put("time", "DATETIME");
		}
	};
	private Map<String, String> tableColumns = new HashMap<String, String>() {
		private static final long serialVersionUID = 1232L;

		{
			put("id", "VARCHAR(128)");
			put("tableId", "Integer");
			put("src_mac", "VARCHAR(20)");
			put("dst_mac", "VARCHAR(20)");
			put("src_ip", "VARCHAR(20)");
			put("dst_ip", "VARCHAR(20)");
			put("src_port", "Integer");
			put("dst_port", "Integer");
			put("protocol", "Integer");
			put("byte_count", "Integer");
			put("pkg_count", "Integer");
			put("time", "DATETIME");

		}
	};

	protected IStorageSourceService storageSource;
	protected IRegistryManagementService serviceRegistry;
	protected List<FlowInfo> flist;// FlowBean List consists of one / many
									// flowbean. A flowbean list mapps to unique
									// dpid

	Map<String, FlowInfo> accumulatedFlowInfos = new HashMap<String, FlowInfo>();

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IFlowPollingService.class);
		return l;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IFlowPollingService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IThreadPoolService.class);
		l.add(IStorageSourceService.class);
		return l;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {

        GlobalConfig config = GlobalConfig.getInstance();
		if (config.ncHost != null)
			this.ncHost = config.ncHost;
        
		Map<String, String> configOptions = context.getConfigParams(this);
		if ((configOptions.get("flowUrl")) != null)
			this.flowStatisticUrl = configOptions.get("flowUrl").replace(
					"[nchost]", ncHost);
		if ((configOptions.get("switchUrl")) != null)
			this.switchUrl = configOptions.get("switchUrl").replace("[nchost]",
					ncHost);
		this.pollingTaskInterval = Integer.parseInt(configOptions
				.get("pollingInterval"));
		this.pollFromSeperateSW = Boolean.parseBoolean(configOptions
				.get("pollingFromSeperateSW"));
		this.saveFlowsToDB = Boolean.parseBoolean(configOptions
				.get("saveFlowsToDB"));

		this.eventManager = context.getServiceImpl(IEventManagerService.class,
				this);
		threadPool = context.getServiceImpl(IThreadPoolService.class);
		logService = context.getServiceImpl(ILogManagementService.class);
		this.flist = new ArrayList<FlowInfo>();
		this.storageSource = context.getServiceImpl(
				IStorageSourceService.class, this);
		this.serviceRegistry = context.getServiceImpl(
				IRegistryManagementService.class, this);
		this.timer = new Timer();

		log.info("<START_TIME>,start,finish,phase_time_used,start_time_since,finish_time_since,type,data_length,mark,event_gen_time");
		log.info("SDS flow polling module initialized...");
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		storageSource.createTable(tableName, tableColumns);//globalTraffic
		storageSource.createTable(globalFlowTableName, globalFlowColumns);
		storageSource.setTablePrimaryKeyName(this.globalFlowTableName, 
				this.primaryKeyName);
		storageSource.setTablePrimaryKeyName(this.tableName,
				this.primaryKeyName);

		timer.schedule(new TimerTask() {
			int serialNumber = 0;

			@Override
			public void run() {
				/**
				 * adv = true: retrieve flows separately from each switch
				 */
				serialNumber = (serialNumber + 1) % 100;
				IP_MAC_Tuple=getNetworkDeviceEntityStatus();
				if (pollFromSeperateSW) {
					try {
						if (switchDpidList == null)
							switchDpidList = getAllSwitchDpids();
						Iterator<String> iter = switchDpidList.iterator();
						while (iter.hasNext()) {
							final String dpid = iter.next();
							// log.info("retrieving flow from switch id '{}'",dpid);
							threadPool.getScheduledExecutor().schedule(
									new Runnable() {

										@Override
										public void run() {
											try {
												pollflow(dpid, Integer.toString(serialNumber));
											} catch (Exception e) {
												e.printStackTrace();
												log.error(
														"Exception in polling flows: {}",
														e.getMessage());
											}
										}

									}, 0, TimeUnit.SECONDS);
							// new Thread(new
							// PollingThread(dpid,serialNumber)).start();
						}
					} catch (Exception e) {
						e.printStackTrace();
						log.error("Exception in IDS detector: {}",
								e.getMessage());
					}
				} else {
					try {
						pollflow("all", Integer.toString(serialNumber));
					} catch (Exception e) {
						e.printStackTrace();
						log.error("Exception in polling flows: {}",
								e.getMessage());
					}
				}
			}
		}, 1000, pollingTaskInterval * 1000);

		serviceRegistry.registerService("", this);
		log.info("BUPT flow polling module started...");
	}

	List<String> getAllSwitchDpids() {
		ObjectMapper mapper = new ObjectMapper();
		ArrayList<String> dpids = new ArrayList<String>();
		try {
			String url = this.switchUrl;
			// log.info("Retrieving switches data from [{}]",url);
			URL datasource = new URL(url);
			JsonNode root = mapper.readTree(datasource);
			// retrieve dpid from json
			if (!root.isArray())
				log.error(" switches info illegal: not an array!");
			for (int j = 0; j < root.size(); j++) {
				JsonNode sw = root.get(j);
				String dpid = (sw.path("dpid")).asText();
				if (dpid != null)
					dpids.add(dpid);
			}
			return dpids;
		} catch (JsonProcessingException e) {
			log.error(" Json process error when polling for switch info: "
					+ e.getMessage());
			return null;
		} catch (IOException e) {
			log.error("IO error when retrieving switch info: " + e.getMessage());
			return null;
		}
	}
	//begin adding by zhangkai
	public HashMap<String, String> getNetworkDeviceEntityStatus() {
		HashMap<String, String> IP_mac_Tuple = new HashMap<String, String>();
		String url = ncDeviceAPIUrl;

		JsonNode DeviceEntityNode = httpGetJson(url);
		if (DeviceEntityNode == null)
			return null;
		try {
			for (int i = 0; i < DeviceEntityNode.size(); i++) {
				JsonNode DeviceNode = DeviceEntityNode.get(i);
				if(DeviceNode.path("ipv4").size()!=0)
				{String Device_Info=DeviceNode.toString();
			//	System.out.println("Device_Info"+Device_Info);
				String Device_IP = DeviceNode.path("ipv4").toString();
				String Device_MAC = DeviceNode.path("mac").toString();
				String attachmentPoint = DeviceNode.path("attachmentPoint").toString();
			//	System.out.println("Device_IP: "+Device_IP+"Device_MAC: "+Device_MAC);
				attachmentInfo.put(Device_MAC, attachmentPoint);
				IP_mac_Tuple.put(Device_MAC,Device_IP);
				//String test=IP_MAC_Tuple.get(Device_IP);
				//System.out.println(test);
				
			}}
		} catch (Exception e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		}
		return IP_mac_Tuple;
	}
//end
	int times =0;
	long result =0;
	public void pollflow(String dpid, String serialNum) throws Exception {

		String url = null;
		if (dpid == null)
			url = this.flowStatisticUrl.replace("[sw]", "all");
		else
			url = this.flowStatisticUrl.replace("[sw]", dpid);
		log.debug("Polling data from url {}", url);
		// test --- push log by inner method or api
		// LogData testData = new LogData(date.toString(), "1", "192.168.1.121",
		// "1", "192.168.1.1", "hacker", "192.168.1.145"
		// , "GET - null - 404", 2000, 5000, SuspiciousDataType.TEARDROP);

		ObjectMapper mapper = new ObjectMapper();

		Date startTime = new Date();
		try {
			URL datasource = new URL(url);// (ncFlowAPI);
			JsonNode rootNode = mapper.readTree(datasource);
			Date getDataTime = new Date();
			log.debug("{} Time {}", dpid, getDataTime.toString());
			log.debug(
					"{} Time consumed to get flow data from floodlight: {}ms, json size: {}",
					dpid, getDataTime.getTime() - startTime.getTime(), rootNode
							.toString().length());
//			times=times+1;
//			result += getDataTime.getTime() - startTime.getTime();
//			log.info("times is "+ times);
//			log.info("mean time is "+ ((double)result)/times+" ms");
			flist = FlowInfoJsonParser.DecodeFlowJson(rootNode);
			Date parseDataTime = new Date();
		//	System.out.println(flist);
			log.debug("Time consumed to parse {} flows: {}ms", flist.size(),
					parseDataTime.getTime() - getDataTime.getTime());

			log.debug((new Date()).getTime() + ',' + dpid + ",T_PARSE,"
					+ (parseDataTime.getTime() - getDataTime.getTime()) + ','
					+ flist.size());
			if (flist.size() == 0) {
				// TODO 4 test only
				eventManager.addEvent(new Event(EventType.RECEIVED_LOG, null,
						this, new ConsoleLogEventArgs(LogLevel.DEBUG,
								InetAddress.getLocalHost().getHostName(), this
										.getClass().getCanonicalName(),
								"no flows, ignore...")));
			} else {
				log.debug("start to process...");
				// next step: insert to table
				// two tables
				Map<String, FlowInfo> flowMapping = new HashMap<String, FlowInfo>();
				List<FlowInfo> flows = new ArrayList<FlowInfo>();
				// statical infomation
				long totalPacketCount = 0, totalByteCount = 0;
				/*for (FlowInfo flow : flist) {
					System.out.println("id="+flow.getId()+",bytes="+flow.getByteCount());
				}*/
				long starTime=System.nanoTime();
				for (int i = 0; i < flist.size(); i++) {
					FlowInfo flow = flist.get(i);
					String dip=flow.getnetworkDestination();//xinzeng by zhangkai
					String dmac=flow.getdataLayerDestination();//xinzeng by zhangkai
				//	System.out.println("dip: "+dip+"dmac: "+dmac);
					//System.out.println("the number of networkDevice"+IP_MAC_Tuple.size());
					if(IP_MAC_Tuple.containsKey("[\""+dmac+"\"]"))
					{
						//System.out.println("including devices");
						String test_ip=IP_MAC_Tuple.get("[\""+dmac+"\"]");
					//	System.out.println(test_ip);
						if(!(test_ip.equals("[\""+dip+"\"]")))
						{
//System.out.println("The MITM attacker's  MAC is:  "+dmac+" and the attacker's attachmentPoint is "+attachmentInfo.get("[\""+dmac+"\"]")+"detected by method 3");
//long endTime=System.nanoTime();
//long Time=endTime-starTime;
//System.out.println("The culculating time of method 3 is : "+Time);					
						}
						}
					flow.setTime(new Date());
					FlowInfo oldFlow = this.accumulatedFlowInfos.put(flow.getId(), new FlowInfo(flow.getId(), flow.getPacketCount(), flow.getByteCount()));
												
					if (oldFlow != null) {
						if (flow.getByteCount() >= oldFlow.getByteCount()) {
							flow.setByteCount(flow.getByteCount()
									- oldFlow.getByteCount());
						}
						if (flow.getPacketCount() >= oldFlow.getPacketCount()){
							flow.setPacketCount(flow.getPacketCount()
									- oldFlow.getPacketCount());
						}
					} else if (flow.getDurationSeconds() >= this.pollingTaskInterval)
						continue;
					
					flows.add(flow);
					flowMapping.put(flow.getId(), flow);
					// increase total byte/packet count
					totalByteCount += flow.getByteCount();
					totalPacketCount += flow.getPacketCount();
				}

				// save flow in each flowsaver
				// for(IFlowSaver saver : this.flowSavers){
				// log.debug("saved {} flows", flows.size());
				// saver.save(flows);
				// }
				if (saveFlowsToDB)
					storageSource.insertEntities(tableName, flows);

				String[] flowMappingInfo = { dpid, serialNum };

				// generate a flow event contains flow list (not mapping, for
				// global traffic analyzing--20140519)
				FlowEventArgs fListArgs = new FlowEventArgs(flows);
				fListArgs.setTotalByteCount(totalByteCount);
				fListArgs.setTotalPacketCount(totalPacketCount);
				// add to event manager
				this.eventManager.addEvent(new Event(
						EventType.RETRIEVED_FLOWLIST, flowMappingInfo, this,
						fListArgs));

				Date retriveDataTime = new Date();
				log.debug("Time consumed to retrive flows : {}ms",
						retriveDataTime.getTime() - parseDataTime.getTime());
				// System.out.println((new
				// Date()).getTime()+','+dpid+",T_RETRIEVE,"
				// +((retriveDataTime.getTime()-parseDataTime.getTime())+','
				// +flowMapping.size()
				// ));
				/*
				 * storageSource.deleteRows(tableName, null);
				 * this.storageSource.insertEntities(tableName, flows);
				 */

				Date now = new Date();
				FlowEventArgs args = new FlowEventArgs(flowMapping, now);
				// add total count to flow event args
				args.setTotalByteCount(totalByteCount);
				args.setTotalPacketCount(totalPacketCount);

				this.eventManager.addBroadcastEvent(
				// PC_Chen
				// subject=null
						/**
						 * appended by wxt subject set to switch_id
						 */
						new Event(EventType.RETRIEVED_FLOW, flowMappingInfo,
								this, args));
				// log.info("<START_TIME>,{},{},{}, , ,POLLING_FLOW,{},<dpid>{},<EV_GEN_TIME>{}",startTime.getTime(),
				// now.getTime(),now.getTime()-startTime.getTime(),flowMapping.size(),dpid,dateFormat.format(now));
				log.debug(
						"SN={},sw={},flow_count={},start_time={},end_time={},time_used={}ms",
						serialNum, dpid, flowMapping.size(),
						dateFormat.format(startTime), dateFormat.format(now),
						now.getTime() - startTime.getTime(),
						dateFormat.format(now));
				// log.info("polling finished, total {} flows",
				// flowMapping.size());
			}
		} catch (JsonProcessingException e) {
			log.error("Json process error when polling flow: " + e.getMessage());
		} catch (IOException e) {
			log.debug("IO error when polling flow: " + e.getMessage());
		}
		System.gc();
	}
	//begin
	JsonNode httpGetJson(String url) {
		try {
			url = ncHost + url;
			ObjectMapper mapper = new ObjectMapper();
			URL datasource = new URL(url);
			JsonNode root = mapper.readTree(datasource);
			return root;
		} catch (JsonProcessingException e) {
			log.error(" Json process error when requesting from url:" + url
					+ ", message: " + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			log.debug(" IO error when requesting from url:" + url
					+ ", message: " + e.getMessage());
			return null;
		}

	}
	//end adding by zhangkai
	@Override
	public void addFlowSaver(IFlowSaver saver) {
		log.info("add flow saver {} to Flow Polling Service", saver);
		this.flowSavers.add(saver);
	}

	@Override
	public List<FlowInfo> getAllFlows() {
		return flist;
	}
	
}
