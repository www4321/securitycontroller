package com.sds.securitycontroller.knowledge.globaltraffic.analyzer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import topology.Topology;
import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;
import com.sds.securitycontroller.knowledge.networkcontroller.Topology;
import com.sds.securitycontroller.knowledge.networkcontroller.TopologyLink;

public class TopologyInstance {
	// String ncHost = "http://10.102.27.200:8080";
	static String ncHost = "http://10.103.239.84:8081";
	String ncNCSwitchesAPIUrl = "/wm/core/controller/switches/json";
	String ncSwitchStatAPIUrl = "/wm/core/switch/<switchId>/<statType>/json"; // statType:
	String ncTopologyAPIUrl = "/wm/topology/<target>/json"; // target:links/switchclusters
	protected static Logger log = LoggerFactory.getLogger(Topology.class);
	protected HashMap<NodePortTuple, TopologyLink> switchPortLinks = new HashMap<NodePortTuple, TopologyLink>();
	protected HashMap<String, List<Short>> switchports = new HashMap<String, List<Short>>();

	Topology topology;
	public static TopologyInstance topo = new TopologyInstance();

	//public static void setNcHost(String ncHost) {
	//	TopologyInstance.ncHost = ncHost;
	//}

	public Topology getTopologyStatus() {
		// get topology links and switchclusters
		// get links
		String url = ncTopologyAPIUrl.replace("<target>", "links");

		JsonNode linksNode = httpGetJson(url);
		if (linksNode == null)
			return null;
		List<TopologyLink> topologyLinks = new ArrayList<TopologyLink>();
		try {
			for (int i = 0; i < linksNode.size(); i++) {
				JsonNode linkNode = linksNode.get(i);
				String src_switch = linkNode.path("src-switch").asText();
				// System.out.println(src_switch);
				int src_port = linkNode.path("src-port").asInt();
				// System.out.println(src_port);
				String dst_switch = linkNode.path("dst-switch").asText();
				// System.out.println(dst_switch);
				int dst_port = linkNode.path("dst-port").asInt();
				// System.out.println(dst_port);
				String type = linkNode.path("type").asText();
				// System.out.println(type);
				String direction = linkNode.path("direction").asText();
				// System.out.println(direction);
				int flag = 0;
				NodePortTuple np = new NodePortTuple(src_switch, src_port);
				NodePortTuple np1 = new NodePortTuple(dst_switch, dst_port);
				TopologyLink link = new TopologyLink(src_switch, src_port,
						dst_switch, dst_port, type, direction, flag);
				switchPortLinks.put(np, link);
				TopologyLink link1 = new TopologyLink(dst_switch, dst_port,
						src_switch, src_port, type, direction, flag);
				switchPortLinks.put(np1, link1);
				putadd(src_switch, (short) src_port);
				putadd(dst_switch, (short) dst_port);

				topologyLinks.add(link);
				topologyLinks.add(link1);
				// String src_switch_1 = linkNode.path("dst-switch").asText();
				// String src_port_1 = linkNode.path("dst_port").asText();
				// String dst_switch_1 = linkNode.path("src-switch").asText();
				// String dst_port_1 = linkNode.path("src-port").asText();
			}
		} catch (Exception e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		}

		// get switchclusters
		/*url = ncTopologyAPIUrl.replace("<target>", "switchclusters");
		JsonNode swclRoot = httpGetJson(url);
		if (swclRoot == null)
			return null;
		Map<String, List<String>> switchClusters = new HashMap<String, List<String>>();
		try {
			Iterator<String> iter = swclRoot.fieldNames();
			while (iter.hasNext()) {
				String key = iter.next();
				JsonNode clusterNode = swclRoot.path(key);
				List<String> swIDs = new ArrayList<String>();
				for (int j = 0; j < clusterNode.size(); j++) {
					String swid = clusterNode.get(j).asText();
					swIDs.add(swid);
				}
				switchClusters.put(key, swIDs);
			}

			// for (int i=0;i<swclRoot.size();i++){
			// JsonNode clusterNode = swclRoot.get(0);//.get(i);
			// List<String> swIDs = new ArrayList<String>();
			//
			// for (int j=0;j<clusterNode.size();j++){
			// String swid = clusterNode.get(i).asText();
			// swIDs.add(swid);
			// }
			// SwitchCluster swcl = new SwitchCluster(swIDs);
			// switchClusters.add(swcl);
			// }
		} catch (Exception e) {
			log.error("error parsing json response: {}", e.getMessage());
			return null;
		}*/
		// update topology
		// this.topology = new Topology(topologyLinks, switchClusters);
		this.topology = new Topology(switchports, switchPortLinks);

		return this.topology;
	}

	public void putadd(String switches, Short port) {

		if (!switchports.containsKey(switches)) {
			switchports.put(switches, new ArrayList<Short>());
		}
		switchports.get(switches).add(port);
	}

	JsonNode httpGetJson(String url) {
		try {
			url = ncHost + url;
			ObjectMapper mapper = new ObjectMapper();
			URL datasource = new URL(url);
			JsonNode root = mapper.readTree(datasource);
			return root;
		} catch (JsonProcessingException e) {
			log.error(" Json process error when requesting from url:" + url
					+ ", message: " + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			log.debug(" IO error when requesting from url:" + url
					+ ", message: " + e.getMessage());
			return null;
		}

	}

}
