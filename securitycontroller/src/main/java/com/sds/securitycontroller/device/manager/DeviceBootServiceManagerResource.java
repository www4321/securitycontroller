package com.sds.securitycontroller.device.manager;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;

public class DeviceBootServiceManagerResource extends ServerResource {

	protected static Logger log = LoggerFactory.getLogger(DeviceManagerResource.class);

    IDeviceManagementService devicemanager = null;
	
	
	@Override  
    public void doInit(){ 
		devicemanager = 
	            (IDeviceManagementService)getContext().getAttributes().
	            get(IDeviceManagementService.class.getCanonicalName());
	}
	
	@Post
    public String handlePostRequest(String fmJson) {
		//boot vms

    	String status = "";
    	String errResult = "";
    	
    	ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {
    		status = "ok";

			JsonNode root = mapper.readValue(fmJson, JsonNode.class);

			String url = root.path("url").asText();
			JsonNode devices = root.path("devices");

			Iterator<JsonNode> it = devices.iterator();
			while(it.hasNext()){
				JsonNode device = it.next();
				DeviceType type = Enum.valueOf(DeviceType.class, device.asText().toUpperCase());
				if(!devicemanager.registerBootAgent(type, url))
					status = "error";
			}
        	
        } catch (IOException e) {
            log.error("Error creating new device: " + fmJson, e);
            e.printStackTrace();
            status = "error"; 
            errResult = e.getMessage();  
        } catch (Exception e) {
            log.error("Error creating new device: ", e);
            e.printStackTrace();
            status = "error"; 
            errResult = e.getMessage();            
        }
        
        
        
        
        JsonFactory jasonFactory = new JsonFactory();
        StringWriter writer = new StringWriter();
        try{
        	JsonGenerator generator = jasonFactory
                    .createGenerator(writer);
        	generator.writeStartObject();
        	generator.writeStringField("status", status);
        	if(errResult != null)
        		generator.writeStringField("error", errResult);
        	generator.writeEndObject();
        	generator.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"status\" : \"error\", \"result\" : \"json conversion failed. \"}"; 
        }
        return writer.toString();
	}
}
