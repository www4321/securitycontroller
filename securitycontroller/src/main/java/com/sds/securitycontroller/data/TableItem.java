package com.sds.securitycontroller.data;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class TableItem implements IDBObject{

	private static final long serialVersionUID = 7958533577146631428L;
	public static final String 	keyId = "id";
	public long 	currentId;
	public String 	targetTableName;

	public String 	espcTableName;
	public String 	defaultcategory;
	public String 	defaulttype;
	public String 	defaultobject_type;
	public Map<String, FeildItem> tableFieldsMap = new HashMap<String, FeildItem>(); //field:FieldItem

    protected static Map<String, Method> dbFieldMapping;
    protected static Logger log = LoggerFactory.getLogger(TableItem.class);

	public TableItem()
	{
	}

	public TableItem(String espcName, String cate, String type, String obj_type, String targetTableName){
		this.espcTableName = espcName;
		this.defaultcategory = cate;
		this.defaulttype = type;
		this.defaultobject_type = obj_type;
		this.targetTableName = targetTableName;
	}

	public void putFeildItem(String key, String type, String co){
		this.tableFieldsMap.put(key, new FeildItem(type, co));
	}

	public FeildItem getFeildItem(String key){
		return this.tableFieldsMap.get(key);
	}

	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tablename", this.espcTableName);
		map.put("defaultcategory", this.defaultcategory);
		map.put("defaulttype", this.defaulttype);
		map.put("defaultobjecttype", this.defaultobject_type);
		map.put("targettablename", this.espcTableName);

	    ObjectMapper om = new ObjectMapper();
	    om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES,false);
	    om.configure(SerializationFeature.INDENT_OUTPUT,true);
	    om.setSerializationInclusion(Include.NON_NULL);
	    String str = "{}";
		try {
			str = om.writeValueAsString(this.tableFieldsMap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		map.put("fields", str);

		return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if(dbFieldMapping == null){
			dbFieldMapping = new HashMap<String, Method>();
		    Class<? extends TableItem> cla=this.getClass();
			try {
				dbFieldMapping.put("tablename", cla.getDeclaredMethod("tablename"));
				dbFieldMapping.put("defaultcategory", cla.getDeclaredMethod("defaultcategory"));
				dbFieldMapping.put("defaulttype", cla.getDeclaredMethod("defaulttype"));
				dbFieldMapping.put("defaultobjecttype", cla.getDeclaredMethod("defaultobjecttype"));
				dbFieldMapping.put("targettablename", cla.getDeclaredMethod("targettablename"));
			} catch (NoSuchMethodException | SecurityException e) {
			    log.error("getFieldValueByKeys error: "+e.getMessage());
				return null;
			}
		}
		Method m = dbFieldMapping.get(key);
		try {
			return m.invoke(this, new Object[0]);
		}catch(Exception e){
			log.error("getFieldValueByKeys error: "+e.getMessage());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {

		TableItem tableItem = new TableItem(
				resultSet.getString("tablename"),
				resultSet.getString("defaultcategory"),
				resultSet.getString("defaulttype"),
				resultSet.getString("defaultobjecttype"),
				resultSet.getString("targettablename"));
//		tableItem.currentId = resultSet.getInt("_id");
		String fstr = resultSet.getString("fields");
		/*Map<String, Object> fieldItem = new HashMap<String, Object>();
		try {
			fieldItem = new ObjectMapper().readValue(fstr, HashMap.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tableItem.putFeildItem((String)fieldItem.get("fieldname"), (String)fieldItem.get("type"), (String)fieldItem.get("correspondence"));
*/
        List<HashMap<String,Object>> fields = new ArrayList<>();
        try {
            fields = new ObjectMapper().readValue(fstr, ArrayList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (HashMap<String , Object> fieldItem : fields) {
            tableItem.putFeildItem((String)fieldItem.get("fieldname"), (String)fieldItem.get("type"), (String)fieldItem.get("correspondence"));
        }
        return tableItem;
	}
}
