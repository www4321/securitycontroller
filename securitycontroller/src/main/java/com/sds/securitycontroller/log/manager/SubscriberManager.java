package com.sds.securitycontroller.log.manager;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.event.EventSubscription;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.storage.QueryClauseItem.OpType;

public class SubscriberManager {

	protected static Logger logger = LoggerFactory
			.getLogger(SubscriberManager.class);
	protected IStorageSourceService storageServer = null;
	protected String subscriberTableName;

	protected static final String key_subscriptionId = "subscriptionId";
	protected static final String key_subscibedAppId = "subscibedAppId";

	public SubscriberManager(IStorageSourceService storage, String tableName) {
		this.storageServer = storage;
		this.subscriberTableName = tableName;
	}

	protected boolean saveSubscriber(EventSubscriptionInfo info) {
		try {
			return this.storageServer.insertEntity(this.subscriberTableName,
					info) > 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Save subscriber message error: {}", e.getMessage());
			return false;
		}
	}

	public boolean updateSubscriber(EventSubscriptionInfo info) {
		try {
			this.storageServer.updateOrInsertEntity(subscriberTableName, info);
			return true;
		} catch (Exception e) {
			logger.error("update subscriber message error: {}", e.getMessage());
			return false;
		}

	}

	public int deleteSubscriber(String appId, String subId) {
		try {
			List<QueryClauseItem> clauseItems = new ArrayList<QueryClauseItem>();
			clauseItems.add(new QueryClauseItem(key_subscibedAppId, appId,
					OpType.EQ));
			if (subId != null && !subId.isEmpty()) {
				clauseItems.add(new QueryClauseItem(key_subscriptionId, subId,
						OpType.EQ));
			}

			QueryClause query = this.storageServer.createQuery(
					this.subscriberTableName, null, clauseItems, null, null);
			/*
			 * IAbstractResultSet result =
			 * this.storageServer.executeQuery(query); if(result.size() <= 0){
			 * return -2; }
			 */

			logger.info("Delete subscriber appid:{}, name: {}", appId, subId);
			return this.storageServer
					.deleteEntities(subscriberTableName, query);
		} catch (Exception e) {
			logger.error(e.toString());
			return -2;
		}

	}

	@SuppressWarnings("unchecked")
	public List<EventSubscriptionInfo> getAllSubscribers() {
		try {
			QueryClause qc = new QueryClause(subscriberTableName);
			List<EventSubscriptionInfo> subinfoList = (List<EventSubscriptionInfo>) this.storageServer
					.executeQuery(qc, EventSubscriptionInfo.class);
			return subinfoList;
		}
		catch (Exception e) {
			logger.error("Get all subscriber message error: {}", e.getMessage());
		}

		return null;

	}

	public static Map<String, Object> objectToMapViaBeanInfo(Object o)
			throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		BeanInfo info = Introspector.getBeanInfo(o.getClass());
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {

			Method reader = pd.getReadMethod();
			// 内容为null的过滤掉.
			if (reader == null || reader.invoke(o) == null) {
				continue;
			}

			// 默认继承Object类的属性，过滤掉.
			if (pd.getName().equalsIgnoreCase("class")) {
				continue;
			}

			if (pd.getPropertyType().isEnum()) {
				result.put(pd.getName(), reader.invoke(o).toString());
			} else if (pd.getPropertyType().equals(String.class)) {
				result.put(pd.getName(), reader.invoke(o));
			} else if (pd.getPropertyType().equals(EventSubscription.class)) {
				result.put(pd.getName(),
						objectToMapViaBeanInfo(reader.invoke(o)));
			}
		}

		return result;
	}

}
