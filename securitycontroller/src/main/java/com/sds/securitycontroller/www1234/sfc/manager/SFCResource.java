package com.sds.securitycontroller.www1234.sfc.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.sds.securitycontroller.www1234.sfc.SFCInstance;
import com.sds.securitycontroller.www1234.sfc.TrafficPattern;
import com.sds.securitycontroller.www1234.sfc.ServiceFunction;

public class SFCResource extends ServerResource{
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
        "sfs":"FIREWALL->WAF->DDOS"
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
        String src_ip = rootNode.path("trafficPattern").path("src-ip").asText();
        String dst_ip = rootNode.path("trafficPattern").path("dst-ip").asText();
        String src_mac = rootNode.path("trafficPattern").path("src-mac").isMissingNode()?"00:00:00:00:00:00":rootNode.path("trafficPattern").path("src-mac").asText();
        String dst_mac = rootNode.path("trafficPattern").path("dst-mac").isMissingNode()?"00:00:00:00:00:00":rootNode.path("trafficPattern").path("dst-mac").asText();
        int src_port = rootNode.path("trafficPattern").path("src-port").asInt();
        int dst_port = rootNode.path("trafficPattern").path("dst-port").asInt();
        String protocol = rootNode.path("trafficPattern").path("protocol").asText();
        
        List<ServiceFunction> sfc = new ArrayList<ServiceFunction>();
        String sfs =rootNode.path("sfs").asText();
        String[] sf = sfs.split("->");
        for(int i=0;i<sf.length;i++){
        	switch(sf[i].trim()){
            case "FIREWALL":
            	sfc.add(ServiceFunction.FIREWALL);break;
            case "DDOS":
            	sfc.add(ServiceFunction.DDOS);break;
            case "IPS":
            	sfc.add(ServiceFunction.IPS);break;
            case "WAF":
            	sfc.add(ServiceFunction.WAF);break;
            }
        }
        
        int proto =-1;
        NodePortTuple start = new NodePortTuple(start_switch,start_port);
        NodePortTuple end = new NodePortTuple(end_switch,end_port);
        switch(protocol){
		case "ICMP":
			proto = 0x01;break;
		case "TCP":
			proto = 0x06;break;
		case "UDP":
			proto = 0x11;break;
		default: break;
		}
        TrafficPattern traffic = new TrafficPattern (src_mac,dst_mac,src_ip,dst_ip,src_port,dst_port,proto);
        SFCInstance sfcInstance =new SFCInstance(sfc_id,traffic,sfc,start,end,priority,new Date());
		sfcManager.mapSFC(sfcInstance);
        return "\"result\":\"successful\"";
	}
	@Get
	public String getStringMethod(){
		List<ServiceFunction> sfc = new ArrayList<ServiceFunction>();
		sfc.add(ServiceFunction.FIREWALL);
		NodePortTuple start = new NodePortTuple("00:00:00:00:00:00:00:01",1);
		NodePortTuple end = new NodePortTuple("00:00:00:00:00:00:00:03",3);
		TrafficPattern traffic = new TrafficPattern ("00:00:00:00:00:01","00:00:00:00:00:04","10.0.0.1","10.0.0.4",22,80,0x01);
		SFCInstance sfcInstance =new SFCInstance("id",traffic,sfc,start,end,2,new Date());
		sfcManager.mapSFC(sfcInstance);
		
		return "www1234";
	}
}
