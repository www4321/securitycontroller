package com.sds.securitycontroller.app;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sds.securitycontroller.utils.http.HTTPHelper;
import com.sds.securitycontroller.utils.http.HTTPHelperResult;

public class AppControlMessage {
	private String url="";
	private String cmd="";
	private Map<String,String> para;

	public AppControlMessage(String url, String cmd, Map<String,String> para){
		this.url=url;
		this.cmd=cmd;
		this.para=para;
	}
	public boolean send(){
		String data="{\"head\":{},\n\"data\":{";
		
		data+="\"operation\":\""+cmd+"\"";
		
		if (null != para) {
			Iterator<String> it = para.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = para.get(key);
				data += "\"" + key + "\":\"" + value + "\"";
			}
		}
		data+="}\n}";
				
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type","application/json"); 
		HTTPHelperResult result=HTTPHelper.httpRequest(url, "put", data, headers);
		
		if(-1 == result.getCode()){		
			return false;
 
		}
		if(200 != result.getCode()){
			return false;
		}		
		return true;
	}
}
