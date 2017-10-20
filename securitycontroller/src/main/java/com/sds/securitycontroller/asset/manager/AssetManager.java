/** 
 *    Copyright 2014 BUPT. 
 **/
package com.sds.securitycontroller.asset.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.asset.Asset;
import com.sds.securitycontroller.asset.Asset.AssetLevel;
import com.sds.securitycontroller.asset.Asset.AssetType;
import com.sds.securitycontroller.asset.NetworkFlowAsset;
import com.sds.securitycontroller.common.Entity;
import com.sds.securitycontroller.directory.registry.IRegistryManagementService;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.flow.FlowEventArgs;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.flow.FlowTrafficIPStats;
import com.sds.securitycontroller.flow.manager.FlowPollingService;
import com.sds.securitycontroller.flow.manager.IFlowPollingService;
import com.sds.securitycontroller.flow.manager.IFlowSaver;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClause.QueryClauseItemType;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.storage.QueryClauseItem.OpType;
import com.sds.securitycontroller.utils.HTTPUtils;

public class AssetManager implements IFlowSaver, IAssetManagerService,
		ISecurityControllerModule, IEventListener {

	protected static Logger log = LoggerFactory
			.getLogger(FlowPollingService.class);
	protected IRestApiService restApi;
	protected IEventManagerService eventManager;
	protected IRegistryManagementService serviceRegistry;

	protected IStorageSourceService recordStorageSource;
	String recordTableName = "records";
	protected final String primaryKeyName = "id";
	private Map<String, String> recordTableColumns = new HashMap<String, String>() {
		private static final long serialVersionUID = 2034L;

		{
			put("src_ip", "Integer");
			put("dst_ip", "Integer");
			put("appid", "Integer");
			put("pkg_count", "Integer");
			put("byte_count", "Integer");
			put("time", "DATETIME");

		}
	};

	protected IStorageSourceService assetStorageSource;
	String assetTableName = "t_asset";

	protected IEventManagerService ims;
	protected IRegistryManagementService registry;

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IAssetManagerService.class);
		return l;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
		m.put(IAssetManagerService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		l.add(IStorageSourceService.class);
		l.add(IFlowPollingService.class);
		l.add(IRestApiService.class);
		l.add(IEventManagerService.class);

		return l;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		this.recordStorageSource = context.getServiceImpl(
				IStorageSourceService.class, this, "record_dbdriver");
		this.assetStorageSource = context.getServiceImpl(
				IStorageSourceService.class, this, "asset_dbdriver");
		this.eventManager = context.getServiceImpl(IEventManagerService.class,
				this);
		this.serviceRegistry = context.getServiceImpl(
				IRegistryManagementService.class, this);

		restApi = context.getServiceImpl(IRestApiService.class);
		log.info("BUPT security controller asset manager initialized...");

	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		this.recordStorageSource.createTable(recordTableName,
				recordTableColumns);
		this.recordStorageSource.setTablePrimaryKeyName(this.recordTableName,
				this.primaryKeyName);
		this.assetStorageSource.setTablePrimaryKeyName(this.assetTableName,
				this.primaryKeyName);
		AssetManagerRoutable r = new AssetManagerRoutable();
		restApi.addRestletRoutable(r);
		serviceRegistry.registerService(r.basePath(), this);

		eventManager.addEventListener(EventType.RETRIEVED_FLOW, this);
		eventManager.addEventListener(EventType.NEW_FLOW, this);

		log.info("BUPT security controller asset manager started");

	}

	@Override
	public void processEvent(Event e) {
		log.debug("received a '{}' event", e.type);
		if (e.type == EventType.RETRIEVED_FLOW) {
			try {
				FlowEventArgs fArgs = (FlowEventArgs) (e.args);
				Map<String, FlowInfo> flowMapping = fArgs.flowMapping;
				log.debug("Saving {} flows (time={})",
						fArgs.flowMapping.size(), fArgs.getTime());
				List<FlowInfo> flowList = new ArrayList<FlowInfo>(
						flowMapping.values());
				this.saveFlowAssets(flowList, true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (e.type == EventType.NEW_FLOW) {
			try {
				FlowEventArgs fArgs = (FlowEventArgs) (e.args);
				FlowInfo flow = fArgs.flowInfo;
				log.debug("Saving 1 flow (time={})", fArgs.getTime());
				this.saveFlowAsset(flow);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
	}

	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
	}

	String flowAssetQueryUrl = "http://assetcheck.research.intra.sds.com/data";

	@Override
	public Asset getFlowAssetEntity(Entity subject, Entity object) {
		String snode, dnode = null;
		if (subject.getType() == KnowledgeType.CLOUD_VM
				&& object.getType() == KnowledgeType.CLOUD_VM) {
			snode = (String) subject.getAttribute("ip");
			dnode = (String) object.getAttribute("ip");
			if (dnode == null) {
				log.error("VM has no IP provided: " + object);
				return null;
			}
		} else {
			log.error("Unknown asset type");
			return null;
		}

		String wnodeUrl, bnodeUrl = null;

		if (snode == null)
			wnodeUrl = flowAssetQueryUrl + "/wnodes?d_node=" + dnode;
		else
			wnodeUrl = flowAssetQueryUrl + "/wnodes?s_node=" + snode
					+ "&d_node=" + dnode;
		Asset wnode = fetchFlowEntity(wnodeUrl);
		if (wnode != null) {
			wnode.setLevel(AssetLevel.HIGH);
			return wnode;
		}
		if (snode == null)
			bnodeUrl = flowAssetQueryUrl + "/bnodes?d_node=" + dnode;
		else
			bnodeUrl = flowAssetQueryUrl + "/bnodes?s_node=" + snode
					+ "&d_node=" + dnode;
		Asset bnode = fetchFlowEntity(bnodeUrl);
		if (bnode != null) {
			bnode.setLevel(AssetLevel.PROHIBITED);
			return bnode;
		}

		return new NetworkFlowAsset(null, AssetLevel.MEDIUM, snode, dnode);
	}

	private Asset fetchFlowEntity(String reqUrl) {
		String resp = HTTPUtils.httpGet(reqUrl, null);
		if (resp == null)
			return null;

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(resp);
			Iterator<JsonNode> nodes = root.elements();
			while (nodes.hasNext()) {
				JsonNode node = nodes.next();
				Integer id = node.path("id").asInt();
				Integer snode = node.path("s_node").asInt();
				Integer dnode = node.path("d_node").asInt();
				Asset entity = new NetworkFlowAsset(id.toString(),
						AssetLevel.MEDIUM, snode.toString(), dnode.toString());
				return entity;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;

	}

	@Override
	public void saveFlowAssets(List<FlowInfo> flows, boolean ignoreZeroCount) {
		// clear the table
		// this.storageSource.deleteRows(tableName, null);
		List<FlowTrafficIPStats> entities = new ArrayList<FlowTrafficIPStats>();
		for (int i = 0; i < flows.size(); i++) {
			FlowInfo flow = flows.get(i);
			long pkg_count = flow.getPacketCount();
			if (pkg_count == 0 && ignoreZeroCount)
				continue;

			String id = flow.getId();
			int proto = flow.getMatch().getNetworkProtocol();
			int dport = flow.getMatch().getTransportDestination();
			long byte_count = flow.getByteCount();
			int appid = dport * 100 + proto;
			String now = "now()";

			FlowTrafficIPStats entity = new FlowTrafficIPStats(id, now, appid,
					flow.getnetworkSourceInt(),
					flow.getnetworkDestinationInt(), pkg_count, byte_count);
			entities.add(entity);
		}

		int inserted = this.recordStorageSource.insertEntities(recordTableName,
				entities);
		log.debug("{} entities have been inserted into database", inserted);
	}

	@Override
	public void saveFlowAsset(FlowInfo flow) {

		String id = flow.getId();
		int proto = flow.getMatch().getNetworkProtocol();
		int dport = flow.getMatch().getTransportDestination();
		int appid = dport * 100 + proto;
		String now = "now()";

		FlowTrafficIPStats entity = new FlowTrafficIPStats(id, now, appid,
				flow.getnetworkSourceInt(), flow.getnetworkDestinationInt(), 1,
				0);

		int inserted = this.recordStorageSource.insertEntity(recordTableName,
				entity);
		log.debug("{} entities have been inserted into database", inserted);
	}

	@Override
	public void deleteFlowAssets() {
		this.recordStorageSource.deleteEntities(recordTableName, null);
	}

	int convertIpToInt(String ipaddr) {
		int[] ip = new int[4];
		String[] parts = ipaddr.split("\\.");

		for (int i = 0; i < 4; i++) {
			ip[i] = Integer.parseInt(parts[i]);
		}
		int ipNumbers = 0;
		for (int i = 0; i < 4; i++) {
			ipNumbers += ip[i] << (24 - (8 * i));
		}
		return ipNumbers;
	}

	// for general assets
	@Override
	public boolean addAsset(Asset asset) {
		String id = this.applyAssetId();
		boolean ret = false;
		do {
			if (null == id) {
				log.error("apply asset id failed:{} ", asset);
				break;
			}
			asset.setId(id);
			int res = this.assetStorageSource.insertEntity(this.assetTableName,
					asset);
			if (res <= 0) {
				log.error("Insert asset to DB failed {} ", asset);
				break;
			}
			ret = true;
		} while (false);
		return ret;
	}

	@Override
	public boolean removeAsset(String id) {
		List<QueryClauseItem> clauseItems = new ArrayList<QueryClauseItem>();
		clauseItems.add(new QueryClauseItem("id", id, OpType.EQ));
		QueryClause query = this.assetStorageSource.createQuery(
				this.assetTableName, null, clauseItems,
				QueryClauseItemType.AND, null);

		return this.assetStorageSource.deleteEntities(this.assetTableName,
				query) >= 0;
	}

	@Override
	public Asset getAsset(String id) {
		Asset asset = (Asset) this.assetStorageSource.getEntity(
				this.assetTableName, id, Asset.class);
		return asset;

	}

	@Override
	public List<Asset> getAllAsset() {
		@SuppressWarnings("unchecked")
		List<Asset> assets = (List<Asset>) this.assetStorageSource.executeQuery(
				new QueryClause(this.assetTableName), Asset.class);
		return assets;
	}

	@Override
	public boolean updateAsset(String id, Map<String, Object> values) {
		values.remove("type");
		values.remove("host");
		values.remove("name");
		values.remove("id");
		List<QueryClauseItem> clauseItems = new ArrayList<QueryClauseItem>();
		clauseItems.add(new QueryClauseItem("id", id, OpType.EQ));
		QueryClause query = new QueryClause(clauseItems,
				this.assetTableName, null, null);

		int res = this.assetStorageSource.updateEntities(this.assetTableName,
				query, values);
		return res > 0;
	}

	@Override
	public String assetExists(Map<String, Object> query) {
		Map<String, Object> parsedQuery = makeQueryMap(query);

		List<QueryClauseItem> clauseItems = new ArrayList<QueryClauseItem>();
		for(Entry<String, Object> entry: parsedQuery.entrySet()){
			clauseItems.add(new QueryClauseItem(entry.getKey(), entry.getValue(), OpType.EQ));
		}
		QueryClause finalQuery = new QueryClause(clauseItems,
				this.assetTableName, null, null);
		
		
		@SuppressWarnings("unchecked")
		List<Asset> list = (List<Asset>)this.assetStorageSource
				.executeQuery(finalQuery, Asset.class);
		if (list.size() > 0) {
			return list.get(0).getId();
		}
		return null;
	}

	@Override
	public String applyAssetId() {
		int MAX = 50;// how many times to try
		Random r = new Random();
		long nid = System.currentTimeMillis();
		String id = "3" + Long.toString(nid);
		int count = 0;

		while (count < MAX && null != this.getAsset(id)) {
			nid += r.nextInt(200);
			id = "3" + Long.toString(nid);
			count++;
		}
		if (MAX == count) {
			return null;
		}
		return id;
	}

	@Override
	public Map<String, Object> buildAsset(Map<String, Object> valueMap)
			throws IOException {
		return Asset.buildAsset(valueMap);
	}

	private Map<String, Object> makeQueryMap(Map<String, Object> asset) {
		Map<String, Object> map = new HashMap<String, Object>();
		String stype = (String) asset.get("type");
		AssetType type = AssetType.valueOf(stype.toUpperCase());

		map.put("type", asset.get("type"));
		map.put("name", asset.get("name"));
		map.put("host", asset.get("host"));

		switch (type) {
		case WEBSITE:
			map.put("url", asset.get("url"));
			break;
		case HOST:
			break;
		case DB:
			map.put("db_type", asset.get("db_type"));
			map.put("db_port", asset.get("db_port"));
			break;
		case NETWORK_DEVICE:
			break;
		default:
			break;
		}

		return map;
	}
}
