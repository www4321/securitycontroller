package com.sds.securitycontroller.www1234.servicefunction.manager;

import java.io.IOException;
import java.util.Iterator;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;
import com.sds.securitycontroller.www1234.servicefunction.ServiceFunctionDevice;
import com.sds.securitycontroller.www1234.sfc.ServiceFunction;

public class ServiceFunctionDeviceResource extends ServerResource {
	
	protected IServiceFunctionService serviceFunctionService = null;
	protected static Logger log = LoggerFactory.getLogger(ServiceFunctionDeviceResource.class);
	@Override
	protected void doInit() throws ResourceException {
		this.serviceFunctionService = (IServiceFunctionService)getContext().getAttributes().get(IServiceFunctionService.class.getCanonicalName());
	}
	
	/**
	 * [{
        "type": FIREWALL,
        "in_ip": 10.0.0.1,
        "in_mac": 00:00:00:00:00:01,
        "out_ip": 10.0.0.2,
        "out_mac": 00:00:00:00:00:02,
        }]
     *   Json arrary
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 */
	@Post
	public String addDevice(String fmjson) throws JsonProcessingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(fmjson);
        //log.info("is array..............."+root.isArray());
        Iterator<JsonNode> eachRoot = root.iterator();
        JsonNode rootNode = null;
        NodePortTuple switchPort1 = null;
        NodePortTuple switchPort2 = null;
        while(eachRoot.hasNext()){
        	rootNode = eachRoot.next();
        	String in_mac = (rootNode.path("in_mac").isMissingNode())?null:rootNode.path("in_mac").asText();
            String out_mac = (rootNode.path("out_mac").isMissingNode())?null:rootNode.path("out_mac").asText();
            String in_ip = (rootNode.path("in_ip").isMissingNode())?null:rootNode.path("in_ip").asText();
            String out_ip = (rootNode.path("out_ip").isMissingNode())?null:rootNode.path("out_ip").asText();
            String type = (rootNode.path("type").isMissingNode())?null:rootNode.path("type").asText();
            ServiceFunction sf = null;
            switch(type){
            case "FIREWALL":
            	sf = ServiceFunction.FIREWALL;break;
            case "DDOS":
            	sf = ServiceFunction.DDOS;break;
            case "IPS":
            	sf = ServiceFunction.IPS;break;
            case "WAF":
            	sf = ServiceFunction.WAF;break;
            }
            //安全设备仅有一个端口时
            if(out_ip==null||out_ip=="")
            	out_ip = in_ip;
            if(out_mac==null||out_mac=="")
            	out_mac = in_mac;
            switchPort1 = serviceFunctionService.getSwitchPort(in_mac);
            switchPort2 = serviceFunctionService.getSwitchPort(out_mac);
            if(switchPort1 == null || switchPort2 == null)
            	return "\"result\":\"fail\"";
            //log.info(switchPort1.toString());
            //log.info(switchPort2.toString());
            ServiceFunctionDevice sfDevice=new ServiceFunctionDevice(sf, in_ip,in_mac,out_ip,out_mac,switchPort1,switchPort2) ;
            //log.info(sfDevice.toString());
            serviceFunctionService.addSFDevice(sfDevice);
        }
        return "\"result\":\"successful\"";
	}
	@Get
	public String getStringMethod(){
		log.info(serviceFunctionService.getSwitchPort("00:00:00:00:00:01").toString());
		return "www1234";
	}
		
}