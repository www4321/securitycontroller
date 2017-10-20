package com.sds.securitycontroller.asset;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class DBAsset extends Asset {


	private static final long serialVersionUID = 2686862624136074228L;
	protected static Logger log = LoggerFactory.getLogger(NetworkFlowAsset.class);
	
	String dbUser;
	String dbPassword;
	String dbType;
	int dbPort;
	
	public DBAsset(String id, AssetLevel level, String dbUser, String dbPassword, String dbType,
			int dbPort){
		this.id = id;
		this.level = level;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
		this.dbType = dbType;
		this.dbPort = dbPort;
		this.type = AssetType.HOST;
	}

	@Override
	public String getAttributesString(){
		return "{db_user : '"+ this.dbUser +"',"
				+ "db_password: '" +this.dbPassword +"'"
				+ "db_proto: '" +this.dbType +"'"
				+ "db_port: " +this.dbPort +""
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
		else if(key.equals("type"))
			return this.type;
		else if(key.equals("level"))
			return this.level;
		else if(key.equals("attrs")){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("db_user", this.dbUser);
			map.put("db_password", this.dbPassword);
			map.put("db_proto", this.dbType);
			map.put("db_port", this.dbPort);
			return map;
		}
		else
			return null;
	}

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {

		String typeJson = resultSet.getString("type");
		AssetType type = Enum.valueOf(AssetType.class, typeJson);
		
		if(type != AssetType.HOST){
			log.error("Unmatch asset type "+ type + " for host asset");
			return null;
		}


		AssetLevel level = Enum.valueOf(AssetLevel.class, resultSet.getString("level"));

		String attrsJson = resultSet.getString("attrs");
		String dbUser = null, dbPassword = null, dbType= null;
		int dbPort = -1;
    	ObjectMapper mapper = new ObjectMapper();
		try{
			JsonNode root = mapper.readValue(attrsJson, JsonNode.class);
			dbUser = root.path("db_user").asText();
			dbPassword = root.path("db_password").asText();
			dbType = root.path("db_proto").asText();
			dbPort = root.path("db_port").asInt();
		} catch (Exception e) {
            log.error("Error parse access policy: ", e);
            e.printStackTrace();      
        }		
		
		return new DBAsset(
				resultSet.getString("id"), level,
				dbUser, dbPassword, dbType, dbPort
				);
	}
}
