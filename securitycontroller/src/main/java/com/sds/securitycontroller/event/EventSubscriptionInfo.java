/**
 *    Copyright 2014 BUPT.
 **/
package com.sds.securitycontroller.event;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sds.securitycontroller.log.manager.SubscriberManager;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class EventSubscriptionInfo implements IDBObject {

	private static final long serialVersionUID = 789940861740879219L;

	protected EventSubscription eventsubscription;
	protected String module;
	protected EventType eventype;
	protected String subscribedAppId;
	protected String subscribeUrl;
	protected String subscriptionId;

    protected static Logger log = LoggerFactory.getLogger(EventSubscriptionInfo.class);

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getSubscibedAppId() {
		return subscribedAppId;
	}

	public void setSubscibedAppId(String subscibedAppId) {
		this.subscribedAppId = subscibedAppId;
	}

	public String getSubscribeUrl() {
		return subscribeUrl;
	}

	public void setSubscribeUrl(String subscribeUrl) {
		this.subscribeUrl = subscribeUrl;
	}

	public EventSubscription getEventsubscription() {
		return eventsubscription;
	}

	public void setEventsubscription(EventSubscription eventsubscription) {
		this.eventsubscription = eventsubscription;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public EventType getEventype() {
		return eventype;
	}

	public void setEventype(EventType eventype) {
		this.eventype = eventype;
	}

    public EventSubscriptionInfo() {
    }

    public EventSubscriptionInfo(EventSubscription eventsubscription,
			String module, EventType eventype, String subscribedAppUrl,
			String subscribedAppId, String subscriptionId) {
		this.eventsubscription = eventsubscription;
		this.module = module;
		this.eventype = eventype;
		this.subscribeUrl = subscribedAppUrl;
		this.subscribedAppId = subscribedAppId;
		this.subscriptionId = subscriptionId;
	}

	@Override
	public String toString() {
		return String
				.format("EventSubscriptionInfo: \nmodule=%s, \neventtype=%s, \nsubscribedAppId=%s, \nsubscribeUrl=%s, \nsubscriptionId=%s, \neventsubscription=%s",
						this.module, this.eventype.toString(),
						this.subscribedAppId, this.subscribeUrl,
						this.subscriptionId, this.eventsubscription.toString());
	}

	@Override
	@JsonIgnore
	public Map<String, Object> getDBElements() {
		Map<String, Object> subMap = new HashMap<String, Object>();
		String subStr = null;
		try {
			subMap = SubscriberManager
					.objectToMapViaBeanInfo(this.eventsubscription);

			ObjectMapper om = new ObjectMapper();
			om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
			om.configure(SerializationFeature.INDENT_OUTPUT, true);
			om.setSerializationInclusion(Include.NON_NULL);
			subStr = om.writeValueAsString(subMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("eventsubscription", subStr);
		map.put("module", this.module);
		map.put("eventype", this.eventype);
		map.put("subscribeUrl", this.subscribeUrl);
		map.put("subscribedAppId", this.subscribedAppId);
		map.put("subscriptionId", this.subscriptionId);
		return map;
	}

	protected static Map<String, Method> dbFieldMapping;

	@Override
	public Object getFieldValueByKey(String key){
		if(dbFieldMapping == null){
			dbFieldMapping = new HashMap<String, Method>();
		    Class<? extends EventSubscriptionInfo> cla=this.getClass();
			try {
				dbFieldMapping.put("eventsubscription", cla.getDeclaredMethod("eventsubscription"));
				dbFieldMapping.put("module", cla.getDeclaredMethod("module"));
				dbFieldMapping.put("eventype", cla.getDeclaredMethod("eventype"));
				dbFieldMapping.put("subscribeUrl", cla.getDeclaredMethod("subscribeUrl"));
				dbFieldMapping.put("subscribedAppId", cla.getDeclaredMethod("subscribedAppId"));
				dbFieldMapping.put("subscriptionId", cla.getDeclaredMethod("subscriptionId"));
			} catch (NoSuchMethodException | SecurityException e) {
			    log.error("getFieldValueByKeys error: "+e.getMessage());
				return null;
			}
		}
		Method m = dbFieldMapping.get(key);
		try {
			return m.invoke(this, new Object[0]);
		}catch(Exception e){
			log.error("getFieldValueByKeys error: "+e.getMessage());
			return null;
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {
		String subStr = resultSet.getString("eventsubscription");
		ReportEventSubscription sub = null;
		try {
			Map<String,Object> subMap = new ObjectMapper().readValue(subStr, HashMap.class);
			sub = new ReportEventSubscription(null,
					null, (String) subMap.get("details"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new EventSubscriptionInfo(
				sub,
				resultSet.getString("module"),
				Enum.valueOf(EventType.class, resultSet.getString("eventype")),
				resultSet.getString("subscribeUrl"),
				resultSet.getString("subscribedAppId"),
				resultSet.getString("subscriptionId"));
	}

}
