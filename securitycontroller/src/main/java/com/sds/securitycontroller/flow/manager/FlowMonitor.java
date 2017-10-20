/** 
*    Copyright 2014 BUPT. 
**/ 
 package com.sds.securitycontroller.flow.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import clojure.lang.RT;
import clojure.lang.Var;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.app.manager.AppDataPusher;
import com.sds.securitycontroller.app.manager.AppPushRequest;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.CompoundEventSubscription;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscription;
import com.sds.securitycontroller.event.EventSubscription.Operator;
import com.sds.securitycontroller.event.EventSubscription.SubscribedValueCategory;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.ISubscriptionResult;
import com.sds.securitycontroller.event.ListOperatorEventSubscription;
import com.sds.securitycontroller.event.OperatorEventSubscription;
import com.sds.securitycontroller.event.ScriptEventSubscription;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.flow.FlowAvgCount;
import com.sds.securitycontroller.flow.FlowEventArgs;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.policy.AppPolicyScale;
import com.sds.securitycontroller.policy.AtomPolicy;
import com.sds.securitycontroller.policy.PolicyActionType;
import com.sds.securitycontroller.policy.PolicyInfo;
import com.sds.securitycontroller.policy.PolicySubject;
import com.sds.securitycontroller.policy.PolicySubject.PolicySubjectType;
import com.sds.securitycontroller.policy.resolver.PolicyEventArgs;
import com.sds.securitycontroller.storage.DateTimeUtils;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.RowOrdering;
import com.sds.securitycontroller.threadpool.IThreadPoolService;

/**
 * storm packages
 * @author wxt
 *
 */

public class FlowMonitor implements IFlowMonitorService, ISecurityControllerModule/*, IEventListener */, Serializable {

	private static final long serialVersionUID = -5083309442832360243L;
	protected static Logger log = LoggerFactory.getLogger(FlowMonitor.class);
    
    public static String flowTableName = "flowpkg";
    public static String tablePrimaryKey = "id";
    public static String tableTime = "time";
    private static boolean isLocalMode = false;
    private static boolean enableHistoryCacheMonitoring = false;
    private static boolean enableTrafficOverseeing = false;
    private static boolean enableMonitoringWithoutSubscriptions = false;
    private static int historyCacheTime = 2;
    
    public static SecurityControllerModuleContext fmContext;

    long testThreshold = 1000;
    protected static FlowMonitor fmInstance = null;
    
    public static Map<String,HashSet<Integer>> hostVisitedPortMap = new HashMap<String,HashSet<Integer>>(); 
    public Map<String, Object> appSubscriptionCacheMap = new HashMap<String, Object>();
    
    protected HashMap<String,FlowAvgCount> flowAvgCount=new HashMap<String,FlowAvgCount>();
    protected HashMap<String,FlowInfo> suspiciousFlows=new HashMap<String,FlowInfo>();
    
    protected static IEventManagerService eventManager;
    protected static IStorageSourceService storageService;
    protected IRegistryManagementService serviceRegistry;
    
    public long getThreshold(String type){
    		return testThreshold;
    }
    
    static DateFormat dateFormat= new SimpleDateFormat("HH:mm:ss.SSS"); 
    /**
     * EventSpout: listens and gets events 
     * @author Administrator
     *
     */
    public static class EventSpout extends BaseRichSpout implements IEventListener{
    	/**
		 * 
		 */
		private static final long serialVersionUID = -4931730445151947137L;
		static Queue<Event> eventQ;// = new LinkedList<Event>();
		Map<EventType, Map<String, EventSubscriptionInfo>> subscriptionInfos;
    	SpoutOutputCollector _collector;
    	
    	
    	long pollingTimeCount = 30;
    	double avgTotalPackets = 60000; 
    	double avgTotalBytes = 10000000; 
    	double avgTotalFlows = 600;

    	int thresholdAvgRatio = 3;
    	
		@SuppressWarnings("rawtypes")
		@Override
		public void open(Map conf, TopologyContext context,
				SpoutOutputCollector collector) {
			try{
				log.info("OPENING EVENT SPOUT ID '{}'",this.toString());
				subscriptionInfos = new HashMap<EventType, Map<String, EventSubscriptionInfo>>();
				eventQ = new LinkedList<Event>();
				eventManager.addEventListener(EventType.RETRIEVED_FLOW, this);
				log.info(" FLOW EVENT LISTENER REGISTED.");
				eventManager.addEventListener(EventType.ADD_SUBSCRIPTION, this);
				eventManager.addEventListener(EventType.REMOVE_SUBSCRIPTION, this);
				log.info(" SUBSCRIPTION UPDATE LISTENER REGISTED.");
				log.info(" EVENT QUEUE INITIALIZED.");
				log.info(" START RETRIEVING EVENT FROM MQ SERVER...");
				if(!eventManager.getStartedStatus()){
					new Thread(new Runnable() {
						@Override
						public void run() {
							eventManager.setStartedStatus(true);
							eventManager.start();
						}
					}).start();
				}
			}
			catch(Exception e){
				log.error(" {} ERROR DURING PREPARING EVENT SPOUT. ",e.getLocalizedMessage());
			}
			_collector = collector;
		}
		
		void updateVisitedPortMap(Map<String, FlowInfo> flowMapping){
			if(flowMapping==null)
				return;
			HashSet<Integer> portSet = null;
			String hostIp = null;
			for(FlowInfo fi:flowMapping.values()){
				if(fi.getPacketCount() > 2){
					hostIp = fi.getMatch().getNetworkDestination();
					portSet = hostVisitedPortMap.get(hostIp);
					if(portSet==null){
						portSet = new HashSet<Integer>();
					}
					portSet.add( fi.getMatch().getTransportDestination() );
					hostVisitedPortMap.put(hostIp, portSet);
				}
			}
		}

