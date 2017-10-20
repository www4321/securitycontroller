package com.sds.securitycontroller.asset;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;


public class NetworkFlowAsset extends Asset {

	private static final long serialVersionUID = 2686862654136074228L;
	protected static Logger log = LoggerFactory.getLogger(NetworkFlowAsset.class);
	
	String snode;
	String dnode;
	
	
	public String getSnode() {
		return snode;
	}

	public String getDnode() {
		return dnode;
	}
	
	public NetworkFlowAsset(String id, AssetLevel level, String snode, String dnode) {
		this.id = id;
		this.snode = snode;
		this.dnode = dnode;
		this.level = level;
		this.type = AssetType.NETWORK_FLOW;
	}

	public NetworkFlowAsset(String id, AssetLevel level, String dnode) {
		this(id, level, null, dnode);
	}
	
	
	

	@Override
	public String getAttributesString(){
		return "{snode : '"+ this.snode +"',"
				+ "dnode: '" +this.dnode +"'"
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
			map.put("snode", this.snode);
			map.put("dnode", this.dnode);
			return map;
		}
		else
			return null;
	}

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {

		String typeJson = resultSet.getString("type");
		AssetType type = Enum.valueOf(AssetType.class, typeJson);
		
		if(type != AssetType.NETWORK_FLOW){
			log.error("Unmatch asset type "+ type + " for flow asset");
			return null;
		}

		String attrsJson = resultSet.getString("attrs");

		AssetLevel level = Enum.valueOf(AssetLevel.class, resultSet.getString("level"));
		
		String snode = null, dnode = null;
    	ObjectMapper mapper = new ObjectMapper();
		try{
			JsonNode root = mapper.readValue(attrsJson, JsonNode.class);
			snode = root.path("snode").asText();
			dnode = root.path("dnode").asText();
		} catch (Exception e) {
            log.error("Error parse access policy: ", e);
            e.printStackTrace();      
        }		
		
		return new NetworkFlowAsset(
				resultSet.getString("id"), level, snode, dnode
				);
	}
	
	
}
