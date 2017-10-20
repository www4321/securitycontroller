/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.storage.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.esotericsoftware.minlog.Log;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.storage.AbstractStorageSource;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClause.QueryClauseType;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.utils.SimpleUtils;

public class RedisStorageSource extends AbstractStorageSource {

    protected static Logger logger = LoggerFactory.getLogger(RedisStorageSource.class);
    protected IRegistryManagementService serviceRegistry;

    
    protected static final String DEFAULT_PRIMARY_KEY_NAME = "id";
    
    String dbhost;
    int dbport = -1;
    String dbname;
    String dbuser;
    String dbpass;
    
    int expire = 100000;

    Jedis[] connections = new Jedis[10];
    Set<Jedis> availableConnections = new HashSet<Jedis>();

    private boolean compressed = true;

    
    @Override
    public void init(SecurityControllerModuleContext context)
            throws SecurityControllerModuleException {
        // read our config options
        Map<String, String> configOptions = context.getConfigParams(this);
        this.dbhost = configOptions.get("dbhost");
        this.dbport = Integer.parseInt(configOptions.get("dbport"));
        this.dbuser = configOptions.get("dbuser");
        this.dbpass = configOptions.get("dbpass");
        this.expire = Integer.parseInt(configOptions.get("expire").trim());
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
		logger.info("BUPT security controller redis storage source initialized."); 
    }    

    @Override
    public void startUp(SecurityControllerModuleContext context) {
        serviceRegistry.registerService("", this);
		logger.info("BUPT security controller redis storage source started."); 
        
    }
    
    
	@Override
    public String getTablePrimaryKeyName(String tableName) {
        return DEFAULT_PRIMARY_KEY_NAME;
    }

    public Jedis getConnection(){

		if(dbhost==null){
			logger.error("Connection string is null");
			return null;
		}
    	synchronized(connections){
	    	for(int i=0;i<connections.length;i++){
	    		Jedis c = connections[i];
	    			    		
	    		if(c == null){
	    			//establish a connection
        			c = new Jedis(dbhost);
        			synchronized(this.availableConnections){
	        			connections[i] = c;
    					this.availableConnections.remove(c); //if c is available
    				} 
        			
    				return c;
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
    
    private void releaseConnection(Jedis conn){
    	if(conn == null)
    		return;
    	conn.close();
    	
		synchronized(this.availableConnections){
			this.availableConnections.add(conn);
		}
    }
    

    
    @Override
    public void createTable(String tableName, Map<String, String> indexedColumns) {
    	return;
    }
    
    
    
	@Override
	public void setTablePrimaryKeyName(String tableName, String primaryKeyName) {
		return;
	}

	@Override
    public int insertEntities(String tableName, List<? extends IDBObject> entities){
		int n = 0;
		for(IDBObject entity: entities){
			n += insertEntity(tableName, entity);
		}
		return n;
	}

	@Override
    public int insertEntity(String tableName, Map<String, Object> entity){
		Log.error("db operation unimplemented");
		return -1;
	}
	
	
	@Override
    public int insertEntities(String tableName, Map<String, Object>[] entities){
		Log.error("db operation unimplemented");
		return -1;
	}
	

	@Override
	protected int insertEntityImpl(String tableName, IDBObject entity) {
		String id = (String)entity.getDBElements().get("id");
		byte[] key = getId(tableName, id).getBytes();
		byte[] value = null;
		try {
			value = SimpleUtils.serialize(entity, compressed);
		} catch (IOException e) {
			e.printStackTrace();
	    	return -1;
		}
		Jedis conn = getConnection();		
        conn.set(key, value);
    	releaseConnection(conn);
    	return 1;
	}

	

	@Override
	protected int deleteEntitiesImpl(String tableName, QueryClause query) {
		
		if(query.getType() != QueryClauseType.CLAUSE_ITEMS){
			logger.error("query type not CLAUSE_ITEMS");
			return -1;
		}
		String id = null;
		for(int i=0;i<query.getItems().size();i++){
			QueryClauseItem item = query.getItems().get(i);
			if(item.getKey().equals("id")){
				id = (String)item.getValue();
				break;
			}
		}
		return deleteEntityImpl(tableName, id);
	}

	
	@Override
	protected int deleteEntityImpl(String tableName, Object entityKey) {

		if(entityKey == null){
			logger.warn("query key has no id item");
			return -1;
		}
		
		Jedis conn = getConnection();
		conn.del(getId(tableName, (String)entityKey));
		
		releaseConnection(conn);
		return 1;		
		
	}
	
	
	@Override
	protected int updateEntitiesImpl(String tableName,
			QueryClause query, Map<String, Object> values) {	
		//unimplemented	     
		return -1;
	}

	@Override
	protected int updateEntityImpl(String tableName, Object rowKey,
			Map<String, Object> values) {
		//unimplemented
		return -1;
	}

     
	

	@Override
	public IDBObject getEntity(String tableName,  Object entityKey,
			Class<? extends IDBObject> objClass) {         
		
		Jedis conn = getConnection();
		byte[] id = (getId(tableName, (String)entityKey)).getBytes();
		byte[] value = conn.get(id);
		Object o = null;
		try {
			o = SimpleUtils.deserialize(value, compressed);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			o = null;
		}		
		
		releaseConnection(conn);
		return (IDBObject)o;
		
	}

	@Override
	public List<? extends IDBObject> executeQuery(QueryClause query,
			Class<? extends IDBObject> objClass) {
		if(query.getType() != QueryClauseType.CLAUSE_ITEMS){
			logger.error("query type not CLAUSE_ITEMS");
			return null;
		}
		String id = null;
		for(int i=0;i<query.getItems().size();i++){
			QueryClauseItem item = query.getItems().get(i);
			if(item.getKey().equals("id")){
				id = (String)item.getValue();
				break;
			}
		}
		if(id == null){
			logger.warn("query key has no id item");
			return null;
		}

		IDBObject o = getEntity(query.getTableName(), id, objClass);
		List<IDBObject> result = new ArrayList<IDBObject>();
		result.add(o);
		
		return result;
	}

	private String getId(String tableName, String id){
		return tableName+"_"+id;		
	}
}
