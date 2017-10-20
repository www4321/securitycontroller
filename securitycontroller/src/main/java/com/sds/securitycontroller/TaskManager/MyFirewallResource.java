package com.sds.securitycontroller.TaskManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
















import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.SecurityDeviceManager.IMySecurityDeviceManagerService;
import com.sds.securitycontroller.SecurityDeviceManager.MySecurityDevice;
import com.sds.securitycontroller.utils.HTTPUtils;
import com.sds.securitycontroller.utils.IJsonable;



public class MyFirewallResource extends ServerResource implements IJsonable,Runnable{
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
            	 
            	 System.out.println("接收到调用firewall订单的时间为："+new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
            	 System.out.println("receive a post order"+fmjson);
                 String ip=null;
                 String type=null;
                 String name=null;
                 MyFirewallOrder order=new MyFirewallOrder();
                 order=decodeJson(fmjson);

                 System.out.println("得到订单后");
   
          		 Map<String, String> headers=new HashMap<String, String>();
         		 headers.put("Content-Type","application/json");
         		 System.out.println(order.getType());
                 if(order.getType().equals("firewall")){
                	 System.out.println("取出设备类型为fireawall的设备");
                	 List<MySecurityDevice> scanDeviceList=new ArrayList<MySecurityDevice>();
                	 List<MySecurityDevice> deviceList=deviceManger.getList();
                	 for(MySecurityDevice device:deviceList){
                		 if(device.getType().equals("firewall")){
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
                     System.out.println("所有firewall设备列表：");
                     for(MySecurityDevice device:deviceList){
                    	 System.out.println("("+device.getIp()+","+device.getName()+","+device.getType()+","+device.getLoad()+")");
                     }
                	 System.out.println("设备名字为："+name+"的负载最小"+"选择设备"+name+"下发任务");
                 }

                String firewallURL="http://"+ip+":5001/";
            	String result="{"+"\""+"orderID"+"\""+":"+order.getId()+","+"\""+"Devicename"+"\""+":"+"\""+name+"\""+","+
 	   	    			"\""+"Devicetype"+"\""+":"+"\""+type+"\""+","+
 	   	    			"\""+"DeviceIP"+"\""+":"+"\""+ip+"\""+
 	   	    			"}";
            	System.out.println(firewallURL);
            	Map<String,String> returnResult = new HashMap<String,String>();
            	returnResult.put("orderId", order.getId());
            	returnResult.put("src_ip", order.getSrc_ip());
            	returnResult.put("dst_ip",order.getDst_ip());
            	String contentJson=JSONObject.toJSONString(returnResult);
            	System.out.println(contentJson);
            	//下发任务
            	String resultscan=HTTPUtils.httpPost(firewallURL, contentJson,headers);            	
            	

            	System.out.println("Firewall return:"+resultscan);

             	return result;                    
             }
             
        

             public  MyFirewallOrder decodeJson(String fmJson) throws IOException {
                 ObjectMapper mapper = new ObjectMapper();
                 JsonNode rootNode = mapper.readTree(fmJson);           
                 String type = (rootNode.path("type").isMissingNode())?null:rootNode.path("type").asText();
                 String src_ip = (rootNode.path("config_data").isMissingNode())?null:rootNode.path("config_data").path("src_ip").asText(); 
                 String dst_ip = (rootNode.path("config_data").isMissingNode())?null:rootNode.path("config_data").path("dst_ip").asText();
                 String orderId = (rootNode.path("orderId").isMissingNode())?null:rootNode.path("orderId").asText();
                 System.out.println("orderId:"+orderId+" type:"+type+" config_data:"+src_ip+" : " + dst_ip);
                 MyFirewallOrder order=new MyFirewallOrder(orderId,type,src_ip,dst_ip) ;
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
			public static void main(String args[]) throws JsonProcessingException, IOException{
				String data = "{\"orderId\": \"3edacbaa\", \"config_data\": {\"src_ip\": \"10.103.238.79\", \"dst_ip\": \"10.102.26.115:80\"}, \"type\": \"firewall\"}";
				System.out.println(data);
				ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(data); 
                System.out.println(rootNode);
                String config_data = (rootNode.path("config_data").isMissingNode())?null:rootNode.path("config_data").path("src_ip").asText();
                System.out.println(config_data);
			}
}

