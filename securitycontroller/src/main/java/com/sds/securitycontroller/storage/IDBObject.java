/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.storage;

import java.io.Serializable;
import java.util.Map;

public interface IDBObject extends Serializable{
	Map<String,Object> getDBElements();
	Object getFieldValueByKey(String key);

    /** This method must be implemented by the client of the storage API
     * to map the current row in the result set to a Java object.
     * 
     * @param resultSet The result set obtained from a storage source query
     * @return The object created from the data in the result set
     */
	IDBObject mapRow(IAbstractResultSet resultSet);
}
