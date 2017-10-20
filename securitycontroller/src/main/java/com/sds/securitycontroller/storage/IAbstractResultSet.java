/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.storage;

import java.util.Date;
import java.util.List;

public interface IAbstractResultSet {
	
	public List<IDBObject> mapToObjects(IDBObject rowMapper);
	
	
    public String getString(String columnName);
    public short getShort(String columnName);
    public int getInt(String columnName);
    public long getLong(String columnName);
    public float getFloat(String columnName);
    public double getDouble(String columnName);
    public boolean getBoolean(String columnName);
    public byte getByte(String columnName);
    public byte[] getByteArray(String columnName);
    public Date getDate(String columnName);

    public Short getShortObject(String columnName);
    public Integer getIntegerObject(String columnName);
    public Long getLongObject(String columnName);
    public Float getFloatObject(String columnName);
    public Double getDoubleObject(String columnName);
    public Boolean getBooleanObject(String columnName);
    public Byte getByteObject(String columnName);
    
    public Object getValueByColumnIndex(int column);
    
    public boolean next();
    public int size();

}
