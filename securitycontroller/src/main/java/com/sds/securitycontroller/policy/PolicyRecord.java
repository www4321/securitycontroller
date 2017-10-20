package com.sds.securitycontroller.policy;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.command.CommandRecord;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class PolicyRecord implements IDBObject{

	private static final long serialVersionUID = 1420197516885364942L;

	public PolicyRecord(){		
	}
	
	public PolicyRecord(String id,String appId,AtomPolicy policy) {
		super();
		this.id=id;
		this.appId = appId;
		this.generatedTime = new Date();
		this.policy = policy;
		policyStatus = PolicyStatus.GENERATED;
		if(policy!=null)
			this.appId=policy.getSubId();
		else
			this.appId="unknown";
	}
	public PolicyRecord(String id,String appId,Date generateTime,PolicyStatus status,AtomPolicy policy,List<String> commandIdList){
		this.id=id;
		this.appId = appId;
		this.generatedTime = generateTime;
		this.policyStatus = status;
		this.policy = policy;
		policy.id = id;
		this.commandIdList=commandIdList;
	}

	public PolicyStatus policyStatus;
	public Date generatedTime = new Date();
	public AtomPolicy policy;
	
	Logger log = LoggerFactory.getLogger(PolicyRecord.class);
	String id;// = UUID.randomUUID().toString();
	String appId;
	String serializedPolicy = null;
	List<String> commandIdList = new ArrayList<String>();
	
	public Map<String, CommandRecord> commandRecordMap = new LinkedHashMap<String, CommandRecord>();
	
	public String getId(){
		return id;
	}
	public String getAppId(){
		return appId;
	}

	// IDBObject extensions
	
	protected static Map<String, Method> dbFieldMapping;
	
	@Override
	public Map<String, Object> getDBElements() {
		Map<String,Object> map = new HashMap<String,Object>();
    	map.put("id", this.id);
    	map.put("appid", this.appId);
    	map.put("generated_time", this.generatedTime.getTime());
    	map.put("status", this.policyStatus);
    	map.put("serialized_policy", AtomPolicy.serializePolicy (this.policy) );
    	map.put("command_ids", serializeIdList( this.commandIdList));
    	map.put("action_type", this.policy.action.type);
    	return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if(dbFieldMapping == null){
			dbFieldMapping = new HashMap<String, Method>();
		    Class<? extends PolicyRecord> cla=this.getClass();	    
			try {
				dbFieldMapping.put("id", cla.getDeclaredMethod("id"));
				dbFieldMapping.put("appid", cla.getDeclaredMethod("appid"));
				dbFieldMapping.put("generated_time", cla.getDeclaredMethod("generated_time"));
				dbFieldMapping.put("status", cla.getDeclaredMethod("status"));
				dbFieldMapping.put("serialized_policy", cla.getDeclaredMethod("serialized_policy"));
				dbFieldMapping.put("command_ids", cla.getDeclaredMethod("command_ids"));
				//TODO 0619
				dbFieldMapping.put("action_type", cla.getDeclaredMethod("action_type"));
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

	static String serializeIdList(List<String> idList){
		String str=null;
		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter();
		JsonGenerator gen;
		try {
			gen = new JsonFactory().createGenerator(writer);
			mapper.writeValue(gen, idList);
			str = writer.toString();
			gen.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			str= null;
		}
		return str;
	}
	
	static List<String> parseIdList(String json){
		List<String> idList=new ArrayList<String>();
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			JsonNode rootNode = mapper.readTree(json);
			String id=null;
			Iterator<JsonNode> iter=rootNode.iterator();
			while(iter.hasNext()){
				id=iter.next().asText();
				if(id!=null)
					idList.add(id);
			}
			return idList;
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {
		PolicyRecord record=null;
		try {
			PolicyStatus status;
			try {
				status=PolicyStatus.valueOf(resultSet.getString("status"));
			} catch (Exception e) {
				status=PolicyStatus.UNDEFINED;
			}
			AtomPolicy atomPolicy=null;
			try {
				atomPolicy=AtomPolicy.deserializeToAtomPolicy(resultSet.getString("serialized_policy"));
			} catch (Exception e) {
				atomPolicy=null;
			}
			List<String> idList=null;
			try {
				idList=parseIdList(resultSet.getString("command_ids"));
			} catch (Exception e) {
				idList=null;
			}
			
			record = new PolicyRecord(resultSet.getString("id"),
					resultSet.getString("appid"),
					new Date(resultSet.getLong("generated_time")),
//					new Date(resultSet.getString("generated_time")),
					status,
					atomPolicy,
					idList
					);
		} catch (Exception e) {
			e.printStackTrace();
			record = null;
		}
		return record;
	}
	
	public AtomPolicy getPolicy(){
		return this.policy;
	}
	
	
	
	
	public static void main(String[] args){
		/*AtomPolicy policy = new AtomPolicy(new PolicyObject(), new PolicyAction(PolicyActionType.ALLOW_FLOW,null));
		
		String pStr=AtomPolicy.serializePolicy(policy);
		System.out.println(pStr);
		AtomPolicy policy2 = AtomPolicy.deserializeToAtomPolicy(pStr);
		System.out.println(AtomPolicy.serializePolicy(policy2));*/
		
//		new PolicyRecord(resultSet.getString("id"),
//				resultSet.getString("id"),
//				new Date(resultSet.getString("id")),
//				PolicyStatus.valueOf(resultSet.getString("id")),
//				deserializeToAtomPolicy(resultSet.getString("serialized_policy")) );
		
		
		
//		serializePolicy();
	}
	public List<String> getCommandIdList() {
		return commandIdList;
	}
	public void setCommandIdList(List<String> commandIdList) {
		this.commandIdList = commandIdList;
	}
	
}
