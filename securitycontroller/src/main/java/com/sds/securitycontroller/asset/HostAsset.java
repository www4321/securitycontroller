package com.sds.securitycontroller.asset;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class HostAsset extends Asset {


	private static final long serialVersionUID = 2686862624136074228L;
	protected static Logger log = LoggerFactory.getLogger(NetworkFlowAsset.class);
	
	String loginUser;
	String loginPassword;
	String loginProto;
	int loginPort;
	String jumpMachine;
	
	public HostAsset(String id, AssetLevel level, String loginUser, String loginPassword, String loginProto,
			int loginPort, String jumpMachine){
		this.id = id;
		this.level = level;
		this.loginUser = loginUser;
		this.loginPassword = loginPassword;
		this.loginProto = loginProto;
		this.loginPort = loginPort;
		this.jumpMachine = jumpMachine;
		this.type = AssetType.HOST;
	}

	@Override
	public String getAttributesString(){
		return "{login_user : '"+ this.loginUser +"',"
				+ "login_password: '" +this.loginPassword +"'"
				+ "login_proto: '" +this.loginProto +"'"
				+ "login_port: " +this.loginPort +""
				+ "jumpmachine: '" +this.jumpMachine +"'"
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
			map.put("login_user", this.loginUser);
			map.put("login_password", this.loginPassword);
			map.put("login_proto", this.loginProto);
			map.put("login_port", this.loginPort);
			map.put("jumpmachine", this.jumpMachine);
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
		String loginUser = null, loginPassword = null, loginProto= null, jumpMachine=null;
		int loginPort = -1;
    	ObjectMapper mapper = new ObjectMapper();
		try{
			JsonNode root = mapper.readValue(attrsJson, JsonNode.class);
			loginUser = root.path("login_user").asText();
			loginPassword = root.path("login_password").asText();
			loginProto = root.path("login_proto").asText();
			loginPort = root.path("login_port").asInt();
			jumpMachine = root.path("jumpmachine").asText();
		} catch (Exception e) {
            log.error("Error parse access policy: ", e);
            e.printStackTrace();      
        }		
		
		return new HostAsset(
				resultSet.getString("id"), level,
				loginUser, loginPassword, loginProto, loginPort, jumpMachine
				);
	}
}