		@Override
		public void nextTuple() {
			if(subscriptionInfos==null||subscriptionInfos.size()==0){
				return;
			}
		}
		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			// emit data to processor, adding trackerID to track tuple
			declarer.declare(new Fields("subinfos","eventtype","time","switch_id","flowmapping","trackerID","sn"));
		}
		/**
		 * Overlooking traffic status 
		 * @param eArgs
		 */
		void overseeTraffic(FlowEventArgs eArgs){
			long totalByteCount = eArgs.getTotalByteCount();
			long totalPacketCount = eArgs.getTotalPacketCount();
			int totalFlowCount = eArgs.flowMapping.size();
			if(pollingTimeCount==0){
				avgTotalBytes = totalByteCount;
				avgTotalPackets = totalPacketCount;
				avgTotalFlows = totalFlowCount;
			}
			//check if exceeds traffic threshold
			if( totalByteCount > avgTotalBytes*thresholdAvgRatio || totalPacketCount > avgTotalPackets*thresholdAvgRatio || totalFlowCount > avgTotalFlows*thresholdAvgRatio ){
				//alert, but not update traffic thresholds (average values)
				if(totalByteCount > avgTotalBytes*thresholdAvgRatio)
					log.warn(" traffic alert: current byte count({}) exceeds threshold({})",totalByteCount,avgTotalBytes*thresholdAvgRatio);
				if(totalPacketCount > avgTotalPackets*thresholdAvgRatio)
					log.warn(" traffic alert: current packet count({}) exceeds threshold({})",totalPacketCount,avgTotalPackets*thresholdAvgRatio);
				if( totalFlowCount > avgTotalFlows*thresholdAvgRatio)
					log.warn(" traffic alert: current flow count({}) exceeds threshold({})", totalFlowCount,avgTotalFlows*thresholdAvgRatio);
			}
			else{
				double coex1 = (double)pollingTimeCount/(double)(pollingTimeCount+1);
				double coex2 = 1/(double)(pollingTimeCount+1);
				avgTotalBytes =  (coex1*avgTotalBytes+coex2*totalByteCount);
				avgTotalPackets =  (coex1*avgTotalPackets+coex2*totalPacketCount);
				avgTotalFlows = (coex1*avgTotalFlows+coex2*(totalFlowCount));
			}
			if(pollingTimeCount<100)
				pollingTimeCount++;
			
			// = (pollingTimeCount>100)? 100 : pollingTimeCount+1;
			
			log.info(" traffic status: FLOW COUNT={},TOTAL PACKETS={},TOTAL BYTES={},AVERAGE FLOWS={},AVERAGE TOTAL PACKETS={},AVERAGE TOTAL BYTES={}"
					,totalFlowCount,totalPacketCount,totalByteCount,(long)avgTotalFlows,(long)avgTotalPackets,(long)avgTotalBytes);
	
			if(totalByteCount > avgTotalBytes*thresholdAvgRatio)
				log.warn(" traffic alert: current byte count({}) exceeds threshold({})",totalByteCount,(long)avgTotalBytes*thresholdAvgRatio);
			if(totalPacketCount > avgTotalPackets*thresholdAvgRatio)
				log.warn(" traffic alert: current packet count({}) exceeds threshold({})",totalPacketCount,(long)avgTotalPackets*thresholdAvgRatio);
			if( totalFlowCount > avgTotalFlows*thresholdAvgRatio)
				log.warn(" traffic alert: current flow count({}) exceeds threshold({})", totalFlowCount,(long)avgTotalFlows*thresholdAvgRatio);

		}
		
		void pushToProcessorBolt(Event e){
			String trackerID = UUID.randomUUID().toString();
			String param1,param2 = null;
			try{
				String[] fmInfo = (String[])e.subject;
				param1 = fmInfo[0];
				param2 = fmInfo[1];
			}
			catch(Exception ex){
				param1 = "NaN";
				param2 = "NaN";
			}
			log.info(" [[event source spout]{}: retrieved flows(ID:{}) from MQ, emitting to processor bolt...",this.hashCode(),trackerID);
			_collector.emit(new Values(subscriptionInfos,e.type,e.args.getTime(),param1,((FlowEventArgs)e.args).flowMapping,trackerID,param2));
			log.info(" [[event source spout]{}: emitting flows(ID:{}) complete"
					,this.hashCode(),trackerID);
		}
		
		@Override
		public void processEvent(Event e) {
			log.debug("received a '{}' event",e.type);
			// when MQ pushes in an event and fm has received subscriptions, add it into event Q
			if( (System.currentTimeMillis()-e.args.getTime().getTime()) > 15000 ){
				log.warn("Ignoring a RETRIEVED_FLOW event generated {} >15s ago!"
						, (System.currentTimeMillis()-e.args.getTime().getTime()));
				return;
			}
			// when subscription info is not null, emit to 
			try{
				FlowEventArgs eArgs=(FlowEventArgs) e.args;
				Map<String, FlowInfo> flowMapping = eArgs.flowMapping;
//				long totalPacketCount = ((FlowEventArgs)(e.args)).getTotalPacketCount();
				updateVisitedPortMap(flowMapping);
				if(!enableMonitoringWithoutSubscriptions){
					if(subscriptionInfos==null||subscriptionInfos.size()==0)
						return;
				}
				
				pushToProcessorBolt(e);
				// monitor history cache
				if(enableHistoryCacheMonitoring){
					analyseHistoryCache(flowMapping);
				}
				// oversee traffic ammount
				if(enableTrafficOverseeing){
					overseeTraffic(eArgs);
				}
			}
			catch(Exception e1){
				log.error(e1.getMessage());
				e1.printStackTrace();
			}
		}

//		//update open port of each host @ 2014.02.14
//		Map<String, Set<Integer>> hostOpenPortsMap = new HashMap<String, Set<Integer>>();//key: host ip, value: port set
		//record history flows @ 2014.02.13
    	Map<String, FlowInfo> historyFlowCache = new HashMap<String, FlowInfo>();
    	final int cacheExpireTick = 1;
    	final int pollingInterval = 3;
		void analyseHistoryCache(Map<String, FlowInfo> flowMapping){

			Date now = new Date();
			FlowInfo duplicatedFlowInfo = null;
			for(Entry<String, FlowInfo> entry:flowMapping.entrySet()){
				duplicatedFlowInfo = historyFlowCache.get(entry.getKey());
				if(duplicatedFlowInfo!= null){
					if( historyFlowCache.get(entry.getKey()).getPacketCount() < entry.getValue().getPacketCount() ){
						historyFlowCache.put(entry.getKey(),entry.getValue());
					}
				}
				else{
					historyFlowCache.put(entry.getKey(),entry.getValue());
				}
			}
			//delete expired flows
			FlowInfo fi;
			String flowID;
			Iterator<String> flowIdIterator = historyFlowCache.keySet().iterator();
			while(flowIdIterator.hasNext()){
				flowID = flowIdIterator.next();
				fi = historyFlowCache.get(flowID);
				if( now.getTime() - fi.getTime().getTime() > historyCacheTime*1000 ){
					flowIdIterator.remove();
					historyFlowCache.remove(flowID);
				}
			}
			//restore in history flow cache
			int size = historyFlowCache.size();
			String trkid = "last_" + historyCacheTime + "s_history";
			log.info("History flow cache updated, size:{}. {}",size);
			//emit to processor
			log.info(" [[event source spout]{}: retrieved flows(ID:{}) from MQ, emitting to processor bolt...",this.hashCode(),trkid);
			_collector.emit(new Values(subscriptionInfos,EventType.RETRIEVED_FLOW,new Date(),"all",historyFlowCache,trkid,trkid));
			log.debug("Sending history flow cache to EVENT PROCESSING BOLT",EventType.RETRIEVED_FLOW);
			log.info(" [[event source spout]{}: send history (ID:{}) complete"
					,this.hashCode(),trkid);
		}
		
