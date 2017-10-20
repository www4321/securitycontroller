package com.sds.securitycontroller.SecurityDeviceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;




import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.TaskManager.IMyTaskManagerService;
import com.sds.securitycontroller.utils.HTTPUtils;


public class DeviceRealtimeInfoResource extends ServerResource{
    private IMyTaskManagerService orderManager=null;

    private IMySecurityDeviceManagerService deviceManger=null;
    
   // protected IStorageSourceService storageSource;
    
	@Override  
   public void doInit() {    
		orderManager = 
               (IMyTaskManagerService)getContext().getAttributes().
               get(IMyTaskManagerService.class.getCanonicalName());    

		deviceManger=
				(IMySecurityDeviceManagerService)getContext().getAttributes().
				get(IMySecurityDeviceManagerService.class.getCanonicalName());
	
	
	}  
    @Get
    public String handleGetRequest(String fmJson) throws JsonProcessingException, IOException{
    	String jsonString="{"+"\""+"device"+"\""+":"+"[";
    	
    	List<MySecurityDevice> deviceList=deviceManger.getList();
    	float[] load=new float[deviceList.size()];
    	int i=0;
//         for(MySecurityDevice device:deviceList){
//        	 System.out.println("("+device.getIp()+","+device.getName()+","+device.getType()+")");
//         }
         
  		 Map<String, String> headers=new HashMap<String, String>();
 		 headers.put("Content-Type","json");        	 
         for(MySecurityDevice device:deviceList){
        		 String scannerURL="http://"+device.getIp()+":8010/scan/nikto/load";
        		 ObjectMapper mapper = new ObjectMapper();
        		 String loadJsonString=HTTPUtils.httpGet(scannerURL, headers);
                 JsonNode rootNode = mapper.readTree(loadJsonString);
                 String cpu = (rootNode.path("cpu").isMissingNode())?null:rootNode.path("cpu").asText();
                 String mem = (rootNode.path("mem").isMissingNode())?null:rootNode.path("mem").asText();
                 load[i]=Float.parseFloat(cpu)*0.25f+Float.parseFloat(mem)*0.25f;
                 i++;
         }
         int j=0;
	    for(MySecurityDevice device:deviceList){
	    	   	
   			jsonString+="{"+"\""+"Devicename"+"\""+":"+"\""+device.getName()+"\""+","+
   	    			"\""+"Devicetype"+"\""+":"+"\""+device.getType()+"\""+","+
   	    			"\""+"DeviceIP"+"\""+":"+"\""+device.getIp()+"\""+","+
   	    			"\""+"load"+"\""+":"+"\""+load[j]+"\""+
   	    			"}"+",";
	   			j++;
	   	}
     	jsonString=jsonString.substring(0,jsonString.length()-1);
     	jsonString+="]"+"}";
    	return jsonString;
    	
    }

}
