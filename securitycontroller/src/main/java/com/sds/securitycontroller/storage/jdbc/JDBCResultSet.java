/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.storage.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class JDBCResultSet implements IAbstractResultSet {
    protected static Logger log = LoggerFactory.getLogger(JDBCResultSet.class);
	
	private ResultSet results;
	

	JDBCResultSet(ResultSet results){
		this.results = results;
	}


	public ResultSet getResults() {
		return results;
	}


	@Override
	public List<IDBObject> mapToObjects(IDBObject rowMapper) {
        List<IDBObject> objectList = new ArrayList<IDBObject>();
        while(this.next()){
        	IDBObject object = rowMapper.mapRow(this);
			objectList.add(object);
		}
		return objectList;
	}

	//Wrapper
	@Override
	public String getString(String columnName){
		try {
			return results.getString(columnName);
		} catch (SQLException e) {
			log.debug("SQLException: {}", e.getMessage());
			return null;
		}
	}

	@Override
    public short getShort(String columnName){
		try {
			return results.getShort(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1;
		}
	}

	@Override
    public int getInt(String columnName){
		try {
			return results.getInt(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1;
		}
	}

	@Override
    public long getLong(String columnName){
		try {
			return results.getLong(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1;
		}
	}

	@Override
    public float getFloat(String columnName){
		try {
			return results.getFloat(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1;
		}
	}

	@Override
    public double getDouble(String columnName){
		try {
			return results.getDouble(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1;
		}
	}

	@Override
    public boolean getBoolean(String columnName){
		try {
			return results.getBoolean(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return false;
		}
	}

	@Override
    public byte getByte(String columnName){
		try {
			return results.getByte(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1;
		}
	}

	@Override
    public byte[] getByteArray(String columnName){
		try {
			return results.getBytes(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return null;
		}
	}

	@Override
    public Date getDate(String columnName){
		try {
			return results.getDate(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return new Date();
		}
	}
    

	@Override
    public Short getShortObject(String columnName){
		try {
			return results.getShort(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1;
		}
	}

	@Override
    public Integer getIntegerObject(String columnName){
		try {
			return results.getInt(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1;
		}
	}

	@Override
    public Long getLongObject(String columnName){
		try {
			return results.getLong(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1L;
		}
	}

	@Override
    public Float getFloatObject(String columnName){
		try {
			return results.getFloat(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1F;
		}
	}

	@Override
    public Double getDoubleObject(String columnName){
		try {
			return results.getDouble(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1D;
		}
	}

	@Override
    public Boolean getBooleanObject(String columnName){
		try {
			return results.getBoolean(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return false;
		}
	}

	@Override
    public Byte getByteObject(String columnName){
		try {
			return results.getByte(columnName);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return -1;
		}
	}

	@Override
	public Object getValueByColumnIndex(int column) {
		try {
			return results.getObject(column);
		} catch (SQLException e) {
			log.error("SQLException: {}", e.getMessage());
			return null;
		}
	}
    

	@Override
	public boolean next() {
		try {
			return this.results.next();
		} catch (SQLException e) {
			return false;
		}
	}


	@Override
	public int size() {
		int size = 0;
		try {
			this.results.beforeFirst();
			this.results.last();  
			size = this.results.getRow();
			this.results.beforeFirst();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return size;
	}



}
