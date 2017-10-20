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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.storage.IStorageResultSet;
import com.sds.securitycontroller.storage.NullValueStorageException;
import com.sds.securitycontroller.storage.StorageException;
import com.sds.securitycontroller.storage.TypeMismatchStorageException;


public class MongoDBResultSet implements IAbstractResultSet {

    MongoDBStorageSource storageSource;
    String tableName;
    String primaryKeyName;
    DBCursor cursor;    
    DBObject obj;
    
    DBObject currentObj;
    
    Iterator<IStorageResultSet> resultSetIterator;

    protected static Logger log = LoggerFactory.getLogger(MongoDBStorageSource.class);
    
    MongoDBResultSet(DBCursor cursor) {
    	this.cursor = cursor;
    	this.currentObj = cursor.curr();
    	if(this.currentObj == null && cursor.hasNext())
    		this.currentObj = cursor.next();
    }
    

    public MongoDBResultSet(DBObject obj) {
    	this.obj = obj;
    	this.currentObj = this.obj;
    }
    
    

    Object getObjectValue(String columnName) {
		if(currentObj != null){
			return currentObj.get(columnName);
		}
		return null;
    }
    
    @Override
    public boolean getBoolean(String columnName) {
        Boolean b = getBooleanObject(columnName);
        if (b == null)
            throw new NullValueStorageException(columnName);
        return b.booleanValue();
    }

    @Override
    public byte getByte(String columnName) {
        Byte b = getByteObject(columnName);
        if (b == null)
            throw new NullValueStorageException(columnName);
        return b.byteValue();
    }

    @Override
    public byte[] getByteArray(String columnName) {
        byte[] b = null;
        Object obj = getObjectValue(columnName);
        if (obj != null) {
            if (!(obj instanceof byte[]))
                throw new StorageException("Invalid byte array value");
            b = (byte[])obj;
        }
        return b;
    }

    @Override
    public double getDouble(String columnName) {
        Double d = getDoubleObject(columnName);
        if (d == null)
            throw new NullValueStorageException(columnName);
        return d.doubleValue();
    }

    @Override
    public float getFloat(String columnName) {
        Float f = getFloatObject(columnName);
        if (f == null)
            throw new NullValueStorageException(columnName);
        return f.floatValue();
    }

    @Override
    public int getInt(String columnName) {
        Integer i = getIntegerObject(columnName);
        if (i == null)
            throw new NullValueStorageException(columnName);
        return i.intValue();
    }

    @Override
    public long getLong(String columnName) {
        Long l = getLongObject(columnName);
        if (l == null)
            throw new NullValueStorageException(columnName);
        return l.longValue();
    }

    @Override
    public short getShort(String columnName) {
        Short s = getShortObject(columnName);
        if (s == null)
            throw new NullValueStorageException(columnName);
        return s.shortValue();
    }

    @Override
    public String getString(String columnName) {
        Object obj = getObjectValue(columnName);
        if (obj == null)
            return null;
        return obj.toString();
    }

