/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.log;


import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sds.securitycontroller.storage.CommonTool;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;


public class ReportItem implements Serializable, IDBObject {
	private static final long serialVersionUID = 2696087338847290712L;
	
	protected static Logger log = LoggerFactory.getLogger(ReportItem.class);
	
	private Map<String, Object>  	jsonMaps = null;// Map<String, Object>
	
	public 	static final String key_Id 			= "_id";
	
	public 	static final String keyRegisterId 	= "registerid";
	public 	static final String keyCategory 	= "category";
	public 	static final String keyType 		= "type";
	public 	static final String keySeverity 	= "severity";
	public 	static final String keyTime 		= "time";
	public 	static final String keyHost 		= "host";
	public 	static final String keyHashId 		= "hashid";
	public 	static final String keyObjectType 	= "objecttype";
	public 	static final String keyOriginalTableName= "originaltable";
	
	public long id;
	
	
	private String rawJson;

	public ReportItem(String json) {
		this.rawJson = json;
	}
	
	public ReportItem(){
	}
	
	public String getRawData(){
		return this.rawJson;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> DecodeLogJson() {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			this.jsonMaps = objectMapper.readValue(this.rawJson, Map.class);
			return this.jsonMaps;
		} catch (JsonParseException e) {
			log.error(e.getMessage());
		} catch (JsonMappingException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		return null;
	}
	public Map<String, Object> getMaps() {
		return this.jsonMaps;
	}
	
	public void setMaps(Map<String, Object> map){
		this.jsonMaps = map;
	}
		
	

	public boolean VerifyItem(){
		return 	this.jsonMaps.containsKey(keyRegisterId) &&
				this.jsonMaps.containsKey(keyCategory) &&
				this.jsonMaps.containsKey(keyType) &&
				this.jsonMaps.containsKey(keySeverity) &&
				this.jsonMaps.containsKey(keyTime) &&
				this.jsonMaps.containsKey(keyHost) &&
				this.jsonMaps.containsKey(keyHashId) &&
				this.jsonMaps.containsKey(keyObjectType);
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public String getCategory(){
		return (String)this.getFieldValueByKey(keyCategory);
	}
	
	public String getType(){
		return (String)this.getFieldValueByKey(keyType);
	}
	
	public String getSeverity(){
		return this.getFieldValueByKey(keySeverity).toString();
	}
	
	public String getTime(){
		return (String)this.getFieldValueByKey(keyTime);
	}
	
	public String getHost(){
		return (String)this.getFieldValueByKey(keyHost);
	}
	
	//public String getHashId(){
	//	return (String)this.getFieldValueByKey(keyHashId).toString();
	//}
	
	public String getRegisterId(){
		return this.getFieldValueByKey(keyRegisterId).toString();
	}
	
	public String getObjectType(){
		return (String)this.getFieldValueByKey(keyObjectType);
	}
	
	@Override
	public String toString(){
		return "LogItem: " + this.jsonMaps.toString();
	}
	
	public String getTableName(){
		return this.getCategory();
	}
	
	
	
	public boolean ConvertDate(Map<String, Object> input, boolean bToStr, boolean matches){
		try{
			
			Object timer = input.get(keyTime);
			if(bToStr){
				if((timer instanceof Date))
				{
					Object date = CommonTool.GetDateStrFromObj(timer);
					if(date != null){
						input.put(keyTime, date);
						return true;
					}
				}
				
			}else {
				if((timer instanceof String))
				{
					if(matches && (!((String)timer).matches("^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)\\s([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9])$")))
					{
						return false;
					}
					
					Object date = CommonTool.GetDateFromStr((String)timer);
					if(date != null){
						input.put(keyTime, date);
						return true;
					}
				}
			}
		}
		catch(Exception e){
			log.error("Convert date error: {}", e.getMessage());
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public Object CreateReport(Object obj){
		List<Map<String, Object>> list = (List<Map<String, Object>>)obj;
		for(Map<String, Object> map : list){
			this.ConvertDate(map, true, false);

			//ClearRedundancy(map);
		}
		
		return list;
	}
	
	public Map<String, Object> getPusherMap(){
		if(this.jsonMaps == null){
			return null;
		}
		
		this.ConvertDate(this.jsonMaps, true, false);
		
		ClearRedundancy(this.jsonMaps);
		
		return this.jsonMaps;
	}
	
	protected static void ClearRedundancy(Map<String, Object> map){
		map.remove(key_Id);
		//map.remove(keyRegisterId);
		map.remove(keyHashId);
		map.remove(keyOriginalTableName);
	}

	@Override
	public Object getFieldValueByKey(String key){
		if(key.equals("id"))
			return this.id;
		try {
			if(this.jsonMaps.containsKey(key)){
				return this.jsonMaps.get(key);
			}
			return null;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.id);
		ObjectMapper om = new ObjectMapper();
	    om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES,false);
	    om.configure(SerializationFeature.INDENT_OUTPUT,true);
	    om.setSerializationInclusion(Include.NON_NULL);
	    String mapStr = null;
	    try {
			mapStr = om.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	    
		map.put("map", mapStr);
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {

		long id = resultSet.getLong("id");
		String mapStr = resultSet.getString("map");	
		Map<String, Object> reportMap = new HashMap<String, Object>();
		try {
			reportMap = new ObjectMapper().readValue(mapStr, HashMap.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		ReportItem item = new ReportItem();
		item.setId(id);
		item.setMaps(reportMap);
		return item;
	}
	
}
