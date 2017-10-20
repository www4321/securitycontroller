package com.sds.securitycontroller.TaskManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
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



public class MyIDSResource extends ServerResource implements IJsonable,Runnable{
//             private IEventManagerService eventManager;
             private IMyTaskManagerService orderManager=null;

             private IMySecurityDeviceManagerService deviceManger=null;
             
         	@Override  
            public void doInit() {    
        		orderManager = (IMyTaskManagerService)getContext().getAttributes().get(IMyTaskManagerService.class.getCanonicalName());            		
        		deviceManger=(IMySecurityDeviceManagerService)getContext().getAttributes().get(IMySecurityDeviceManagerService.class.getCanonicalName());        	
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
            	 
            	 System.out.println("接收到调用IDS订单的时间为："+new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
            	 System.out.println("receive a post order"+fmjson);
                 String ip=null;
                 String type=null;
                 String name=null;
                 MyTask order=new MyTask();
                 order=decodeJson(fmjson);
                 orderManager.addOrder(order);
                 System.out.println("得到订单后");
   
          		 Map<String, String> headers=new HashMap<String, String>();
         		 headers.put("Content-Type","application/json");
         		 System.out.println(order.getType());
                 if(order.getType().equals("ids")){
                	 System.out.println("取出设备类型为IDS的设备");
                	 List<MySecurityDevice> scanDeviceList=new ArrayList<MySecurityDevice>();
                	 List<MySecurityDevice> deviceList=deviceManger.getList();
                	 for(MySecurityDevice device:deviceList){
                		 if(device.getType().equals("ids")){
                			 scanDeviceList.add(device);
                		 }
                	 }
                	
                	 float min=100f;
                	 
                	 for(MySecurityDevice device:scanDeviceList){
                		 String scannerURL="http://"+device.getIp()+":5000/";
                		
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
                     System.out.println("所有IDS设备列表：");
                     for(MySecurityDevice device:deviceList){
                    	 System.out.println("("+device.getIp()+","+device.getName()+","+device.getType()+","+device.getLoad()+")");
                     }
                	 System.out.println("设备名字为："+name+"的负载最小"+"选择设备"+name+"下发任务");
                 }

                String IDSURL="http://"+ip+":5000/";
            	String result="{"+"\""+"orderID"+"\""+":"+order.getId()+","+"\""+"Devicename"+"\""+":"+"\""+name+"\""+","+
 	   	    			"\""+"Devicetype"+"\""+":"+"\""+type+"\""+","+
 	   	    			"\""+"DeviceIP"+"\""+":"+"\""+ip+"\""+
 	   	    			"}";
            	System.out.println(IDSURL);
            	String contentJson="{"+"\""+"config_data"+"\""+":"+"\""+order.getHost()+"\""+","+
            			"\""+"orderId"+"\""+":"+"\""+order.getId()+"\""+
            			"}";
            	System.out.println(contentJson);
            	//下发任务
            	String resultids=HTTPUtils.httpPost(IDSURL, contentJson,headers);            	
            	System.out.println("收到告警结果的时间为："+new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
                Map<String,String> returnResult = new HashMap<String,String>();
            	ObjectMapper mapper  = new ObjectMapper();
            	JsonNode jsonNode = mapper.readTree(resultids);
            	if(jsonNode.isArray()){
            		Iterator<JsonNode> iter = jsonNode.iterator();
            		while(iter.hasNext()){
            			JsonNode json = iter.next();
            			String alert_type = json.path("type").isMissingNode()?null:json.path("type").toString();
            			String alert_detail = json.path("detail").isMissingNode()?null:json.path("detail").toString();
            			System.out.println("alert_type"+alert_type);
            			System.out.println("alert_detail"+alert_detail);
            			if(alert_type.equals("[\"Possible TCP DoS [**]\"]")){
            				String[] content = alert_detail.split(" -> ");
            				String[] source = content[0].split(":");
            				String dst = content[1];
//            				System.out.println("source:"+source[0].replace("[\"", ""));
//                			System.out.println("dst:"+dst.replace("\"]", ""));
                			returnResult.put("src_ip",source[0].replace("[\"", ""));
                			returnResult.put("dst_ip",dst.replace("\"]", ""));
            			}
            			
            		}
            	}
            	System.out.println("IDS alter message "+JSONObject.toJSONString(returnResult));
             	return JSONObject.toJSONString(returnResult);                   
             }

             public  MyTask decodeJson(String fmJson) throws IOException {
                 ObjectMapper mapper = new ObjectMapper();
                 JsonNode rootNode = mapper.readTree(fmJson); 
                 System.out.println("rootNode:"+rootNode);
                 String type = (rootNode.path("type").isMissingNode())?null:rootNode.path("type").asText();
                 String config_data = (rootNode.path("config_data").isMissingNode())?null:rootNode.path("config_data").asText();    
                 String orderId = (rootNode.path("orderId").isMissingNode())?null:rootNode.path("orderId").asText();
                 MyTask order=new MyTask(orderId,type, config_data) ;
                 System.out.println("orderId:"+orderId+" type:"+type+" config_data:"+config_data);
                 return order;
             }
             

            @Override
            public String toJsonString() throws JsonGenerationException,
                    IOException {
                // TODO Auto-generated method stub
                
                return null;
            }



			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
			
}

