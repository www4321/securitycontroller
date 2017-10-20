package com.sds.securitycontroller.TaskManager;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.SecurityDeviceManager.IMySecurityDeviceManagerService;
import com.sds.securitycontroller.SecurityDeviceManager.MySecurityDevice;
import com.sds.securitycontroller.utils.HTTPUtils;
import com.sds.securitycontroller.utils.IJsonable;




public class MyWafResource extends ServerResource implements IJsonable{
//             private IEventManagerService eventManager;
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
             
                         
             
             @Get("json")
             public Object handleGet() throws JsonGenerationException, IOException{ 
                 System.out.println("testdone");
                 MyTask ot=new MyTask();
                 Log.debug("receive a get request");
                 return ot.toString();
             }
             @Post
             public String handlePost(String fmjson) throws IOException{
            	 
            	 System.out.println("收到北向waf防护任务的时间为："+new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
            	 System.out.println("receive a post waforder"+fmjson);
            	 
            	 MyWafOrder order=this.decodeJson(fmjson);
            	 String type=null;
            	 String ip=null;
            	 String name=null;



  
                 
          		 Map<String, String> headers=new HashMap<String, String>();
         		 headers.put("Content-Type","application/json");
//         		 System.out.println(order.getType());
                 if(order.getType().equals("waf")){
                	 System.out.println("取出设备类型为waf的设备");
                	 List<MySecurityDevice> scanDeviceList=new ArrayList<MySecurityDevice>();
                	 List<MySecurityDevice> deviceList=deviceManger.getList();
                	 for(MySecurityDevice device:deviceList){
                		 if(device.getType().equals("waf")){
                			 scanDeviceList.add(device);
                		 }
                	 }
                	
                	 float min=100f;
                	 
                	 for(MySecurityDevice device:scanDeviceList){
                		 String scannerURL="http://"+device.getIp()+":5001/";
                		
                		 ObjectMapper mapper = new ObjectMapper();
                		 String loadJsonString=HTTPUtils.httpGet(scannerURL, headers);
                		 System.out.println(loadJsonString);
                         JsonNode rootNode = mapper.readTree(loadJsonString);
                         String cpu = (rootNode.path("cpu").isMissingNode())?null:rootNode.path("cpu").asText();
                         String mem = (rootNode.path("mem").isMissingNode())?null:rootNode.path("mem").asText();
                         System.out.println(cpu);
                         System.out.println(mem);
                         float load=Float.parseFloat(cpu)*0.5f+Float.parseFloat(mem)*0.5f;
                         device.setLoad(load);
                	 }
                	 for(MySecurityDevice device:scanDeviceList){

                		 if(device.getLoad()<min){
                			 min=device.getLoad();
                			 ip=device.getIp();
                			 name=device.getName();
                			 type=device.getType();
                		 }
                	 }
                     System.out.println("所有waf设备列表：");
                     for(MySecurityDevice device:deviceList){
                    	 System.out.println("("+device.getIp()+","+device.getName()+","+device.getType()+","+device.getLoad()+")");
                     }
                	 System.out.println("设备名字为："+name+"的负载最小"+"选择设备"+name+"下发任务");
                 }else {
                	 
                 }

//           	String scannerURL="http://"+ip+":5000/scan/nikto?"+order.getHost()+"|"+order.getId(); 
                String scannerURL="http://"+ip+":5001/";
            	String result="{"+"\""+"orderID"+"\""+":"+order.getId()+","+"\""+"Devicename"+"\""+":"+"\""+name+"\""+","+
 	   	    			"\""+"Devicetype"+"\""+":"+"\""+type+"\""+","+
 	   	    			"\""+"DeviceIP"+"\""+":"+"\""+ip+"\""+
 	   	    			"}";
            	System.out.println(scannerURL);
            	System.out.println("下发waf策略的时间为："+new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
//            	HTTPUtils.httpGet(scannerURL, headers);
            	String contentJson="{"+"\""+"ip"+"\""+":"+"\""+order.getIp()+"\""+","+
            			"\""+"siteName"+"\""+":"+"\""+order.getSiteName()+"\""+","+
            			"\""+"orderId"+"\""+":"+"\""+order.getId()+"\""+","+
            			"\""+"rules"+"\""+":"+order.getRules()+
            			"}";
            	System.out.println(contentJson);
            	String resultscan=HTTPUtils.httpPost(scannerURL, contentJson,headers);            	
            	 System.out.println("收到waf返回结果的时间为："+new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
//             	 String liuyepingString="{"+"\""+"orderID"+"\""+":"+"12345"+","+"\""+"Devicename"+"\""+":"+"\""+"werw"+"\""+","+
// 	   	    			"\""+"Devicetype"+"\""+":"+"\""+"3r3r"+"\""+","+
// 	   	    			"\""+"DeviceIP"+"\""+":"+"\""+"24525"+"\""+
// 	   	    			"}";
            	System.out.println(resultscan);

             	return result;                    
             }
             
        

             public  MyWafOrder decodeJson(String fmJson) throws IOException {
                 ObjectMapper mapper = new ObjectMapper();
                 JsonNode rootNode = mapper.readTree(fmJson);           
                 String type = (rootNode.path("type").isMissingNode())?null:rootNode.path("type").asText();
                 String siteName = (rootNode.path("siteName").isMissingNode())?null:rootNode.path("siteName").asText(); 
                 String ip = (rootNode.path("ip").isMissingNode())?null:rootNode.path("ip").asText(); 
                 String rules = (rootNode.path("rules").isMissingNode())?null:rootNode.path("rules").asText(); 
                 String orderId = (rootNode.path("orderId").isMissingNode())?null:rootNode.path("orderId").asText();
                 MyWafOrder order=new MyWafOrder(orderId,type, ip,siteName,rules) ;
                 return order;
             }
             

            @Override
            public String toJsonString() throws JsonGenerationException,
                    IOException {
                // TODO Auto-generated method stub
                
                return null;
            }



				
			
			


	
}
