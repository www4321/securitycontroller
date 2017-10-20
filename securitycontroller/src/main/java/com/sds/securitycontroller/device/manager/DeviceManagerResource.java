/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.device.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.device.Device;
import com.sds.securitycontroller.device.DeviceFactory;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.utils.InputMessage;
import com.sds.securitycontroller.utils.OutputMessage;
import com.sds.securitycontroller.utils.http.HTTPHelper;
import com.sds.securitycontroller.utils.http.HTTPHelperResult;


public class DeviceManagerResource extends ServerResource  {
	protected static Logger log = LoggerFactory.getLogger(DeviceManagerResource.class);
	
    IDeviceManagementService devicemanager = null;
    IEventManagerService scheduler = null;

	String devId = null;
	Device dev = null;
	InputMessage request = null;
	OutputMessage response = null;
	
	
	@Override  
    public void doInit() {    
		this.devicemanager = 
	            (IDeviceManagementService)getContext().getAttributes().
	            get(IDeviceManagementService.class.getCanonicalName());   
		this.scheduler = 
	            (IEventManagerService)getContext().getAttributes().
	            get(IEventManagerService.class.getCanonicalName());
        this.devId = (String) getRequestAttributes().get("id");
        if(this.devId != null){
        	this.dev = devicemanager.getDev(this.devId);
        }
    }  
	

	@Get("json")
    public Object handleGetRequest() {
		//System.out.println("liu ye ping sa diao2");
		do{
			if(null != devId){
				if (null == dev) {
					response.setResult(404,404, "no such dev");
					break;
				}
				response.putData("dev", dev);					 
			}else{				
				List<Device> allDevs = devicemanager.getDevs();
				response.putData("devs", allDevs);				 
			}
			response.setHttp_code(200);
		}while(false);
		
		return response.toString(); 
    }

    @Post
    public String handlePostRequest(String fmJson) {
    	String pstr = this.getQueryValue("passive");
    	boolean p= (null==pstr)?false:Boolean.valueOf(pstr);
    	if(true != p){
    		return positiveRegister(fmJson);
    	}else{
    		return passiveRegister(fmJson);
    	}
    }
    
    @Delete
    public String handleDeleteRequest(String fmJson) {
    	do{
        	if(this.devId == null){    			
    			response.setResult(404,404,"id should be provided.");
        		break;
        	}            	
        	try {
	        	dev = devicemanager.getDev(devId);
	    	    if(this.dev == null){
	                response.setResult(404,404,"dev is not found.");
	    	    	break;
	    	    }
		    	if(!devicemanager.removeDev(dev)){
		    		response.setResult(404,404,"dev remove failed "+devId);
		    	}
	        } catch (Exception e) {
	            log.error("Error delete app: ", e);
	            response.setResult(404,404,"error in delete app");
	            break;
	        }
        	response.setResult(200,200,"dev removed "+devId);
    	}while(false);
    	    	
    	return response.toString();
    }
    
    
    @Put
    public String handlePutRequest(String fmJson) {
    	do{    	
    	   	if(this.devId == null){
    	   		response.setResult(404,404,"app id missing.");
        		break;
    	   	}   
        	if(this.dev == null){
        		response.setResult(404,404,"dev not found.");
        		break;
        	}
                
        	try {
        	    request=new InputMessage(false,fmJson);  
        		if(!devicemanager.updateDev(dev, request.getData())){        			 
        			response.setResult(404,404,"update dev failed");
                	break;
        		}
                 	
            } catch (IOException e) {
                log.error("Error put dev: " + fmJson, e);
                response.setResult(404,404,"exception in decode json or updateapp "+e.getMessage());
                break;  
            } catch (Exception e) {
                log.error("Error put dev: ", e);
                response.setResult(404,404,"exception in decode json or updateapp "+e.getMessage());
                break;            
            }
	        response.setResult(200,200,"updated!");
    	}while(false);
    	
    	return response.toString();
    }
    
    private String passiveRegister(String fmJson){
    	
    	
    	InputMessage UImsg=null;
    	String myIp=null;
    	do{
    		if(this.devId != null){ 
    			response.setResult(404,404,"dev id is not needed.");
        		break;
    		}    		     
    		try {
    			UImsg = new InputMessage(false, fmJson);
    			myIp=devicemanager.getMyIp();
	        } catch (Exception e) {
	            log.error("Error passive register: " + fmJson, e);
	            response.setResult(404,404,"Could not parse request."+e.getMessage());
	            break;
	        }   
    		JsonNode jn = UImsg.getData();
			if(!jn.has("ip")){
				response.setResult(404,404,"json error.");
				break;
			}
			
			String devip = jn.path("ip").asText();
			String url="http://"+devip+":9999/4sdnapi/register";
			
			String content="{\"head\":{},\"data\":{\"sc\":\"http://"+ myIp +":8888/sc/devs/info\"}}";
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type","application/json");
			
			HTTPHelperResult result=null;
			result=HTTPHelper.httpPost(url, content, headers);
			if(-1 == result.getCode()){
				response.setResult(500,500,"http failed.");
				break;
			}
			InputMessage imsg=null;
			try {
				result.setMsg(result.getMsg().toLowerCase());
				imsg=new InputMessage(false,result.getMsg());
			} catch (IOException e) {
				response.setResult(result.getCode(),result.getCode(), result.getMsg());
				break;
			}
			
			response.setHttp_code(result.getCode());
			response.setOpt_status(result.getCode());
			response.putData("id", imsg.getData().path("id").asText()); 
    	}while(false);
    	
    	return response.toString();    	
    }
    
    private String positiveRegister(String fmJson){
    	String myip="";
    	do{
    		if(this.devId != null){ 
    			response.setResult(404,404,"dev id is not needed.");	
        		break;
    		}    		     
    		try {
    			request = new InputMessage(false, fmJson);    	 
    			dev = DeviceFactory.createDevice(request.getData(), this.scheduler);
	        	if(null == dev){	        	 
	        		response.setResult(404,404,"create dev failed");
	        		break;
	        	}	     
	        	
	        	
	        } catch (Exception e) {
	            log.error("Error parsing new dev: " + fmJson, e);
	            response.setResult(404,404,"Could not parse new dev."+e.getMessage());
	            break;
	        }     	 
    		
    		try{
    			myip=devicemanager.getMyIp();
    		} catch (Exception e) {
    			log.error("Error getting my ip" );
	            response.setResult(500,500,"Could not get self ip."+e.getMessage());
	            break;
    		}    		
    		
    		String tmpId=devicemanager.devRegistered(dev);
       		if(null == tmpId){       					       
	    		if(!devicemanager.addDev(dev)){
	    			response.setResult(404,404,"add dev error.");
	    			break;
	    		}
	    		tmpId=dev.getId();
    		}       		
       		
       		response.setHttp_code(200);
       		response.setOpt_status(200);
       		response.putData("id", tmpId);
       		response.putData("sc", "http://"+ myip +":8888/sc/");
    	 
    	}while(false);
    	
    	return response.toString();
    }
    
    
    
}
