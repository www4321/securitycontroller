/** 
*    Copyright 2014 BUPT. 
**/ 
/**
*    Copyright 2011, Big Switch Networks, Inc. 
*    Originally created by David Erickson, Stanford University
* 
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

package com.sds.securitycontroller.storage.mongodb;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.storage.AbstractStorageSource;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.storage.IRawTypeStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClause.QueryClauseType;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.storage.QueryClauseItem.OpType;
import com.sds.securitycontroller.storage.RowOrdering;
import com.sds.securitycontroller.storage.RowOrdering.Item;



public class MongoDBStorageSource extends AbstractStorageSource implements IRawTypeStorageSourceService{
    protected static Logger logger = LoggerFactory.getLogger(MongoDBStorageSource.class);
    protected IRegistryManagementService serviceRegistry;

    String dbhost;
    int dbport = -1;
    String dbname;
    String dbuser;
    String dbpass;
    

    DB[] connections = new DB[10];
    Set<DB> availableConnections = new HashSet<DB>();

        
    protected static final String DEFAULT_PRIMARY_KEY_NAME = "_id";
    
    private Map<String,String> tablePrimaryKeyMap = new HashMap<String,String>();


    public DB getConnection(){

		if(dbname == null || dbport<0 || dbhost==null){
			logger.error("Connection string is null");
			return null;
		}
    	synchronized(connections){
	    	for(int i=0;i<connections.length;i++){
	    		DB c = connections[i];
	    		Mongo mg = (c==null)?null:c.getMongo();
	    		
	    			    		
	    		if(mg == null || mg.isLocked()){
	    			//establish a connection
	        		try{
	        			if(dbpass != null){
		        			MongoCredential credential = MongoCredential.createMongoCRCredential(dbuser, dbname, dbpass.toCharArray());
		        			mg = new MongoClient(new ServerAddress(dbhost+":"+dbport), Arrays.asList(credential));
	        			}
	        			else{
	        				mg = new MongoClient(dbhost,dbport);
	        			}
	        			
	        			DB db = mg.getDB(dbname);
	        			
	    				synchronized(this.availableConnections){
	                        connections[i] = db;
	    					this.availableConnections.remove(c); //if c is available
	    				} 
	    				return db;
	                }catch(UnknownHostException|MongoException e){   
	                    logger.error("Database connection failed:{}", e.getMessage()); 
	                    dropConnection(c);
	                    return null;
	                }    	
	    		}
	    		else{
	    			if(availableConnections.contains(c)){//the connections[i] is available
	    				synchronized(this.availableConnections){
	    					this.availableConnections.remove(c);
	    				}
	        			return connections[i];
	    			}
	    			else{//the connections[i] is unavailable, skip
	    				continue;
	    			}
	    		}
	    	}
    	}
    		
		return null;    		
    }


    private void releaseConnection(DB db){
    	if(db == null)
    		return;
    	
		synchronized(this.availableConnections){
			this.availableConnections.add(db);
		}
    }
    

    private void dropConnection(DB db){
    	if(db == null)
    		return;
    	synchronized(this.connections){
    		synchronized(this.availableConnections){
	    		if(!this.availableConnections.contains(db))
	    			this.availableConnections.add(db);
    		}
			Mongo mongo = db.getMongo();
			
			if(mongo != null){
				mongo.close();
				mongo = null;
			}
    	}
    }
    
    
	@Override
    public String getTablePrimaryKeyName(String tableName) {
        String primaryKeyName = tablePrimaryKeyMap.get(tableName);
        if (primaryKeyName == null)
            primaryKeyName = DEFAULT_PRIMARY_KEY_NAME;
        return primaryKeyName;
    }
    
		
	@Override
	public int insertEntities(String tableName,
			List<? extends IDBObject> entities) {
		int n = 0;
		for(IDBObject entity: entities){
			//TODO invoke insert raw entity
			n += insertEntity(tableName, entity);
//			n += insertEntityRawType(tableName, entity)?1:0;
		}
		return n;
	}
	@Override
	public int insertEntities(String tableName, Map<String, Object>[] entities) {

		int n = 0;
		for(Map<String, Object> items: entities){
			n += insertEntity(tableName, items);
		}
		return n;
	}	

	@Override
	public void setTablePrimaryKeyName(String tableName, String primaryKeyName) {
        if ((tableName == null) || (primaryKeyName == null))
            throw new NullPointerException();
        tablePrimaryKeyMap.put(tableName, primaryKeyName);
	}
	
	@Override
	public List<? extends IDBObject> executeQuery(String tableName, 
			String condition, RowOrdering order, Class<? extends IDBObject> objClass){
		logger.error("String query for mongodb is not supported.");
		return null;
	}
	
//	@Override
//	protected IAbstractResultSet executeQueryImpl(QueryClause query) {
//        return executeParameterizedQuery(query.getTableName(),
//        		query.getColumnNames(), query,
//        		query.getOrdering());
//	}
    
	@Override
	public List<? extends IDBObject> executeQuery(QueryClause query, Class<? extends IDBObject> objClass) {
		IAbstractResultSet result =  executeParameterizedQuery(query.getTableName(),
        		query.getColumnNames(), query,
        		query.getOrdering());

        List<IDBObject> objectList = new ArrayList<IDBObject>();

        IDBObject rowMapper = getDBObject(objClass);		
        if(rowMapper == null)
        	return objectList;

        do{
        	IDBObject object = rowMapper.mapRow(result);
            objectList.add(object);
        } while(result.next());
        return objectList;

		
	}


    private MongoDBResultSet executeParameterizedQuery(String tableName, String[] columnNameList,
            QueryClause query, RowOrdering rowOrdering) {
    	MongoDBResultSet result = null;
    	
       if(query.getType() == QueryClauseType.CLAUSE_ITEMS || query.getType() == QueryClauseType.EMPTY){
        	//hash key-value
    	   BasicDBObject queryObj = getQueryObject(query);
    	   if(queryObj == null)
    		   return null;

            DB conn = getConnection();
            DBCollection collection = conn.getCollection(tableName);
            try {
        		DBCursor cur = collection.find(queryObj);
        		if(query.getOrdering()!=null){
        			RowOrdering order = query.getOrdering();
        			Item item = order.getItemList().get(0);
        			if(item.getDirection() == RowOrdering.Direction.DESC)
                		cur = cur.sort(new BasicDBObject(item.getColumn(),-1));
        			else
        				cur = cur.sort(new BasicDBObject(item.getColumn(), 1));
        			if(query.getExtra()!=null){
        				String extra = query.getExtra();
        				int p1 = extra.indexOf("limit ");
        				if(p1>=0){
        					int p2 = p1+"limit ".length();
        					int p3 = extra.indexOf(" ", p2);
        					int limit = Integer.parseInt(extra.substring(p2,  p3));
        					cur = cur.limit(limit);
        				}
        			}
        		}
        		result = new MongoDBResultSet(cur);
    		}
        	catch (Exception e) {
        		logger.error("Query error: "+e.getMessage());  
    		}
            finally{	
    			releaseConnection(conn);
            }    	
        }
       else if(query.getType() == QueryClauseType.STRING){
        	logger.error("String query for Mongo db is not supported");
        }
        else{
        	logger.error("Unknown query type for Mongo db : "+ query.getType());
        }
    	return result;
    }

    @Override
	protected int insertEntityImpl(String tableName, IDBObject entity) {
		Map<String, Object> items = entity.getDBElements();
		return insertEntity(tableName, items);
	}
	

    @Override
	public int insertEntity(String tableName, Map<String, Object> entity) {
		DB db = getConnection();
		DBCollection collection = db.getCollection(tableName);        
                
		try {
    		BasicDBObject dbObj = getQueryObject(entity);
        	int n = collection.insert(dbObj).getN();
        	releaseConnection(db);
        	return (n>=0)?1:-1;
		}
    	catch (Exception e) {
            logger.error("Insert entity failed:{}", e.getMessage());
            releaseConnection(db);
    		return 0;
		}		
	}
	@Override
	protected int updateEntitiesImpl(String tableName,
			QueryClause query, Map<String, Object> values) {

		DB db = getConnection();
		DBCollection collection = db.getCollection(tableName);        
		BasicDBObject queryObj = getQueryObject(query);
		BasicDBObject updateObj = getQueryObject(values);
        DBObject updateSetValue=new BasicDBObject("$set",updateObj);  

        int n = collection.update(queryObj, updateSetValue).getN();
    	releaseConnection(db);

    	return (n>=0)?1:-1;		
	}
	@Override
	protected int updateEntityImpl(String tableName, Object entityKey,
			Map<String, Object> values) {
		DB db = getConnection();
		DBCollection collection = db.getCollection(tableName);        
        
        String primaryKeyName = getTablePrimaryKeyName(tableName);
        
		try {
    		BasicDBObject queryObj = new BasicDBObject(primaryKeyName,entityKey);

    		BasicDBObject updateObj = getQueryObject(values);
            DBObject updateSetValue=new BasicDBObject("$set",updateObj); 

            int n = collection.update(queryObj, updateSetValue).getN();
        	releaseConnection(db);

        	return (n>=0)?1:-1;
		}
    	catch (Exception e) {
            logger.error("Insert entity failed:{}", e.getMessage());
            releaseConnection(db);
    		return 0;
		}
	}
	@Override
	protected int deleteEntityImpl(String tableName, Object entityKey) {

		DB db = getConnection();
		DBCollection collection = db.getCollection(tableName);        
        
        String primaryKeyName = getTablePrimaryKeyName(tableName);
        
		try {
    		BasicDBObject queryObj = new BasicDBObject(primaryKeyName,entityKey);
        	int n = collection.remove(queryObj).getN();
        	releaseConnection(db);

        	return (n>=0)?1:-1;
		}
    	catch (Exception e) {
            logger.error("Insert entity failed:{}", e.getMessage());
            releaseConnection(db);
    		return 0;
		}
	}
	@Override
	protected int deleteEntitiesImpl(String tableName, QueryClause query) {

		DB db = getConnection();
		DBCollection collection = db.getCollection(tableName);        
		try {
			BasicDBObject queryObj = getQueryObject(query);
        	int n = collection.remove(queryObj).getN();
        	releaseConnection(db);

        	return (n>=0)?1:-1;
		}
    	catch (Exception e) {
            logger.error("Insert entity failed:{}", e.getMessage());
            releaseConnection(db);
    		return 0;
		}		
	}
	@Override
	public IDBObject getEntity(String tableName,
			Object entityKey, Class<? extends IDBObject> objClass) {
		MongoDBResultSet result = null;
		DB db = getConnection();
		DBCollection collection = db.getCollection(tableName);   

        String primaryKeyName = getTablePrimaryKeyName(tableName);
        
		try {
    		BasicDBObject queryObj = new BasicDBObject(primaryKeyName,entityKey);
    		DBCursor cur = collection.find(queryObj);
    		result = new MongoDBResultSet(cur);
		}
    	catch (Exception e) {
    		logger.error("Query error: "+e.getMessage());  
		}
        finally{	
			releaseConnection(db);
        } 

    	IDBObject rowMapper = getDBObject(objClass);
    	if(rowMapper == null)
    		return null;
    	return rowMapper.mapRow(result);    	
	}

	private BasicDBObject getQueryObject(QueryClause query){
        BasicDBObject queryObj = new BasicDBObject();
		if(query.getType() == QueryClauseType.EMPTY){
			return queryObj;
		}
        List<QueryClauseItem> items = query.getItems();
        for(QueryClauseItem item: items){
        	if(item.getOp() == OpType.EQ){
        		queryObj.put(item.getKey(), item.getValue());
        	}
        	else if(item.getOp() == OpType.GT){
        		queryObj.put(item.getKey(), new BasicDBObject("$gt", item.getValue()));
        	}
        	else if(item.getOp() == OpType.LT){
        		queryObj.put(item.getKey(), new BasicDBObject("$lt", item.getValue()));
        	}
        	else if(item.getOp() == OpType.NE){
            	queryObj.put(item.getKey(), new BasicDBObject("$ne", item.getValue()));
        	}
        	else{
        		logger.error("unimplemented op: "+ item.getOp());
        		return null;
        	}            	
        }
        return queryObj;
	}

	private BasicDBObject getQueryObject(Map<String, Object> items){
		BasicDBObject queryObj = new BasicDBObject();
		for(Entry<String, Object> item: items.entrySet()){
			Object v = item.getValue();
			if(v instanceof Enum)
				v = v.toString();
    		queryObj.put(item.getKey(), v);
		}
		return queryObj;
	}
    
	@Override
	public Object executeQueryRawType(String collectionName, Object query){
		DB db = this.getConnection();
		DBCollection collection = db.getCollection(collectionName);
		try{
			DBCursor cursor = collection.find((DBObject)query);
			List<Object> reList = new ArrayList<Object>();
			while (cursor.hasNext()) {
				reList.add(cursor.next());
			}
			
			cursor.close();
			this.releaseConnection(db);
			return reList;
		}
		catch(Exception e){
			logger.error("Insert raw entity failed:{}", e.getMessage());
		}
		finally{
			this.releaseConnection(db);
		}
		return null;
	}
	
	protected DBObject map2Obj(Map<String, Object> map){
		DBObject obj = new BasicDBObject();
		obj.putAll(map);
		return obj;
	}
	

	@Override
	@SuppressWarnings("unchecked")
//	@Override
	public boolean insertEntityRawType(String collectionName, Object entity) {
			
		DB db = this.getConnection();
		DBCollection collection = db.getCollection(collectionName);
		try {
			int n = collection.insert(this.map2Obj((Map<String, Object>)entity)).getN();
			return n >= 0;
		} 
		catch (Exception e){
			 logger.error("Insert raw entity failed:{}", e.getMessage());
		}
		finally{
			this.releaseConnection(db);
		}
		
		return false;
	}


	@Override
	@SuppressWarnings("unchecked")
//	@Override
	public boolean insertEntitiesRawType(String collectionName,
			List<Object> entities) {
		
		DB db = this.getConnection();
		DBCollection collection = db.getCollection(collectionName);
		
		try {
			List<DBObject> objList = new ArrayList<DBObject>();
			for(Object obj : entities){
				objList.add(this.map2Obj((Map<String, Object>)obj));
				//objList.add((DBObject) JSON.parse((String)json));
			}
			int n = collection.insert(objList).getN();
			return n >= 0;
		}
		catch (Exception e){
			 logger.error("Insert raw entity list failed:{}", e.getMessage());
		}
		finally{
			this.releaseConnection(db);
		}
		return false;
	}


//	@Override
	@Override
	public boolean updateEntitiesRawType(String collectionName,
			QueryClause query, Map<String, Object> entityValues) {
		// TODO Auto-generated method stub
		return false;
	}


//	@Override
	@Override
	public boolean updateOrInsertEntityRawType(String tableName, Object entity) {
		// TODO Auto-generated method stub
		return false;
	}


//	@Override
	@Override
	public boolean deleteEntityRawType(String collectionName, Object entityKey) {
		// TODO Auto-generated method stub
		return false;
	}


//	@Override
	@Override
	public boolean deleteEntitiesRawType(String collectionName,
			QueryClause query) {
		// TODO Auto-generated method stub
		return false;
	}
	
    
    @Override
    public void init(SecurityControllerModuleContext context)
            throws SecurityControllerModuleException {
        // read our config options
        Map<String, String> configOptions = context.getConfigParams(this);
        //String dbdriver = configOptions.get("dbdriver");
        dbhost = configOptions.get("dbhost");
        dbport = Integer.parseInt(configOptions.get("dbport"));
        dbname = configOptions.get("dbname");
        dbuser = configOptions.get("dbuser");
        dbpass = configOptions.get("dbpass");
        //logger.debug("Database connection set to {}", constr);
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
		logger.info("BUPT security controller mongodb storage source initialized."); 
    }


    @Override
    public void startUp(SecurityControllerModuleContext context) {
        serviceRegistry.registerService("", this);
		logger.info("BUPT security controller mongodb storage source started."); 
        
    }
}
