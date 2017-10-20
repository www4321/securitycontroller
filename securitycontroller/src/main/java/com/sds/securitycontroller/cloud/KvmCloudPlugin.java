package com.sds.securitycontroller.cloud;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.utils.InputMessage;
import com.sds.securitycontroller.utils.http.HTTPHelper;
import com.sds.securitycontroller.utils.http.HTTPHelperResult;

public class KvmCloudPlugin implements ICloudPlugin{
	protected static Logger log = LoggerFactory.getLogger(KvmCloudPlugin.class);
	private String cloud_url="";
    private Map<String, String> headers = new HashMap<String, String>();
    public KvmCloudPlugin(String cloud_url){
    	headers.put("Content-Type", "application/json");
    	this.cloud_url=cloud_url;
    }
	@Override
	public int newVm(DeviceType type, StringBuffer vmid) {
		String url=cloud_url+"api?action=new_vm&type="+type.toString();
		HTTPHelperResult result = HTTPHelper.httpGet(url, headers);
		int ret=-1;
		if(200 == result.getCode()){
			InputMessage res=null;
			try {
				res=new InputMessage(true, result.getMsg());
				JsonNode jn=res.getData();
				vmid.append(jn.path("vmid").asText());
				ret=0;
			} catch (IOException e) {
				log.error("new vm exception");
			}
		}
		
		return ret;
	}
 
	@Override
	public int getIp(String vmid, StringBuffer ip) {
		String url=cloud_url+"api?action=get_ip&vmid="+vmid;
		HTTPHelperResult result = HTTPHelper.httpGet(url, headers);
		int ret=-1;
		if(200 == result.getCode()){
			InputMessage res=null;
			try {
				res=new InputMessage(true, result.getMsg());
				JsonNode jn=res.getData();
				ip.append(jn.path("ip").asText());
				if(ip.length()!=0){
					ret= 0;
				}
			} catch (IOException e) {
				log.error("new vm exception");
			}
		}
		
		return ret;
	}
	@Override
	public int powerOn(String vmid) {
		String url=cloud_url+"api?action=power_on&vmid="+vmid;
		HTTPHelperResult result = HTTPHelper.httpGet(url, headers);
		int ret=-1;
		if(200 == result.getCode()){
			InputMessage res=null;
			try {
				res=new InputMessage(true, result.getMsg());
				JsonNode jn=res.getData();
				ret=jn.path("code").asInt();				
			} catch (IOException e) {
				log.error("new vm exception");
			}
		}
		return ret;
	}

	@Override
	public int powerOff(String vmid) {
		String url=cloud_url+"api?action=power_off&vmid="+vmid;
		HTTPHelperResult result = HTTPHelper.httpGet(url, headers);
		int ret=-1;
		if(200 == result.getCode()){
			InputMessage res=null;
			try {
				res=new InputMessage(true, result.getMsg());
				JsonNode jn=res.getData();
				ret=jn.path("code").asInt();				
			} catch (IOException e) {
				log.error("new vm exception");
			}
		}
		return ret;
	}

	@Override
	public int powerReset(String vmid) {
		String url=cloud_url+"api?action=power_reset&vmid="+vmid;
		HTTPHelperResult result = HTTPHelper.httpGet(url, headers);
		int ret=-1;
		if(200 == result.getCode()){
			InputMessage res=null;
			try {
				res=new InputMessage(true, result.getMsg());
				JsonNode jn=res.getData();
				ret=jn.path("code").asInt();				
			} catch (IOException e) {
				log.error("new vm exception");
			}
		}
		return ret;
	}

	@Override
	public int getStatus(String vmid) {
		String url=cloud_url+"api?action=get_status&vmid="+vmid;
		HTTPHelperResult result = HTTPHelper.httpGet(url, headers);
		int ret=-1;
		if(200 == result.getCode()){
			InputMessage res=null;
			try {
				res=new InputMessage(true, result.getMsg());
				JsonNode jn=res.getData();
				ret=jn.path("status").asInt();				
			} catch (IOException e) {
				log.error("new vm exception");
			}
		}
		return ret;
	}
	@Override
	public int deleteVm(String vmid) {
		String url=cloud_url+"api?action=delete_vm&vmid="+vmid;
		HTTPHelperResult result = HTTPHelper.httpGet(url, headers);
		int ret=-1;
		if(200 == result.getCode()){
			InputMessage res=null;
			try {
				res=new InputMessage(true, result.getMsg());
				JsonNode jn=res.getData();
				ret=jn.path("code").asInt();				
			} catch (IOException e) {
				log.error("new vm exception");
			}
		}
		return ret;
	}

 

	 
}
