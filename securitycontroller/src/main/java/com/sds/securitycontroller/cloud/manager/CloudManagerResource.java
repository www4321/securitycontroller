package com.sds.securitycontroller.cloud.manager;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.utils.OutputMessage;

public class CloudManagerResource extends ServerResource {
	protected static Logger log = LoggerFactory.getLogger(CloudManagerResource.class);
	OutputMessage response = null;
	ICloudAgentService cloudmanager = null;
	@Override  
    public void doInit() {    
        cloudmanager = 
                (ICloudAgentService)getContext().getAttributes().
                get(ICloudAgentService.class.getCanonicalName());
    	response=new OutputMessage(true, this);
 
    }  
	
	
	@Get("json")
    public Object handleRequest() {
        String action = this.getQueryValue("action");
        action=null == action?"":action.toLowerCase();
        String vmid = "";
        StringBuffer sbvmid=new StringBuffer();
        StringBuffer sbip=new StringBuffer();
        StringBuffer sbstatus=new StringBuffer();
        String type = "";
        int ret = 0;
                
        switch(action){
        case "newvm":
        	type = this.getQueryValue("type");        
        	if(null == type || null == vmid){
        		break;
        	}
        	ret = cloudmanager.newVm(DeviceType.valueOf(type.toUpperCase()),sbvmid);
        	if(-1 == ret){
        		response.setResult(404,404,"apply for new vm failed");
        	}else{
        		response.setResult(200,200,"vmid",sbvmid.toString());
        	}
        	
        	break;
        case "poweron":
        	vmid = this.getQueryValue("vmid");
        	if(cloudmanager.powerOn(vmid)){
        		response.setResult(200,200,"start vm ok");
        	}else{
        		response.setResult(404,404,"start vm filed");
        	}
        	
        	break;
        case "poweroff":
        	vmid = this.getQueryValue("vmid");
        	if(cloudmanager.powerOff(vmid)){
        		response.setResult(200,200,"stopVm vm ok");
        	}else{
        		response.setResult(404,404,"stopVm vm filed");
        	}
        	break;
        case "powerreset":
        	vmid = this.getQueryValue("vmid");
           	if(cloudmanager.powerReset(vmid)){
        		response.setResult(200,200,"restartVm vm ok");
        	}else{
        		response.setResult(404,404,"restartVm vm filed");
        	}
        	break;
        case "getstatus"://info from remote kvm
        	vmid = this.getQueryValue("vmid");
        	ret = cloudmanager.getStatus(vmid);
        	if(2 == ret){
        		response.setResult(200,200,"status","running");
        	}else if(3 == ret){
        		response.setResult(200,200,"status","stopped");
        	}else{
        		response.setResult(404,404,"error");
        	}
        	break;
        case "getdevstatus":
        	vmid = this.getQueryValue("vmid");
        	ret = cloudmanager.getDevStatus(vmid, sbstatus);
        	if(0 == ret){
        		response.setResult(200,200,"status",sbstatus.toString());        	
        	}else{
        		response.setResult(404,404,"error");
        	}
        	break;
        case "setstatus":
        	vmid = this.getQueryValue("vmid");
        	String status = this.getQueryValue("status");
        	if(cloudmanager.setDevStatus(vmid,status)){
        		response.setResult(200,200,"ok");
        	}else{
        		response.setResult(404,404,"failed");
        	}
        	break;
        case "newdev":
        	type = this.getQueryValue("type");        
        	if(null == type || null == vmid){
        		break;
        	}        	
        	if(cloudmanager.newDev(DeviceType.valueOf(type.toUpperCase()))){
        		response.setResult(200,200,"request send");        		
        	}else{
        		response.setResult(404,404,"apply for new vm failed");
        	}        	
        	break;
        case "getip":
        	vmid = this.getQueryValue("vmid");         
        	if(cloudmanager.getIp(vmid, sbip)){
        		response.setResult(200,200,"ip",sbip.toString());
        	}else{
        		response.setResult(404,404,"failed");
        	}
        	break;
        case "delvm":
        	vmid = this.getQueryValue("vmid");
           	if(0 == cloudmanager.delVm(vmid)){
        		response.setResult(200,200,"delVm vm ok");
        	}else{
        		response.setResult(404,404,"delVm vm filed");
        	}
        	break;
        default:
        	break;
        }
        
        return response.toString();
    }
	
  
}
 