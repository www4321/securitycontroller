package com.sds.securitycontroller.device.manager;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.resource.Get;
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

public class DeviceBootManagerResource extends ServerResource {

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
	
	@Get("json")
    public Object handleGetRequest() {

    	JsonFactory jasonFactory = new JsonFactory();
        StringWriter writer = new StringWriter();
    	if(this.id != null){
    		if(this.device == null)
    			return "{\"status\" : \"error\", \"result\" : \"device not found. \"}";

	        try{
	        	JsonGenerator generator = jasonFactory
	                    .createGenerator(writer);
	        	generator.writeStartObject();
	        	generator.writeStringField("status", "ok");
	        	if(device!=null){
	        		generator.writeObjectFieldStart("device");
	        		device.writeJsonString(generator);
	            	generator.writeEndObject(); 
	        	}
	        	generator.writeEndObject();
	
	        	generator.close();
	        } catch (IOException e) {
	            log.error("json conversion failed: ", e.getMessage());
	            return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "+e.getMessage()+" \"}"; 
	        }catch (Exception e) {
	            log.error("getting app failed: ", e.getMessage());
	            return "{\"status\" : \"error\", \"result\" : \"getting devicee failed: "+e.getMessage()+"\"}"; 
	        }
	        return writer.toString();
    	}
    	else{
	        try{
	        	JsonGenerator generator = jasonFactory
	                    .createGenerator(writer);
	        	generator.writeStartObject();
	        	generator.writeStringField("status", "ok");
	        	
	        	generator.writeArrayFieldStart("devices");
	        	List<BootDevice> allDevices = devicemanager.getAllBootDevices();
	        	for(BootDevice device: allDevices){
	            	generator.writeStartObject();
	            	device.writeJsonString(generator);
		        	generator.writeEndObject();
	        	}
	        	generator.writeEndArray();

	        	generator.writeEndObject();
	        	
	
	        	generator.close();
	        } catch (IOException e) {
	            log.error("json conversion failed: ", e.getMessage());
	            return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "+e.getMessage()+" \"}"; 
	        }catch (Exception e) {
	            log.error("getting app failed: ", e.getMessage());
	            return "{\"status\" : \"error\", \"result\" : \"getting devices failed: "+e.getMessage()+"\"}"; 
	        }
	        return writer.toString();
    	}
	}
	
	@Post
    public String handlePostRequest(String fmJson) {
		//boot vms

    	String status = "";
    	String errResult = "";
    	
    	ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {

			JsonNode root = mapper.readValue(fmJson, JsonNode.class);
			

			String type = root.path("type").asText();
			String name = root.path("name").asText();
			String attach = root.path("attach").asText();
			String attachType = root.path("attach_type").asText();


    		Map<String, Object> attrs = new HashMap<String, Object>();
    		JsonNode attributes = root.path("attributes");
    		Iterator<Entry<String, JsonNode>> entities = attributes.fields();
    		while( entities.hasNext()) {
    			Entry<String, JsonNode> entity = entities.next();
    			String key=entity.getKey();
    			Object value = entity.getValue(); 
    			attrs.put(key, value);
    		}

            BootDevice device = devicemanager.bootSecurityDeviceInstance(name, type, attach, attachType, attrs);
        	if(device != null)
        		status = "ok";
        	else
        		status = "error";
        	
        } catch (IOException e) {
            log.error("Error creating new device: " + fmJson, e);
            e.printStackTrace();
            status = "error"; 
            errResult = e.getMessage();  
        } catch (Exception e) {
            log.error("Error creating new device: ", e);
            e.printStackTrace();
            status = "error"; 
            errResult = e.getMessage()+ "," +e.toString();            
        }
        
        
        
        
        JsonFactory jasonFactory = new JsonFactory();
        StringWriter writer = new StringWriter();
        try{
        	JsonGenerator generator = jasonFactory
                    .createGenerator(writer);
        	generator.writeStartObject();
        	generator.writeStringField("status", status);
        	if(device!=null){
            	generator.writeObjectFieldStart("result");
        		generator.writeObjectFieldStart("device");
        		device.writeJsonString(generator);
            	generator.writeEndObject();
            	generator.writeEndObject();
        	}
        	else
        		generator.writeStringField("result", errResult);
        	generator.writeEndObject();
        	generator.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"status\" : \"error\", \"result\" : \"json conversion failed. \"}"; 
        }
        return writer.toString();
	}
}
