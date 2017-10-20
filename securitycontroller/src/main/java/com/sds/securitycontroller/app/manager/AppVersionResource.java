/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.utils.InputMessage;
import com.sds.securitycontroller.utils.OutputMessage;
import com.sds.securitycontroller.utils.http.HTTPHelper;
import com.sds.securitycontroller.utils.http.HTTPHelperResult;


public class AppVersionResource extends ServerResource{
	protected static Logger log = LoggerFactory.getLogger(AppManagerResource.class);
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
		do {
			if (null == appId) {
				response.setResult(404,404, "appid is null");
				break;
			}
			if (null == app) {
				response.setResult(404,404, "no such app");
				break;
			} 
			
			String pkgVer=appmanager.getPackageVersion(app.getGuid());
			String curVer=app.getVersion();	 
			String jsonstr="";
			
			if(null == pkgVer){
				jsonstr+="\"UPDATABLE\":false,\n"+"\"CURRENT_VERSION\":\""+curVer+"\"";
			}else{
				if(pkgVer.compareToIgnoreCase(curVer)>0){
					jsonstr+="\"UPDATABLE\":true,\n"+"\"CURRENT_VERSION\":\""+curVer+"\",\n\"NEW_VERSION\":\""+pkgVer+"\"";
				}else{
					jsonstr+="\"UPDATABLE\":false,\n"+"\"CURRENT_VERSION\":\""+curVer+"\"";
				}
			}
			response.setResult(200,200,jsonstr);
		} while (false);
		
		return response.toString();
	}
	    
    @Put
    public String handlePutRequest(String fmJson) {    	
    	 
    	do{
			if (null == appId) {
				response.setResult(404,404, "appid is null");
				break;
			}
			if (null == app) {
				response.setResult(404,404, "no such app");
				break;
			}
			
			String url="http://"+app.getHost()+":"+app.getPort()+"/version";			
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type","application/json");
			HTTPHelperResult result=HTTPHelper.httpRequest(url, "PUT", "", headers);
			
 			if(-1 == result.getCode()){
				response.setResult(500,500,"sc error."+result.getMsg());
				break;
			}
			
			InputMessage imsg=null;
			try {
				imsg=new InputMessage(false,result.getMsg());
			} catch (IOException e) {
				response.setResult(result.getCode(),result.getCode(), result.getMsg());
				break;
			} 
			
			if (200 != result.getCode()) {
				response.setResult(500, 500,"sc error "+imsg.getData().path("result").asText());
				break;
			}
			response.setResult(200,200, "operation succeed"); 
    	}while(false);    	
    	
    	return response.toString();
    }
}
