package com.sds.securitycontroller.asset;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.minlog.Log;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public abstract class Asset implements IDBObject{

	private static final long serialVersionUID = 7407372989586959804L;

	public enum AssetType {
		// cloud domain
		CLOUD_VM,
		// network domain
		NETWORK_DEVICE, NETWORK_FLOW,
		//phisical domain
		HOST,
		//app domain
		WEBSITE, DB,	 
		// unknown
		UNDEFINED
	}

	AssetType type;
	
	//真实对象
	Object sourceObject;

	public enum AssetLevel {
		HIGH, MEDIUM, LOW, PROHIBITED,
	}

	String id;
	// 重要程度
	AssetLevel level;
	
	public Asset(){		
	}

	public AssetLevel getLevel() {
		return level;
	}

	public void setLevel(AssetLevel level) {
		this.level = level;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public abstract String getAttributesString();
	

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {

		String typeJson = resultSet.getString("type");
		AssetType type = Enum.valueOf(AssetType.class, typeJson);
		
		if(type == AssetType.NETWORK_FLOW)
			return new NetworkFlowAsset(null, AssetLevel.PROHIBITED, null).mapRow(resultSet);
		else if(type == AssetType.HOST)
			return new HostAsset(null, AssetLevel.PROHIBITED, null, null,null,-1, null).mapRow(resultSet);
		else if(type == AssetType.NETWORK_DEVICE)
			return new NetworkDeviceAsset(null, AssetLevel.PROHIBITED, null, null,null,-1, null).mapRow(resultSet);
		else if(type == AssetType.DB)
			return new DBAsset(null, AssetLevel.PROHIBITED, null, null,null,-1).mapRow(resultSet);
		else if(type == AssetType.WEBSITE)
			return new WebsiteAsset(null, AssetLevel.PROHIBITED, null, -1).mapRow(resultSet);
		else{
			Log.error("Unimplemented asset type: " + type);
			return null;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
    public static Map<String, Object> buildAsset(Map<String, Object> valueMap)throws IOException{
    	if(null == valueMap){
    		throw new IOException("value map error");
    	}
    	Map<String, Object> asset= new HashMap<String, Object>();
    	String stype= (String)valueMap.get("type");
    	String name = (String)valueMap.get("name");
    	String host = (String)valueMap.get("host");
    	Object ownner = valueMap.get("ownner");
		//check the common fields
    	if(null == stype || stype.isEmpty()){
    		throw new IOException("type error");
    	} 
		if(null == name || name.isEmpty()){
			throw new IOException("type error");
		}
		if(null == host || host.isEmpty()){
			throw new IOException("host error");
		}
		if(null == ownner || !(ownner instanceof List)){
			throw new IOException("ownner error");
		}
		//fill the common fields
		asset.put("type", 	valueMap.get("type"));
		asset.put("name", 	valueMap.get("name"));
		asset.put("host", 	valueMap.get("host"));
		asset.put("ownner", valueMap.get("ownner"));
    	//build according to the Enum value type
		AssetType type=AssetType.valueOf(stype.toUpperCase());
		switch(type){
		case WEBSITE:
			buildWebsiteAsset(valueMap, asset);
			break;
		case HOST:
			buildHostAsset(valueMap, asset);
			break;
		case DB:
			buildDBAsset(valueMap, asset);
			break;
		case NETWORK_DEVICE:	
			buildNetworkDeviceAsset(valueMap, asset);
			break;
		default:
			break;
		}
    	return asset;
    }
    
    private static void buildWebsiteAsset(Map<String, Object> valueMap, Map<String, Object> asset) throws IOException{     
    	String url	= (String)valueMap.get("url"); 
    	Object port = valueMap.get("port");
    	if(null == url || url.isEmpty()){
    		throw new IOException("url error");
    	}
    	if(null == port || !(port instanceof Integer) ){
    		throw new IOException("port error");
    	}
    	asset.put("url", 	valueMap.get("url"));
    	asset.put("port", 	valueMap.get("port")); 
    }
    
	private static void buildHostAsset(Map<String, Object> valueMap, Map<String, Object> asset) throws IOException{
		String login_user 		= (String)valueMap.get("login_user");
    	String login_password 	= (String)valueMap.get("login_password");
    	String login_proto 		= (String)valueMap.get("login_proto");
    	Object login_port 		= valueMap.get("login_port");
    	Object jumpmachine 		= valueMap.get("jumpmachine");
    	
    	
    	if(null == login_user || login_user.isEmpty()){
    		throw new IOException("login_user error");
    	}
    	if(null == login_password || login_password.isEmpty()){
    		throw new IOException("login_password error");
    	}
    	if(null == login_proto || login_proto.isEmpty()){
    		throw new IOException("login_proto error");
    	}
    	if(null == login_port || !(login_port instanceof Integer) ){
    		throw new IOException("login_port error");
    	}
    	if(null == jumpmachine || !(jumpmachine instanceof List)){
    		throw new IOException("jumpmachine error");
    	}
    	
    	asset.put("login_user", 	valueMap.get("login_user"));
    	asset.put("login_password", valueMap.get("login_password"));
    	asset.put("login_proto", 	valueMap.get("login_proto"));
    	asset.put("login_port", 	valueMap.get("login_port"));
    	asset.put("jumpmachine", 	valueMap.get("jumpmachine")); 
	}
	private static void buildNetworkDeviceAsset(Map<String, Object> valueMap, Map<String, Object> asset) throws IOException{
		String login_user 		= (String)valueMap.get("login_user");
    	String login_password 	= (String)valueMap.get("login_password");
    	String login_proto 		= (String)valueMap.get("login_proto");
    	Object login_port 		= valueMap.get("login_port");
    	Object jumpmachine 		= valueMap.get("jumpmachine");
    	
    	
    	if(null == login_user || login_user.isEmpty()){
    		throw new IOException("login_user error");
    	}
    	if(null == login_password || login_password.isEmpty()){
    		throw new IOException("login_password error");
    	}
    	if(null == login_proto || login_proto.isEmpty()){
    		throw new IOException("login_proto error");
    	}
    	if(null == login_port || !(login_port instanceof Integer) ){
    		throw new IOException("login_port error");
    	}
    	if(null == jumpmachine || !(jumpmachine instanceof List)){
    		throw new IOException("jumpmachine error");
    	}
    	
    	asset.put("login_user", 	valueMap.get("login_user"));
    	asset.put("login_password", valueMap.get("login_password"));
    	asset.put("login_proto", 	valueMap.get("login_proto"));
    	asset.put("login_port", 	valueMap.get("login_port"));
    	asset.put("jumpmachine", 	valueMap.get("jumpmachine")); 
	}
	private static void buildDBAsset(Map<String, Object> valueMap, Map<String, Object> asset) throws IOException{
		String db_type 		= (String)valueMap.get("db_type");
    	String db_user 		= (String)valueMap.get("db_user");
    	String db_password 	= (String)valueMap.get("db_password");
    	Object db_port 		= valueMap.get("db_port");
    
    	if(null == db_type || db_type.isEmpty()){
    		throw new IOException("db_type error");
    	}
    	if(null == db_user || db_user.isEmpty()){
    		throw new IOException("db_user error");
    	}
    	if(null == db_password || db_password.isEmpty()){
    		throw new IOException("db_password error");
    	}
    	if(null == db_port || !(db_port instanceof Integer) ){
    		throw new IOException("db_port error");
    	} 
    	asset.put("db_type", 		valueMap.get("db_type")); 
    	asset.put("db_user", 		valueMap.get("db_user")); 
    	asset.put("db_password", 	valueMap.get("db_password")); 
    	asset.put("db_port", 		valueMap.get("db_port")); 
	}
	
}
