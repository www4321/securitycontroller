/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app.manager;

import java.io.IOException;
import java.util.Date;

import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.event.EventSubscription;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.ReportEventSubscription;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.utils.ResponseFactory;

class AppSubscriptionException extends Exception{
	
	private static final long serialVersionUID = 8908320732037920L;

	public AppSubscriptionException(String msg){
		super(msg);
	}
}


public class AppSubscriptionManagerResource extends ServerResource{
	protected static Logger log = LoggerFactory.getLogger(AppSubscriptionManagerResource.class);
	String id = null;
	String subscribername = null;
	App app = null;
	IAppManagementService appmanager = null;
	IEventManagerService eventManager = null;
	

	@Override  
    public void doInit() {    
        appmanager = 
                (IAppManagementService)getContext().getAttributes().
                get(IAppManagementService.class.getCanonicalName());
        eventManager = 
                (IEventManagerService)getContext().getAttributes().
                get(IEventManagerService.class.getCanonicalName());

        id = (String) getRequestAttributes().get("id");
        if(id != null){
        	app = appmanager.getApp(id);
        }
        subscribername = (String) getRequestAttributes().get("subscribername");
    }  
	

	@SuppressWarnings("rawtypes")
	@Delete
	public String handleDelete(String fmJson){
		ResponseFactory response = new ResponseFactory();
		IEventManagerService eventManager = null;
		Class serviceClass = null;
		do{

			if(this.app == null){
				log.error("app "+this.id + " not found");
	    		response.putData(404,  "result", "app "+this.id + " not found", this);
	    		break;
			}
			
			Object[] rqArgs = {this.id, this.subscribername};
			serviceClass=com.sds.securitycontroller.log.manager.ILogManagementService.class;
			String methodName="deleteSubscriber";
			
			Object result = eventManager.makeRPCCall(serviceClass, methodName, rqArgs);
			if(result != null){
				int  code = (int)result;
				if (code < 0) {
					response.putData(404, "result", "The app id or subscriber name not found.", this);
				}
				else{
					response.putData(200, "result", String.format("App[%s][%s] deleted succeed.", this.id, this.subscribername), this);
				}
			}
			else{
				response.putData(500, "result", "Server error.", this);
			}
			break;
			
		}while(false);
		
		
		return response.toString();
	}
	

    @Post
    public String handlePost(String fmJson) {
    	EventSubscriptionInfo subscription = null;
    	ResponseFactory response = new ResponseFactory();
    
    	
        try {
        	if(this.app == null){
        		log.error("app "+this.id + " not found");
        		throw new Exception("app "+this.id + " not found");
        	}        	
        	
        	subscription = decodeJsonToSubscription(fmJson, this.id);
        	eventManager.addConditionToListener(subscription);
        	response.putData(200, "result", "ok", this);
        } catch (IOException e) {
            log.error("Error parsing new app subscription: " + fmJson + e.getMessage());
            response.putData(404, "result", "Error parsing new app subscription: " + fmJson + e.getMessage(), this);
        }
        catch(AppSubscriptionException e){
            log.error("Error creating new app msg: {} {}", e.getMessage());
            response.putData(404, "result", String.format("{\"status\" : \"error\", \"result\" : \"%s. \"}", e.getMessage()),this) ;
        }
        catch (Exception e) {
            log.error("Error creating new app msg: {} {}", e.toString(), e.getCause());
            response.putData(404, "result", "Error creating new app msg: " + e.getMessage(), this);
        }        

        return response.toString();
    }
    
   

    public static EventSubscriptionInfo decodeJsonToSubscription(String fmJson, 
    		String appId) throws IOException, AppSubscriptionException{

/*
    	ObjectMapper mapper = new ObjectMapper();
    	JsonNode rootNode = mapper.readTree(fmJson);
    	JsonNode subNode = rootNode.path("data");
    	String id = subNode.path("id").asText();
    	if(id == null)
        	id = subNode.path("name").asText();
    	String st = subNode.path("eventtype").asText();
    	EventType eventType = EventType.valueOf(st);
    	String subscribedAppUrl = subNode.path("subscribeurl").asText();
    	String subscribedAppId = subNode.path("subscribedappid").asText();
    	if(subscribedAppId == null)
    		subscribedAppId = appId;
    	String module = subNode.path("module").asText();

    	JsonNode subrootNode = subNode.path("eventsubscription");

    	EventSubscription ces = null;
		if(eventType == EventType.NEW_REPORT_ITEM){
	    	//xi'an developer
	    	JsonNode filterNode = subNode.path("filter");
			ReportEventSubscription res = new ReportEventSubscription(null, null, filterNode.toString());
			res.testComparetor();
		}
		else{
			//beijing
			ces = parseJsonNode(subrootNode);
		}

    	EventSubscriptionInfo eventinfo = new EventSubscriptionInfo(ces, module, eventType,
    			subscribedAppUrl, subscribedAppId, id);
    	eventinfo.setSubscriptionId(id);
    	return eventinfo;
*/
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(fmJson);
        JsonNode subNode = rootNode.path("data").path("subscription");

        String id = subNode.path("name").asText();
        //String subscribedAppId = subNode.path("appid").asText();
        String st = subNode.path("eventtype").asText();
        EventType eventtype = EventType.valueOf(st);
        String subscribedAppUrl = subNode.path("callbackurl").asText();

        JsonNode filterNode = subNode.path("filter");
        EventSubscription ces = parseJsonNode(eventtype, filterNode);

        EventSubscriptionInfo eventinfo = new EventSubscriptionInfo(ces, null, eventtype, subscribedAppUrl, appId, id);
        return eventinfo;
    }
	
