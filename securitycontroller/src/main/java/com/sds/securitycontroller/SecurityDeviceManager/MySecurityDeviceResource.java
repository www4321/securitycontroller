package com.sds.securitycontroller.SecurityDeviceManager;

import java.io.IOException;
import java.util.List;


import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.utils.InputMessage;
import com.sds.securitycontroller.utils.OutputMessage;
public class MySecurityDeviceResource extends ServerResource{
	protected static Logger log = LoggerFactory.getLogger(MySecurityDeviceResource.class);
	String devId = null;
	MySecurityDevice dev = null;
	InputMessage request = null;
	OutputMessage response = null;
	
    IMySecurityDeviceManagerService devicemanager = null;
    @Override
	public void doInit() {    
		this.devicemanager = 
	            (IMySecurityDeviceManagerService)getContext().getAttributes().
	            get(IMySecurityDeviceManagerService.class.getCanonicalName());
    }
    @Get
    public String handleGetRequest(String fmJson){
    	System.out.println("收到设备列表请求");
    	String jsonString;
    	jsonString="{"+"\""+"device"+"\""+":"+"[";
    	

    	List<MySecurityDevice> list=devicemanager.getList();
//    	for(ScanDevice device:list){
//   	
//   			jsonString+="{"+"\""+"Devicename"+"\""+":"+"\""+device.getName()+"\""+","+
//   	    			"\""+"Devicetype"+"\""+":"+"\""+device.getType()+"\""+","+
//   	    			"\""+"DeviceIP"+"\""+":"+"\""+device.getIp()+"\""+","+
//   	    			"\""+"Disk"+"\""+":"+"\""+device.getDisk()+"\""+","+
//   	    			"\""+"memory"+"\""+":"+"\""+device.getMemory()+"\""+
//   	    			"}"+",";
//   			
//   		}
//       	jsonString=jsonString.substring(0,jsonString.length()-1);
//
    	    for(MySecurityDevice device:list)
    	    {
    	    	System.out.println("("+device.getIp()+","+device.getName()+","+device.getType()+")");
    	    }
	    	for(MySecurityDevice device:list){
	   	
	   			jsonString+="{"+"\""+"Devicename"+"\""+":"+"\""+device.getName()+"\""+","+
	   	    			"\""+"Devicetype"+"\""+":"+"\""+device.getType()+"\""+","+
	   	    			"\""+"DeviceIP"+"\""+":"+"\""+device.getIp()+"\""+
	   	    			"}"+",";
	   			
	   		}
	       	
    	
//         	for(int i=0;i<=4;i++)
//         	{
//				jsonString+="{"+"\""+"Devicename"+"\""+":"+"\""+i+"\""+","+
//				"\""+"Devicetype"+"\""+":"+"\""+2*i+"\""+","+
//				"\""+"DeviceIP"+"\""+":"+"\""+3*i+"\""+","+
//				"\""+"Disk"+"\""+":"+"\""+4*i+"\""+","+
//				"\""+"memory"+"\""+":"+"\""+5*i+"\""+
//				"}"+",";
//         	}
         	
         	jsonString=jsonString.substring(0,jsonString.length()-1);
         	jsonString+="]"+"}";

    	return jsonString;
    }
    @Post
    public String handlePostRequest(String fmJson) throws JsonProcessingException, IOException {
    	System.out.println(fmJson);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(fmJson);           
        System.out.println("开始解析"); 
              
        String name = (rootNode.path("name").isMissingNode())?null:rootNode.path("name").asText();     
        String ip = (rootNode.path("ip").isMissingNode())?null:rootNode.path("ip").asText();
        System.out.println(ip);
        String type = (rootNode.path("type").isMissingNode())?null:rootNode.path("type").asText();
        MySecurityDevice device=new MySecurityDevice(name, ip, type) ;
        System.out.println("("+device.getIp()+","+device.getName()+","+device.getType()+")");
		devicemanager.addSecurityDevice(device);
    	return fmJson;
    	
    }
    public  MySecurityDevice decodeJson(String fmJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(fmJson);           

        System.out.println("开始解析"); 
            
        String name = (rootNode.path("name").isMissingNode())?null:rootNode.path("name").asText();     
        String ip = (rootNode.path("ip").isMissingNode())?null:rootNode.path("ip").asText();
        System.out.println(ip);
        String type = (rootNode.path("type").isMissingNode())?null:rootNode.path("type").asText();
        
        MySecurityDevice order=new MySecurityDevice(name, ip, type) ;
        System.out.println(order.toString()); 
        return order;
    }


}
