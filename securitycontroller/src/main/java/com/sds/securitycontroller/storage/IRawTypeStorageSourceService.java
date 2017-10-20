package com.sds.securitycontroller.storage;

import java.util.List;
import java.util.Map;

public interface IRawTypeStorageSourceService {

//    public void setTablePrimaryKeyName(String tableName, String primaryKeyName);
//    public String getTablePrimaryKeyName(String tableName);
//    public void createTable(String tableName, Map<String, String> indexedColumns);
//    Set<String> getAllTableNames();
//    QueryClause createQuery(String tableName, String[] columnNames, List<QueryClauseItem> clauseItems, RowOrdering ordering);    
//    QueryClause createQuery(String tableName, String[] columnNames, String condition, RowOrdering ordering);

    /**
     * query for object 
     * @param query: query param which with query conditions
     * @return
     */
	public Object executeQueryRawType(String collectionName, Object query);

//    Object executeQueryRawType(String collectionName, String condition, RowOrdering order);
    
//    List<Serializable> executeQuery(QueryClause query, IDBObject rowMapper);
//    List<Serializable> executeQuery(String tableName, String[] columnNames,String condition, RowOrdering order, IDBObject rowMapper);
    
    /**
     * 
     * @param collectionName
     * @param entity
     * @return
     */
    boolean insertEntityRawType(String collectionName, Object entity);
    
    boolean insertEntitiesRawType(String collectionName, List<Object> entities);
//    boolean insertEntities(String collectionName, Map<String, Object>[] entities);

    /** Update the rows in the given table. Any rows matching the predicate
     * are updated with the column names/values specified in the values map.
     * (The values map should not contain the special column "id".)
     * @param tableName The table to update
     * @param query The predicate to use to select which rows to update
     * @param entityValues The map of column names/values to update the rows.
     */
    boolean updateEntitiesRawType(String collectionName, QueryClause query, Map<String,Object> entityValues);
    
    /** Update or insert a row in the table with the given row key (primary
     * key) and column names/values. (If the values map contains the special
     * column "id", its value must match rowId.)
     * @param tableName The table to update or insert into
     * @param  Map<String,Object> values The map of column names/values to update the rows
     */   
    boolean updateOrInsertEntityRawType(String tableName, Object entity);
    
    /**
     * Delete the rows that match the predicate
     * @param tableName
     * @param predicate
     */
	boolean deleteEntityRawType(String collectionName, Object entityKey);
    boolean deleteEntitiesRawType(String collectionName, QueryClause query);
    
//    Object getEntityRawType(String tableName, Object entityKey, IDBObject rowMapper);
//    void setExceptionHandler(IStorageExceptionHandler exceptionHandler);
//    public void addListener(String tableName, IStorageSourceListener listener);
//    public void removeListener(String tableName, IStorageSourceListener listener);
//    public void notifyListeners(List<StorageSourceNotification> notifications);

}
