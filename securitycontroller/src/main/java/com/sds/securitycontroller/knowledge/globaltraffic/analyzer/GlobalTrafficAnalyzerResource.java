package com.sds.securitycontroller.knowledge.globaltraffic.analyzer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.core.web.ResultPage;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.knowledge.globaltraffic.MatchPath;
import com.sds.securitycontroller.restserver.RestSimpleObjectResponse;

public class GlobalTrafficAnalyzerResource extends ServerResource {
	
	protected static Logger log = LoggerFactory.getLogger(GlobalTrafficAnalyzerResource.class);
	IGlobalTrafficAnalyzeService globalTrafficAnalyzer;
	boolean getOne=false;
	
	@Override
	public void doInit(){
		globalTrafficAnalyzer = (IGlobalTrafficAnalyzeService)getContext().getAttributes().
				get(IGlobalTrafficAnalyzeService.class.getCanonicalName());
		try {
			getOne= Boolean.valueOf( (String) getRequestAttributes().get("methodname") );
		} catch (Exception e) {
			log.warn(" empty 'getOne' args, set to default value 'false'.");
			getOne=false;
		}
	}
	
	@Get("json")
	public Object handleRequest() {
		Map<String, MatchPath> globalFlowMap = globalTrafficAnalyzer.getGloableTrafficMapping();
		/*for (String key : globalFlowMap.keySet()) {
			for (FlowMatch fm : globalFlowMap.get(key).getmatchlist()) {
				System.out.println(fm.getNetworkProtocol());
			}
		}*/
		if(globalFlowMap == null)
			return "{\"status\" : \"ok\", \"result\" : []}";
        try{
        	RestSimpleObjectResponse response = new RestSimpleObjectResponse("ok",globalFlowMap);
        	
        	return response;
        }
        catch (Exception e) {
            log.error("getting global flow mapping failed: ", e.getMessage());
            return "{\"status\" : \"error\", \"result\" : \"error: "+e.getMessage()+"\"}"; 
        }
	}
	
	@Post
    public Object handlePostRequest(String fmJson){
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jn = mapper.readTree(fmJson);
			FlowMatch queryMatch=FlowMatch.resolveFlowMatchFromJsonNode(jn);
			if(queryMatch==null)
				return "{\"status\" : \"ok\", \"result\" : []}";
			List<MatchPath> matchPaths = globalTrafficAnalyzer.queryFlowMatch(queryMatch, getOne);
			
			ResultPage<MatchPath> resultObject=new ResultPage<MatchPath>();
			resultObject.allCount=globalTrafficAnalyzer.getAllCount();
			resultObject.allList=matchPaths;
			Map<String, Object> result=new HashMap<String, Object>();
			result.put("allCount", resultObject.allCount);
			result.put("allList", matchPaths);
			
			RestSimpleObjectResponse response = new RestSimpleObjectResponse("ok",result);
        	return response;
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return "{\"status\" : \"error\", \"result\" : \"error: "+e.getMessage()+"\"}"; 
		}
	}
}
