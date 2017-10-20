/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.core.internal;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import com.esotericsoftware.minlog.Log;



public class GlobalConfig {
	private static GlobalConfig config = new GlobalConfig();

	public static GlobalConfig getInstance(){
		return config;
	}
	
    public String hostName = "localhost";
    
    public String ampqHost = "controller.research.intra.sds.com";
    public String ampqPort = "5672";
    public String ampqUserName = "guest";
    public String ampqPassword = "guest";
    public String topicExchangeName = "topic_securitycontroller";
    public String fanoutExchangeName = "fanout_securitycontroller";
    public String zooKeeperHost = "127.0.0.1:2181";
    public String ncHost = "http://nc.research.intra.sds.com:8081";
    public String iaasHost = "http://os.research.intra.sds.com";
    public String srcDir = "/usr/src/securitycontroller";
    public String myip = "127.0.0.1";
    
    public void loadConfig(String configName){
		Field[] fs = this.getClass().getDeclaredFields();
    	BufferedReader br;
    	String         line;

    	try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(configName), Charset.forName("UTF-8")));
	    	while ((line = br.readLine()) != null) {
	    		if(line.startsWith("#"))
	    			continue;
	    		String[] s = line.split("=", 2);
	    		if(s == null || s.length!=2)
	    			continue;
	    		
	    		String key = s[0].trim();
	    		String value = s[1].trim();
	    		
	    		for(Field f : fs){
	    			if(!key.equals(f.getName()))
	    				continue;
	    			Type type = f.getType();
	    			if(type == String.class)
	    				f.set(this, value);
	    			else if(type == Integer.class){
	    				f.set(this, Integer.parseInt(value));
	    				break;
	    			}
	    			else if(type == Long.class){
	    				f.set(this, Long.parseLong(value));
	    				break;
	    			}
	    			else if(type == Float.class){
	    				f.set(this, Float.parseFloat(value));
	    				break;
	    			}
	    			else{
	    				Log.error("Unresolved type :"+ type);
	    				break;
	    			}
	    		}
	    		
	    	}
	
	    	br.close();
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	br = null;
    }
}
