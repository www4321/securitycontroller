package com.sds.securitycontroller.TaskManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;



import com.fasterxml.jackson.core.JsonGenerationException;
import com.sds.securitycontroller.utils.HTTPUtils;

public class NorthReqResource extends ServerResource{
	
    @Get("json")
    public String handleGet() throws JsonGenerationException, IOException{ 
//		Map<String, String> headers=new HashMap<String, String>();
//		headers.put("Content-Type","application/json");
//		String jasonResp=HTTPUtils.httpGet("http://10.102.25.137:5000/",headers);
//		String[] resp=jasonResp.split(",");
//		String ip=resp[1].trim().replace('"',' ').trim();
//		String username=resp[2].trim().replace('"',' ').trim();
//		String passwd=resp[3].trim().replace('"',' ').trim();
//		String cpu=resp[1].trim().replace('"',' ').trim();
//		String[] cpu_usage=new String[2];
//		cpu_usage[0]=resp[5].trim().replace('"',' ').trim();
//		cpu_usage[1]=resp[15].trim().replace('"',' ').trim();
//		String[] memory_usage=new String[2];
//		memory_usage[0]=resp[7].trim().replace('"',' ').trim();
//		memory_usage[1]=resp[17].trim().replace('"',' ').trim();
		
		
//		System.out.println("返回"+jasonResp);
//		System.out.println(resp[0]);
//		System.out.println(resp[1]);
//		System.out.println(resp[2]);
//		System.out.println(resp[3]);
//		System.out.println(resp[4]);
//		System.out.println(resp[5]);
//		System.out.println(resp[6]);
//		System.out.println(resp[8]);
//		System.out.println(resp[9]);
//		System.out.println(resp[10]);
//		System.out.println(resp[11]);
//		System.out.println(resp[12]);
//		System.out.println(resp[13]);
//		System.out.println(resp[14]);
//		System.out.println(resp[15]);
//        String json="{"+"'"+"cpu0"+":"+"'"+cpu_usage[0]+"'"+","
//        		+"'"+"cpu1"+":"+"'"+cpu_usage[1]+"'"+","
//        		+"'"+"memory0"+":"+"'"+memory_usage[0]+"'"+","
//        		+"'"+"memory1"+":"+"'"+memory_usage[1]+"'"+","
//        		+"}";
		
//		String json="{"+"\""+"cpu0"+"\"" +":"+ "\""+cpu_usage[0]+"\""+","+
//				"\""+"cpu1"+"\"" +":"+ "\""+cpu_usage[1]+"\""+","+
//				"\""+"memory0"+"\"" +":"+ "\""+memory_usage[0]+"\""+","+
//				"\""+"memory1"+"\"" +":"+ "\""+memory_usage[1]+"\""+","+
//               "}";
   	
//    	String json="{"+"\""+"cpu0"+"\"" +":"+ "\""+30+"\""+","+
//    			"\""+"cpu1"+"\"" +":"+ "\""+40+"\""+","+
//    			"\""+"m0"+"\"" +":"+ "\""+40+"\""+","+
//    			"\""+"m1"+"\"" +":"+ "\""+50+"\""+
//    			"}";
    	
    	
//    	String json="{"+"\""+"device1"+"\"" +":"+"{"+ "\""+"ip1"+"\""+":"+"\""+"ip1"+"\""+","+"\""+"cpu1"+"\""+":"+"\""+"cpu1"+"\""+"}"+","+
//    			"\""+"device1"+"\"" +":"+"{"+ "\""+"ip1"+"\""+":"+"\""+"ip1"+"\""+","+"\""+"cpu1"+"\""+":"+"\""+"cpu1"+"\""+"}"+
//		"}";
//    	String load1,load2;
//		Map<String, String> headers=new HashMap<String, String>();
//		headers.put("Content-Type","application/json");
//		String jasonResp=HTTPUtils.httpGet("http://10.103.24.102:5000",headers);
//		String[] resp=jasonResp.split(",");
		
//		System.out.println(resp[0]);
//		System.out.println(resp[1]);
//		System.out.println(resp[2]);
//		System.out.println(resp[3]);
//		System.out.println(resp[4]);
//		System.out.println(resp[5]);
//		System.out.println(resp[6]);
//		System.out.println(resp[7]);
//		System.out.println(resp[8]);
//		System.out.println(resp[9]);
//		System.out.println(resp[10]);
//		System.out.println(resp[11]);
//		System.out.println(resp[12]);
//		System.out.println(resp[13]);
//		
		
//		String ip1=resp[1].trim().replace('"',' ').trim();
//		String username1=resp[2].trim().replace('"',' ').trim();
//		String passwd1=resp[3].trim().replace('"',' ').trim();
//		String cpu_usage1=resp[5].trim();
//		String memory_usage1=resp[7].trim();
////		String hardMemory_usage1=resp[9].trim();
//		
//		String ip2=resp[11].trim().replace('"',' ').trim();
//		String username2=resp[12].trim().replace('"',' ').trim();
//		String passwd2=resp[13].trim().replace('"',' ').trim();
//		String cpu_usage2=resp[15].trim();
//		String memory_usage2=resp[17].trim();
////		String hardMemory_usage2=resp[21].trim();
//		
		float[] load=new float[2];
//		load[0]=(int)((Integer.parseInt(cpu_usage1)+Integer.parseInt(memory_usage1)*0.4)/1.4);
//		load[1]=(int)((Integer.parseInt(cpu_usage2)+Integer.parseInt(memory_usage2)*0.4)/1.4);
    	
//    	String json="{"+"\""+"load1"+"\""+":"+"\""+load[0]+"\""+","+
//    			"\""+"load2"+"\""+":"+"\""+load[1]+"\""+
//    			"}";
		System.out.println("收到北向设备负载请求");
		Map<String, String> headers=new HashMap<String, String>();
		headers.put("Content-Type","application/json");

		String jasonResp1=HTTPUtils.httpGet("http://10.103.26.166:8888/sc/getScannerInfo",headers);
		System.out.println("第一台设备成功访问");
		String jasonResp2=HTTPUtils.httpGet("http://10.103.27.10:8888/sc/getScannerInfo",headers);
		System.out.println("第二台设备成功访问");

		String[] resp1=jasonResp1.split(",");
		String[] resp2=jasonResp2.split(",");
		System.out.println(resp1[0]);
		System.out.println(resp1[1]);
		System.out.println(resp1[2]);
		System.out.println(resp2[0]);
		System.out.println(resp2[1]);
		System.out.println(resp2[2]);
		load[0]=Float.parseFloat(resp1[0])*0.2f+Float.parseFloat(resp1[1])*0.8f;
		load[1]=Float.parseFloat(resp2[0])*0.2f+Float.parseFloat(resp2[1])*0.8f;
		System.out.println("第一台的负载为："+load[0]);
		System.out.println("第二台的负载为："+load[1]);
		
//		load[0]=(int)((Integer.parseInt(resp1[0])+Integer.parseInt(resp1[1])*0.4)/1.4);
//		load[1]=(int)((Integer.parseInt(resp2[0])+Integer.parseInt(resp2[1])*0.4)/1.4);
    	String json="{"+"\""+"load1"+"\""+":"+"\""+load[0]+"\""+","+
		"\""+"load2"+"\""+":"+"\""+load[1]+"\""+
		"}";

    
        return json;
    }

}