    protected static Class<?> getType(String stype){
    	if(stype.indexOf("String")>=0)
    		return String.class;
    	else if(stype.indexOf("int")>=0)
    		return Integer.class;
    	else if(stype.indexOf("float")>=0)
    		return Float.class;
    	else if(stype.indexOf("double")>=0)
    		return Double.class;
    	else if(stype.indexOf("datetime")>=0)
    		return Date.class;
    	return String.class;
    }
    
	protected static EventSubscription parseJsonNode(EventType type, JsonNode node)throws AppSubscriptionException{
		
/*
		String stype = node.path("subscriptiontype").asText().toUpperCase();
		if(stype.equals("OPERATOR")){
			String svtype = node.path("valuetype").asText();
			Class<?> vtype = null;
			try {
				vtype = Class.forName(svtype);
			} catch (ClassNotFoundException e) {
				log.error("No such type: {}", svtype);
			}
			
			OperatorEventSubscription opes = new OperatorEventSubscription(node.path("subscribedkey").asText()
						, EventSubscription.Operator.valueOf(node.path("operator").asText()), node.path("value").asText()
						, EventSubscription.SubscribedValueCategory.valueOf(node.path("subscribedvaluecategory").asText())
						, EventSubscription.SubscriptionType.valueOf(node.path("subscriptiontype").asText())
						, vtype);

			return opes;
		}
		else if(stype.equals("LIST")){
			String svtype = node.path("valuetype").asText();
			Class<?> vtype = null;
			try {
				vtype = Class.forName(svtype);
			} catch (ClassNotFoundException e) {
				log.error("No such type: {}", svtype);
			}
			
			Operator listOP = EventSubscription.Operator.valueOf(node.path("listoperator").asText());
			JsonNode childNode = node.path("subscription");
			EventSubscription child = parseJsonNode(childNode);
			
			ListOperatorEventSubscription lopes = new ListOperatorEventSubscription(node.path("subscribedkey").asText()
						, EventSubscription.Operator.valueOf(node.path("operator").asText()), node.path("value").asText()
						, EventSubscription.SubscribedValueCategory.valueOf(node.path("subscribedvaluecategory").asText())
						, EventSubscription.SubscriptionType.valueOf(node.path("subscriptiontype").asText())
						, vtype, child, listOP);

			return lopes;
		}
		else if(stype.equals("COMPOUND")){ //CompoundEventSubscription
			
	    	List<EventSubscription> eslist = new ArrayList<EventSubscription>();

	    	JsonNode childrenNodes = node.path("subscription");
	    	Iterator<JsonNode> iter = childrenNodes.elements();
	    	while(iter.hasNext()){
	    		JsonNode childNode = iter.next();
    			EventSubscription child = parseJsonNode(childNode);
    			eslist.add(child);
	    	}
	    	EventSubscription[] earray = new EventSubscription[eslist.size()];
	    	eslist.toArray(earray);
	    	CompoundEventSubscription ces = new CompoundEventSubscription(
	    			Operator.valueOf(node.path("operator").asText()),
	    			node.path("negated").asBoolean(), node.path("operationvaluetype").getClass(),
	    			SubscribedValueCategory.valueOf(node.path("subscribedvaluecategory").asText()), 
	    			EventSubscription.SubscriptionType.valueOf(node.path("subscriptiontype").asText()),
	    			earray);
			return ces;
		}
		else if(stype.equals("SCRIPT")){
			String script = node.path("subscriptionScript").asText();
			ScriptEventSubscription ses = new ScriptEventSubscription(script);
			return ses;
		}
		// add support for report subscription: by wxt 0404
		else if(stype.equals("REPORT")){
			String targetType = node.path("targetType").asText();
			String reporterType = node.path("reporterType").asText();
			String details = node.path("details").toString();
			ReportEventSubscription res = new ReportEventSubscription(targetType, reporterType, details);
			if(res.testComparetor()){
				return res;
			}
			else{
				throw new AppSubscriptionException("subscription format error,it can not create comparator!");
			}
		}
		else{
			log.error("Unknown subscription type: {}", stype);
			return null;
		}
*/
        switch(type){
            case NEW_REPORT_ITEM:
                ReportEventSubscription res = new ReportEventSubscription(null, null, node.toString());
                if(res.testComparetor()){
                    return res;
                }
                else{
                    throw new AppSubscriptionException("subscription format error,it can not create comparator!");
                }

            default:
                break;
        }

        return null;
	}
}
