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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.storage.QueryClause.QueryClauseItemType;

public abstract class AbstractStorageSource 
    implements IStorageSourceService, ISecurityControllerModule {
    protected static Logger logger = LoggerFactory.getLogger(AbstractStorageSource.class);

    // Shared instance of the executor to use to execute the storage tasks.
    // We make this a single threaded executor, because if we used a thread pool
    // then storage operations could be executed out of order which would cause
    // problems in some cases (e.g. delete and update of a row getting reordered).
    // If we wanted to make this more multi-threaded we could have multiple
    // worker threads/executors with affinity of operations on a given table
    // to a single worker thread. But for now, we'll keep it simple and just have
    // a single thread for all operations.
    protected static ExecutorService defaultExecutorService = Executors.newSingleThreadExecutor();

    
    protected Set<String> allTableNames = new CopyOnWriteArraySet<String>();
    protected ExecutorService executorService = defaultExecutorService;
    protected IStorageExceptionHandler exceptionHandler;

    private Map<String, Set<IStorageSourceListener>> listeners =
        new ConcurrentHashMap<String, Set<IStorageSourceListener>>();

    
    protected static final String DB_ERROR_EXPLANATION =
            "An unknown error occurred while executing asynchronous " +
            "database operation";
    
    abstract class StorageCallable<V> implements Callable<V> {
        @Override
		public V call() {
            try {
                return doStorageOperation();
            }
            catch (StorageException e) {
                logger.error("Failure in asynchronous call to executeQuery", e);
                if (exceptionHandler != null)
                    exceptionHandler.handleException(e);
                throw e;
            }
        }
        abstract protected V doStorageOperation();
    }
    
    abstract class StorageRunnable implements Runnable {
        @Override
		public void run() {
            try {
                doStorageOperation();
            }
            catch (StorageException e) {
                logger.error("Failure in asynchronous call to updateRows", e);
                if (exceptionHandler != null)
                    exceptionHandler.handleException(e);
                throw e;
            }
        }
        abstract void doStorageOperation();
    }
    
    public AbstractStorageSource() {
        this.executorService = defaultExecutorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = (executorService != null) ?
                executorService : defaultExecutorService;
    }
    
    @Override
    public void setExceptionHandler(IStorageExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    
    @Override
    public abstract void setTablePrimaryKeyName(String tableName, String primaryKeyName);

    @Override
    public void createTable(String tableName, Map<String, String> indexedColumns) {
        allTableNames.add(tableName);
    }

    @Override
    public Set<String> getAllTableNames() {
        return allTableNames;
    }

	@Override
	public QueryClause createQuery(String tableName, String[] columnNames,
			List<QueryClauseItem> clauseItems, QueryClauseItemType type, RowOrdering ordering) {
		QueryClause qc = new QueryClause(clauseItems, type, tableName, columnNames, ordering);
        return qc;
	}
	

	@Override
	public QueryClause createQuery(String tableName, String[] columnNames,
			String condition, RowOrdering ordering) {
		QueryClause qc = new QueryClause(condition, tableName, columnNames, ordering, null);
        return qc;
	}


    protected abstract int insertEntityImpl(String tableName, IDBObject entity);
    
	@Override
	public int insertEntity(String tableName, IDBObject entity) {
		return insertEntityImpl(tableName, entity);
	}


    protected abstract int updateEntitiesImpl(String tableName, QueryClause query,
                                    Map<String, Object> values);
    @Override
    public int updateEntities(String tableName, QueryClause query, Map<String,Object> values){
        return updateEntitiesImpl(tableName, query, values);
    }
    
	
    protected abstract int updateEntityImpl(String tableName, Object entityKey,
                                   Map<String, Object> values);
    
    public int updateOrInsertEntity(String tableName, Object entityKey, IDBObject entity) {
    	Map<String, Object> values = entity.getDBElements();
    	Object result = getEntity(tableName, entityKey, entity.getClass());
		if(result == null)
    		return insertEntity(tableName, entity);
		else
			return updateEntityImpl(tableName, entityKey, values);
    }
    

    @Override
    public int updateOrInsertEntity(String tableName, IDBObject entity) {
    	String keyName = getTablePrimaryKeyName(tableName);
    	Map<String, Object> values = entity.getDBElements();
    	Object keyValue = values.get(keyName);
    	return updateOrInsertEntity(tableName, keyValue, entity);
    }

    
    protected abstract int deleteEntityImpl(String tableName, Object entityKey);

    @Override
    public int deleteEntity(String tableName, Object entityKey) {
    	return deleteEntityImpl(tableName, entityKey);
    }    


    protected abstract int deleteEntitiesImpl(String tableName, QueryClause query);
    
    @Override
    public int deleteEntities(String tableName, QueryClause query) {
    	return deleteEntitiesImpl(tableName, query);
    }
    

    //protected abstract IAbstractResultSet executeQueryImpl(QueryClause query);
//    
//    @Override
//    public IAbstractResultSet executeQuery(QueryClause query) {
//        return executeQueryImpl(query);
//    }
//    
//
//    @Override
//    public IAbstractResultSet executeQuery(String tableName, String[] columnNames, String condition, RowOrdering order) {
//    	QueryClause query = createQuery(tableName, columnNames, condition, order);
//        return executeQueryImpl(query);
//    }
    
    
    

    @Override
    public List<? extends IDBObject> executeQuery(String tableName, String condition, RowOrdering order, Class<? extends IDBObject> objClass) {
    	QueryClause query = createQuery(tableName, null, condition, order);
        return executeQuery(query, objClass);
    }
    

    @Override
    public synchronized void addListener(String tableName, IStorageSourceListener listener) {
        Set<IStorageSourceListener> tableListeners = listeners.get(tableName);
        if (tableListeners == null) {
            tableListeners = new CopyOnWriteArraySet<IStorageSourceListener>();
            listeners.put(tableName, tableListeners);
        }
        tableListeners.add(listener);
    }
  
    @Override
    public synchronized void removeListener(String tableName, IStorageSourceListener listener) {
        Set<IStorageSourceListener> tableListeners = listeners.get(tableName);
        if (tableListeners != null) {
            tableListeners.remove(listener);
        }
    }

    protected synchronized void notifyListeners(StorageSourceNotification notification) {
        String tableName = notification.getTableName();
        Set<Object> keys = notification.getKeys();
        Set<IStorageSourceListener> tableListeners = listeners.get(tableName);
        if (tableListeners != null) {
            for (IStorageSourceListener listener : tableListeners) {
                try {
                    switch (notification.getAction()) {
                        case MODIFY:
                            listener.rowsModified(tableName, keys);
                            break;
                        case DELETE:
                            listener.rowsDeleted(tableName, keys);
                            break;
                    }
                }
                catch (Exception e) {
                    logger.error("Exception caught handling storage notification", e);
                }
            }
        }
    }
    
    @Override
    public void notifyListeners(List<StorageSourceNotification> notifications) {
        for (StorageSourceNotification notification : notifications)
            notifyListeners(notification);
    }
    
    // ISecurityControllerModule

    @Override
    public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
        Collection<Class<? extends ISecurityControllerService>> l = 
                new ArrayList<Class<? extends ISecurityControllerService>>();
        l.add(IStorageSourceService.class);
        return l;
    }
    
    @Override
    public Map<Class<? extends ISecurityControllerService>,
               ISecurityControllerService> getServiceImpls() {
        Map<Class<? extends ISecurityControllerService>,
            ISecurityControllerService> m = 
                new HashMap<Class<? extends ISecurityControllerService>,
                            ISecurityControllerService>();
        m.put(IStorageSourceService.class, this);
        return m;
    }
    
    @Override
    public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
        Collection<Class<? extends ISecurityControllerService>> l = 
                new ArrayList<Class<? extends ISecurityControllerService>>();
        return l;
    }

    @Override
    public void init(SecurityControllerModuleContext context)
            throws SecurityControllerModuleException {
    }

    public void init(SecurityControllerModuleContext context, ISecurityControllerModule module)
            throws SecurityControllerModuleException {
    }

    @Override
    public void startUp(SecurityControllerModuleContext context) {
    }
    
    protected IDBObject getDBObject(Class<? extends IDBObject> objClass){
		IDBObject rowMapper = null;

		try {
	        Constructor<? extends IDBObject> c = objClass.getDeclaredConstructor();
	        c.setAccessible(true);  
			rowMapper = c.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(objClass.toString());
			//logger.error(c.length);
			logger.error(e.getMessage());
			return null;
		}
		return rowMapper;
	}
}
