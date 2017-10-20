package com.sds.securitycontroller.app.manager;

import java.io.IOException; 
import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.app.AppRealtimeInfo;
import com.sds.securitycontroller.utils.InputMessage;
import com.sds.securitycontroller.utils.OutputMessage;

public class AppRealtimeResource extends ServerResource{
protected static Logger log = LoggerFactory.getLogger(AppRealtimeResource.class);

	

	String appId = null;
	App app = null;
	IAppManagementService appmanager = null;
	InputMessage request=null;
	OutputMessage response=null;
	
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
		AppRealtimeInfo info = null;  		 
		do {
			if (null != appId) {
				if (null == app) {
					response.setResult(404, 404,"no such app");
					break;
				}			
				info = appmanager.getAppRealtimeInfo(appId);				 				
				response.putData("realtime_info", info);
			} else {		
				List<AppRealtimeInfo> infolist = appmanager.getAllAppRealtimeInfo();				
				response.putData("realtime_infos", infolist);			
			}
		} while (false);

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
				response.setResult(404, 404,"no such app");
        		break;
        	}
        	try {
        		request=new InputMessage(false, fmJson); 
        		if(!appmanager.updateAppRealtimeInfo(appId, request.getData())){        			               
                	response.setResult(404, 404,"update failed.");
                    break;
        		}
            } catch (IOException e) {
                log.error("Error put app: " + fmJson, e);               
                response.setResult(404, 404,e.getMessage());
                break;  
            } catch (Exception e) {
                log.error("Error put app: ", e);
                response.setResult(404, 404,"exception in decode json or updateapp");
                break;            
            }
        	response.setResult(200,200,"updated succeed");
    	}while(false);
       
    
    	return response.toString();
    }
}
