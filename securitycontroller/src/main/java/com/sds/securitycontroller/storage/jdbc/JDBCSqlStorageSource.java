/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.storage.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.storage.AbstractStorageSource;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClause.QueryClauseItemType;
import com.sds.securitycontroller.storage.QueryClause.QueryClauseType;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.storage.QueryClauseItem.OpType;
import com.sds.securitycontroller.storage.RowOrdering;
import com.sds.securitycontroller.storage.RowOrdering.Item;

public class JDBCSqlStorageSource extends AbstractStorageSource {

    protected static Logger logger = LoggerFactory.getLogger(JDBCSqlStorageSource.class);
    protected IRegistryManagementService serviceRegistry;

    
    protected static final String DEFAULT_PRIMARY_KEY_NAME = "id";
    
    private Map<String,String> tablePrimaryKeyMap = new HashMap<String,String>();
    String constr;
    String dbuser;
    String dbpass;

    Connection[] connections = new Connection[10];
    Set<Connection> availableConnections = new HashSet<Connection>();

    private boolean isClosed(Connection conn){
    	if(conn != null){
			try{
				return conn.isClosed();
			}
			catch(SQLException ex){
				return false;
			}
		}
    	return false;
    }

    public Connection getConnection(){

		if(constr == null){
			logger.error("Connection string is null");
			return null;
		}
    	synchronized(connections){
	    	for(int i=0;i<connections.length;i++){
	    		Connection c = connections[i];
	    		if(c == null || isClosed(c)){
	    			//establish a connection
	        		try{   
	                    Connection conn = DriverManager.getConnection(constr , dbuser , dbpass ) ; 
	    				synchronized(this.availableConnections){
	                        connections[i] = conn;
	    					this.availableConnections.remove(c); //if c is available
	    				} 
	    				conn.setAutoCommit(false); 
	    				return conn;
	                }catch(SQLException e){   
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
		        		try{  
		        			c.setAutoCommit(false); 
		                }catch(SQLException e){   
		                    logger.debug("Database connection failed:{}", e.getMessage()); 
		                    dropConnection(c);
		                    return null;
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
    
    private void releaseConnection(Connection conn){
    	if(conn == null)
    		return;
    	try{
    		conn.setAutoCommit(true); 
    	}catch(SQLException e){   
            logger.debug("Database connection failed:{}", e.getMessage());
        }   
    	
		synchronized(this.availableConnections){
			this.availableConnections.add(conn);
		}
    }
    

    private void dropConnection(Connection conn){
    	if(conn == null)
    		return;
    	synchronized(this.connections){
    		synchronized(this.availableConnections){
	    		if(!this.availableConnections.contains(conn))
	    			this.availableConnections.add(conn);
    		}
    		try{
    			conn.close();
        		conn = null;
    		} catch(SQLException ex) {
    			System.err.println("SQLException: " + ex.getMessage());
    			releaseConnection(conn);
    		}
    	}
    }
    
    
    @Override
    public void createTable(String tableName, Map<String, String> indexedColumns) {
        super.createTable(tableName, indexedColumns);
        String primaryKey = null;
        if(indexedColumns.containsKey("PRIMARY_KEY")){
        	primaryKey = indexedColumns.get("PRIMARY_KEY");
        	indexedColumns.remove("PRIMARY_KEY");
        }
        
        
        String extra = null;
        if(indexedColumns.containsKey("EXTRA")){
        	extra = indexedColumns.get("EXTRA");
        	indexedColumns.remove("EXTRA");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("create table if not exists ");
        sb.append(tableName);
        sb.append(" (");
        for(Entry<String, String> entry : indexedColumns.entrySet()){
        	sb.append(entry.getKey());
            sb.append(" ");
            sb.append(entry.getValue());
            sb.append(",");
        }

        
        //wangzelang修改
//        int i=0;
//        int j=indexedColumns.size();
//        for(Entry<String, String> entry : indexedColumns.entrySet()){
//        	if(i<j)
//        	{
//        	sb.append(entry.getKey());
//            sb.append(" ");
//            sb.append(entry.getValue());
//            sb.append(",");
//        	}
//        	else{
//        		sb.append(entry.getKey());
//                sb.append(" ");
//                sb.append(entry.getValue());
//                sb.append(")");
//        	}
//        }
        
        if(primaryKey == null)
        	sb.replace(sb.length()-1, sb.length(), ")");
        else {
//        	sb.replace(sb.length()-1, sb.length(), "PRIMARY KEY ("+primaryKey+"))");
            sb.append("PRIMARY KEY (" + primaryKey + "))");
        }
        if(extra != null)
        	sb.append(" "+extra);
        String createString = sb.toString();
    	Connection conn = getConnection(); 
        try {
			conn.setAutoCommit(true); 
        	Statement stmt = conn.createStatement();
	   		stmt.executeUpdate(createString);
			//conn.commit();
			stmt.close();
			releaseConnection(conn);
		} catch(SQLException ex) {
			Log.error("SQLException: " + ex.getMessage());
			releaseConnection(conn);
		}
        System.out.println(sb);
		
    }
    
    
    
	@Override
	public void setTablePrimaryKeyName(String tableName, String primaryKeyName) {
        if ((tableName == null) || (primaryKeyName == null))
            throw new NullPointerException();
        tablePrimaryKeyMap.put(tableName, primaryKeyName);
	}


//	@Override
//	protected IAbstractResultSet executeQueryImpl(QueryClause query) {
//        return executeParameterizedQuery(query.getTableName(),
//        		query.getColumnNames(), query,
//        		query.getOrdering());
//	}


	@Override
	public List<? extends IDBObject> executeQuery(QueryClause query, Class<? extends IDBObject> objClass) {
	    /**
	     * 返回JDBC数据库查询结果
	     *  columnNames 要查询的字段，为空则全部查询
	     *  query where语句生成
	     *  rowOrdering 排序 主键
	     */
		
		
		String tableName= query.getTableName();
		String[] columnNames = query.getColumnNames();
		RowOrdering rowOrdering = query.getOrdering();
		
		IAbstractResultSet jdbcResults = null;

        StringBuilder sb = new StringBuilder();
		sb.append("select ");
		if(columnNames == null){
			sb.append(" * ");
		}
		else{
			for(int i=0;i<columnNames.length-1;i++){
				sb.append(columnNames[i]);
				sb.append(",");
			}
			sb.append(columnNames[columnNames.length-1]);
		}
		sb.append(" from ");
		sb.append(tableName);
		sb.append(" ");
		
		if(query.getType() == QueryClauseType.EMPTY || (query.getCondition() == null && (query.getItems()!=null && query.getItems().size() == 0))){
		
			// do nothing
		}
		else{
			String condition = getQueryObject(query);
			sb.append(" where "+condition);
		}
        if (rowOrdering != null){
        	sb.append(" order by ");
        	List<Item> items = rowOrdering.getItemList();
        	for(int i=0; i<items.size()-1;i++){
        		Item item = items.get(i);
        		sb.append(item.getColumn()+" " +item.getDirection()+",");
        	}
    		Item item = items.get(items.size()-1);        	
    		sb.append(item.getColumn()+" " +item.getDirection());
        }
        
        if(query.getExtra() != null)
        	sb.append(" "+query.getExtra());   
        String str = sb.toString();

		Statement statement = null;
        Connection conn = getConnection();
        if(conn == null){
        	Log.debug("Connection of " +this.constr+ " fails");
        	jdbcResults = null;
        }
        else{
			try {
				statement = conn.createStatement();
				ResultSet results =  statement.executeQuery(str);
				jdbcResults = new JDBCResultSet(results);
			} catch (SQLException e) {
				logger.error("SQLException:{}", e.getMessage());
				jdbcResults = null;
			}
        }

        IDBObject rowMapper = getDBObject(objClass);
        List<IDBObject> objectList = new ArrayList<IDBObject>();
        
		if(jdbcResults == null || rowMapper == null){
        	try {
        		if(statement != null)
        			statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			releaseConnection(conn);
			return objectList;
		}        
        
        while(jdbcResults.next()){
        	IDBObject object = rowMapper.mapRow(jdbcResults);
            objectList.add(object);
        }

        try {
        	if(statement != null)
        		statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        releaseConnection(conn);
             
        return objectList;		
	}

    
	@Override
    public int insertEntities(String tableName, List<? extends IDBObject> entities){
		Statement statement;
		Connection conn = getConnection();
		int affected = 0;
		Iterator<? extends IDBObject> it = entities.iterator();
		
		int [] af = new int[]{0};
		String str="";
		try {
			statement = conn.createStatement();
	        while(it.hasNext()){
	        	IDBObject entity = it.next();
	        	Map<String, Object> attrs=entity.getDBElements();
	    		str = getInsertQueryString(tableName, attrs);
	    		if(str == null)
	    			continue;
	        	statement.addBatch(str);
	        	af = statement.executeBatch();
	        }
	        //af = statement.executeBatch();
        	statement.close();
			releaseConnection(conn);
		} catch (SQLException e) {
			//logger.error("SQLException:{}", e.getMessage());
			releaseConnection(conn);
			return -1;
		}
		for(int i=0;i<af.length;i++){
			affected += af[i];
		}
		return affected;
	}

	

	@Override
    public int insertEntity(String tableName, Map<String, Object> entity){
		Statement statement;
		Connection conn = getConnection();
		int affected = 0;
		
		try {
			statement = conn.createStatement();
    		String str = getInsertQueryString(tableName, entity);
			affected = statement.executeUpdate(str);
			conn.commit();
			statement.close();
			releaseConnection(conn);
		} catch (SQLException e) {
			logger.error("SQLException:{}", e.getMessage());
			releaseConnection(conn);
			return -1;
		}
		return affected;
	}
	
	@Override
    public int insertEntities(String tableName, Map<String, Object>[] entities){
		Statement statement;
		Connection conn = getConnection();
		int affected = 0;
		
		int [] af = new int[]{0};
		try {
			statement = conn.createStatement();
	        for(int i=0;i<entities.length; i++){
	        	Map<String, Object> entity = entities[i];
	    		String str = getInsertQueryString(tableName, entity);
	    		if(str == null)
	    			continue;
	        	statement.addBatch(str);
	        }
	        af = statement.executeBatch();
        	statement.close();
			releaseConnection(conn);
		} catch (SQLException e) {
			logger.error("SQLException:{}", e.getMessage());
			releaseConnection(conn);
			return -1;
		}
		for(int i=0;i<af.length;i++){
			affected += af[i];
		}
		return affected;
	}
	
	private String getInsertQueryString(String tableName, Map<String, Object> values){
		if(values.size()==0)
			return null;
		StringBuilder sk = new StringBuilder();
		StringBuilder sv = new StringBuilder();
		sk.append('(');
		sv.append('(');
		for(Map.Entry<String, Object> entry : values.entrySet()){
			sk.append(entry.getKey());
			sk.append(',');
			sv.append(convertString(entry.getValue()));
			sv.append(',');
		}
		sk.replace(sk.length()-1, sk.length(), ")");
		sv.replace(sv.length()-1, sv.length(), ")");
		
		String str = "insert into " + tableName + " " + sk.toString() + " values " +sv.toString();
		return str;
	}
	
	@Override
	protected int insertEntityImpl(String tableName, IDBObject entity) {

		Map<String, Object> elements = entity.getDBElements();
		String str = getInsertQueryString(tableName, elements);
		if(str == null)
			return -1;
		Statement statement;
		int affected = -1;
		Connection conn = getConnection();
		try {
			statement = conn.createStatement();
			affected = statement.executeUpdate(str);
			conn.commit();
        	statement.close();
			releaseConnection(conn);
		} catch (SQLException e) {
			logger.error("SQLException:{}", e.toString());
			e.printStackTrace();
			releaseConnection(conn);
		}
		return affected;
	}



	@Override
	protected int deleteEntitiesImpl(String tableName, QueryClause query) {
		
		String str = "delete from " + tableName;
		if(query != null){
			String condition = query.toString();
			str += (" where "+condition);
		}
		Statement statement;
		int affected = 0;
		Connection conn = getConnection();
		try {
			statement = conn.createStatement();
			affected = statement.executeUpdate(str);
			conn.commit();
        	statement.close();
			releaseConnection(conn);
		} catch (SQLException e) {
			logger.error("SQLException:{}", e.getMessage());
			releaseConnection(conn);
			return -1;
		}
		return affected;		
	}
	
	
	@Override
	protected int updateEntitiesImpl(String tableName,
			QueryClause query, Map<String, Object> values) {		     

		StringBuilder s = new StringBuilder();
		for(Map.Entry<String, Object> entry : values.entrySet()){
			s.append("");
			s.append(entry.getKey());
			s.append('=');
			s.append(convertString(entry.getValue()));
			s.append(',');
		}
		s.replace(s.length()-1, s.length(), "");
		
		
		String str = "update " + tableName + " set " + s.toString();
		
		if(query.getType() != QueryClauseType.EMPTY){
			String condition = getQueryObject(query);
			str += (" where "+condition);
		}

		Statement statement;
		int affected = 0;
		Connection conn = getConnection();
		try {
			statement = conn.createStatement();
			affected = statement.executeUpdate(str);
			conn.commit();
        	statement.close();
			releaseConnection(conn);
		} catch (SQLException e) {
			releaseConnection(conn);
			logger.error("SQLException:{}", e.getMessage());
			return -1;
		}
		return affected;
	}

	@Override
	protected int updateEntityImpl(String tableName, Object rowKey,
			Map<String, Object> values) {
        String primaryKeyName = getTablePrimaryKeyName(tableName);
        QueryClause qc = new QueryClause(primaryKeyName, rowKey, tableName, null, null);
        return updateEntitiesImpl(tableName, qc, values);
	}

	
	
	@Override
	protected int deleteEntityImpl(String tableName, Object entityKey) {
        String primaryKeyName = getTablePrimaryKeyName(tableName);

		
		String str = "delete from " + tableName + " where " + primaryKeyName + "="+convertString(entityKey);
		Statement statement;
		int affected = 0;
		Connection conn = getConnection();
		try {
			statement = conn.createStatement();
			affected = statement.executeUpdate(str);
			conn.commit();
        	statement.close();
			releaseConnection(conn);
		} catch (SQLException e) {
			logger.error("SQLException:{}", e.getMessage());
			releaseConnection(conn);
			return -1;
		}
		return affected;
	}

    @Override
    public IDBObject getEntity(String tableName, Object entityKey, Class<? extends IDBObject> objClass){
    	IDBObject result = null;
        String primaryKeyName = getTablePrimaryKeyName(tableName);

		String str = "select * from " + tableName + " where " + primaryKeyName + "=" + convertString(entityKey);
		ResultSet results = null;
		Connection conn = getConnection();
		try {
			Statement statement = conn.createStatement();
			
			results = statement.executeQuery(str);
			JDBCResultSet r = new JDBCResultSet(results);
	    	if(r.next()==false)
	    		result = null;
	    	else{
		    	IDBObject rowMapper = getDBObject(objClass);
//System.out.println(rowMapper.toString());
		    	if(rowMapper == null)
		    		result = null;
		    	else
		    		result = rowMapper.mapRow(r);
	    	}
	    	statement.close();
		} catch (SQLException e) {
			logger.error("SQLException:{}", e.getMessage());
			releaseConnection(conn);
			return null;
		}
		releaseConnection(conn);
		return result;
    }
	
	@Override
    public String getTablePrimaryKeyName(String tableName) {
        String primaryKeyName = tablePrimaryKeyMap.get(tableName);
        if (primaryKeyName == null)
            primaryKeyName = DEFAULT_PRIMARY_KEY_NAME;
        return primaryKeyName;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    class RowComparator implements Comparator<Map<String,Object>> {
        private RowOrdering rowOrdering;
        
        public RowComparator(RowOrdering rowOrdering) {
            this.rowOrdering = rowOrdering;
        }
        
        @Override
		public int compare(Map<String,Object> row1, Map<String,Object> row2) {
            if (rowOrdering == null)
                return 0;
            
            for (RowOrdering.Item item: rowOrdering.getItemList()) {
                Comparable key1 = (Comparable)row1.get(item.getColumn());
                Comparable key2 = (Comparable)row2.get(item.getColumn());
                int result = key1.compareTo(key2);
                if (result != 0) {
                    if (item.getDirection() == RowOrdering.Direction.DESC)
                        result = -result;
                    return result;
                }
            }
            
            return 0;
        }
        
        @Override
		public boolean equals(Object obj) {
            if (!(obj instanceof RowComparator))
                return false;
            RowComparator rc = (RowComparator)obj;
            if (rc.rowOrdering == null)
                return this.rowOrdering == null;
            return rc.rowOrdering.equals(this.rowOrdering);
        }
    }

    /**
     * 解析query为条件 字符串
     * @param query
     * @return 条件字符串
     */
	private String getQueryObject(QueryClause query){
		if(query.getType() == QueryClauseType.STRING)
			return query.getCondition();
		
		String result = "";
        List<QueryClauseItem> items = query.getItems();
        
        for(QueryClauseItem item: items){
        	if(!result.isEmpty()){
        		if(query.getOp() == QueryClauseItemType.AND )
        			result += " and ";
        		else if(query.getOp() == QueryClauseItemType.OR )
        			result += " or ";
        		else{
        			logger.warn("unknow op:"+query.getOp());
        		}
        	}
        	if(item.getOp() == OpType.EQ){
        		result += (item.getKey() + "=" + convertString(item.getValue()));
        	}
        	else if(item.getOp() == OpType.GT){
        		result += (item.getKey() + ">" + convertString(item.getValue()));
        	}
        	else if(item.getOp() == OpType.LT){
        		result += (item.getKey() + "<" + convertString(item.getValue()));
        	}
        	else if(item.getOp() == OpType.GTE){
        		result += (item.getKey() + ">=" + convertString(item.getValue()));
        	}
        	else if(item.getOp() == OpType.LTE){
        		result += (item.getKey() + "<=" + convertString(item.getValue()));
        	}
        	else if(item.getOp() == OpType.NE){
        		result += (item.getKey() + "<>" + convertString(item.getValue()));
        	}
        	else if(item.getOp() == OpType.LIKE){
        		result += (item.getKey() + " LIKE " + convertString(item.getValue()));
        	}
        	else{
        		logger.error("unimplemented op: "+ item.getOp());
        		return null;
        	}            	
        }
        return result;
	}
	
	protected Object convertString(Object obj){
		if("now()".equals(obj) || "NOW()".equals(obj))				
			return obj;
		else if(obj instanceof String || obj instanceof Enum)
			return "'" + obj + "'";
		else if(obj instanceof Integer || obj instanceof Long || obj instanceof Float)
			return obj.toString();
		else
			return "'"+obj+"'";
	}
    
    
    @Override
    public void init(SecurityControllerModuleContext context)
            throws SecurityControllerModuleException {
		this.init(context, this);
    }
    	

    @Override
	public void init(SecurityControllerModuleContext context, ISecurityControllerModule module)
            throws SecurityControllerModuleException {
        // read our config options
        Map<String, String> configOptions = context.getConfigParams(module);
        String dbtype = configOptions.get("dbtype").trim();
        String dbhost = configOptions.get("dbhost");
        int dbport = Integer.parseInt(configOptions.get("dbport"));
        String dbname = configOptions.get("dbname").trim();
        dbuser = configOptions.get("dbuser").trim();
        dbpass = configOptions.get("dbpass").trim();
        if(dbtype == null)
        	dbtype = "mysql";
        this.constr = "jdbc:"+dbtype+"://"+dbhost+":"+dbport+"/"+dbname+"?useUnicode=true&characterEncoding=UTF-8";
        logger.debug("Database connection set to {}", constr);
	    this.serviceRegistry = context.getServiceImpl(IRegistryManagementService.class, this);	
		logger.info("BUPT security controller JDBC storage source initialized."); 
    }

    @Override
    public void startUp(SecurityControllerModuleContext context) {
        serviceRegistry.registerService("", this);
		logger.info("BUPT security controller JDBC storage source started."); 
        
    }
}
