/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.mongodb.morphia.annotations.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.app.AppFactory.AppCategory;
import com.sds.securitycontroller.app.AppFactory.AppType;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.utils.IJsonable;

public class App implements IDBObject, IJsonable {
	private static final long serialVersionUID = 3606194650280542553L;
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@Id
	protected String id = "";
	private String guid="";
	protected String hash="";
	protected boolean enable = false;
	protected String name = "";
	private String alias = "";
	protected AppCategory category = AppCategory.UNKNOWN;
	protected AppType type = AppType.UNKNONW;
	protected long register_time = 0;
	protected String version="";
	protected String host="";
	protected int port=0;
	protected String root_url="";
	protected String protocol="";
	private String manage_url="";
	private long reg_time = 0;	

	
		
	protected static Map<String, Method> dbFieldMapping;    
    protected static Logger log = LoggerFactory.getLogger(App.class);

    public App(){
    	this(null, null,null, true,null,null,null,null,0,null,null,null,0, AppType.UNKNONW,AppCategory.UNKNOWN);
    }
    
	public App(	String guid,
				String hash,				
				String name,		 
				String version,
				String host,
				int    port,
				String root_url,
				String manage_url,
				String protocol,
				AppType type,
				AppCategory category) 
	{
		this(	"", //here
				guid,
				hash,
				true,//here				
				name,
				"",  //here
				version,
				host,
				port,
				root_url,
				manage_url,
				protocol,
				System.currentTimeMillis() / 1000L,//here			
				type,
				category
				);
	}

	public App(
			String id,//here
			String guid,
			String hash,
			boolean enable, //here
			String name,
			String alias,
			String version,
			String host,
			int port,
			String root_url,
			String manage_url,
			String protocol,	
			long reg_time,	//here
			AppType type,
			AppCategory category
			) {
		this.id = id;
		this.guid = guid;
		this.name = name;
		this.enable = enable;
		this.category = category;
		this.type = type;
		this.setReg_time(reg_time);
		this.version = version;
		this.hash = hash;
		this.host = host;
		this.port = port;
		this.setRoot_url(root_url);
		this.setManage_url(manage_url);
		this.protocol = protocol;
	}

	public App(App app){
		this.id 		= app.id;
		this.name 		= app.name;
		this.enable 	= app.enable;
		this.category 	= app.category;
		this.type 		= app.type;
		this.register_time = app.register_time;
		this.version 	= app.version;
		this.hash 		= app.hash;
		this.host 		= app.host;
		this.port 		= app.port;
		this.root_url 	= app.root_url;
		this.protocol 	= app.protocol;
	}
	

	@Override
    public String toString(){
    	return this.type+"\t"+this.id;
    }

	@Override
    public String toJsonString() throws JsonGenerationException, IOException{
		StringWriter writer = new StringWriter();
		JsonFactory jasonFactory = new JsonFactory();       
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject(); 
			
	    	generator.writeStringField("id", 			this.id);
	    	generator.writeStringField("name", 			this.name);
	    	generator.writeBooleanField("enable", 		this.enable);
	    	generator.writeStringField("type", 			this.type.toString());
	    	generator.writeStringField("category", 		this.category.toString());
 	    	generator.writeStringField("register_time", format.format(this.getRegistedTime()));
	    	generator.writeStringField("version", 		this.version);
	    	generator.writeStringField("hash", 			this.hash);
	    	generator.writeStringField("host", 			this.host);
	    	generator.writeNumberField("port", 			this.port);
	    	generator.writeStringField("root_url",		this.root_url);
	    	generator.writeStringField("protocol", 		this.protocol);
			
			generator.writeEndObject();
			generator.close();
		} catch (IOException e) {
			throw new IOException("json writter error");
		} catch (Exception e) {
			throw new IOException("json writter error");
		}
	 
