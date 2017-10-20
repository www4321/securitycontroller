package com.sds.securitycontroller.asset;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class WebsiteAsset extends Asset{
	

	private static final long serialVersionUID = 2686862654136074398L;
	protected static Logger log = LoggerFactory.getLogger(WebsiteAsset.class);
	
	String url;
	int port;
	
	public WebsiteAsset(String id, AssetLevel level, String url, int port){
		this.id = id;
		this.level = level;
		this.url = url;
		this.port = port;
		this.type = AssetType.WEBSITE;
	}
	
	

	@Override
	public String getAttributesString(){
		return "{url : '"+ this.url +"',"
				+ "port: " +this.id +""
				+ "}";
	}
	
	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.id);
		map.put("type", this.type.toString());
		map.put("level", this.level.toString());
		map.put("attrs", this.getAttributesString());
		return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if(key.equals("id"))
			return this.id;
		else if(key.equals("level"))
			return this.level;
		else if(key.equals("type"))
			return this.type;
		else if(key.equals("attrs")){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("url", this.url);
			map.put("port", this.port);
			return map;
		}
		else
			return null;
	}

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {

		String typeJson = resultSet.getString("type");
		AssetType type = Enum.valueOf(AssetType.class, typeJson);
		
		if(type != AssetType.WEBSITE){
			log.error("Unmatch asset type "+ type + " for website asset");
			return null;
		}
		

		AssetLevel level = Enum.valueOf(AssetLevel.class, resultSet.getString("level"));

		String attrsJson = resultSet.getString("attrs");
		String url = null;
		int port = -1;
    	ObjectMapper mapper = new ObjectMapper();
		try{
			JsonNode root = mapper.readValue(attrsJson, JsonNode.class);
			url= root.path("url").asText();
			port = root.path("port").asInt();
		} catch (Exception e) {
            log.error("Error parse access policy: ", e);
            e.printStackTrace();      
        }		
		
		return new WebsiteAsset(
				resultSet.getString("id"), level, url, port
				);
	}


}
