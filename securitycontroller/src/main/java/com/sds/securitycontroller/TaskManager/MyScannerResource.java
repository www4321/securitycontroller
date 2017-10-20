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



public class MyScannerResource extends ServerResource implements IJsonable,Runnable{
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
            	 
            	 System.out.println("接收到北向扫描订单的时间为："+new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
            	 System.out.println("receive a post order"+fmjson);
                 String ip=null;
                 String type=null;
                 String name=null;
                 MyTask order=new MyTask();
//                 System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date()));
                 order=decodeJson(fmjson);
                 orderManager.addOrder(order);
                 System.out.println("得到订单后");
  
                 
          		 Map<String, String> headers=new HashMap<String, String>();
         		 headers.put("Content-Type","application/json");
         		 System.out.println(order.getType());
                 if(order.getType().equals("scanner")){
                	 System.out.println("取出设备类型为scanner的设备");
                	 List<MySecurityDevice> scanDeviceList=new ArrayList<MySecurityDevice>();
                	 List<MySecurityDevice> deviceList=deviceManger.getList();
                	 for(MySecurityDevice device:deviceList){
                		 if(device.getType().equals("scanner")){
                			 scanDeviceList.add(device);
                		 }
                	 }
                	
                	 float min=100f;
                	 
                	 for(MySecurityDevice device:scanDeviceList){
                		 String scannerURL="http://"+device.getIp()+":5002/";
                		
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
                     System.out.println("所有scanner设备列表：");
                     for(MySecurityDevice device:deviceList){
                    	 System.out.println("("+device.getIp()+","+device.getName()+","+device.getType()+","+device.getLoad()+")");
                     }
                	 System.out.println("设备名字为："+name+"的负载最小"+"选择设备"+name+"下发任务");
                 }else {
                	 
                 }

//           	String scannerURL="http://"+ip+":5002/scan/nikto?"+order.getHost()+"|"+order.getId(); 
                String scannerURL="http://"+ip+":5002/";
            	String result="{"+"\""+"orderID"+"\""+":"+order.getId()+","+"\""+"Devicename"+"\""+":"+"\""+name+"\""+","+
 	   	    			"\""+"Devicetype"+"\""+":"+"\""+type+"\""+","+
 	   	    			"\""+"DeviceIP"+"\""+":"+"\""+ip+"\""+
 	   	    			"}";
            	System.out.println(scannerURL);
            	System.out.println("向扫描器下发任务的时间为："+new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
//            	HTTPUtils.httpGet(scannerURL, headers);
            	String contentJson="{"+"\""+"url"+"\""+":"+"\""+order.getHost()+"\""+","+
            			"\""+"orderId"+"\""+":"+"\""+order.getId()+"\""+
            			"}";
            	System.out.println(contentJson);
            	//下发任务
            	String resultscan=HTTPUtils.httpPost(scannerURL, contentJson,headers);            	
            	 System.out.println("收到扫描结果的时间为："+new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
//             	 String liuyepingString="{"+"\""+"orderID"+"\""+":"+"12345"+","+"\""+"Devicename"+"\""+":"+"\""+"werw"+"\""+","+
// 	   	    			"\""+"Devicetype"+"\""+":"+"\""+"3r3r"+"\""+","+
// 	   	    			"\""+"DeviceIP"+"\""+":"+"\""+"24525"+"\""+
// 	   	    			"}";
            	System.out.println("扫描结果为："+resultscan);

             	return result;                    
             }
             
        

             public  MyTask decodeJson(String fmJson) throws IOException {
                 ObjectMapper mapper = new ObjectMapper();
                 JsonNode rootNode = mapper.readTree(fmJson);           
                 String type = (rootNode.path("type").isMissingNode())?null:rootNode.path("type").asText();
                 String target = (rootNode.path("target").isMissingNode())?null:rootNode.path("target").asText();    
                 String orderId = (rootNode.path("orderId").isMissingNode())?null:rootNode.path("orderId").asText();
                 MyTask order=new MyTask(orderId,type, target) ;
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
class computeTask implements Runnable{


	String result;
	String id;
	String host;
	String scannerIp;
	public  computeTask(String id,String host,String scannerIp) {
		this.id=id;
		this.host=host;
		this.scannerIp=scannerIp;

	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("计算任务线程已启动");
		Map<String, String> headers=new HashMap<String, String>();
		headers.put("Content-Type","json");
    	String contentJson="{"+"\""+"orderId"+"\""+":"+"\""+this.id+"\""+","+
    			"\""+"targetURL"+"\""+":"+"\""+this.host+"\""+
    			"}";
    	String scannerURL="http://"+scannerIp+":8888/sc/getScannerInfo";
  
		String result=HTTPUtils.httpPost(scannerURL, contentJson, headers);	

	}
	
}
