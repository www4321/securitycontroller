package com.sds.securitycontroller.knowledge.globaltraffic.analyzer;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class GlobalTrafficAnalyzerRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		// TODO Auto-generated method stub
		Router router = new Router(context);
		router.attach("/", GlobalTrafficAnalyzerResource.class);
		/**
		 * 	REQUEST: 
		 * GET ,URL: /sc/globalflow/records[?src_mac={src_mac}&
		 * 	dst_mac={dst_mac}&
		 * 	src_ip={src_ip}&
		 * 	dst_ip={dst_ip}&
		 * 	src_port={src_port}&
		 * 	dst_port={dst_port}&
		 *  src_mac={src_mac}&
		 * 	starttime={starttime}
		 * 	endtime={endtime}]
		 * 
		 *  FUNCTION: query total packets and bytes count that satisfied:
		 *  1. query fields(src_mac, dst_ip, etc) matched
		 *  2. between 'starttime' & 'endtime' (UNIX timestamp, milisec)
		 *  3. if 'time' param is specified, returns records that timestamp = {time}  
		 * 
		 *  RESPONSE:
		 *  {
			    "status": "ok",
			    "result": [
			        {
			            "totalByteCount": 136061,
			            "totalPacketCount": 141
			        }
			    ]
			}
		 *  
		 **/
		router.attach("/records", TrafficRecordResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/sc/globalflow";
	}

}
