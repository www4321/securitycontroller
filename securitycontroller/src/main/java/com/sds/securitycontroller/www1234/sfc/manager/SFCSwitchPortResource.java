package com.sds.securitycontroller.www1234.sfc.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
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
import com.sds.securitycontroller.www1234.servicefunction.manager.IServiceFunctionService;

import com.sds.securitycontroller.www1234.sfc.SFCSwitchPortInstance;


public class SFCSwitchPortResource extends ServerResource{
	protected IServiceFunctionService serviceFunctionService = null;
	protected ISFCService sfcManager = null;
	protected static Logger log = LoggerFactory.getLogger(SFCResource.class);
	@Override
	protected void doInit() throws ResourceException {
		this.serviceFunctionService = (IServiceFunctionService)getContext().getAttributes().get(IServiceFunctionService.class.getCanonicalName());
		this.sfcManager = (ISFCService)getContext().getAttributes().get(ISFCService.class.getCanonicalName());
	}
	
	/**
	 * {
        "sfc-id":"sfc-id-1",
        "priority":"10",
        "start-node":{
        	"switch":"00:00:00:00:00:00:00:01",
        	"port":"1"
        },
        "end-node":{
        	"switch":"00:00:00:00:00:00:00:03",
        	"port":"3"
        },
        "trafficPattern": {
        	"src-ip":"10.0.0.1",
        	"dst-ip":"10.0.0.4",
        	"src-mac":"00:00:00:00:00:01",
        	"dst-mac":"00:00:00:00:00:04",
        	"src-port":"22",
        	"dst-port":"80",
        	"protocol":"TCP"
        },
        "sfs":[
        		{
        			"switch":"00:00:00:00:00:00:00:03",
        		 	"port":"4"
        		},
        		{
        			"switch":"00:00:00:00:00:00:00:03",
        		 	"port":"4"
        		}
        	]
        }
     *   
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 */
	@Post
	public String sfcMap(String fmjson) throws JsonProcessingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(fmjson);
        if(rootNode.path("sfc-id").isMissingNode() || rootNode.path("priority").isMissingNode() || rootNode.path("start-node").isMissingNode()
        		|| rootNode.path("end-node").isMissingNode() || rootNode.path("trafficPattern").isMissingNode() || 
        		rootNode.path("sfs").isMissingNode())
        	return "\"result\":\"fail\"";
        
        
        String sfc_id = rootNode.path("sfc-id").asText();
        long priority =rootNode.path("priority").asLong();
        String start_switch =rootNode.path("start-node").path("switch").asText();
        int start_port =rootNode.path("start-node").path("port").asInt();
        String end_switch =rootNode.path("end-node").path("switch").asText();
        int end_port =rootNode.path("end-node").path("port").asInt();
        Map<String,String> traffic = new HashMap<String,String>();
        traffic.put("src-ip", rootNode.path("trafficPattern").path("src-ip").asText());
        traffic.put("dst-ip", rootNode.path("trafficPattern").path("dst-ip").asText());
        if(!rootNode.path("trafficPattern").path("src-mac").isMissingNode()){
        	traffic.put("src-mac", rootNode.path("trafficPattern").path("src-mac").asText());
        }
        if(!rootNode.path("trafficPattern").path("dst-mac").isMissingNode()){
        	traffic.put("dst-mac", rootNode.path("trafficPattern").path("dst-mac").asText());
        }
        if(!rootNode.path("trafficPattern").path("src-port").isMissingNode()){
        	traffic.put("src-port", rootNode.path("trafficPattern").path("src-port").asText());
        }
        if(!rootNode.path("trafficPattern").path("dst-port").isMissingNode()){
        	traffic.put("dst-port", rootNode.path("trafficPattern").path("dst-port").asText());
        }
        String protocol = rootNode.path("trafficPattern").path("protocol").asText();
        String proto = null;
        switch(protocol){
		case "ICMP":
			proto = "0x01";break;
		case "TCP":
			proto = "0x06";break;
		case "UDP":
			proto = "0x11";break;
		default: break;
		}
        traffic.put("protocol", proto);
        
        log.info("\n traffic pattern : "+JSONObject.toJSONString(traffic));
        
        NodePortTuple start = new NodePortTuple(start_switch,start_port);
        NodePortTuple end = new NodePortTuple(end_switch,end_port);
        
        List<NodePortTuple> path = new ArrayList<NodePortTuple>();
        path.add(start);
        Iterator<JsonNode> iter = rootNode.path("sfs").iterator();
        while(iter.hasNext()){
        	JsonNode node = iter.next();
        	path.add(new NodePortTuple(node.path("switch").asText(), node.path("port").asInt()));
        }
        path.add(end);
        SFCSwitchPortInstance sfcSwitchPortInstance =new SFCSwitchPortInstance(sfc_id,new Date(),path,priority,traffic);
		sfcManager.mapSFCbySwPorts(sfcSwitchPortInstance);
        return "\"result\":\"successful\"";
	}
	@Get
	public String getStringMethod(){
		
		
		return "www1234";
	}
}
