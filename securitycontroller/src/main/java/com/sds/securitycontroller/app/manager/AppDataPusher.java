/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app.manager;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.utils.HTTPUtils;
import com.sds.securitycontroller.utils.ResponseFactory;


public class AppDataPusher implements Runnable{

    public AppDataPusher(AppPushRequest req) {
		super();
		this.req = req;
	}

	protected static Logger log = LoggerFactory.getLogger(AppDataPusher.class);
    private AppPushRequest req;
    DateFormat dateFormat =  new SimpleDateFormat("HH:mm:ss.SSS");
    
	public ResponseFactory pushDataToApp(){
		Date t1,t2,t3,t4;
		
		t1 = new Date();

		String 	subscribeId 	= req.getSubscriptionId();
		String 	subscribeUrl 	= req.getSubscribeUrl();
		List<?> subscribeData 	= req.getData();
		

		ResponseFactory res = new ResponseFactory();
		res.putData(200, "subscriptions", subscribeData, null);
		res.putData("subscribeId", subscribeId);
		res.putData("appid", req.getAppId());


		t2 = new Date();
		log.debug("<APP-PUSH-TEST> GENERATE REQ TIME USED:{}",t2.getTime()-t1.getTime());
		
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type","application/json");
		log.debug("Pushing data to APP at {}",	subscribeUrl);
		String jsonResp = HTTPUtils.httpPost(subscribeUrl, res.toString(), headers);
	
		t3 = new Date();
		log.debug("<APP-PUSH-TEST>,{},POST REQ TIME USED:{},",dateFormat.format(t3),t3.getTime()-t2.getTime());
		
		if(jsonResp == null) //Error
			return new ResponseFactory(404, "result", "return null response", null);
		try {
			return ResponseFactory.CreateResponse(jsonResp);
		} catch (IOException e) {
			log.error("error response: {}", e.getMessage());
			return new ResponseFactory(404, "result", "error response: " + e.getMessage(), null);

		}  
		finally{
			t4 = new Date();
			log.debug("<APP-PUSH-TEST> PARSE RESPONSE TIME USED:{}",t4.getTime()-t3.getTime());
			log.debug("<APP-PUSH-TEST>,{},OK,data length:{}",dateFormat.format(t4), subscribeData.size());
		}
	}

	@Override
	public void run() {
		pushDataToApp();
	}

}
