package com.sds.securitycontroller.knowledge.globaltraffic.analyzer;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.knowledge.globaltraffic.MatchPath;
import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;
import com.sds.securitycontroller.knowledge.networkcontroller.Topology;
import com.sds.securitycontroller.knowledge.networkcontroller.TopologyLink;
import com.sds.securitycontroller.utils.Cypher;

public class CaculateFlowPath {

	protected String datalayerdestination;
	protected String datalayersource;
	protected String src_dpid;
	protected int src_port;
	protected String dst_dpid;
	// protected int port;
	static HashMap<String, MatchPath> path = new HashMap<String, MatchPath>();
	static ArrayList<LinkedList<FlowMatch>> modstore = new ArrayList<LinkedList<FlowMatch>>();
	static HashMap<FlowMatch, LinkedList<NodePortTuple>> sortpath = new HashMap<FlowMatch, LinkedList<NodePortTuple>>();
	static HashMap<FlowMatch, ArrayList<NodePortTuple>> caculatepath = new HashMap<FlowMatch, ArrayList<NodePortTuple>>();
	static ArrayList<FlowMatch> modflow = new ArrayList<FlowMatch>();
	static TopologyInstance topo = new TopologyInstance();
	public static Topology topology;
	// static CaculateFlowPath cp=new CaculateFlowPath();
	ArrayList<String> pathlist = new ArrayList<String>();
	public static HashMap<NodePortTuple, TopologyLink> switchPortLinks;
	public static Map<String, List<Short>> switchPorts ;

	public CaculateFlowPath(){
		topology = topo.getTopologyStatus();
		if(topology != null){
			switchPortLinks = topology.getSwitchPortLinks();
			switchPorts = topology.getSwitchPorts();
		}
	}
	
	
	
	
	public void caculateflowpath(FlowMatch match, NodePortTuple dpid) {

		putadd(match, dpid);
	}

	public void putadd(FlowMatch match, NodePortTuple dpid) {

		if (!caculatepath.containsKey(match)) {
			caculatepath.put(match, new ArrayList<NodePortTuple>());

		}
		caculatepath.get(match).add(dpid);

	}

	public void sort() {
		sortpath.clear();
		if (caculatepath != null & !(caculatepath.isEmpty())) {
	//	if (!(caculatepath.isEmpty())) {
			Set<FlowMatch> set = caculatepath.keySet();

			Iterator<FlowMatch> it = set.iterator();
			while (it.hasNext()) {
				FlowMatch key = it.next();
				// System.out.println(key);
				ArrayList<NodePortTuple> integer = caculatepath.get(key);
				FlowMatch matchl = new FlowMatch();
				matchl = key;
				for (int i = 0; i < integer.size(); i++) {
					if (switchPortLinks.containsKey(integer.get(i))) {

						switchPortLinks.get(integer.get(i)).setFlag(1);

					}

				}
				LinkedList<NodePortTuple> pathlink = new LinkedList<NodePortTuple>();
				pathlink.add(integer.get(0));
				if (switchPortLinks.containsKey(integer.get(0))) {
					String s1 = switchPortLinks.get(integer.get(0))
							.getDst_switch();
					searchlast(s1, pathlink, integer);
					searchfirst(integer.get(0).getNodeId(), pathlink);
				} else
					searchfirst(integer.get(0).getNodeId(), pathlink);

				sortpath.put(matchl, pathlink);
			}

		}
	}

	public void searchlast(String n, LinkedList<NodePortTuple> pathlink,
			ArrayList<NodePortTuple> integer) {
		List<Short> pl = switchPorts.get(n);
		List<NodePortTuple> npl = new ArrayList<NodePortTuple>();
		for (int i = 0; i < pl.size(); i++) {
			NodePortTuple n1 = new NodePortTuple(n, pl.get(i));
			npl.add(n1);
		}
		int p = 0;
		for (int j = 0; j < npl.size(); j++) {

			if (switchPortLinks.get(npl.get(j)).getFlag() == 1) {
				p = 1;
				switchPortLinks.get(npl.get(j)).setFlag(0);
				pathlink.addLast(npl.get(j));
				if (switchPortLinks.containsKey(npl.get(j))) {
					String m = switchPortLinks.get(npl.get(j)).getDst_switch();
					searchlast(m, pathlink, integer);
				}

			}

		}
		if (p == 0) {
			for (int k = 0; k < integer.size(); k++) {
				if (integer.get(k).getNodeId().equals(n))
					pathlink.addLast(integer.get(k));
			}
		}
	}