		@Override
		public void addListenEventCondition(EventType type,
				EventSubscriptionInfo condition) {
			// add listener to event scheduler
			eventManager.addConditionToListener(condition);
		}

		@Override
		public void processAddListenEventCondition(EventType type,
				EventSubscriptionInfo condition) {
			// get subscription info 
			Map<String, EventSubscriptionInfo> conditions = null;
			if(this.subscriptionInfos.containsKey(type)){
				conditions = this.subscriptionInfos.get(type);
			}else{
				conditions = new HashMap<String, EventSubscriptionInfo>();
				this.subscriptionInfos.put(type, conditions);
			}
			conditions.put(condition.getSubscriptionId(), condition);
			log.info("RECEIVED SUBSCRITION ID: {}",condition.getSubscriptionId());
			/*
			 * IF IS SUBSCRIPTION SCRIPT, SAVE TO SCRIPT FILE.
			 */
			if (condition.getEventsubscription() instanceof ScriptEventSubscription){
				ScriptEventSubscription ses = (ScriptEventSubscription)condition.getEventsubscription();
				try {
					log.info("SAVING SCRIPT SUBSCRIPTIOIN CONDITION , SCRPIT CONTENTS:\r\n{}",ses.getSubscriptionScript());
					File scriptFile = new File(scriptFilePrefix+condition.getSubscriptionId()+".clj");
					if(!scriptFile.exists()){
						scriptFile.createNewFile();
					}
					FileOutputStream fs = new FileOutputStream(scriptFile);
					fs.write(ses.getSubscriptionScript().getBytes());
					log.info("SCRIPT CONDITION SAVED TO FILE '{}'",scriptFile);
					fs.close();
				} catch (Exception e) {
					log.error("SAVING CONDITION FAILED.{}",e.getCause());
					e.printStackTrace();
				} 
			}
			log.debug(" spout received subscription.");
		}
    }
    
    
    /**
     * 
     * @author Administrator
     *
     */
    public static class EventProcessBolt extends BaseRichBolt implements IEventListener{
    	
		private static final long serialVersionUID = 1L;// = new HashMap<EventType, Map<String, EventSubscriptionInfo>>();
		OutputCollector _collector;
		
		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("url","subscriptionId","time","result","trackerID"));
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void prepare(Map stormConf, TopologyContext context,
				OutputCollector collector) {
			_collector = collector;
			log.info("EVENT PROCESS BOLT INITIALIZED. ");
		}

		@Override
		public void execute(Tuple input) {
			@SuppressWarnings("unchecked")
			Map<EventType, Map<String, EventSubscriptionInfo>> subscriptionInfos =
					(HashMap<EventType, Map<String, EventSubscriptionInfo>>) input.getValue(0);//new HashMap<EventType, Map<String, EventSubscriptionInfo>>();
			EventType etype = (EventType) input.getValue(1);
			Date time = (Date) input.getValue(2);
			Map<String, EventSubscriptionInfo> subscriptions = subscriptionInfos.get(etype);
			
			String switch_id = input.getString(3);
			@SuppressWarnings("unchecked")
			Map<String, FlowInfo> flowMapping = (Map<String, FlowInfo>) input.getValue(4);
			//tracker ID
			String trackerID = input.getString(5);

			String serialNumber = input.getString(6);
			Date d1,d2;
			d1 = new Date();
			
			if(enableMonitoringWithoutSubscriptions){
				log.info("[flow event processor]:{} get flows(ID:{},SN:{},switch={},count={},generated_time={}), start to check."
						,this.hashCode(),trackerID, serialNumber,switch_id,flowMapping.size(),time);
				long time1=System.nanoTime();
				portscanDetect(flowMapping);
				long time2=System.nanoTime();
				log.info("bolt=,{},sn=,{},chktime=,{},flow_count=,{}",
						this.hashCode(),serialNumber,(time2-time1),flowMapping.size());
			}
			
			if(subscriptions==null||subscriptions.size()==0)
				return;
			
			Iterator<Entry<String, EventSubscriptionInfo>> it = subscriptions.entrySet().iterator();
			List<ISubscriptionResult> res;// = processEventsWithCondition(subscription, flowMapping);//new ArrayList<ISubscriptionResult>();//null;
			while(it.hasNext()){
				Entry<String, EventSubscriptionInfo> entry = it.next();
				EventSubscriptionInfo subscription = entry.getValue();
				String subscriptionId = entry.getKey();
				// judge if this subscription is presented in script
				if(subscription.getEventsubscription() instanceof ScriptEventSubscription){
					log.info("[[flow event processor]:{} flows(ID:{}) checking flows with SCRIPT subscription ID '{}'"
							,this.hashCode(), subscription.getSubscriptionId());
					res = processEventsBySubscriptionScript(subscription, flowMapping);
					System.err.println(trackerID +":"+portscanResString);
				}
				else {
					log.info("[[flow event processor]:{} flows(ID:{}) checking flows with NORMAL subscription ID '{}'"
							,this.hashCode(), trackerID, subscription.getSubscriptionId());
					res = processEventsWithCondition(subscription, flowMapping);
				}
				
				
				d2 = new Date();

				if(res == null || res.size()==0){
					log.info("[[flow event processor]:{} flows(ID:{}) checking with subscription ID '{}' complete, no abnormal results generated."
							, this.hashCode(), trackerID,subscription.getSubscriptionId());
					continue;
				}
				
				//remove other checked flows
				Iterator<ISubscriptionResult> it1 = res.iterator();
				while(it1.hasNext()){	
					FlowInfo f = (FlowInfo)it1.next();
					flowMapping.remove(f.getId());
				}
				
				log.info("[[flow event processor]:{} flows(ID: {}) check complete and get {} ABNORMAL flows, sending to policy Generator."
						,this.hashCode(), trackerID, flowMapping.size(), time,d2.getTime()-d1.getTime());
				//mark test
				_collector.emit(new Values(subscription.getSubscribeUrl(), subscriptionId, time, res,trackerID));	

			}
			log.info("[[flow event processor]:{} flows(ID: {}) - check complete",this.hashCode(), flowMapping.hashCode());
			
		}

		@Override
		public void processEvent(Event e) {
//			log.debug("bolt event!!");
		}

		@Override
		public void addListenEventCondition(EventType type,
				EventSubscriptionInfo condition) {
		}

		@Override
		public void processAddListenEventCondition(EventType type,
				EventSubscriptionInfo condition) {
		}
		
		class HostStatic{
			int unstablishedFlowCount=0;
			int flowCount=0;
			int pktCount=0;
			double fixedPortSetCount=0;
			Set<Integer> dstPortSet = new HashSet<Integer>();
			Set<String> dstIpSet = new HashSet<String>();
			@Override
			public String toString(){
				return "index="+getCalculatedIndex()+",fl_cnt="+flowCount+",ufl_cnt="+unstablishedFlowCount+
						",pkt_cnt="+pktCount+",dport_cnt="+dstPortSet.size()+
						",fixdport_cnt="+fixedPortSetCount+
						",dip_cnt="+dstIpSet.size();
			}
			final static int deltaT = 3600;
			
			public double getCalculatedIndex(){
				double x = 0.3/4/0.9;
				double I = x*unstablishedFlowCount / flowCount + (1-x)*dstPortSet.size()/200;
				return I;
			}
			
			public void updateFixedPortCount(String dstIp,int port,boolean isOpen,int t0){
				if(dstPortSet.contains(port))
					return;
				double w=1.0;
				if(!isOpen){//closed port
					HashSet<Integer> hostVisitedPorts = hostVisitedPortMap.get(dstIp);
					if(hostVisitedPorts!=null){
						if(hostVisitedPorts.contains(port)){
							w=0.5;//once open port
						}
					}
				}
				else	
					w=0.1;//open port
				fixedPortSetCount += w;
			}
		}
		
		void portscanDetect(Map<String, FlowInfo> flowMapping){
			HashMap<String, HostStatic> srcHostStatics = new HashMap<String, HostStatic>();  
			String srcIp = null;
			String dstIp = null;
			HostStatic hs = null;
			for(FlowInfo fi:flowMapping.values()){
				srcIp=fi.getMatch().getNetworkSource();
//				boolean portOpen=true;
				if(! srcHostStatics.containsKey(srcIp) ){
					srcHostStatics.put(srcIp, new HostStatic());
				}
				hs = srcHostStatics.get(srcIp);
				hs.flowCount ++;
				dstIp = fi.getMatch().getNetworkDestination();
				int dport = fi.getMatch().getTransportDestination();
				if(fi.getPacketCount()<2){
					hs.unstablishedFlowCount ++;
//					portOpen=false;
				}
//				hs.updateFixedPortCount(dstIp,dport,portOpen,0);
				hs.dstPortSet.add(dport);
				hs.dstIpSet.add(dstIp);
				hs.pktCount += fi.getPacketCount();
				srcHostStatics.put(srcIp, hs);
			}
			for(Entry<String, HostStatic> entry:srcHostStatics.entrySet()){
				log.info("sip="+entry.getKey()+","+entry.getValue().toString());
			}
		}
		
	}
	/***
	 * push examine results to app
	 * @author Administrator
	 *
	 */
    public static class AppPusherBolt extends BaseRichBolt{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5268665234025079971L;

		@SuppressWarnings("rawtypes")
		@Override
		public void prepare(Map stormConf, TopologyContext context,
				OutputCollector collector) {
		}

		@Override
		public void execute(Tuple input) {
			String subscriptionUrl = input.getString(0);
			String subscriptionId = input.getString(1);
			Date time = (Date) input.getValue(2);
			String trackerID = input.getString(4);
			
			try {
				@SuppressWarnings("unchecked")
				List<ISubscriptionResult> res = (List<ISubscriptionResult>) input.getValue(3);
				log.info(" [[APP pusher ]:{} checking result(ID:{}) received(count={},generate_time={},matched_subscription={}), pushing to app... "
						,this.hashCode(),trackerID,res.size(),time,subscriptionId);
				AppPushRequest req = new AppPushRequest(subscriptionUrl, subscriptionId, res, null);
				AppDataPusher pusher = new AppDataPusher(req);
				pusher.pushDataToApp();
				log.info("[[APP pusher ]:{} pushed {} results(trackerID={}) to url:{}",this.hashCode(),res.size(),trackerID,subscriptionUrl);
			}
			catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			
		}
    	
    }
    
    
    public static class PolicyGenerateBolt extends BaseBasicBolt{
		/**
		 * 
		 */
		private static final long serialVersionUID = -2304035225380941965L;

		private static int durationSeconds;
		
		static HashMap<String,TenantStat> tenantMap =new HashMap<String,TenantStat>();
		static HashMap<String,UserStat> userMap=new HashMap<String,UserStat>();
		static HashMap<String,VMStat> vms=new HashMap<String,VMStat>();			//vmId -->CloundVM
		
		static HashMap<String,String> ipUserMap=new HashMap<String,String>();			//vmId -->CloundVM
		static HashMap<String,String> ipTenantMap=new HashMap<String,String>();			//vmId -->CloundVM
		static HashMap<String,HashMap<String,String>> tenantIpVmMap =new HashMap<String,HashMap<String,String>>();
		
		static class AppPolicyItem {
			public AppPolicyScale scale;
			public String id;
			
			public AppPolicyItem(AppPolicyScale scale,String id){
				this.scale=scale;
				this.id=id;
			}
			@Override
			public String toString(){
				return scale + id;
			}
		}
		
		static class VMStat{
			public String id=null;
			public String uid=null;
			public String tid=null;
			public List<String> ips=null;
			public List<String> flows =null;
			public long packetCount;
			public long byteCount;

		    public VMStat(String id,String uid,String tid,List<String>ips,List<String>flows,long pc,long bc){
				this.id=id;
				this.uid=uid;
				this.tid=tid;
				this.ips=ips;
				this.flows=flows;
				this.packetCount=pc;
				this.byteCount=bc;
			}
		}
		static class UserStat{
			public String id;
			public String Tid;
			public List<VMStat> vmList=null;
			public long packetCount;
			public long byteCount;
			public UserStat(String id,String tid,List<VMStat> vmStats){
				this.id=id;
				this.Tid=tid;
				this.vmList=vmStats;
			}
		}
		static class TenantStat{
			public String id;
			public List<UserStat> userList=null;
			public long packetCount;
			public long byteCount;

			public TenantStat(String id,List<UserStat> userStats){
				this.id=id;
				this.userList=userStats;
			}
		}
		
		
		public PolicyGenerateBolt(){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					boolean getMapping = false;
					while(!getMapping){
						getMapping = getContextMaps();
						try {
							Thread.sleep(100000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
			}).start();
			
		}
		
		boolean getContextMaps(){
			ObjectMapper mapper = new ObjectMapper();
			String vmapi="http://sc.research.intra.sds.com:8888/sc/cloudagent/list/vm/json";
			try{	
				URL datasource = new URL(vmapi);
				JsonNode rootNode = mapper.readTree(datasource);
//				JsonNode rootNode = mapper.readTree(jsonstrVM);
				Iterator<Entry<String, JsonNode>> iter = rootNode.fields();
				
				while(iter.hasNext()){					// per vm
					Entry<String, JsonNode> entry= iter.next();
					JsonNode tenantNode = entry.getValue(); 
					String vmId=tenantNode.path("id").textValue();
					String tenantId=tenantNode.path("tenantId").textValue();
					String userId=tenantNode.path("userId").textValue();
					JsonNode nwNode=tenantNode.path("networks");
			
					// to create vm--> ips mapping !
					Iterator<Entry<String, JsonNode>> nwIter = nwNode.fields();
					ArrayList<String> cache = new ArrayList<String>();
					while(nwIter.hasNext()){			// per network
						Entry<String, JsonNode> nwEntry= nwIter.next();
						JsonNode addrNode = nwEntry.getValue().path("addresses");
						Iterator<JsonNode> addrIter= addrNode.elements();
						while (addrIter.hasNext()){		// per address
							JsonNode nwItem = addrIter.next();
							cache.add(nwItem.path("addr").textValue());
						}
					}
					if(!vmId.isEmpty()){
						VMStat cvm=new VMStat(vmId,userId,tenantId,cache,null,0,0);
						vms.put(vmId, cvm);
					}
					//to create user--> VMs mapping
					ArrayList<VMStat> vmsTemp=new ArrayList<VMStat>();
					if(userMap.containsKey(userId)){
						vmsTemp.addAll(userMap.get(userId).vmList);
						if(!vmsTemp.contains(vmId)){
							vmsTemp.add(vms.get(vmId));
							userMap.get(userId).vmList = vmsTemp;
						}
					}else{
						vmsTemp.add(vms.get(vmId));
						userMap.put(userId, new UserStat(userId,tenantId,vmsTemp));
					}
						
					//to create Tenant--> users mapping
					ArrayList<UserStat> usersTemp=new ArrayList<UserStat>();
					if(tenantMap.containsKey(tenantId)){
						usersTemp.addAll(tenantMap.get(tenantId).userList);
						if(!usersTemp.contains(userId)){
							usersTemp.add(userMap.get(userId));
							tenantMap.get(tenantId).userList = usersTemp;
						}
					}else{
						usersTemp.add(userMap.get(userId));
						tenantMap.put(tenantId,new TenantStat(tenantId,usersTemp));
					}
				}
				return true;
			}catch (IOException e) {
//					log.info(e.toString());
					e.printStackTrace();
					log.warn("unable to get cloud info. retry in 1 sec.");
					return false;
			}
		}
		
		static List<AppPolicyItem> checkDdosOnAllScale(long[] packetThreshold,
				int durationsecondThreshold, List<FlowInfo> flowlist) {
			//refresh CloudMaps
			log.debug("method checkDdosOnAllScale() starts");
			Date start=new Date();

			
			List<AppPolicyItem> policyList = new ArrayList<AppPolicyItem>();
			List<AppPolicyItem> policyList3 = new ArrayList<AppPolicyItem>();
			List<AppPolicyItem> policyList2 = new ArrayList<AppPolicyItem>();
			List<AppPolicyItem> policyList1 = new ArrayList<AppPolicyItem>();
			List<AppPolicyItem> policyList0 = new ArrayList<AppPolicyItem>();

			// check nw_src in every flow , see what vm it belongs to, complete vms count
			String flowId="";
			long pc;
			long bc;
			for(int i = 0; i<flowlist.size(); i++){
				flowId=flowlist.get(i).getId();
				pc=flowlist.get(i).getPacketCount();
				bc=flowlist.get(i).getByteCount();
				// check flows exceed threshold[0]
				if (pc > packetThreshold[0]) {
					if (durationSeconds <= durationsecondThreshold) {
						policyList0.add(new AppPolicyItem(AppPolicyScale.FLOW,flowlist.get(i).getId()));
					}
				}else{					// only counts flows not reach threshold in flow scale.
					//PC_Chen
					//getnetworkSource() actually returns dstIp, causes unknown  
					String dstIp = flowlist.get(i).getMatch().getNetworkSource();
					Iterator<Entry<String, VMStat>> vmIter = vms.entrySet().iterator();
					VMStat virtMathine=null;
					while(vmIter.hasNext()){
						Entry<String, VMStat> entry=vmIter.next();
						virtMathine=entry.getValue();
						ArrayList<String> flowsTemp=new ArrayList<String>();
						
						if(virtMathine.ips!=null && virtMathine.ips.contains(dstIp)){
							if(virtMathine.flows!=null){
								flowsTemp.addAll(virtMathine.flows);
							}
							flowsTemp.add(flowId);	
							virtMathine.flows = flowsTemp;
							virtMathine.packetCount = virtMathine.packetCount+pc;
							virtMathine.byteCount = virtMathine.byteCount +bc;
							break;
						}
					}
				}
			}
			//complete statistics on User and Tenant scale
			Iterator<Entry<String, VMStat>> vmIter = vms.entrySet().iterator();
			VMStat virtMathine=null;
			while(vmIter.hasNext()){
				Entry<String, VMStat> entry=vmIter.next();
				virtMathine=entry.getValue();
				UserStat userTmp=null;
				// check vms traffic exceed threshold[1]
				if(virtMathine.packetCount>packetThreshold[1]){
					policyList1.add(new AppPolicyItem(AppPolicyScale.VM,virtMathine.id));
				}else if(virtMathine.packetCount!=0){
					//increase userMap count
					userTmp=userMap.get(virtMathine.uid);
					userTmp.packetCount = userTmp.packetCount+virtMathine.packetCount;
					userTmp.packetCount = userTmp.packetCount+virtMathine.packetCount;
				}
			}
			Iterator<Entry<String, UserStat>> userIter = userMap.entrySet().iterator();
			UserStat userTmp=null;
			TenantStat tenantTmp=null;
			while(userIter.hasNext()){
				Entry<String, UserStat> entry=userIter.next();
				userTmp= entry.getValue();
				// check users traffic exceed threshold[2]
				if(userTmp.packetCount>packetThreshold[2]){
					policyList2.add(new AppPolicyItem(AppPolicyScale.USER,userTmp.id));
				}else{
					tenantTmp=tenantMap.get(userTmp.id);
					if(tenantTmp!=null){
						tenantTmp.packetCount = tenantTmp.packetCount+userTmp.packetCount;
					}
				}
			}
			
			log.info("traffic statistics per VM : {}",vms.toString());
			log.info("traffic statistics per User : {}",userMap.toString());
			log.info("traffic statistics per Tenant : {}",tenantMap.toString());
			
			Iterator<Entry<String, TenantStat>> tenantIter = tenantMap.entrySet().iterator();
			TenantStat tenantTmp1=null;
			while(tenantIter.hasNext()){
				Entry<String, TenantStat> tenantEntry=tenantIter.next();
				tenantTmp1=tenantEntry.getValue();
				if(tenantTmp1.packetCount>packetThreshold[3]){
					policyList3.add(new AppPolicyItem(AppPolicyScale.TENANT,tenantTmp1.id));
				}
			}
			policyList.addAll(policyList3);
			AppPolicyItem pi=null;
			for (int i = 0; i < policyList2.size(); i++){			// check if user contains in Tenant policy.
				pi=policyList2.get(i);
				boolean contains=false;
				for(int j = 0; j < policyList3.size(); j++){	
					if(tenantMap.get(policyList3.get(j).id).userList.contains(userMap.get(pi.id))){
						contains=true;
					}
				}
				if(!contains){
					policyList.add(pi);
				}
			}
			int listSize=policyList.size();
			for (int i = 0; i < policyList1.size(); i++){		//check if vm contain in Tenant/User policy
				pi=policyList1.get(i);
				boolean contains=false;
				for(int j = 0; j < listSize; j++){
					if(policyList.get(j).scale.equals(AppPolicyScale.USER)){
						if(userMap.get(policyList.get(j).id).vmList.contains(vms.get(pi.id))){
							contains=true;
						}
					}else{			//tenant policy
						UserStat cu=null;
						for(int k=0;k<tenantMap.get(policyList.get(j).id).userList.size();k++){
							cu=tenantMap.get(policyList.get(j).id).userList.get(k);
							if(cu.vmList.contains(vms.get(pi.id))){
								contains=true;
							}
						}
					}
				}
				if(!contains){
					policyList.add(pi);
				}
			}

			Date retTime=new Date();
			log.info("time comsumed by method checkDdosOnAllScale(): {} ms",retTime.getTime()-start.getTime());
			return policyList;
		}
		
		
		static PolicyInfo convertToCleanPolicy(
				List<AppPolicyItem> policyList) {
			try {
				PolicyActionType type = PolicyActionType.REDIRECT_FLOW;
				PolicySubject subject = new PolicySubject("",PolicySubjectType.SECURITY_CONTROLLER);
				List<AtomPolicy> atomlist = new ArrayList<AtomPolicy>();
				for (int i = 0; i < policyList.size(); i++) {
					AtomPolicy atom = new AtomPolicy(policyList.get(i).scale,
							policyList.get(i).id,
							policyList.get(i).id.getClass()
							); // to be changed
					atomlist.add(atom);
				}
				AtomPolicy[] atomarray = new AtomPolicy[atomlist.size()];
				atomlist.toArray(atomarray);

				PolicyInfo policyInfo=new PolicyInfo("", atomarray, false);
				policyInfo.setSubject(subject);
				policyInfo.setActionType(type);
				policyInfo.setNegated(false);
				log.info("ads have generated clean policies: {}", policyInfo.toString());
				return policyInfo;
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public void execute(Tuple input, BasicOutputCollector collector) {
			// generate ADS policy and send to policy resolver
			String subscriptionId = input.getString(1);
			Date time = (Date) input.getValue(2);
			String trackerID = input.getString(4);
			
			try {
				@SuppressWarnings("unchecked")
				List<FlowInfo> res = (List<FlowInfo>) input.getValue(3);
				log.info(" [[ADS policy generator]:{} checking result(ID:{}) received(count={},generate_time={},matched_subscription={}), generating policy... "
						,this.hashCode(),trackerID,res.size(),time,subscriptionId);
				
				PolicyInfo policyInfo = null;
				
				long[] packetThresholds = {2000,0,0,50000};
				int durationsecondThreshold = 99999;
				List<AppPolicyItem> atomPolicies = checkDdosOnAllScale(packetThresholds, durationsecondThreshold, res);
	            if(atomPolicies.size()>0){
	            	policyInfo = convertToCleanPolicy(atomPolicies);            	
	            	policyInfo.setSubId("adsapp");
	            	policyInfo.setForce(false);
	    			PolicyEventArgs args = new PolicyEventArgs(policyInfo);
	    			eventManager.addEvent(new Event(EventType.RECEIVED_POLICY, null,
	    					this, args));
	    			log.info("  [[ADS policy generator]:{} policy has generated for checking result(ID:{}), dispatching to Policy Resolver."
	    					,this.hashCode(),trackerID);
	            }else{
	            	log.info("  [[ADS policy generator]:{} no DOS attack detected for checking result(ID:{})."
	    					,this.hashCode(),trackerID);
	            }
			}
			catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}

		
		
		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
		}
    }

    public StormTopology stormTopology;
    boolean isTopologyBuilt = false; 
    
    void buildFlowMonitorTopology(boolean isLocal) {
    	TopologyBuilder builder = new TopologyBuilder();
    	builder.setSpout("eventGetter", new EventSpout(),1);
    	builder.setBolt("eventProcessor", new EventProcessBolt(),processorCount).fieldsGrouping("eventGetter", new Fields("switch_id"));//.shuffleGrouping("eventGetter");
    	if(pushingResult){
    		if(builtInADSPolicyGenerator){
    			builder.setBolt("policyGenerator", new PolicyGenerateBolt(),policyGeneratorCount).fieldsGrouping("eventProcessor", new Fields("subscriptionId"));
    		}
    		else{
    			builder.setBolt("appPusher", new AppPusherBolt(),pusherCount).fieldsGrouping("eventProcessor", new Fields("subscriptionId"));
    		}
    	}
    	
    	stormTopology = builder.createTopology();
    	Config conf = new Config();
    	conf.setDebug(false);
    	if(isLocal){
    		conf.setMaxTaskParallelism(5);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("flowmonitor", conf, stormTopology);
            isTopologyBuilt = true;
            log.info(" Storm processing topology established, mode: [LOCAL]");
    	}
    	else{
    		conf.setNumWorkers(5);
            try {
    			StormSubmitter.submitTopology("flowmonitor", conf, stormTopology);
    			isTopologyBuilt = true;
                log.info(" Storm processing topology established, mode: [DISTRIBUTED]");
    		} catch (AlreadyAliveException e) {
    			e.printStackTrace();
    		} catch (InvalidTopologyException e) {
    			e.printStackTrace();
    		}
    	}
        this.isTopologyBuilt = true;
    }
    
	//if some flows should be sent, return a list, otherwise null.
	@SuppressWarnings("unchecked")
	static
	List<ISubscriptionResult> processEventsWithCondition(EventSubscriptionInfo subscription, Map<String, FlowInfo> flowMapping){
		EventSubscription eventSubscription = subscription.getEventsubscription();
		
		FlowInfo refFlow = flowMapping.values().iterator().next();
		return (List<ISubscriptionResult>)(List<?>)processEventsWithCondition(eventSubscription, SubscribedValueCategory.ROOT, 
				(Collection<Object>)(Collection<?>)flowMapping.values(), refFlow);		
	}
	
    
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IFlowMonitorService.class);
		return l;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = 
				new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IFlowMonitorService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IThreadPoolService.class);
		l.add(IStorageSourceService.class);
		
