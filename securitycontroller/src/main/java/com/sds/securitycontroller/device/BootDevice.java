package com.sds.securitycontroller.device;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;

public class BootDevice {
	DeviceType type;

	String name;
	String id;
	
	Map<String, Object> attrs = new HashMap<String, Object>();

	

	protected DeviceStatus status = DeviceStatus.UNKNOWN;

	private String connectType = null;


	private String managementIp = null;
    
    
    public BootDevice(DeviceType type, String name, String id, DeviceStatus status, String connectType, String managementIp){
    	this.type = type;
    	this.name = name;
    	this.id = id;
    	this.status = status;
    	this.connectType = connectType;
    	this.managementIp = managementIp;
    }

	public String getConnectType() {
		return connectType;
	}

	public void setConnectType(String connectType) {
		this.connectType = connectType;
	}

	public String getManagementIp() {
		return managementIp;
	}

	public void setManagementIp(String managementIp) {
		this.managementIp = managementIp;
	}

	public DeviceType getType() {
		return type;
	}


	public void setType(DeviceType type) {
		this.type = type;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}

	public DeviceStatus getStatus() {
		return status;
	}


	public void setStatus(DeviceStatus status) {
		this.status = status;
	}
	
    public Object getAttribute(String key) {
		return attrs.get(key);
	}

	public void setAttribute(String key, Object value) {
		this.attrs.put(key, value);
	}

    public void writeJsonString(JsonGenerator generator) throws JsonGenerationException, IOException{
    	generator.writeStringField("id", this.id);
    	generator.writeStringField("name", this.name);
    	generator.writeStringField("connect_type", this.connectType);
    	generator.writeStringField("status", this.status.toString());
    	generator.writeStringField("management_ip", this.managementIp);
    	generator.writeStringField("type", this.type.toString());
		generator.writeObjectFieldStart("attributes");
		writeMap(generator, this.attrs);
    	generator.writeEndObject();
    }
    

	@SuppressWarnings("unchecked")
	private void writeMap(JsonGenerator generator, Map<String, Object> map) throws JsonGenerationException, IOException{
		for(Entry<String, Object> entry : map.entrySet()){
			String key = entry.getKey();
			Object value = entry.getValue();
			if(value instanceof Integer)
				generator.writeNumberField(key, (int)value);
			else if(value instanceof String)
				generator.writeStringField(key, (String)value);
			else if(value instanceof HashMap){
				writeMap(generator, (Map<String, Object>)value);
			}
			else if(value instanceof List){
				generator.writeArrayFieldStart(key);
				for(Object element : (List<Object>)value){
					generator.writeStartObject();
					writeMap(generator, (Map<String, Object>)element);
					generator.writeEndObject();
				}
				generator.writeEndArray();
			}
			else
				Log.error("Unknow type: "+ value);
		}
    }
    
}