    @Override
    public Date getDate(String column) {
        Date d;
        Object obj = getObjectValue(column);
        if (obj == null) {
            d = null;
        } else if (obj instanceof Date) {
            d = (Date) obj;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                d = dateFormat.parse(obj.toString());
            }
            catch (ParseException exc) {
                throw new TypeMismatchStorageException(Date.class.getName(), obj.getClass().getName(), column);
            }
        }
        return d;
    }


    @Override
    public Short getShortObject(String columnName)
    {    	
        Short s;
        Object obj = getObjectValue(columnName);
        if (obj instanceof Short) {
            s = (Short)obj;
        } else if (obj != null) {
            try {
                s = Short.parseShort(obj.toString());
            }
            catch (NumberFormatException exc) {
                throw new TypeMismatchStorageException(Short.class.getName(), obj.getClass().getName(), columnName);
            }
        } else {
            s = null;
        }
        return s;
    }
    
    @Override
    public Integer getIntegerObject(String columnName)
    {
        Integer i;
        Object obj = getObjectValue(columnName);
        if (obj instanceof Integer) {
            i = (Integer)obj;
        } else if (obj != null) {
            try {
                i = Integer.parseInt(obj.toString());
            }
            catch (NumberFormatException exc) {
                throw new TypeMismatchStorageException(Integer.class.getName(), obj.getClass().getName(), columnName);
            }
        } else {
            i = null;
        }
        return i;
    }

    @Override
    public Long getLongObject(String columnName)
    {
        Long l;
        Object obj = getObjectValue(columnName);
        if (obj instanceof Long) {
            l = (Long)obj;
        } else if (obj != null) {
            try {
                l = Long.parseLong(obj.toString());
            }
            catch (NumberFormatException exc) {
                throw new TypeMismatchStorageException(Long.class.getName(), obj.getClass().getName(), columnName);
            }
        } else {
            l = null;
        }
        return l;
    }

    @Override
    public Float getFloatObject(String columnName)
    {
        Float f;
        Object obj = getObjectValue(columnName);
        if (obj instanceof Float) {
            f = (Float)obj;
        } else if (obj != null) {
            try {
                f = Float.parseFloat(obj.toString());
            }
            catch (NumberFormatException exc) {
                throw new TypeMismatchStorageException(Float.class.getName(), obj.getClass().getName(), columnName);
            }
        } else {
            f = null;
        }
        return f;
    }

    @Override
    public Double getDoubleObject(String columnName)
    {
        Double d;
        Object obj = getObjectValue(columnName);
        if (obj instanceof Double) {
            d = (Double)obj;
        } else if (obj != null) {
            try {
                d = Double.parseDouble(obj.toString());
            }
            catch (NumberFormatException exc) {
                throw new TypeMismatchStorageException(Double.class.getName(), obj.getClass().getName(), columnName);
            }
        } else {
            d = null;
        }
        return d;
    }

    @Override
    public Boolean getBooleanObject(String columnName)
    {
        Boolean b;
        Object obj = getObjectValue(columnName);
        if (obj instanceof Boolean) {
            b = (Boolean)obj;
        } else if (obj != null) {
            try {
                b = Boolean.parseBoolean(obj.toString());
            }
            catch (NumberFormatException exc) {
                throw new TypeMismatchStorageException(Boolean.class.getName(), obj.getClass().getName(), columnName);
            }
        } else {
            b = null;
        }
        return b;
    }

    @Override
    public Byte getByteObject(String columnName)
    {
        Byte b;
        Object obj = getObjectValue(columnName);
        if (obj instanceof Byte) {
            b = (Byte)obj;
        } else if (obj != null) {
            try {
                b = Byte.parseByte(obj.toString());
            }
            catch (NumberFormatException exc) {
                throw new TypeMismatchStorageException(Byte.class.getName(), obj.getClass().getName(), columnName);
            }
        } else {
            b = null;
        }
        return b;
    }

    

	@Override
	public List<IDBObject> mapToObjects(IDBObject rowMapper) {
        List<IDBObject> objectList = new ArrayList<IDBObject>();

        do{
        	IDBObject object = rowMapper.mapRow(this);
            objectList.add(object);
        } while(this.next());

        /*
        Class<? extends  IDBObject> mappedClass = rowMapper.getClass();
        Morphia morphia = new Morphia();
		if(!morphia.isMapped(mappedClass))
			morphia.map(mappedClass);
        Iterator<DBObject> iter = this.cursor.iterator();
        while(iter.hasNext()){
        	DBObject dbo = iter.next();
    		//map to object
        	Object obj = morphia.fromDBObject(mappedClass, dbo);
    		objectList.add(obj);
        }
        */

        return objectList;
	}

	
	@Override
	public boolean next() {
		//only one object, not an object set
		if(this.cursor == null){
			this.currentObj = null;
			return false;
		}
		else{			
			//an object set
			if(this.cursor.hasNext()){
				this.currentObj = this.cursor.next();
				return true;
			}
			else{
				this.currentObj = null;
				return false;
			}
		}
	}


	@Override
	public int size() {
		if(this.cursor == null)
			return (this.currentObj == null)?0:1;
		return this.cursor.size();
	}


	@Override
	public Object getValueByColumnIndex(int column) {
		//unimplemented
		log.error("getValueByColumnIndex in mongo driver is unimplemented");
		return null;
	}
}
