package com.sds.securitycontroller.test2;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.storage.jdbc.JDBCSqlStorageSource;

public class TestDBObject implements IDBObject {
	private String id;
	private String name;
	protected static Map<String, Method> dbFieldMapping;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public TestDBObject() {
		super();
	}
	
	public TestDBObject(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.id);
		map.put("name", this.name);
		
		return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if (dbFieldMapping == null) {
			dbFieldMapping = new HashMap<String, Method>();
			Class<? extends TestDBObject> cla = this.getClass();
			try {
				dbFieldMapping.put("id", cla.getDeclaredMethod("id"));
				dbFieldMapping.put("name", cla.getDeclaredMethod("name"));
				
			} catch (NoSuchMethodException | SecurityException e) {
			    
				return null;
			}
		}
		Method m = dbFieldMapping.get(key);		    
		try { 
			return m.invoke(this, new Object[0]);
		}catch(Exception e){
			//log.error("getFieldValueByKeys error: "+e.getMessage());
			return null;
		}
	}

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {
		String id=resultSet.getString("id")!=null?resultSet.getString("id"):"";
		String name=resultSet.getString("name")!=null?resultSet.getString("name"):"";
		
		
		return new TestDBObject(id, name);
	}
	public static void main(String[]args){
		Map<String, String> indexedColumns = new HashMap<String, String>(){
			private static final long serialVersionUID = 1L;
			{
				put("id","varchar(128)");
				put("name","varchar(128)");
				put("PRIMARY_KEY","id");
			}
		};
		JDBCSqlStorageSource storageSource = new JDBCSqlStorageSource();
		storageSource.createTable("", indexedColumns);
	}
}
