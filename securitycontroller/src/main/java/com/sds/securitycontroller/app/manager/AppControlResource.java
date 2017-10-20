package com.sds.securitycontroller.app.manager;

import java.util.ArrayList;
import java.util.List;

import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.app.AppControlMessage;
import com.sds.securitycontroller.utils.JsonRequest;
import com.sds.securitycontroller.utils.JsonResponse;

public class AppControlResource extends ServerResource{
	protected static Logger log = LoggerFactory.getLogger(AppManagerResource.class);
	String appId = null;
	App app = null;
	IAppManagementService appmanager = null;
	JsonRequest request=null;
	JsonResponse response=new JsonResponse();
	List<String> ops= new ArrayList<String>();

	
	@Override  
    public void doInit() {    
        appmanager = 
                (IAppManagementService)getContext().getAttributes().
                get(IAppManagementService.class.getCanonicalName());
        appId = (String) getRequestAttributes().get("id");
        if(appId != null){
        	app = appmanager.getApp(appId);	 
        }
        //init the allowed operations
    	ops.add("START");
    	ops.add("RESTART");
    	ops.add("STOP");
    	ops.add("PAUSE");    	 
    	ops.add("UPGRADE");
    	ops.add("REMOVE");
    }  
	
	
	//this method is for ui
    @Put
    public String handlePutRequest(String fmJson) {
    	do{   	
			if (null == appId) {
				response.setMessage(404, "appid is null");
				break;
			}
			if (null == app) {
				response.setMessage(404, "no such app");
				break;
			}
    		try {
    			request = new JsonRequest(fmJson);
    			String  operation	= request.getData().path("OPERATION").asText();
    			
    
    			if(!ops.contains(operation.toCharArray())){
    				response.setMessage(404,"invalid operation");	
    				break;
    			}    			
    			
    			String url=app.getHost()+":"+app.getPort()+"/status";	
    			AppControlMessage msg=new AppControlMessage(url,operation,null);
    			if(!msg.send()){
    				response.setMessage(404,"invalid operation");	
    				break;
    			}
    	 	     
	        } catch (Exception e) {
	            log.error("Error parsing new app: " + fmJson, e);
	            response.setMessage(404,"Could not parse new app.");
	            break;
	        }     
    		
    		response.setMessage(200,"operation succeed");    		
    	}while(false);
    	
    	return response.toString();
    }
}