		return writer.toString();
    }

    @Override
    @JsonIgnore 
    public Map<String,Object> getDBElements(){
    	Map<String,Object> map = new HashMap<String,Object>();
    	map.put("id", 				this.id);
    	map.put("guid", 			this.getGuid());
    	map.put("hash", 			this.hash);
    	map.put("enable", 			this.enable);
    	map.put("name", 			this.name);
    	map.put("alias", 			this.getAlias());
    	map.put("version", 			this.version);
    	map.put("host", 			this.host);
    	map.put("port", 			this.getPort());
    	map.put("root_url",			this.root_url);
    	map.put("manage_url",		this.manage_url);
    	map.put("protocol", 		this.protocol);
    	map.put("reg_time", 		this.getReg_time());
    	map.put("type", 			this.type);
    	map.put("category", 		this.category);
    	return map;
    }


	@Override
	public Object getFieldValueByKey(String key){
		if(dbFieldMapping == null){
			dbFieldMapping = new HashMap<String, Method>();
		    Class<? extends App> cla=this.getClass();		    
			try {
				dbFieldMapping.put("id", 		cla.getDeclaredMethod("id"));
				dbFieldMapping.put("guid", 		cla.getDeclaredMethod("guid"));
				dbFieldMapping.put("hash", 		cla.getDeclaredMethod("hash"));
				dbFieldMapping.put("enable", 	cla.getDeclaredMethod("enable")); 
				dbFieldMapping.put("name", 		cla.getDeclaredMethod("name"));
				dbFieldMapping.put("alias", 	cla.getDeclaredMethod("alias"));
				dbFieldMapping.put("version", 	cla.getDeclaredMethod("version"));				
				dbFieldMapping.put("host", 		cla.getDeclaredMethod("host"));
				dbFieldMapping.put("port", 		cla.getDeclaredMethod("port"));
				dbFieldMapping.put("root_url", 	cla.getDeclaredMethod("root_url"));
				dbFieldMapping.put("manage_url",cla.getDeclaredMethod("manage_url"));
				dbFieldMapping.put("protocol", 	cla.getDeclaredMethod("protocol"));
				dbFieldMapping.put("reg_time", 	cla.getDeclaredMethod("reg_time"));
				dbFieldMapping.put("type", 		cla.getDeclaredMethod("type"));
				dbFieldMapping.put("category", 	cla.getDeclaredMethod("category"));
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


	//string to object fields
	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {
	
		return new App(
				resultSet.getString("id"),
				resultSet.getString("guid"),
				resultSet.getString("hash"),
				resultSet.getBoolean("enable"),
				resultSet.getString("name"),
				resultSet.getString("alias"),
				resultSet.getString("version"),
				resultSet.getString("host"),
				resultSet.getInt("port"),
				resultSet.getString("root_url"),
				resultSet.getString("manage_url"),
				resultSet.getString("protocol"),		
				resultSet.getInt("reg_time"),
				Enum.valueOf(AppType.class, resultSet.getString("type")),
				Enum.valueOf(AppCategory.class, resultSet.getString("category")));
	}

	/*
	 * type should not change
	 * category should not change
	 * reg time should not change
	 * name should not change
	 * enable should not change
	 */ 
	public void updateInfo(JsonNode jn) throws IOException{	
		if(jn.has("hash")){
			this.hash = jn.path("hash").asText();
		}
		if(jn.has("alias")){
			this.setAlias(jn.path("alias").asText());
		}			
		if(jn.has("version")){
			this.version = jn.path("version").asText();
		}
		if(jn.has("host")){
			this.host = jn.path("host").asText();
		}
		if(jn.has("port")){
			this.port = jn.path("port").asInt();
		}
		if(jn.has("root_url")){
			this.root_url = jn.path("root_url").asText();
		}
		if(jn.has("manage_url")){
			this.manage_url = jn.path("manage_url").asText();
		}
		if(jn.has("protocol")){
			this.protocol = jn.path("protocol").asText();
		} 
	}


	//getter and setter

	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	 
	public boolean getEnable() {
		return this.enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {		
		if (0 == this.port ) {
			if("HTTPS" == this.protocol){
				return 443;
			}else{
				return 80;
			}
		}
		return this.port;
	}

	public void setPort(short port) {
		this.port = port;
	}
	public String getRoot_url() {
		return root_url;
	}

	public void setRoot_url(String root_url) {
		if(null != root_url){
			this.root_url=root_url.endsWith("/")?root_url:root_url+"/";
		}
	}
	
	public String getManage_url() {
		return manage_url;
	}

	public void setManage_url(String manage_url) {
		this.manage_url = manage_url;
	}
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	//for json output
    public String getRegistedTime() {
		return format.format(this.getReg_time()*1000);
	}

  //for internal usage
  	@JsonIgnore 
  	public long getReg_time() {
  		return reg_time;
  	}
  	@JsonIgnore 
  	public void setReg_time(long reg_time) {
  		this.reg_time = reg_time;
  	}
	public AppType getType() {
		return type;
	}

	public void setType(AppType type) {
		this.type = type;
	}
	public AppCategory getCategory() {
		return category;
	}

	public void setCategory(AppCategory category) {
		this.category = category;
	} 
 

}
