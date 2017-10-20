/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;

public class Topology extends KnowledgeEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7205328789321170871L;
	List<TopologyLink> links;
//	Map<SwitchCluster> switchClusters;
	Map<String, List<String>> switchClusterMap;
	protected HashMap<String, List<Short>> switchPorts;
	 protected HashMap<NodePortTuple, TopologyLink> switchPortLinks;
	public HashMap<String, List<Short>> getSwitchPorts() {
		return switchPorts;
	}
	public void setSwitchPorts(HashMap<String, List<Short>> switchPorts) {
		this.switchPorts = switchPorts;
	}
	public HashMap<NodePortTuple, TopologyLink> getSwitchPortLinks() {
		return switchPortLinks;
	}
	public void setSwitchPortLinks(HashMap<NodePortTuple, TopologyLink> switchPortLinks) {
		this.switchPortLinks = switchPortLinks;
	}
	public Topology(List<TopologyLink> links,Map<String, List<String>> switchClusterMap){
		this.type = KnowledgeType.NETWORK_TOPOLOGY;
		this.links = links;
		this.switchClusterMap = switchClusterMap;
	}
	public Topology(HashMap<String, List<Short>> switchPorts,HashMap<NodePortTuple, TopologyLink> switchPortLinks) {
		this.type = KnowledgeType.NETWORK_TOPOLOGY;
		this.switchPortLinks=switchPortLinks;
		this.switchPorts=switchPorts;
	}
	
	public List<TopologyLink> getLinks() {
		return links;
	}
	public void setLinks(List<TopologyLink> links) {
		this.links = links;
	}
	public Map<String, List<String>> getSwitchClusters() {
		return switchClusterMap;
	}
	public void setSwitchClusters(Map<String, List<String>> switchClusterMap) {
		this.switchClusterMap = switchClusterMap;
	}
}