//		l.add(com.sds.securitycontroller.cloud.manager.ICloudAgentService.class);
		
		return l;
	}
	
	static final String FLOW_MONITOR_MODE ="flow.monitor.mode";

	static int processorCount = 2;
	static int pusherCount = 2;
	static int policyGeneratorCount = 2;
	static boolean pushingResult = true;
	static boolean builtInADSPolicyGenerator = true;

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
	     
	     fmInstance = this;
	     log.info("BUPT flow monitor initialized");  
	}
	
	@Override
	public void startUp(SecurityControllerModuleContext context) {
		fmContext = context;
	    try {
			Map<String, String> configOptions = context.getConfigParams(this);
			isLocalMode = !configOptions.get("mode").equals("distributed");
			enableHistoryCacheMonitoring = Boolean.parseBoolean(configOptions.get("enableHistoryCacheMonitoring"));
			enableMonitoringWithoutSubscriptions = Boolean.parseBoolean(configOptions.get("enableMonitoringWithoutSubscriptions"));
			enableTrafficOverseeing = Boolean.parseBoolean(configOptions.get("enableTrafficOverseeing"));
			
			historyCacheTime = Integer.parseInt(configOptions.get("historyCacheTime"));
			
			if(configOptions.get("processorCount") != null)
				processorCount = Integer.parseInt((configOptions.get("processorCount")));
			if(configOptions.get("pusherCount") !=null)
				pusherCount = Integer.parseInt((configOptions.get("pusherCount")));
			if(configOptions.get("policyGeneratorCount") !=null)
				policyGeneratorCount = Integer.parseInt((configOptions.get("policyGeneratorCount")));
			
			if(configOptions.get("pushingResult") !=null)
				pushingResult = Boolean.parseBoolean((configOptions.get("pushingResult")));
			if(configOptions.get("builtInADSPolicyGenerator") !=null)
				builtInADSPolicyGenerator = Boolean.parseBoolean((configOptions.get("builtInADSPolicyGenerator")));			

			buildFlowMonitorTopology(isLocalMode);
			
			log.info("FLOW MONITOR IS RUNNING IN [{}] MODE. SUBSCRIPTION SCRIPTS ARE SAVED IN {}",isLocalMode?"LOCAL":"DISTRIBUTED",scriptFilePrefix);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Could not load default configure file", e);
			System.exit(1);
		}
        serviceRegistry.registerService("", this);
	    log.info("BUPT flow monitor started...");  
	}

	//if some flows should be sent, return a list, otherwise null.
	@SuppressWarnings("unchecked")
	Collection<ISubscriptionResult> processEventWithCondition(EventSubscriptionInfo subscription, FlowInfo flow){
		EventSubscription eventSubscription = subscription.getEventsubscription();
		Object res = processEventsWithCondition(eventSubscription, SubscribedValueCategory.ROOT, null, flow);
		if(res != null){
			List<ISubscriptionResult> ires = (List<ISubscriptionResult>)(List<?>)res;
			return ires;
		}
		return null;
	}
	//The object must implements IDBObject
	//db operation: input = null
	//obj operation: input = collection
	@SuppressWarnings("unchecked")
	public static Object processEventsWithCondition(EventSubscription subscription, SubscribedValueCategory parentSubscribeValueCategory, Collection<Object> input, Object ref){	    
		String queryCondition = "";
		EventSubscription.Operator  op = subscription.getOperator();
		SubscribedValueCategory svt = subscription.getSubscribedValueCategory();
		
		if(parentSubscribeValueCategory == SubscribedValueCategory.ROOT){// the root layer
			parentSubscribeValueCategory = svt;
		}		
		
		//Compound Subscription
		if(subscription instanceof CompoundEventSubscription){
			CompoundEventSubscription cs = (CompoundEventSubscription) subscription;
			EventSubscription[] children = cs.getSubscriptionList(); 
			
			//DB query
			if(parentSubscribeValueCategory == SubscribedValueCategory.RECORD){ //a subclause of a db query
				queryCondition += "(";
				for(int i = 0; i<children.length-1;i++){
					queryCondition += ("(" +processEventsWithCondition(children[i], parentSubscribeValueCategory, null, ref)+ ")");
					queryCondition += op.toString();
				}
				queryCondition += ("(" +processEventsWithCondition(children[children.length-1], parentSubscribeValueCategory, null, ref)+ ")");
				queryCondition += ")";

				if(svt == SubscribedValueCategory.PARENT) // no need to query for db
					return queryCondition;
				else{
					return storageService.executeQuery(flowTableName, queryCondition, new RowOrdering(tableTime), FlowInfo.class);
				}
			}
			//Object test
			else if(parentSubscribeValueCategory == SubscribedValueCategory.OBJECT){//a sub condition test of objects

				if(input.size()!=1)
					log.error("input size not equal to 1!");
				Object obj = input.iterator().next();
				
				List<Object> ol = new ArrayList<Object>();
				ol.add(obj);
				ISubscriptionResult resobj = null;
				for(int i = 0; i<children.length-1;i++){
					resobj = (ISubscriptionResult)processEventsWithCondition(children[i], parentSubscribeValueCategory, ol, ref);
					if(op == Operator.AND && resobj == null){
						return null;
					}
					else if(op == Operator.OR && resobj != null)
						return resobj;
				}
				return resobj;
			}
			else if(parentSubscribeValueCategory == SubscribedValueCategory.OBJLIST){ // a list operator, such as count/size or iteration of all items				
				Iterator<Object> it = input.iterator();
				Collection<ISubscriptionResult> resobjs = new ArrayList<ISubscriptionResult>();
				while(it.hasNext()){
					Object obj = it.next();
					List<Object> ol = new ArrayList<Object>();
					ol.add(obj);
					ISubscriptionResult resobj = null;
					for(int i = 0; i<children.length;i++){
						resobj = (ISubscriptionResult)processEventsWithCondition(children[i], parentSubscribeValueCategory, ol, ref);
						if(op == Operator.AND && resobj == null){
							break;
						}
						else if(op == Operator.OR && resobj != null){
							resobjs.add(resobj);
							break;
						}
						else if(i == children.length-1 && resobj != null)
							resobjs.add(resobj);
					}
				}
				return resobjs;
			}
		}
		else if(subscription instanceof ListOperatorEventSubscription){
			ListOperatorEventSubscription los = (ListOperatorEventSubscription)subscription;

			Object v = los.getValue();
			Class<?> vtype = los.getValueType();
			op = los.getOperator();					
			
			if(parentSubscribeValueCategory == SubscribedValueCategory.OBJLIST){//should be count operator
				EventSubscription child = los.getSubscription();
				Collection<ISubscriptionResult> resobjs = (Collection<ISubscriptionResult>)(Collection<?>)processEventsWithCondition(child, parentSubscribeValueCategory, input, ref);

				Operator listOperator = ((ListOperatorEventSubscription) subscription).getListOperator();
				if(listOperator==Operator.COUNT){
					//compare count
					int count = resobjs.size();

					Object refv = los.calcValueExpression((String)v, vtype, ref);			
					if(los.calcObj(count, (int)refv, op))
						return resobjs;
					else
						return null;
				}
			}
		}
		//Leaf node Subscription
		else if(subscription instanceof OperatorEventSubscription){

			if(parentSubscribeValueCategory == SubscribedValueCategory.OBJECT
					|| parentSubscribeValueCategory == SubscribedValueCategory.OBJLIST){
				if(input.size()!=1)
					log.error("input size not equal to 1!");
				Object obj = input.iterator().next();
				
				OperatorEventSubscription operatorEventSubscription = (OperatorEventSubscription)	subscription;
				String key = operatorEventSubscription.getSubscribedKey();
				Object v = operatorEventSubscription.getValue();
				Class<?> vtype = operatorEventSubscription.getValueType();
				op = operatorEventSubscription.getOperator();						
				String ops = operatorEventSubscription.getOperatorString();
				
				//Special values
				//NOW +/- 5min
				if(vtype == Date.class && v instanceof String){
					String[] vs = ((String)v).split(" ");
					String datepos = "";
					String date1 = "";
					int d=0, h = 0, m = 0, s = 0;
					for(int i =0;i<vs.length;i++){
						if(vs[i].endsWith("d"))
							d =  Integer.parseInt(vs[i].substring(0, vs[i].length()-1));
						else if(vs[i].endsWith("h"))
							h =  Integer.parseInt(vs[i].substring(0, vs[i].length()-1));
						else if(vs[i].endsWith("m"))
							m =  Integer.parseInt(vs[i].substring(0, vs[i].length()-1));
						else if(vs[i].endsWith("s"))
							s =  Integer.parseInt(vs[i].substring(0, vs[i].length()-1));
						else if(vs[i].equals("-"))
							datepos = vs[i];
						else if(vs[i].equals("+"))
							datepos = "";
						else if(date1.equals("")){//should be the date
							if(vs[i].indexOf("NOW")>=0)
								date1 = DateTimeUtils.getNow();
							else//should be 'xxxx-xx-xx xx:xx:xx' format
								date1 = vs[i];
						}
					}
					//ADDTIME('2007-12-31 23:59:59.999999', '1 1:1:1.000002');
					v = String.format("%s(%s,'%s%d %d:%d:%d')", DateTimeUtils.getAddTime(), date1, datepos, d, h, m, s);
				}
				
				if(svt == SubscribedValueCategory.SQL){
					queryCondition = operatorEventSubscription.calcSQLValueExpression((String)v, obj);
					return storageService.executeQuery(flowTableName, queryCondition, null, FlowInfo.class);
				}
				else if(svt == SubscribedValueCategory.OBJECT){
					Object refv = operatorEventSubscription.calcValueExpression((String)v, vtype, ref);				
					if(operatorEventSubscription.compareObj(obj, refv))
						return obj;
					else
						return null;				
				}
				else {// SubscribedValueCategory.RECORD
					////such as dbobj.x > "evtobj.y+5"
					// or dbobj.x > 100
					//First calculate expr
					Object resv = operatorEventSubscription.calcValueExpression((String)v, vtype, obj);
					queryCondition = String.format("%s %s %s", key, ops, resv);
					return queryCondition;		
				}
			}
			else {
				log.error("OperatorEventSubscription should have OBJECT as subscribed category, but {} used", parentSubscribeValueCategory);				
			}
		}
		else{
			log.error("No such condition {}", subscription);
			return "";
		}
		
		log.error("Should not be here, return null");
		return null;
	}
	
	static String portscanResString = null;
	
	/**
	 * Get clojure-scripted subscription infos from text file
	 */
	static final String clojureNS ="com.core";
	static final String clojureProcessFunctionName = "proccessFlowmapping";// "checkFlow";
	static String scriptFilePrefix = "./script/";//"D:\\securitycontroller\\script\\";//"/usr/src/securitycontroller/script/";// = ;
	
	static Var loadFromScriptEventSubscription(String scriptFileName){
		//String scriptFile = subscription.get;// = subscription.getSubscriptionScript();
		try {
			RT.loadResourceScript(scriptFileName);//.loadResourceScript(fileName);	
			Var var = RT.var(clojureNS, clojureProcessFunctionName);
			return var;
		}
		catch (Exception e){
			log.error(e.getLocalizedMessage());
			return null;
		}		
	}
	static Var executor=null;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static List<ISubscriptionResult> processEventsBySubscriptionScript(EventSubscriptionInfo subscriptionInfo, Map<String, FlowInfo> flowMapping){
		List<ISubscriptionResult> results = null;
		if(!(subscriptionInfo.getEventsubscription() instanceof ScriptEventSubscription)){
			return null;
		}
		if(executor==null){
			executor = loadFromScriptEventSubscription(subscriptionInfo.getSubscriptionId()+".clj");
			log.info("LOADED SCRIPT SUBSCRIPTION CONDITION FROM {}.clj",subscriptionInfo.getSubscriptionId());
		}
		if(executor!=null){
			try{
				//2014-01-14 wxt
				Object invokeResObj = executor.invoke(flowMapping,fmInstance);
				if(invokeResObj instanceof List){
					// result is list
					List tmpList = (List) invokeResObj;
					results = tmpList;
					/*
					Iterator<FlowInfo> it = flowMapping.values().iterator();
					FlowInfo refFlow = it.next();
					
					log.info("COMPARING FLOWS WITH REF FLOW '{}'",refFlow.getId());
					while(it.hasNext()){
						FlowInfo objFlow = it.next();
						if((boolean)executor.invoke(objFlow,refFlow)){
							log.info("SCRIPT CONDITION '{}' CHECK RESULT: [MATCHED].",objFlow.getId());
							results.add(objFlow);
						}
						else 
							log.info("SCRIPT CONDITION '{}' CHECK RESULT: [NOT MATCHED].",objFlow.getId());
					}
					*/
					
				}
				else {
					// result is string
					System.err.println(invokeResObj);
					portscanResString = invokeResObj.toString();
					log.info(" invoke process result:{}",invokeResObj);
				}
			}
			catch (Exception e){
				log.error(e.getLocalizedMessage());
				e.printStackTrace();
				results = null;
			}
		}
		return results;
	}

	public HashMap<String, FlowInfo> getSuspiciousFlows() {
		return suspiciousFlows;
	}

	public void setSuspiciousFlows(HashMap<String, FlowInfo> suspiciousFlows) {
		this.suspiciousFlows = suspiciousFlows;
	}
	public  HashMap<String, FlowAvgCount> getFlowAvgCount() {
		return flowAvgCount;
	}

	public void setFlowAvgCount(HashMap<String, FlowAvgCount> flowAvgCount) {
		this.flowAvgCount = flowAvgCount;
	}
}

