/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.log;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class Report implements IDBObject,Serializable{

	private static final long serialVersionUID = 2696087338847390742L;

	public enum TargetType{
		FLOW,
		VM,
		HOST,
		USER,
		TENANT,
		NETWORK,
		SUBNETWORK,
		SWPORT,
	}
	

	public enum ReporterType{
		VM,
		HOST,
		USER,
		TENANT,
		NETWORK,
		SUBNETWORK,
		SWPORT,
		// security device
		SECURITY_DEVICE,
		// security app
		SECURITY_APP
	}
	
	String id;
	String registerid;
	String category;
	
	String targetId;
	TargetType targetType;

	String reporterId;
	ReporterType reporterType;
	
	ReportType type;
	ReportLevel level;
	String start_time;
	String complete_time;
	String content;
	protected static Map<String, Method> dbFieldMapping;
	protected static Logger log = LoggerFactory.getLogger(Report.class);
	
	public static String[] dbFieldsStrings = {"id","content","complete_time","start_time","type","target_type","reporter_type","target_id","reporter_id","level"};
	
	public Report(){}
	
	public Report(String id,ReportType reportType,String start_time,String complete_time,String content){
		this.id = id;
		this.type = reportType;
		this.start_time = start_time;
		this.complete_time = complete_time;
		this.content = content;
	}
	
	public Report(String targetId, TargetType targetType,String reporterId,
			ReporterType reporterType, ReportType type, String start_time,
			String complete_time, String content,ReportLevel level) {
		super();
		UUID uuid = UUID.randomUUID();
		this.id = uuid.toString();
		this.targetId = targetId;
		this.reporterId = reporterId;
		this.targetType = targetType;
		this.reporterType = reporterType;
		this.type = type;
		this.start_time = start_time;
		this.complete_time = complete_time;
		this.content = content;
		this.level = level;
	}
	
	public Report(String id,String targetId, TargetType targetType,String reporterId,
			ReporterType reporterType, ReportType type, String start_time,
			String complete_time, String content,ReportLevel level) {
		super();
		this.id = id;
		this.targetId = targetId;
		this.reporterId = reporterId;
		this.targetType = targetType;
		this.reporterType = reporterType;
		this.type = type;
		this.start_time = start_time;
		this.complete_time = complete_time;
		this.content = content;
		this.level = level;
	}
	
	@Override
	public String toString(){
		return "Report [id="+id+", type="+type+", start_time="+start_time+", complete_time="
				+complete_time+", content="+content+" ]";
	}
	
	@Override
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.id);
		map.put("type", this.type);
		map.put("start_time", this.start_time);
		map.put("complete_time", this.complete_time);
		map.put("content", this.content);
		//
		map.put("target_type", this.targetType);
		map.put("reporter_type", this.reporterType);
		map.put("target_id", this.targetId);
		map.put("reporter_id",this.reporterId);
		map.put("level", this.level);
		return map;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		if (dbFieldMapping == null) {
			dbFieldMapping = new HashMap<String, Method>();
			Class<? extends Report> cla = this.getClass();
			try {
				dbFieldMapping.put("id", cla.getDeclaredMethod("id"));
				dbFieldMapping.put("type", cla.getDeclaredMethod("type"));
				dbFieldMapping.put("start_time", cla.getDeclaredMethod("start_time"));
				dbFieldMapping.put("complete_time", cla.getDeclaredMethod("complete_time"));
				dbFieldMapping.put("content", cla.getDeclaredMethod("content"));
				dbFieldMapping.put("target_type", cla.getDeclaredMethod("targetType"));
				dbFieldMapping.put("reporter_type", cla.getDeclaredMethod("reporterType"));
				dbFieldMapping.put("target_id", cla.getDeclaredMethod("targetId"));
				dbFieldMapping.put("reporter_id", cla.getDeclaredMethod("reporterId"));
				dbFieldMapping.put("level", cla.getDeclaredMethod("level"));
				
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

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {
		TargetType targetType = null;
		ReporterType reporterType = null;
		ReportType type = null;
		ReportLevel level = null;
		try {
			targetType = TargetType.valueOf(resultSet.getString("target_type"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			reporterType = ReporterType.valueOf(resultSet.getString("reporter_type"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			type = ReportType.valueOf(resultSet.getString("type"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			level = ReportLevel.valueOf( resultSet.getString("level") );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Report(resultSet.getString("target_id"), targetType,resultSet.getString("reporter_id"),
				reporterType, type, resultSet.getString("start_time"),
				resultSet.getString("complete_time"), resultSet.getString("content"),level);
	}
	
	public void writeJsonString(JsonGenerator generator) throws JsonGenerationException, IOException{
    	generator.writeObjectFieldStart("report");// report:{
    	generator.writeStringField("id", this.id);// id:"id"
    	generator.writeStringField("type",(this.type==null)?null:this.type.toString());// type:"type"
    	generator.writeStringField("start_time", this.start_time);// start_time:"start_time"
    	generator.writeStringField("complete_time", this.complete_time);// type:"complete_time"
    	generator.writeStringField("content", this.content);// content:"content"
    	generator.writeStringField("target_type", (this.targetType==null)?null:this.targetType.toString());// content:"content"
    	generator.writeStringField("target_id", this.targetId);
    	generator.writeStringField("reporter_type", (this.reporterType==null)?null:this.reporterType.toString());// content:"content"
    	generator.writeStringField("reporter_id", this.reporterId);// content:"content"
    	generator.writeStringField("level", (this.level==null)?null:this.level.toString());// content:"content"
    	generator.writeEndObject();  //}	
    }
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ReportType getReportType() {
		return type;
	}
	public void setReportType(ReportType reportType) {
		this.type = reportType;
	}

	public ReportLevel getReportLevel() {
		return level;
	}

	public void setReportLevel(ReportLevel reportLevel) {
		this.level = reportLevel;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getStart_time() {
		return start_time;
	}
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getComplete_time() {
		return complete_time;
	}
	public void setComplete_time(String complete_time) {
		this.complete_time = complete_time;
	}
	

	public String getTargetId() {
		return targetId;
	}

	public TargetType getTargetType() {
		return targetType;
	}

	public String getReporterId() {
		return reporterId;
	}

	public ReporterType getReporterType() {
		return reporterType;
	}

}
