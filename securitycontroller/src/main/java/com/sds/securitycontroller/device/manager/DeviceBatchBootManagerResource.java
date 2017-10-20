package com.sds.securitycontroller.device.manager;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sds.securitycontroller.device.BootDevice;

public class DeviceBatchBootManagerResource extends ServerResource {

	protected static Logger log = LoggerFactory.getLogger(DeviceManagerResource.class);

	String id = null;
	BootDevice device = null;
    IDeviceManagementService devicemanager = null;
	
	
	@Override  
    public void doInit(){ 
		devicemanager = 
	            (IDeviceManagementService)getContext().getAttributes().
	            get(IDeviceManagementService.class.getCanonicalName());
        id = (String) getRequestAttributes().get("id");
        if(id != null){
        	device = devicemanager.getBootDevice(id);
        }
	}
	
	@Post
    public String handlePostRequest(String fmJson) {
		//boot vms

    	String status = "ok";
    	Map<String, Object> addedResults = new HashMap<String, Object>(); 
    	Map<String, Object> updatedResults = new HashMap<String, Object>(); 
    	Map<String, Object> deletedResults = new HashMap<String, Object>(); 
    	String errStr = null;
    	
    	ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {

			JsonNode root = mapper.readValue(fmJson, JsonNode.class);
			
			JsonNode addedDevices = root.path("addedDevices");
			Iterator<JsonNode> it = addedDevices.iterator();
			while(it.hasNext()){
				JsonNode addedDevice = it.next();
				String type = addedDevice.path("type").asText().toUpperCase();
				String name = addedDevice.path("name").asText();
				String managementIp = addedDevice.path("management_ip").asText();
				String connectType = addedDevice.path("attach_type").asText();

	    		Map<String, Object> attrs = new HashMap<String, Object>();
	    		JsonNode attributes = addedDevice.path("attributes");
	    		Iterator<Entry<String, JsonNode>> entities = attributes.fields();
	    		while( entities.hasNext()) {
	    			Entry<String, JsonNode> entity = entities.next();
	    			String key=entity.getKey();
	    			Object value = entity.getValue(); 
	    			attrs.put(key, value);
	    		}
	    		try{
	    			BootDevice device = devicemanager.bootSecurityDeviceInstance(name, type, managementIp, connectType, attrs);
	    			addedResults.put(device.getId(), device);
	    		}
	    		catch (Exception ex){
	    			addedResults.put(name, ex.toString());
	    			status = "error";
	    		}
	    		 
			}
			
			
			
			
			JsonNode deletedDevices = root.path("deletedDevices");
			it = deletedDevices.iterator();
			while(it.hasNext()){
				@SuppressWarnings("unused")
				JsonNode deletedDevice = it.next();
			}
			
			JsonNode updatedDevices = root.path("updatedDevices");
			it = updatedDevices.iterator();
			while(it.hasNext()){
				@SuppressWarnings("unused")
				JsonNode updatedDevice = it.next();
			}
        	
        } catch (IOException e) {
            log.error("Error creating new device: " + fmJson, e);
            e.printStackTrace();
            status = "error"; 
            errStr =  e.getMessage();  
        } catch (Exception e) {
            log.error("Error creating new device: ", e);
            e.printStackTrace();
            status = "error"; 
            errStr =  e.getMessage();          
        }
        
        
        
        
        JsonFactory jasonFactory = new JsonFactory();
        StringWriter writer = new StringWriter();
        try{
        	JsonGenerator generator = jasonFactory
                    .createGenerator(writer);
        	generator.writeStartObject();//1
        	generator.writeStringField("status", status);
        	
        	if(errStr != null)
            	generator.writeStringField("error", errStr);
        		

    		generator.writeObjectFieldStart("result");//2
    		
    		generator.writeObjectFieldStart("addedDevices"); //3
        	for(Entry<String, Object> entry: addedResults.entrySet()){
        		String key = entry.getKey();
        		Object object = entry.getValue();
        		if(object instanceof String){
        			generator.writeStringField(key, (String)object);        			
        		}
        		else if(object instanceof BootDevice){
        			((BootDevice) object).writeJsonString(generator);
        		}
        	}
        	generator.writeEndObject();
        	
        	generator.writeObjectFieldStart("updatedDevices");
        	for(Entry<String, Object> entry: updatedResults.entrySet()){
        		String key = entry.getKey();
        		Object object = entry.getValue();
        		if(object instanceof String){
        			generator.writeStringField(key, (String)object);        			
        		}
        		else if(object instanceof BootDevice){
        			((BootDevice) object).writeJsonString(generator);
        		}
        	}
        	generator.writeEndObject();
        	
        	generator.writeObjectFieldStart("deletedDevices");
        	for(Entry<String, Object> entry: deletedResults.entrySet()){
        		String key = entry.getKey();
        		Object object = entry.getValue();
        		if(object instanceof String){
        			generator.writeStringField(key, (String)object);        			
        		}
        		else if(object instanceof BootDevice){
        			((BootDevice) object).writeJsonString(generator);
        		}
        	}
        	generator.writeEndObject();//3

        	generator.writeEndObject();//2
        	
        	generator.writeEndObject();//1
        	generator.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"status\" : \"error\", \"result\" : \"json conversion failed. \"}";
        }
        return writer.toString();
	}
}
