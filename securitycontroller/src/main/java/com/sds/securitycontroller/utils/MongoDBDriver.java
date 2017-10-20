/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.utils;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.mongodb.morphia.Morphia;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.sds.securitycontroller.storage.IDBObject;

public class MongoDBDriver{
	static private MongoDBDriver instance = null;
	Mongo mg = null;
	Morphia morphia = null;
    DB db;
    DBCollection collection =null;
    String host = "db.research.intra.sds.com";
    String dbName = "securitycontroller";
    String collectionName = "flow";

    protected static Logger log = LoggerFactory.getLogger(MongoDBDriver.class);
    
    
    private MongoDBDriver(String host){
    	if (host != null)
    		this.host = host;
    	init();
    }
    public static MongoDBDriver getInstance(){
    	instance = new MongoDBDriver(null);
    	return instance;
    }
    
    public void init() {
        try {
            mg = new MongoClient(host,27017);
            //mg = new Mongo("localhost", 27017);
            morphia = new Morphia();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (MongoException e) {
            e.printStackTrace();
        }
        db = mg.getDB(dbName);
        collection = db.getCollection(collectionName);
    }
    public void destory() {
        if (mg != null)
            mg.close();
        mg = null;
        db = null;
        collection = null;
        System.gc();
    }
    
    public void print(Object o) {
        System.out.println(o);
    } 
    
    //list all
    public void listAll() {
//        print("查询users的所有数据：");
        //db游标
        DBCursor cur = collection.find();
        while (cur.hasNext()) {
            print(cur.next());
        }
    }
    
    //C
    public void insertOne(String key,Object obj){
    	try {
    		BasicDBObject dbObj = new BasicDBObject(key,obj);
        	print(collection.insert(dbObj).getN());
		}
    	catch (Exception e) {
    		log.error("insert error: "+ e.getMessage());
		}
    }
    
    public void insertDBOList(List<Object> list,@SuppressWarnings("rawtypes") Class insertClass){
    	try {
    		for(Object obj:list){
    			morphia.map(insertClass);
    			DBObject dbo = morphia.toDBObject(obj);
    			collection.insert(dbo);
    		}
//    		print(collection.insert(list).getN());
		}
    	catch (MongoException e) {
    		log.error("Duplicate key error!: {}",e.getMessage());
    		//e.printStackTrace();
		}
    }
    
    public void insertMap(@SuppressWarnings("rawtypes") Map map){
    	try {
    		BasicDBObject bdbo = new BasicDBObject(map);
    		print(collection.insert(bdbo).getN());
		}
    	catch (Exception e) {
    		log.error("insert error: "+ e.getMessage());
    		//e.printStackTrace();
		}
    }

    
	public Object findOneByKey(String key,Object value,Class<? extends IDBObject> mappedClass){
    	
    	Object result = null;
    	try {
    		DBObject one = collection.findOne(new BasicDBObject(key,value));

    		//map to object
    		if(!morphia.isMapped(mappedClass))
    			morphia.map(mappedClass);
    		result = morphia.fromDBObject(mappedClass, one);
    		/*
    		MongoDBResultSet set = new MongoDBResultSet(one);
    		IDBObject tmp = mappedClass.newInstance();
    		result = tmp.mapRow(set);
    		*/    
		}
    	catch (Exception e) {
    		log.error("find error: "+ e.getMessage());
    		//e.printStackTrace();
    		result = null;
		}
    	return result;
	}

	public Object findOneByCompoundConditions(Map<String, Object> conditionMap,Class<? extends IDBObject> mappedClass){
    	DBObject dbo = null;
    	Object result = null;
    	try {
    		DBObject compoundCondition_query=new BasicDBObject();  
            //
    		for(Entry<String,Object> entry: conditionMap.entrySet()){
    			compoundCondition_query.put(entry.getKey(), entry.getValue());
    		}
    		DBCursor compoundQueryResult=collection.find(compoundCondition_query);  
    		if(compoundQueryResult.hasNext()){
    			dbo = compoundQueryResult.next();
        		//map to object
        		if(!morphia.isMapped(mappedClass))
        			morphia.map(mappedClass);
        		result = morphia.fromDBObject(mappedClass, dbo);
    		}
		}
    	catch (Exception e) {
    		log.error("find error: "+ e.getMessage());
    		result = null;
		}
    	return result;
    }
    
}