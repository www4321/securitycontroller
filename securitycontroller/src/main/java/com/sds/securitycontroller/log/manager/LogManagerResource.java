/** 
*    Copyright 2014 BUPT 
**/ 
package com.sds.securitycontroller.log.manager;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.log.Report;
import com.sds.securitycontroller.log.ReportItem;
import com.sds.securitycontroller.utils.ResponseFactory;


public class LogManagerResource extends ServerResource{

	protected static Logger logger = LoggerFactory
			.getLogger(LogManagerResource.class);
	String id = null;
	Report report = null;
	IEventManagerService eventManager = null;
	ILogManagementService logmanager = null;
	
	private static final String urlApp 		= "app";
	private static final String urlDev 		= "dev";
	private static final String urlQuery 	= "query";
	

	@Override
	public void doInit() {
		logmanager = (ILogManagementService) getContext().getAttributes()
				.get(ILogManagementService.class.getCanonicalName());
		eventManager = 
                (IEventManagerService)getContext().getAttributes().
                get(IEventManagerService.class.getCanonicalName());
		
		id = (String) getRequestAttributes().get("id");

		if (id != null) {

			switch (id) {
			case urlApp:
			case urlDev:
				// transmission report
				break;
			case urlQuery:
				// query report
				break;
			default:
				//report = logmanager.getReport(id);
				logger.info("The request id={}", id);
			}
		} else {
			logger.debug("get report list");
			// TODO get report list
		}
	}


	@Get("json")
    public Object handleGetRequest() {	
    	if(this.id == null)
            return "{\"status\" : \"error\", \"result\" : \"report id missing. \"}";
    	if(this.report == null)
            return "{\"status\" : \"error\", \"result\" : \"report not found. \"}";
    	JsonFactory jasonFactory = new JsonFactory();
        StringWriter writer = new StringWriter();
        try{
        	JsonGenerator generator = jasonFactory
                    .createGenerator(writer);
        	generator.writeStartObject();
        	generator.writeStringField("status", "ok");
        	generator.writeObjectFieldStart("result");
        	if(report!=null)
        		report.writeJsonString(generator);
        	generator.writeEndObject();
        	generator.writeEndObject();

        	generator.close();
        } catch (IOException e) {
        	logger.error("json conversion failed: ", e.getMessage());
            return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "+e.getMessage()+" \"}"; 
        }catch (Exception e) {
        	logger.error("getting app failed: ", e.getMessage());
            return "{\"status\" : \"error\", \"result\" : \"getting app failed: "+e.getMessage()+"\"}"; 
        }
        return writer.toString();
    }

	@Post
	public String processNewReport(String fmJson) {
		if(id != null){
			switch(id){
			case urlApp:
			case urlDev:
				//transmission report
				return this.processTransmissionReport(fmJson);
			case urlQuery:
				//query report
				return this.processQueryReport(fmJson);
			default:
				logger.info("The post data={}", fmJson);
				return new ResponseFactory(404, "result", "no process for the url.", this).toString();
			}
		}
		else
		{
			return new ResponseFactory(404, "result", "no process for the url.", this).toString();
		}
		
	}

	@SuppressWarnings("unchecked")
	public String processQueryReport(String fmJson){
		ResponseFactory re = new ResponseFactory();
		
		try{
			do{
				Object logs = DecodeLogJson(fmJson);
				if(logs == null){
					re.putData(404,  "result", "no query data", this);
					break;
				}
				
				if(!(logs instanceof Map<?,?>)){
					re.putData(404,  "result", "query data format error", this);
					break;
				}
				
				ReportItem item = new ReportItem(fmJson);
				item.setMaps((Map<String, Object>) logs);
				
				//TODO is Report?
				List<Report> result = logmanager.queryReport(item);
				if (result != null){
					re.putData(200, "logs", result, null);
					break;
					
				}
				else{
					re.putData(200, "logs", "", null);
					break;
				}
		
			}while(false);
			
		}catch(Exception e){
			logger.error("Error creating new report: {}", e.getMessage());
			re.putData(404, "result", "Parse query json failuer.", this);
		}
		
		return re.toString();
		
		}

	@SuppressWarnings("unchecked")
	protected String processTransmissionReport(String fmJson) {
		ResponseFactory response = new ResponseFactory();
		try {
	
			Object logs = DecodeLogJson(fmJson);
			if(logs != null)
			{
				List<ReportItem> listReport = new ArrayList<ReportItem>();
				if(logs instanceof List<?>){
					for(Map<String, Object> map: (List<Map<String, Object>>)logs){
						this.ProcessSingleLog(map, listReport, response);
						if(response.isError()){
								return response.toString();
							}
						}
				}
				else{
					this.ProcessSingleLog((Map<String, Object>) logs, listReport, response);
					if(response.isError()){
						return response.toString();
					}
				}
				
				if( listReport.size() > 0){
					response.putData(200, "result", "ok", null);
					logmanager.addReport(listReport);
				}
				else{
					response.putData(415, "result", "no data.", this);
				}
			}
			else
			{
				response.putData(415, "result", "no log item.", this);
			}
			return response.toString();
		
		} catch (Exception e) {
			String error = String.format("Error creating new report: %s", e.getMessage());
			logger.error(error);
			response.putData(406,  "result", "Error creating new report: Unexpected character or json format error.", this);
			return response.toString();
		} 
		
	}
	
	protected void ProcessSingleLog(Map<String, Object> logmap, List<ReportItem> listReport, ResponseFactory response){
		ReportItem reportItem = new ReportItem();
		reportItem.setMaps(logmap);
		
		if(!reportItem.VerifyItem()){
			response.putData(404, "result",  "pack verify failed.", this);
			return;
		}
		
		if(!reportItem.ConvertDate(reportItem.getMaps(), false, true) ){
			//转化时间
			response.putData(404, "result",  "Time format error or not legal, you must like yyyy-mm-dd HH:MM:SS.", this);
			return;
		}
		
		listReport.add(reportItem);
	}
	

	public static  Object DecodeLogJson(String fmJson) throws Exception{
		ResponseFactory response = ResponseFactory.CreateResponse(fmJson);
		return response.getData("logs");
	}
}

