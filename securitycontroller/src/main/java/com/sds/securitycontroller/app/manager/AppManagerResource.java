/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app.manager;

import java.io.IOException;
import java.util.List;

import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.app.AppFactory;
import com.sds.securitycontroller.utils.InputMessage;
import com.sds.securitycontroller.utils.OutputMessage;


public class AppManagerResource extends ServerResource{
	protected static Logger log = LoggerFactory.getLogger(AppManagerResource.class);
	String appId = null;
	App app = null;
	IAppManagementService appmanager = null;
	InputMessage request = null;
	OutputMessage response = null;
	
	@Override
    public void doInit() {    
        appmanager = 
                (IAppManagementService)getContext().getAttributes().
                get(IAppManagementService.class.getCanonicalName());
    	response=new OutputMessage(true, this);
        appId = (String) getRequestAttributes().get("id");
        if(appId != null){
        	app = appmanager.getApp(appId);	 
        }
    }  
	
	@Get("json")
    public Object handleGetRequest() {
		String from = this.getQueryValue("from");
		int size = (null==this.getQueryValue("size"))?1000:Integer.valueOf(this.getQueryValue("size"));
		do{
			if(null != appId){
				if (null == app) {
					response.setResult(404, 404, "no such app");
					break;
				}
				appmanager.fillUpdateInfo(app);
				response.putData("app", app);	
			}else{		
				if (!(size > 0 && size <1000)) {
					log.error("size range error");
					response.setResult(201, 201, "size range error");
					break;
				}
				
				List<App> allApps = appmanager.getAllApps(from, size);
					response.putData("apps", allApps);		
					response.setHttp_code(200);
			}
		}while(false);
		
		return response.toString();	
	}
	
	//for register only
    @Post
    public String handlePostRequest(String fmJson) { 
    	do{
    		if(this.appId != null){
    			response.setResult(404, 404,"app id is not needed.");
        		break;
    		}    		     
    		try {
    			request=new InputMessage(false, fmJson);	        	 
	        	app = AppFactory.createApp(request.getData());
	        	if(null == app){
	        		response.setResult(404, 404,"create app failed.");
	        		break;
	        	}	     
	        } catch (Exception e) {
	            log.error("Error parsing new app: " + fmJson, e);
	            response.setResult(404, 404,"Could not parse new app."+e.getSuppressed());
	            break;
	        }  
    		String tmpId=appmanager.appRegistered(app);
       		if(null != tmpId){
       			response.setResult(200,200,"id",tmpId);
    			break;
    		}  
    		    		       
    		if(!appmanager.addApp(app)){
    			response.setResult(404, 404,"add app error.");
    			break;
    		}
    		//return an app's id
    		response.setResult(200,200,"id",app.getId());
    	}while(false);
    	
    	return response.toString();    	 
    }
    

    @Delete
    public String handleDeleteRequest(String fmJson) {    	
    	do{
        	if(this.appId == null){    	
    			response.setResult(404, 404,"id should be provided.");
        		break;
        	}            	
        	try {
	        	app = appmanager.getApp(appId);
	    	    if(this.app == null){
	                response.setResult(404, 404,"app is not found.");
	    	    	break;
	    	    }
		    	if(!appmanager.removeApp(app)){
		    		response.setResult(404, 404,"app remove failed "+appId);
		    		break;
		    	}
	        } catch (Exception e) {
	            log.error("Error delete app: ", e);
	            response.setResult(404, 404,"error in delete app"+e.getMessage());
	            break;
	        }
        	response.setResult(200,200,"app removed "+appId);
    	}while(false);
    	    	
    	return response.toString();
    }
    
    
    @Put
    public String handlePutRequest(String fmJson) {    	
    	do{    	
    	   	if(this.appId == null){
    	   		response.setResult(404, 404,"app id missing.");
        		break;
    	   	}   
        	if(this.app == null){
        		response.setResult(404, 404,"app not found.");
        		break;
        	}
                
        	try {        		
        		request=new InputMessage(false, fmJson);
        		if(!appmanager.updateApp(app, request.getData())){        			 
        			response.setResult(404, 404,"update app failed");
                	break;
        		}
                 	
            } catch (IOException e) {
                log.error("Error put app: " + fmJson, e);
                response.setResult(404, 404,"exception in decode json or updateapp "+e);
                break;  
            } catch (Exception e) {
                log.error("Error put app: ", e);
                response.setResult(404, 404,"exception in decode json or updateapp "+e);
                break;            
            }
	        response.setResult(200,200,"updated");
    	}while(false);
    	
    	return response.toString();
    }

    
    
    
}
