package com.sds.securitycontroller.utils.realtime;

import java.util.ArrayList;
import java.util.List;

import com.sds.securitycontroller.storage.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealtimeBasicManager implements IRealtimeBasicManagement{
	protected static Logger log = LoggerFactory.getLogger(RealtimeBasicManager.class);
	private IStorageSourceService storageSource=null;
	private String tableName="t_realtime_basic";
	
	public RealtimeBasicManager(IStorageSourceService source ){
		this.storageSource=source;
	}

	@Override
	public boolean addRealtimeBasic(String obj_id, String type) {				
		if(null != this.getRealtimeBasic(obj_id, type)){
			return true;
		}
		RealtimeBasic rb = new RealtimeBasic();
		rb.setObj_id(obj_id);
		rb.setType(type);
		int ret = storageSource.insertEntity(this.tableName, rb);
		if(ret<=0) 
			return false;
		return true;
	}

	@Override
	public boolean updateRealtimeBasic(String obj_id, String type,
			RealtimeBasic rb) {
		if(null == this.getRealtimeBasic(obj_id, type)){
			log.error("app realtimeinfo record not found:",obj_id, type);
			this.addRealtimeBasic(obj_id, type);//try to add one
		}		
		rb.setUpdate_time((int) (System.currentTimeMillis() / 1000L));
		rb.setObj_id(obj_id);
		rb.setType(type);
		List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
		items.add(new QueryClauseItem("obj_id",obj_id,QueryClauseItem.OpType.EQ));
		items.add(new QueryClauseItem("type",type,QueryClauseItem.OpType.EQ)); 
		
		QueryClause qc = new QueryClause(items, this.tableName, null, null);		
		int ret=storageSource.updateEntities(this.tableName, qc, rb.getDBElements());		 
		if(ret<=0) 
			return false;
		return true;		 
		
	}

	@Override
	public boolean removeRealtimeBasic(String obj_id, String type) {
		List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
		items.add(new QueryClauseItem("obj_id",obj_id,QueryClauseItem.OpType.EQ));
		items.add(new QueryClauseItem("type",type,QueryClauseItem.OpType.EQ)); 		
		QueryClause qc = new QueryClause(items, this.tableName, null, null);		
		int ret=storageSource.deleteEntities(this.tableName, qc);
		if(ret<=0) 
			return false;
		return true;
	}

	@Override
	public RealtimeBasic getRealtimeBasic(String obj_id, String type) {
		List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
		items.add(new QueryClauseItem("obj_id",obj_id,QueryClauseItem.OpType.EQ));
		items.add(new QueryClauseItem("type",type,QueryClauseItem.OpType.EQ)); 
		
		QueryClause qc = new QueryClause(items, this.tableName, null, null);
		@SuppressWarnings("unchecked")
		List<RealtimeBasic> result = (List<RealtimeBasic>)this.storageSource.executeQuery(
				qc, RealtimeBasic.class);
		if(result!=null && !result.isEmpty())
			return result.get(0);
		else
			return null;
	}

	@Override
	public List<RealtimeBasic> getAllRealtimeBasic(String type) {
		List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
		items.add(new QueryClauseItem("type",type,QueryClauseItem.OpType.EQ));	 
		QueryClause qc = new QueryClause(items, this.tableName, null, new RowOrdering("obj_id"));
		@SuppressWarnings("unchecked")

		List<RealtimeBasic> result = (List<RealtimeBasic>)this.storageSource.executeQuery(
				qc, RealtimeBasic.class);		
		return result;
	}
	 
}















