package com.sds.securitycontroller.knowledge.globaltraffic.analyzer;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.flow.FlowTrafficStats;
import com.sds.securitycontroller.restserver.RestSimpleObjectResponse;

public class TrafficRecordResource extends ServerResource {
	
	IGlobalTrafficAnalyzeService globalTrafficAnalyzer;
	protected static Logger log = LoggerFactory.getLogger(TrafficRecordResource.class);
	long timestamp=0;
	Map<String, String> queryConditions;
	
	@Override
	public void doInit(){
		globalTrafficAnalyzer = (IGlobalTrafficAnalyzeService)getContext().getAttributes().
				get(IGlobalTrafficAnalyzeService.class.getCanonicalName());
		//TODO
		Form queryParams = getRequest().getResourceRef().getQueryAsForm();
		queryConditions=new HashMap<String,String>();
		if(queryParams!=null && !queryParams.isEmpty() ){
			//queryConditions=new HashMap<String,String>();
			try {
				if( queryParams.getFirstValue("src_mac")!=null &&!queryParams.getFirstValue("src_mac").isEmpty())
					queryConditions.put("src_mac",queryParams.getFirstValue("src_mac"));
				if( queryParams.getFirstValue("dst_mac")!=null&&!queryParams.getFirstValue("dst_mac").isEmpty() )
					queryConditions.put("dst_mac",queryParams.getFirstValue("dst_mac"));
				if( queryParams.getFirstValue("src_ip")!=null &&!queryParams.getFirstValue("src_ip").isEmpty())
					queryConditions.put("src_ip",queryParams.getFirstValue("src_ip"));
				if( queryParams.getFirstValue("dst_ip")!=null&&!queryParams.getFirstValue("dst_ip").isEmpty() )
					queryConditions.put("dst_ip",queryParams.getFirstValue("dst_ip"));
				if( queryParams.getFirstValue("src_port")!=null &&!queryParams.getFirstValue("src_port").isEmpty())
					queryConditions.put("src_port",queryParams.getFirstValue("src_port"));
				if( queryParams.getFirstValue("dst_port")!=null &&!queryParams.getFirstValue("dst_port").isEmpty())
					queryConditions.put("dst_port",queryParams.getFirstValue("dst_port"));
				if( queryParams.getFirstValue("time")!=null&&!queryParams.getFirstValue("time").isEmpty() )
					queryConditions.put("time",queryParams.getFirstValue("time"));
				
				if( queryParams.getFirstValue("starttime")!=null&&!queryParams.getFirstValue("starttime").isEmpty() )
					queryConditions.put("starttime",queryParams.getFirstValue("starttime"));
				if( queryParams.getFirstValue("endtime")!=null &&!queryParams.getFirstValue("endtime").isEmpty())
					queryConditions.put("endtime",queryParams.getFirstValue("endtime"));
				
 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Get("json")
	public Object handleRequest() {
		FlowTrafficStats results=globalTrafficAnalyzer.queryTrafficStatus(queryConditions);
		if(results==null)
		{
			return new RestSimpleObjectResponse("error","null");
		}
		Map<String,Long> data=new HashMap<String,Long>();
		data.put("totalPacketCount", results.pkg_count);
		data.put("totalByteCount", results.byte_count);
		RestSimpleObjectResponse response = new RestSimpleObjectResponse("ok",data);
		return response;
	}
}
