package com.sds.securitycontroller.TaskManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonGenerationException;

public class MyTaskResultResource extends ServerResource{
	private IMyTaskManagerService orderManager;
	String orderId;
//    @Get("json")
//    public Object handleGet() throws JsonGenerationException, IOException{ 
//        System.out.println("testdone");
//        MyOrder ot=new MyOrder();
//        Log.debug("receive a get request");
//        return ot.toString();
//    }
 	@Override  
    public void doInit() {    
		orderManager = 
                (IMyTaskManagerService)getContext().getAttributes().
                get(IMyTaskManagerService.class.getCanonicalName());   
		        this.orderId = (String) getRequestAttributes().get("orderId");}  
    @Get("json")
    public String handleGet() throws JsonGenerationException, IOException{ 
    	String filepathString="D:/my_eclpse/securitycontroller/scanResult/"+this.orderId+".xml";
    	File file=new File(filepathString);
    	BufferedReader bReader;
    	StringBuilder sb=new StringBuilder();
    	String string;
    	bReader=new BufferedReader(new FileReader(file));
    	while((string=bReader.readLine())!=null){
    		sb.append(string);
    		sb.append('\n');
    	}
    	bReader.close();
    	
    	return sb.toString();
    	

    	
    }
    @Post
    public void handlePost(String fmjson) throws IOException{
		String[] results=fmjson.split("wwaannggzzeellaanngg");
		System.out.println("收到结果");
		System.out.println(results[1]);
		Map<String,Object> values1=new HashMap<String, Object>();
		values1.put("status", "1");
		orderManager.updateMyOrder(results[0], values1);
		Map<String,Object> values2=new HashMap<String, Object>();
		values2.put("scanningTime", results[2]);
		orderManager.updateMyOrder(results[0], values2);
		                                        
    }
}
