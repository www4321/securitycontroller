package com.sds.securitycontroller.TaskManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;










import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.storage.QueryClauseItem.OpType;


public class MyTaskManager implements ISecurityControllerModule,IMyTaskManagerService{
	

	protected IRestApiService restApi; 
	protected IEventManagerService eventManager;
	protected IStorageSourceService storageSource;
    protected IRegistryManagementService serviceRegistry;
	protected static Logger log = LoggerFactory.getLogger(MyTaskManager.class);
//	protected List<MyOrder> orderList=new ArrayList<MyOrder>();

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services = new ArrayList<Class<? extends ISecurityControllerService>>();
		services.add(IMyTaskManagerService.class);
		return services;
	}
	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = 
				new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IMyTaskManagerService.class, this);
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
		// TODO Auto-generated method stub
		 this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
		 this.restApi = context.getServiceImpl(IRestApiService.class);
		 this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);
		 this.storageSource = context.getServiceImpl(IStorageSourceService.class, this);
		 log.info("BUPT security controller ordermanager initialized...");
		
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		// TODO Auto-generated method stub
		MyTaskRoutable ohr=new MyTaskRoutable();
		restApi.addRestletRoutable(ohr);
		serviceRegistry.registerService(ohr.basePath(), this);
		storageSource.createTable(this.getTableName("mytask"), this.getTableColumns());
        storageSource.setTablePrimaryKeyName(this.getTableName("mytask"), "id");
		log.info("BUPT security controller orderhandlermanager started...");
		
	}

	@Override
	public boolean addOrder(MyTask order) {
		// TODO Auto-generated method stub
//		System.out.println("开始添加订单1 开始生成订单号"); 
//		String id = this.generateOrderId();
//		System.out.println("订单号为："+id); 
//		if(null == id){
//			log.error("order id failed:{} ", order);
//			return false;
//		}
//		order.setId(id);
//		System.out.println("添加一个订单，id为："+id); 


		int res = this.storageSource.insertEntity("mytask", order);
		if(res<=0){
		    log.error("Insert order to DB failed {} ", order);
			return false;
		} 
//        TaskEventArgs oea=new TaskEventArgs(order);        
//        Event event=new Event(EventType.New_Task,order,this,oea);
//        eventManager.addEvent(event);
        //System.out.println("产生一个新订单事件");
        return true;
	
	}
	@Override
	public boolean removeOrder(MyTask order) {
		// TODO Auto-generated method stub
		int res = storageSource.deleteEntity(getTableName("dev"), order.getId());
 		if(res<0){
 			return false;
 		}
 		return true;

	}
	@Override
	public MyTask getOrder(String id) {
		// TODO Auto-generated method stub
		MyTask order = null;
		do{
			if(null == id || id.isEmpty()){
				break;
			}
			System.out.println("得到task"); 
			order = (MyTask)this.storageSource.getEntity(getTableName("mytask"), id, MyTask.class);				
		}while(false);		
		return order;
	}
	@Override
	public List<MyTask> getAllOrders(String from, int size) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<MyTask> getAllOrders() {
		// TODO Auto-generated method stub
		QueryClause qc = new QueryClause(getTableName("mytask"));

		List<MyTask> allOrders = (List<MyTask>)storageSource.executeQuery(qc, MyTask.class);
    	
    	return allOrders;
	}
	
	

	//----------DB related methods --------------
	public String getTableName(String type) {  	 
	    	return type;
	}
	
	public String getTablePK(String id) {
	    	return id;
	}

	public Map<String, String> getTableColumns() {
	    Map<String, String> appTableColumns = new HashMap<String, String>(){
			private static final long serialVersionUID = 343251L;

		{
	    	put("id", 				"VARCHAR(128)");
	    	
	    	put("type", 			"VARCHAR(64)");
	    	put("host", 			"VARCHAR(64)");

	    }};
	 //   System.out.println("生成myorder表头"); 
	    	return appTableColumns;
	}

	private String generateOrderId() {
		int MAX=50;//how many times to try
		Random r=new Random();
		long nid=System.currentTimeMillis();		
		String id="2"+Long.toString(nid);
		int count=0;
		
		while(count<MAX && null != this.getOrder(id)){
			nid+=r.nextInt(200);
			id="2"+Long.toString(nid);
			count++;
		}
		if(MAX == count){
			return null;
		}
		System.out.println("生成id"); 
		return id;
//		return Integer.toString((int)(10000*Math.random()));
	}
	@Override
	public boolean  updateMyOrder(String id, Map<String,Object> values) {
		// TODO Auto-generated method stub

		List<QueryClauseItem> clauseItems = new ArrayList<QueryClauseItem>();
		clauseItems.add(new QueryClauseItem("id", id, OpType.EQ));
		QueryClause query = new QueryClause(clauseItems,
				"mytask", null, null);

		int res = this.storageSource.updateEntities("mytask",
				query, values);
		return res > 0;

	
	}
	
}
