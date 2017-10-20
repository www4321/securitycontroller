/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.storage;

import java.util.HashMap;
import java.util.Map;

public class TableMapping {
	
	static Map<String, String> tableMapping = new HashMap<String, String>();

	
	public static String getTableMapping(String alias){
		return tableMapping.get(alias);
	}
	
	public static void addTableMapping(String alias, String tableName){
		tableMapping.put(alias, tableName);
	}

}
