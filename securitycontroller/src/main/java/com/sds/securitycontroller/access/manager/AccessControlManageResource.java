package com.sds.securitycontroller.access.manager;

import java.io.IOException;
import java.io.StringWriter;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sds.securitycontroller.app.manager.AppManagerResource;
import com.sds.securitycontroller.common.Entity;
import com.sds.securitycontroller.utils.JsonRequest;
import com.sds.securitycontroller.utils.JsonResponse;

public class AccessControlManageResource extends ServerResource{
	protected static Logger log = LoggerFactory.getLogger(AppManagerResource.class);
	String subjectId = null;
	String objectId = null;
	String operation = null;
	IAccessControlManagementService aclManager = null;
	JsonRequest request=null;
	JsonResponse response=new JsonResponse();

	
	@Override  
    public void doInit() {    
        aclManager = 
                (IAccessControlManagementService)getContext().getAttributes().
                get(IAccessControlManagementService.class.getCanonicalName());
        subjectId = (String) getRequestAttributes().get("subject");
        objectId = (String) getRequestAttributes().get("object");
        operation = (String) getRequestAttributes().get("op");
    }  
	

	@Get("json")
    public Object handleGetRequest() {

    	JsonFactory jasonFactory = new JsonFactory();
        StringWriter writer = new StringWriter();
    	if(this.subjectId != null && this.objectId!= null && this.operation!=null ){    
    		Entity subject = new Entity(this.subjectId);
    		Entity object = new Entity(this.subjectId); 
    		SubjectOperation op = Enum.valueOf(SubjectOperation.class, operation.toUpperCase());
    		AccessControlContext context = new AccessControlContext();
    		boolean allow = aclManager.allowAccess(subject, object, op, context);
    		
	        try{
	        	JsonGenerator generator = jasonFactory
	                    .createGenerator(writer);
	        	generator.writeStartObject();
	        	generator.writeStringField("status", "ok");
	        	
	        	generator.writeObjectFieldStart("acl");
	        	generator.writeStringField("subject", this.subjectId);
	        	generator.writeStringField("object", this.objectId);
	        	generator.writeStringField("operation", this.operation);
	        	generator.writeBooleanField("allow", allow);
	        	generator.writeEndObject();
	
	        	generator.close();
	        } catch (IOException e) {
	            log.error("json conversion failed: ", e.getMessage());
	            return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "+e.getMessage()+" \"}"; 
	        }catch (Exception e) {
	            log.error("getting acl failed: ", e.getMessage());
	            return "{\"status\" : \"error\", \"result\" : \"getting acl failed: "+e.getMessage()+"\"}"; 
	        }
    	}
        return writer.toString();
	}
}
