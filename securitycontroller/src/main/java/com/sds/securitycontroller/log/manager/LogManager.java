/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.log.manager;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.app.manager.AppDataPusher;
import com.sds.securitycontroller.app.manager.AppPushRequest;
import com.sds.securitycontroller.device.manager.IDeviceManagementService;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscription;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.ReportEventSubscription;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.log.LogPushRequest;
import com.sds.securitycontroller.log.Report;
import com.sds.securitycontroller.log.ReportEventArgs;
import com.sds.securitycontroller.log.ReportItem;
import com.sds.securitycontroller.log.ReportLevel;
import com.sds.securitycontroller.log.ReportType;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.restserver.RestResponse;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.utils.HTTPUtils;
import com.sds.securitycontroller.utils.ResponseFactory;

public class LogManager implements ILogManagementService,
		ISecurityControllerModule, IEventListener {

	protected static Logger log = LoggerFactory.getLogger(LogManager.class);
	protected IEventManagerService eventManager;
	protected IDeviceManagementService deviceManager;
	protected SubscriberManager subscriberManager;
	protected IStorageSourceService storageSource;
	protected IRestApiService restApi;
	
	protected Map<EventType, Map<String, Map<String, EventSubscriptionInfo>>> subscriptionTypedMap = 
			new HashMap<EventType, Map<String, Map<String, EventSubscriptionInfo>>>();

    protected IRegistryManagementService serviceRegistry;

	public final String logTableName = "logs";
	public final String reportTableName = "reports";
	protected final String primaryKeyName = "date";
	protected boolean useDB = true;
	
	protected static final int maxThreadPool = 20;
	protected ExecutorService excutorService = Executors
			.newFixedThreadPool(maxThreadPool);
	
	private Map<String, String> tableColumns = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("id", "VARCHAR(50)");
			put("start_time", "VARCHAR(30)");
			put("complete_time", "VARCHAR(30)");
			put("type", "VARCHAR(30)");
			put("content", "MEDIUMTEXT");

		}
	};

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		return l;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(ILogManagementService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IStorageSourceService.class);
		if (this.useDB)
			l.add(IStorageSourceService.class);
		return l;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
		this.deviceManager = context
				.getServiceImpl(IDeviceManagementService.class, this);
		this.storageSource = context
				.getServiceImpl(IStorageSourceService.class, this);
		this.restApi = context.getServiceImpl(IRestApiService.class);
		eventManager.addEventListener(EventType.RECEIVED_LOG, this);
		// listening report event 0404
		eventManager.addEventListener(EventType.RECEIVED_ALERT,this);
		eventManager.addEventListener(EventType.ATTACK_WARNING,this);
		eventManager.addEventListener(EventType.NEW_REPORT_ITEM,this);
		eventManager.addEventListener(EventType.APP_REMOVED, this);
		// listening subscription adding 0404
		eventManager.addEventListener(EventType.ADD_SUBSCRIPTION,this);

		if (this.useDB)
			storageSource = context.getServiceImpl(IStorageSourceService.class, this);

	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	

		// read our config options
		Map<String, String> configOptions = context.getConfigParams(this);
		String subscribersTable = configOptions.get("subscribersTable");
		if (subscribersTable != null && storageSource != null) {
			this.subscriberManager = new SubscriberManager(this.storageSource,
					subscribersTable);
		}
		
		log.info("BUPT security controller log manager initialized.");
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		if (this.useDB) {
			storageSource.createTable(logTableName, tableColumns);
			storageSource.setTablePrimaryKeyName(this.logTableName,
					this.primaryKeyName);
		}
		LogManagerRoutable r = new LogManagerRoutable();
        restApi.addRestletRoutable(r);
        serviceRegistry.registerService(r.basePath(), this);
		// Update information from database
		if (this.subscriberManager != null) {
			List<EventSubscriptionInfo> infos = this.subscriberManager
					.getAllSubscribers();
            if (null != infos) {
                for (EventSubscriptionInfo condition : infos) {
                    this.updateLocalSubscriptionInfo(condition);
                }
            }
		}
		
		log.info("BUPT security controller log manager started.");
	}

	@Override
	public void processEvent(Event e) {
		EventType type = e.type;
		switch (type) {
		case RECEIVED_LOG: {
			if (e.args instanceof ReportEventArgs) {
				ReportEventArgs args = (ReportEventArgs) e.args;
				Report report = args.report;
				addReport(report);

			} else if (e.args instanceof ConsoleLogEventArgs) {

			}
		}
			break;
		case NEW_REPORT_ITEM: {
			Map<String, Map<String, EventSubscriptionInfo>> info;
			if ((info = subscriptionTypedMap.get(e.type)) != null) {
				log.info(
						"Start processing a [{}] report event(gen_time={})...\t Active Thread: {}",
						e.type, e.time,
						((ThreadPoolExecutor) this.excutorService)
								.getActiveCount());
				this.excutorService.execute(new ReportAnalyzerThread(info, e));
				/*log.info("\n\tAfter excute, Active thread: {}",
						((ThreadPoolExecutor) this.excutorService)
								.getActiveCount());
				*/
			}
		}
			break;
		case APP_REMOVED: {

			App app = (App) e.subject;
			log.info("Will remove app id:{} subscription information",
					app.getId());
			this.subscriberManager.deleteSubscriber(app.getId(), null);
			this.deleteSubscriptionInfo(app.getId(), null, EventType.NEW_REPORT_ITEM);
		}
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addReport(Object entity) {
		try {
			List<ReportItem> list = (List<ReportItem>) entity;
			for (ReportItem item : list) {
				
				if (storageSource.insertEntity(item.getTableName(),
						item) > 0) {					
				} else {
					log.error("save report error {}", item.getRawData());
				}
				
				
				//------------
				//对于扫描结果重新处理，只推送基本信息+taskd_id
				if (item.getCategory().equals("SCAN"))
				{
					Map<String, Object> newResult = new HashMap<String,Object>();
					Map<String, Object> oriResult = item.getMaps();
					for(String key: oriResult.keySet())
					{						
						if(key != "content"){
							newResult.put(key, oriResult.get(key));
						}
						else
						{
							Map<String,Object> contentObj = (Map<String, Object>) oriResult.get("content");
							if(contentObj != null )
							{
								if(contentObj.containsKey("task_id")){
									newResult.put("task_id", contentObj.get("task_id"));
								}							 
							}						 
						}
					item.setMaps(newResult);
					}
				}
				//-----------
			}

			// Add subscribe
			ReportEventArgs args = new ReportEventArgs(list);
			eventManager.addEvent(new Event(EventType.NEW_REPORT_ITEM, null,this, args));

			return true;
		} catch (Exception e) {
			log.error("Add report error {}", e.getMessage());
			return false;
		}
	}

	@Override
	public boolean addReport(Report report) {
		if (this.useDB) {
			int res = storageSource.insertEntity(reportTableName, report);
			if (res <= 0)
				log.error("Errors! Cannot insert the log into the DB...");
			else {

			}
		}
		// is attack
		if (report.getReportLevel() == ReportLevel.ATTACK) {
			ReportEventArgs args = new ReportEventArgs(report);
			eventManager.addEvent(new Event(EventType.ATTACK_WARNING, null,
					this, args));
			// eventManager.addEvent(new Event(EventType.RECEIVED_LOG, null,
			// this, args));
		} else if (report.getReportLevel() == ReportLevel.ALERT) {
			// TODO for wxt
			ReportEventArgs args = new ReportEventArgs(report);
			eventManager.addEvent(new Event(EventType.RECEIVED_ALERT, null,
					this, args));
			// add push to app with pushReport() if it is true error
			if (report.getReportType() == ReportType.SCAN) {
				// if match...
			}
		}
		return true;
	}

	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		/*
		 * this.subscriptionInfos.put(type, condition);
		 * 
		 * log.debug("addListenEventCondition: type:{}, content:{}",
		 * type.toString(), condition.toString()); //0404 IEventManagerService
		 * eventManager=EventManager.getInstance();
		 * eventManager.addConditionToListener(condition);
		 */
	}

	public static RestResponse pushReport(LogPushRequest req) {
		String url = req.getLogurl();
		log.info("Starting push data to :{}", url);
		Report data = req.getLogdata();

		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter();
		String jsonReq = "";
		JsonGenerator gen;
		try {
			gen = new JsonFactory().createGenerator(writer);
			mapper.writeValue(gen, data);
			jsonReq = writer.toString();
			gen.close();
			writer.close();
		} catch (IOException e) {
			log.error("Error when convert REST request: {}", e.getMessage());
			return new RestResponse("error",
					"error when convert REST request: " + e.getMessage());
		}
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		String jsonResp = HTTPUtils.httpPost(url, jsonReq, headers);
		if (jsonResp == null) // Error
			return new RestResponse("error", "return null response");
		try {
			JsonNode root = mapper.readValue(jsonResp, JsonNode.class);
			String status = root.path("status").asText();
			String result = root.path("result").asText();
			RestResponse response = new RestResponse(status, result);
			return response;
		} catch (IOException e) {
			log.error("error response: {}", e.getMessage());
			return new RestResponse("error", "error response: "
					+ e.getMessage());
		}
	}

	@Override
	public Report getReport(String id) {
		Report report = (Report)storageSource.getEntity(reportTableName, id, Report.class);
			return report; 
	}

	@Override
	public List<Report> queryReport(Object query) {
		try {
			ReportItem reportItem = (ReportItem) query;
			Map<String, Object> q = reportItem.getMaps();
			List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
			for(Entry<String, Object> entry: q.entrySet())
				items.add(new QueryClauseItem(entry.getKey(),
						entry.getValue(),QueryClauseItem.OpType.EQ));
			QueryClause qc = new QueryClause(items, this.logTableName, null, null);
			@SuppressWarnings("unchecked")
			List<Report> reports = (List<Report>)storageSource.executeQuery(qc, Report.class);
			return reports;
			/*
			LogQuery logquery = new LogQuery(reportItem.getMaps());
			DBObject queryObj = logquery.processQuery();
			log.info(queryObj.toString());
			List<?> result = null;

			CursorObject cursorObj = new CursorObject();
			cursorObj.addHideFieldObject(ReportItem.key_Id);
			// cursorObj.addHideFieldObject(ReportItem.keyRegisterId);
			cursorObj.addHideFieldObject(ReportItem.keyHashId);
			cursorObj.addHideFieldObject(ReportItem.keyOriginalTableName);

			for (String tableName : logquery.getTableNameList()) {
				Object temp = storageSource.executeQueryRawType(tableName,
						queryObj, cursorObj);// reportItem.getTableName()
				log.info("now current table:{}", tableName);
				if (result == null) {
					result = (List<?>) temp;
				} else {
					result.addAll((Collection) temp);
				}
			}
			return reportItem.CreateReport(result);
			*/
			

		} catch (Exception e) {
			log.error("Query error:{}", e.getMessage());
			return null;
		}

		// return String.format("{\"status\" : \"%s\", \"details\" : \"%s\"}",
		// "ok", "");
	}

	// add @ 0404

	/**
	 * Thread handling incoming report
	 * 
	 * @author wxt
	 * 
	 */
	class ReportAnalyzerThread implements Runnable {

		public Map<String, Map<String, EventSubscriptionInfo>> subscriptionInfos;
		public Event e;

		public ReportAnalyzerThread(
				Map<String, Map<String, EventSubscriptionInfo>> subscriptionInfos,
				Event e) {
			this.subscriptionInfos = subscriptionInfos;
			this.e = e;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			for (Map<String, EventSubscriptionInfo> mapSubs : subscriptionInfos
					.values()) {
				for (EventSubscriptionInfo condition : mapSubs.values()) {
					if (!(condition.getEventsubscription() instanceof ReportEventSubscription))
						continue;

					List<Object> results = analyzeReport(
							(ReportEventSubscription) condition
									.getEventsubscription(),
							((ReportEventArgs) (e.args)).reportList);
					// push to subscriber
					if (results != null) {
						log.info("analyze results: {} results found.",
								results.size());
						pushReportDataToSubscriber(condition, results);
					}

				}
			}
		}
	}

	/*
	 * [type, map<String, EventSubscriptionInfo>] | \[getSubscriptionId,
	 * EventSubscriptionInfo]
	 */

	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		do {
			// get subscription info
			// if not a report event subscription: not recording
			EventSubscription subscription = condition.getEventsubscription();

			if (!(subscription instanceof ReportEventSubscription)) {
				log.info("Not receive a '{}' subscription.", type);
				break;
			}

			// save condition
			if (this.subscriberManager.updateSubscriber(condition)) {
				this.updateLocalSubscriptionInfo(condition);
			}
			else{
				log.error("Save subscriber information failed.");
			}

			// update condition from database

		} while (false);

	}
	protected void deleteSubscriptionInfo(String appId, String subsName, EventType event){
		if(appId != null && event != null){
			if(this.subscriptionTypedMap.containsKey(event)){
				Map<String, Map<String, EventSubscriptionInfo>> event_conditions = this.subscriptionTypedMap.get(event);
				if(event_conditions.containsKey(appId)){
					if(subsName != null){
						Map<String, EventSubscriptionInfo> app_conditions = event_conditions.get(appId);
						app_conditions.remove(subsName);
					}
					else{
						event_conditions.remove(appId);
					}
				}
			}
			
			log.info("\n\t Update subscription: {}\n", this.subscriptionTypedMap);
		}
	}
	

	protected void updateLocalSubscriptionInfo(EventSubscriptionInfo condition) {

		ReportEventSubscription reportEventSubscription = (ReportEventSubscription) condition.getEventsubscription();
		if (!reportEventSubscription.CreateComparator()) {
			log.error("Create comparator error '{}'.",reportEventSubscription.getDetails());
			return;
		}

		// restore in subscription typed map
		Map<String, Map<String, EventSubscriptionInfo>> event_conditions = null;
		if (this.subscriptionTypedMap.containsKey(condition.getEventype())) {
			event_conditions = this.subscriptionTypedMap.get(condition.getEventype());
		} else {
			event_conditions = new HashMap<String, Map<String, EventSubscriptionInfo>>();
			this.subscriptionTypedMap.put(condition.getEventype(),
					event_conditions);
		}

		Map<String, EventSubscriptionInfo> app_conditions = null;
		if (event_conditions.containsKey(condition.getSubscibedAppId())) {
			app_conditions = event_conditions.get(condition.getSubscibedAppId());
		} else {
			app_conditions = new HashMap<String, EventSubscriptionInfo>();
			event_conditions.put(condition.getSubscibedAppId(), app_conditions);
		}

		app_conditions.put(condition.getSubscriptionId(), condition);
		log.info("\n\t Update subscription: {}\n", this.subscriptionTypedMap);
		//log.info("Received a '{}' subscription(id={}, {})",
		//		condition.getEventype(),
		//		condition.getSubscriptionId(), 
		//		condition.toString());

		//log.info("\n\tUpdate subscription information succeed!");
	}

	/**
	 * return report analysis result
	 * 
	 * @param condition
	 *            : currently only distinguish condition's type
	 * @param report
	 * @return report analysis result
	 */
	/*
	 * List<ISubscriptionResult> analyzeReport(ReportEventSubscription
	 * condition, Report report){ //compare condition target type and reporter
	 * type (app or dev) if(!(
	 * report.getReporterType().equals(condition.getReporterType()) &&
	 * report.getTargetType().equals(condition.getTargetType()) ) ){ // if these
	 * types unmatched, return null return null; } List<ISubscriptionResult>
	 * results = new ArrayList<ISubscriptionResult>(); ReportSubscriptionResult
	 * rsr = new ReportSubscriptionResult(report.getContent(),
	 * report.getReportType().toString(), report.getTargetType().toString(),
	 * report.getTargetId()); if(rsr!=null) results.add(rsr); return results; }
	 */

	List<Object> analyzeReport(ReportEventSubscription condition,
			List<ReportItem> reports) {

		try {
			List<Object> results = null;
			for (ReportItem report : reports) {
				do {
					if (condition.getComparatorObject() == null) {
						break;
					}

					if (condition.getComparatorObject().CompareReportItem(
							report)) {
						if (results == null) {
							results = new ArrayList<Object>();
						}

						results.add(report.getPusherMap());
						break;
					}

				} while (false);
			}
			return results;
		} catch (Exception e) {
			log.error("Analyze report error:{}", e.getMessage());
			return null;
		}
	}

	/**
	 * push to app
	 * 
	 * @param condition
	 * @param resList
	 */
	void pushReportDataToSubscriber(EventSubscriptionInfo condition,
			List<Object> resList) {
		String url = condition.getSubscribeUrl();
		try {
			AppPushRequest req = new AppPushRequest(url,
					condition.getSubscriptionId(), resList,
					condition.getSubscibedAppId());
			AppDataPusher pusher = new AppDataPusher(req);
			ResponseFactory response = pusher.pushDataToApp();
			log.info(" pushed analysed result to subscriber app: {}",
					response.toString());
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public Object handleRPCRequest(String methodName, Object[] args) {
		switch (methodName) {
		case "deletesubscriber":
			if (this.subscriberManager != null) {
				int ret = this.subscriberManager.deleteSubscriber(
						(String) args[0], (String) args[1]);
				if (ret >= 0) {
					this.deleteSubscriptionInfo((String) args[0], (String) args[1], EventType.NEW_REPORT_ITEM);
				}

				return ret;
			}
			return 0;
		default:
			break;
		}
		return null;
	}

	@Override
	public List<Map<String,Object>> getScanReport(String map, String reduce) {
		//TODO unimplemented
		//return storageSource.excuteMapReduce("SCAN", map, reduce);
		return new ArrayList<Map<String,Object>> ();
	}
}