	public void searchfirst(String p, LinkedList<NodePortTuple> pathlink) {
		List<Short> pl = switchPorts.get(p);
		List<NodePortTuple> npl = new ArrayList<NodePortTuple>();
		List<NodePortTuple> npo = new ArrayList<NodePortTuple>();
		for (int i = 0;pl!=null&& i < pl.size(); i++) {
			NodePortTuple n1 = new NodePortTuple(p, pl.get(i));
			npl.add(n1);
		}
		for (int j = 0; j < npl.size(); j++) {
			if (switchPortLinks.containsKey(npl.get(j))) {
				String m1 = switchPortLinks.get(npl.get(j)).getDst_switch();
				int p1 = switchPortLinks.get(npl.get(j)).getDst_port();
				NodePortTuple np1 = new NodePortTuple(m1, p1);
				npo.add(np1);
			}
		}
		for (int k = 0; k < npo.size(); k++) {
			if (switchPortLinks.get(npo.get(k)).getFlag() == 1) {
				switchPortLinks.get(npo.get(k)).setFlag(0);
				switchPortLinks.get(npl.get(k)).setFlag(0);
				pathlink.addFirst(npo.get(k));
				searchfirst(npo.get(k).getNodeId(), pathlink);
			}

		}
	}

	public HashMap<String, MatchPath> findmod() {

		path.clear();
		while (!modflow.isEmpty()) {
			LinkedList<FlowMatch> modlink = new LinkedList<FlowMatch>();
			modlink.addFirst(modflow.get(0));
			modlink.addLast(modflow.get(1));
			modflow.remove(0);
			modflow.remove(0);
			String id = null;
			int i = 0;
			for (i = 0; i < modflow.size(); i++) {
				if (modflow.get(i).equals(modlink.getFirst())) {
					modlink.addFirst(modflow.get(i - 1));
					modflow.remove(i);
					modflow.remove(i - 1);
					System.out.println(modflow);
					i--;
				}
				if (modflow.get(i).equals(modlink.getLast())) {
					modlink.addLast(modflow.get(i + 1));
					modflow.remove(i);
					modflow.remove(i);
					i--;
				}
			}
			LinkedList<NodePortTuple> link = new LinkedList<NodePortTuple>();
			link = sortpath.get(modlink.getFirst());

			sortpath.remove(modlink.getFirst());

			for (i = 1; i < modlink.size(); i++) {
				LinkedList<NodePortTuple> lk = sortpath.get(modlink.get(i));
				if (lk != null) {
					lk.removeFirst();
					link.addAll(lk);
				}
				sortpath.remove(modlink.get(i));
			}
			id = getid(modlink);
			MatchPath flowinfo = new MatchPath(modlink, link);
			path.put(id, flowinfo);

			System.out.println(modflow);

		}

		if (sortpath != null || !sortpath.isEmpty()) {
			Set<FlowMatch> set1 = sortpath.keySet();

			Iterator<FlowMatch> it = set1.iterator();
			while (it.hasNext()) {
				FlowMatch key1 = it.next();
				LinkedList<FlowMatch> matchlist1 = new LinkedList<FlowMatch>();
				LinkedList<NodePortTuple> path1 = sortpath.get(key1);
				matchlist1.add(key1);
				String id1 = null;
				id1 = getid(matchlist1);
				MatchPath flowinfo1 = new MatchPath(matchlist1, path1);
				path.put(id1, flowinfo1);
			}
		}
		return path;
	}

	public String getid(LinkedList<FlowMatch> matchlist) {
		String id = null;
		String each = null;
		String raw = null;
		int i;
		for (i = 0; i < matchlist.size(); i++) {
			each = matchlist.get(i).getDataLayerDestination()
					+ matchlist.get(i).getDataLayerSource()
					+ matchlist.get(i).getDataLayerType()
					+ matchlist.get(i).getDataLayerVirtualLan()
					+ matchlist.get(i).getNetworkDestination()
					+ matchlist.get(i).getNetworkDestinationMaskLen()
					+ matchlist.get(i).getNetworkProtocol()
					+ matchlist.get(i).getNetworkSource()
					+ matchlist.get(i).getNetworkSourceMaskLen()
					+ matchlist.get(i).getTransportDestination()
					+ matchlist.get(i).getTransportSource()
					+ matchlist.get(i).getwildcards()
					+ matchlist.get(i)
							.getDataLayerVirtualLanPriorityCodePoint()
					+ matchlist.get(i).getNetworkTypeOfService();
			raw = raw + each;
		}
		byte[] rawbytes = null;
		try {
			rawbytes = raw.getBytes("UTF-16LE");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		id = Cypher.getMD5(rawbytes);

		return id;

	}
	public void getvalue2() {
		int cou = 0;
		int sum = 0;
		if (path != null || !path.isEmpty()) {
			Set<String> set = path.keySet();

			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = it.next();
				// System.out.println(key);
				MatchPath integer1 = path.get(key);
				
				cou = cou + 1;
				sum = sum + integer1.getpathlink().size();
			}
		}
	}
}
