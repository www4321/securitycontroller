package com.sds.securitycontroller.test1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.test2.TestDBObject;

public class EventSendResource extends ServerResource {
	
	protected IEventSendService eventSendService=null;
	protected IStorageSourceService storageSource = null;
	protected static Logger log = LoggerFactory.getLogger(EventSendResource.class);
	@Override
	protected void doInit() throws ResourceException {
		this.eventSendService=(IEventSendService)getContext().getAttributes().get(IEventSendService.class.getCanonicalName());
		this.storageSource = (IStorageSourceService)getContext().getAttributes().get(IStorageSourceService.class.getCanonicalName());
	}
	
	@Put
	public void send(){
		eventSendService.sendEvent();
	}
	@Get
	public String getStringMethod(){
		//eventSendService.sendEvent();
		Map<String, String> indexedColumns = new HashMap<String, String>(){
			private static final long serialVersionUID = 1L;
			{
				put("id","varchar(128)");
				put("name","varchar(128)");
				put("PRIMARY_KEY","id");
			}
		};
		storageSource.createTable("flow", indexedColumns);
		String id = storageSource.getTablePrimaryKeyName("flow");
		List<TestDBObject> entities = new ArrayList<TestDBObject>();
		entities.add(new TestDBObject("1","11"));
		entities.add(new TestDBObject("2","22"));
		storageSource.insertEntities("flow", entities);
		TestDBObject a= (TestDBObject) storageSource.getEntity("flow", "1", TestDBObject.class);
		log.info("...................................."+a.getId()+" "+a.getName());
		QueryClause query = new QueryClause("flow");
		@SuppressWarnings("unchecked")
		List<TestDBObject> entities1 = (List<TestDBObject>) storageSource.executeQuery(query, TestDBObject.class);
		log.info("...................................."+entities1.get(1).getId()+" "+entities1.get(1).getName());
		return "www1234";
	}
}
