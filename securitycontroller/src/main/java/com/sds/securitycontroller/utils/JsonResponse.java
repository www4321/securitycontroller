package com.sds.securitycontroller.utils;

import java.io.IOException; 
import java.util.ArrayList;
import java.util.List;

 
 
public class JsonResponse {
	private  int code=0;
	private  String head="";
	private  String data="";
	
	public JsonResponse(){
		
	}
	
    public JsonResponse(int code, String head, String data){ 
    	this.setCode(code);
    	this.setHead(head);
    	this.setData(data);    
    }
    @Override
	public String toString(){
    	if(code<0 || code >10000){
    		return "";
    	}
    	if(null == head){
    		return "";
    	}
    	if(null == data){
    		return "";
    	}
    	return "{\n\"OPT_STATUS\":"+Integer.valueOf(getCode())+",\n\"HEAD\":{"+getHead()+"},\n\"DATA\":{"+getData()+"}\n}";
    }
    
    public void buildData(String type,  IJsonable obj) throws IOException{
		if(null == obj){
			this.data="";
			return;
		}
		List<IJsonable> list = new ArrayList<IJsonable>();
		list.add(obj);
		this.data = buildDataImpl(type,list);		
	}
    
    public void buildData(String type, List<? extends IJsonable> list) throws IOException{
		this.data = buildDataImpl(type,list);
	}
	//---private function---
    private String buildDataImpl(String type, List<? extends IJsonable> list) throws IOException{ 
 
    	if(null == list || list.isEmpty()){    		
    		return "";
    	}	
    	
    	String json="\""+type+"\":";
    	if (list.size() > 0) {
    		json += "[";    		    		
			for (IJsonable obj : list) {			 
				json = json+obj.toJsonString()+",";
				
			}
			if(json.endsWith(",")){
				json=json.substring(0, json.length()-1);
			}			
    		json += "]";
    	}else{    	
    		json += ((IJsonable)list.get(0)).toJsonString();    
    	} 
		return json;
	}
    //the data field will be a single string: "RESULT":"XXXXXXXX"
    public void setMessage(int code, String data){
    	this.setCode(code);
    	this.setData("\"RESULT\":\""+data+"\"");
    }
    public void setMessage(int code, String type, String data){
    	this.setCode(code);
    	this.setData("\""+type+"\":\""+data+"\"");
    }
    
    
    //properties
	public int getCode() {
		return code;
	}
	public String getHead() {
		return head;
	}
	public String getData() {
		return data;
	}
	public void setCode(int code) {   	
		this.code = code;
	}
	public void setHead(String head) { 
		this.head = head;
	}
	public void setData(String data) { 
		this.data = data;
	}

    
    
    
}
