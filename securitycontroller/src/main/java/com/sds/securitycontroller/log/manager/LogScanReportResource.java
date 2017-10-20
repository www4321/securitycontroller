package com.sds.securitycontroller.log.manager;

import java.util.List;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.log.Report;
import com.sds.securitycontroller.utils.InputMessage;
import com.sds.securitycontroller.utils.OutputMessage;

public class LogScanReportResource extends ServerResource {

	protected static Logger log = LoggerFactory.getLogger(LogScanReportResource.class);
	protected static Logger logger = LoggerFactory
			.getLogger(LogManagerResource.class);
	String id = null;
	Report report = null;
	IEventManagerService eventManager = null;
	ILogManagementService logmanager = null;
	
	
	InputMessage request = null;
	OutputMessage response = null;
	
	@Override  
    public void doInit() {    
    	response=new OutputMessage(true, this);
        logmanager = (ILogManagementService) getContext().getAttributes()
				.get(ILogManagementService.class.getCanonicalName());
    }

	
	@Get("json")
    public Object handleGetRequest() {	
    	
        return "";
    }
	
	@Post
	public String handlePostRequest(String fmJson) {
		List<Map<String, Object>> result=null;
		do{ 		 
    		try {
    			request=new InputMessage(false, fmJson);
    			String map=request.getData().path("map").asText();
    			String reduce=request.getData().path("map").asText();
    			result = logmanager.getScanReport(map, reduce);
    			
    			response.putData("result", result);
    			response.setHttp_code(200);
    			response.setOpt_status(200);
	        } catch (Exception e) {
	            log.error("Error parsing new app: " + fmJson, e);
	            response.setResult(404, 404,"Could not parse scanreport."+e.getSuppressed());
	            break;
	        }
    		 
    	}while(false);
    	
    	return response.toString(); 
	}

}
