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

package com.sds.securitycontroller.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.storage.QueryClause.QueryClauseItemType;


public interface IStorageSourceService extends ISecurityControllerService {

    /** Set the column to be used as the primary key for a table. This should
     * be guaranteed to be unique for all of the rows in the table, although the
     * storage API does not necessarily enforce this requirement. If no primary
     * key name is specified for a table then the storage API assumes there is
     * a column named "id" that is used as the primary key. In this case when
     * a new row is inserted using the storage API and no id is specified
     * explictly in the row data, the storage API automatically generates a
     * unique ID (typically a UUID) for the id column. To work across all
     * possible implementations of the storage API it is safest, though, to
     * specify the primary key column explicitly.
     * FIXME: It's sort of a kludge to have to specify the primary key column
     * here. Ideally there would be some sort of metadata -- perhaps stored
     * directly in the table, at least in the NoSQL case -- that the
     * storage API could query to obtain the primary key info.
     * @param tableName The name of the table for which we're setting the key
     * @param primaryKeyName The name of column to be used as the primary key
     */
    public void setTablePrimaryKeyName(String tableName, String primaryKeyName);
    

    public String getTablePrimaryKeyName(String tableName);

    /** Create a new table if one does not already exist with the given name.
     * 
     * @param tableName The name of the table to create.
     * @param indexedColumns Which columns should be indexed, and the column types
     */
    public void createTable(String tableName, Map<String, String> indexedColumns);
    
    /**
     * @return the set of all tables that have been created via createTable
     */
    Set<String> getAllTableNames();
    
    /** Create a query object representing the given query parameters. The query
     * object can be passed to executeQuery to actually perform the query and obtain
     * a result set.
     * 
     * @param tableName The name of the table to query.
     * @param columnNames The list of columns to return in the result set.
     * @param predicate The predicate that specifies which rows to return in the result set.
     * @param ordering Specification of order that rows are returned from the result set
     * returned from executing the query. If the ordering is null, then rows are returned
     * in an implementation-specific order.
     * @return Query object to be passed to executeQuery.
     */
    QueryClause createQuery(String tableName, String[] columnNames, List<QueryClauseItem> clauseItems, 
    		QueryClauseItemType type, RowOrdering ordering);    
    QueryClause createQuery(String tableName, String[] columnNames, String condition, RowOrdering ordering);

    /** Execute a query created with the given query parameters.
     *
     * @param query The query to execute
     * @param tableName The name of the table to query.
     * @param columnNames The list of columns to return in the result set.
     * @param ordering Specification of order that rows are returned from the result set
     * @return The result set containing the rows/columns specified in the query.
     */
//    IAbstractResultSet executeQuery(QueryClause query);
//    IAbstractResultSet executeQuery(String tableName, String[] columnNames,String condition, RowOrdering order);
    
    
    /** Execute a query and call the row mapper to map the results to Java objects.
     * 
     * @param rowMapper The client-supplied object that maps the data in a row in the result
     * set to a client object.    
     * @return The result object list containing for the query.
     */
    
    List<? extends IDBObject> executeQuery(QueryClause query, Class<? extends IDBObject>  objClass);
    List<? extends IDBObject> executeQuery(String tableName, String condition, RowOrdering order,  Class<? extends IDBObject> objClass);
    
    /** Insert a new row in the table with the given column data.
     * If the primary key is the default value of "id" and is not specified in the
     * then a unique id will be automatically assigned to the row.
     * @param tableName The name of the table to which to add the row
     * @param Map<String,Object> values The map of column names/values to add to the table.
     * @param IDBObject values The object to add to the table.
     */
    int insertEntity(String tableName, IDBObject entity);    
    int insertEntities(String tableName, List<? extends IDBObject> entities);
    
    int insertEntity(String tableName, Map<String, Object> entity);   
    int insertEntities(String tableName, Map<String, Object>[] entities);

    /** Update the rows in the given table. Any rows matching the predicate
     * are updated with the column names/values specified in the values map.
     * (The values map should not contain the special column "id".)
     * @param tableName The table to update
     * @param query The predicate to use to select which rows to update
     * @param entityValues The map of column names/values to update the rows.
     */
    int updateEntities(String tableName, QueryClause query, Map<String,Object> entityValues);
    
    /** Update or insert a row in the table with the given row key (primary
     * key) and column names/values. (If the values map contains the special
     * column "id", its value must match rowId.)
     * @param tableName The table to update or insert into
     * @param  Map<String,Object> values The map of column names/values to update the rows
     */   
    int updateOrInsertEntity(String tableName, IDBObject entity);
    
    /**
     * Delete the rows that match the predicate
     * @param tableNamestorageSource
     * @param predicate
     */
    int deleteEntity(String tableName, Object entityKey);
    int deleteEntities(String tableName, QueryClause query);
    
    /** Query for a row with the given ID (primary key).
     * 
     * @param tableName The name of the table to query
     * @param entityKey The primary key of the row
     * @return The result set containing the row with the given ID
     */
    IDBObject getEntity(String tableName, Object entityKey, Class<? extends IDBObject> objClass);
    
    /**
     * Set exception handler to use for asynchronous operations.
     * @param exceptionHandler
     */
    void setExceptionHandler(IStorageExceptionHandler exceptionHandler);
    
    /** Add a listener to the specified table. The listener is called
     * when any modifications are made to the table. You can add the same
     * listener instance to multiple tables, since the table name is
     * included as a parameter in the listener methods.
     * @param tableName The name of the table to listen for modifications
     * @param listener The listener instance to call
     */
    public void addListener(String tableName, IStorageSourceListener listener);
    
    /** Remove a listener from the specified table. The listener should
     * have been previously added to the table with addListener.
     * @param tableName The name of the table with the listener
     * @param listener The previously installed listener instance
     */
    public void removeListener(String tableName, IStorageSourceListener listener);
    
    /** This is logically a private method and should not be called by
     * clients of this interface.
     * @param notifications the notifications to dispatch
     */
    public void notifyListeners(List<StorageSourceNotification> notifications);

}