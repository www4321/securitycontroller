/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.data.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.utils.Time;

import com.sds.securitycontroller.data.FeildItem;
import com.sds.securitycontroller.data.TableItem;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.log.ReportEventArgs;
import com.sds.securitycontroller.log.ReportItem;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.storage.AbstractStorageSource;
import com.sds.securitycontroller.storage.CommonTool;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.LongObject;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.storage.QueryClauseItem.OpType;
import com.sds.securitycontroller.storage.RowOrdering;
import com.sds.securitycontroller.storage.jdbc.JDBCSqlStorageSource;


public class DataManager implements ISecurityControllerModule,
		IDataManagerService, IEventListener {
	
	protected static Logger logger = LoggerFactory.getLogger(DataManager.class);
	
	protected static final int maxThreadPool = 5;
	protected ExecutorService service = Executors.newFixedThreadPool(maxThreadPool);
	
	protected AbstractStorageSource espcStorageSource;
	protected IStorageSourceService scStorageSource;
	protected IEventManagerService eventManager;
    protected List<TableItem> tableDescriptionList = new ArrayList<TableItem>();
    protected String espctableinfo = "espc_table";
    protected boolean enableSynchronize = true;
	protected int synchronizenum = 100;

	@Override
	public void processEvent(Event e) {

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
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		return l;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> 
		m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IDataManagerService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IStorageSourceService.class);
		return l;
	}


	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		

		this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
		//this.espcStorageSource = context.getServiceImpl(IStorageSourceService.class, this, "espc_dbdriver");
		this.espcStorageSource = new JDBCSqlStorageSource();
		this.espcStorageSource.init(context, this);
		this.scStorageSource = context.getServiceImpl(IStorageSourceService.class, this, "dbdriver");
		
		Map<String, String> configOptions = context.getConfigParams(this);
		String result;
		if((result = configOptions.get("espctableinfo")) != null){
			this.espctableinfo = result;
		}
		
		if((result = configOptions.get("enableSynchronize")) != null){
			 this.enableSynchronize = result.trim().equals("true");
		}
		
		if((result = configOptions.get("synchronizenum")) != null){
			try{
				this.synchronizenum = Integer.valueOf(result);
			}
			catch(Exception e){
				logger.error("synchronizenum is not a number.");
			}
			 
		}
		
        logger.info("\n\t######################################################################\n\tespctableinfo={}, enableSynchronize={}, synchronizenum={}\n\t######################################################################", 
        		this.espctableinfo, 
        		this.enableSynchronize,
        		this.synchronizenum);
		
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		
		new Thread(new Runnable(){
			@Override
			public void run(){
				if(loadConfig()){
					if(enableSynchronize){
						worker();
					}else{
						logger.info("\n\t######################################################################\n\tConfig enable synchronization false.\n\t######################################################################");
						
					}
				
				}
				else{
					logger.error("\n\t######################################################################\n\tLoad config to synchronize failed.\n\t######################################################################");
					
				}
				
			}
			
		}).start();
		
	}

	@SuppressWarnings("unchecked")
	protected boolean loadConfig(){
		try{
			QueryClause qc = new QueryClause(this.espctableinfo);
			List<TableItem> tableItems = (List<TableItem>)this.scStorageSource.executeQuery(qc, TableItem.class);
			
			
			for(TableItem tableItem: tableItems){
				this.tableDescriptionList.add(tableItem);
				tableItem.currentId = this.findLastId(tableItem);
				this.adjustCurrentId(tableItem);
				logger.info("[{}--max id:{}, old table:{}]", tableItem.targetTableName, tableItem.currentId, tableItem.espcTableName);

			}			
			return true;
		}
		catch(Exception e){
            e.printStackTrace();
			logger.error("read config error {}", e.getMessage());
			return false;
		}
	}
	
	protected void adjustCurrentId(TableItem tableItem)
	{
		QueryClause qc = new QueryClause("", tableItem.espcTableName, null, new RowOrdering(TableItem.keyId, RowOrdering.Direction.DESC), "limit 1");
        qc.setType(QueryClause.QueryClauseType.EMPTY);
		@SuppressWarnings("unchecked")
		List<LongObject> items = (List<LongObject>)this.espcStorageSource.executeQuery(qc, LongObject.class);
		
		if(items == null || items.size()== 0 )
		{
			logger.debug("[adjustCurrentId] result == null");
			return;
		}
		
		int id = items.get(0).getValue().intValue();
		
		if(id > tableItem.currentId)
		{
			logger.info("[adjustCurrentId] adjust {}'s id to {}", tableItem.espcTableName, id);
			tableItem.currentId = id;
		}
	}

	protected long findLastId(TableItem tableItem){
		QueryClause qc = new QueryClause(null, tableItem.targetTableName, null, new RowOrdering(TableItem.keyId, RowOrdering.Direction.DESC), "limit 1");
		@SuppressWarnings("unchecked")
		List<ReportItem> resultList =  (List<ReportItem>)
			this.scStorageSource.executeQuery(qc, ReportItem.class);
		return resultList==null || resultList.size() < 1  ? 0 :(long) resultList.get(0).id;
	}
	
	protected ReportItem createReportItem(IAbstractResultSet resultSet, TableItem table){
		ReportItem item = new ReportItem();
		Map<String, Object> reportMap = new HashMap<String, Object>();
		
		reportMap.put(ReportItem.keyCategory, 	table.defaultcategory);
		reportMap.put(ReportItem.keyType, 		table.defaulttype);
		reportMap.put(ReportItem.keyObjectType,	table.defaultobject_type);
		reportMap.put(ReportItem.keyOriginalTableName,	table.espcTableName);
		
		if(table.currentId < resultSet.getLong(TableItem.keyId)){
			table.currentId = resultSet.getLong(TableItem.keyId);
		}
		
		for(String fieldName : table.tableFieldsMap.keySet()){
			FeildItem feild = table.tableFieldsMap.get(fieldName);
			reportMap.put(feild.getCorrespondence(), 
					this.getDataFromType(feild.getType(), 
					resultSet, 
					fieldName));
		}
		
		item.setMaps(reportMap);
		return item;
	}
	
	protected Object getDataFromType(String objectType, IAbstractResultSet resultSet, String key){
		switch(objectType){
		case "int":
			return resultSet.getInt(key);
		case "long":
			return resultSet.getLong(key);
		case "float":
			return resultSet.getFloat(key);
		case "double":
			return resultSet.getDouble(key);
		case "bytea":
			return resultSet.getByteArray(key);
		case "Date":
			try {
				return CommonTool.GetDateFromStr(resultSet.getString(key));
			} catch (Exception e) {
				return resultSet.getString(key);
			}
		case "String":
		default:
			return resultSet.getString(key);
		}
		
	}

	
	protected void worker() {
		
		while (true) {
			try {
				synchronizeDatabase();
				
				Time.sleep(1000);
			} catch (InterruptedException  e) {
				logger.debug(e.getMessage());
			}
			
		}
		
	}
	
	protected void synchronizeDatabase() throws InterruptedException{
		for (TableItem tableItem : this.tableDescriptionList) {
			try {
				this.service.execute(new ReadAndWriteThread(tableItem));
				
			} catch (Exception e) {
				logger.error("Synchronize database frome espc, error:{}",e.getMessage());
				Time.sleep(2);
			}
		}
		
	}
	
	class ReadAndWriteThread implements Runnable{

		protected TableItem tableItem;
		
		public ReadAndWriteThread(TableItem tableItem){
			this.tableItem = tableItem;
		}
		
		@Override
		public void run() {
			try{
				synchronized(this.tableItem){
					do{
						logger.debug("start synchronize talbe {} from {} to {}", 
								tableItem.espcTableName, 
								tableItem.currentId, 
								tableItem.currentId + synchronizenum);
						List<QueryClauseItem> clauseItems = new ArrayList<QueryClauseItem>();
						clauseItems.add(new QueryClauseItem(TableItem.keyId, tableItem.currentId, OpType.GT));
						clauseItems.add(new QueryClauseItem(TableItem.keyId, tableItem.currentId + synchronizenum, OpType.LTE));						
						QueryClause query = new QueryClause(clauseItems, tableItem.espcTableName, null, null);
						
						@SuppressWarnings("unchecked")
						List<ReportItem> reports = (List<ReportItem>)espcStorageSource.executeQuery(query, ReportItem.class);
						if(null == reports){
							logger.debug("end synchronize[result == null]");
							break;
						}
						
						long saveCount = 0;
						for(ReportItem report: reports) {
							Map<String, Object> reportMap = new HashMap<String, Object>();
							reportMap.put(ReportItem.keyCategory, 	tableItem.defaultcategory);
							reportMap.put(ReportItem.keyType, 		tableItem.defaulttype);
							reportMap.put(ReportItem.keyObjectType,	tableItem.defaultobject_type);
							reportMap.put(ReportItem.keyOriginalTableName,	tableItem.espcTableName);
							report.setMaps(reportMap);

							if(tableItem.currentId < report.id){
								tableItem.currentId = report.id;
							}
							
							saveCount += scStorageSource.insertEntity(
									report.getTableName(), report.getMaps());
							
							//Add subscribe
							reports.add(report);
						}
						
						logger.debug("saveCount = {}, reports.size() = {}", saveCount, reports.size());
						
						if (reports.size() > 0){
							ReportEventArgs args = new ReportEventArgs(reports);
							eventManager.addEvent(new Event(EventType.NEW_REPORT_ITEM, null, this, args));
							
							if(saveCount > 0){
								logger.info("@##############@Synchronize database frome espc table [{}], data count:{}", tableItem.espcTableName, saveCount);
							}
						}
						
						logger.debug("end synchronize");
						
					}
					while(false);
				}
			}
			catch(Exception e){
				logger.error("Synchronize database frome espc, error:{}", e.getMessage());
			}
			
		}
	}
}
