/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.manager;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;
//import com.sds.securitycontroller.knowledge.networkcontroller.NetDevice;
import com.sds.securitycontroller.knowledge.cloud.CloudPort;
import com.sds.securitycontroller.knowledge.cloud.CloudTenant;
import com.sds.securitycontroller.knowledge.cloud.CloudVM;
import com.sds.securitycontroller.knowledge.cloud.CloudVM.AddressInfo;
import com.sds.securitycontroller.knowledge.cloud.CloudVM.NetworkInfo;
import com.sds.securitycontroller.utils.JsonUtils;

public class KnowledgeBaseResource extends ServerResource {
	
	protected static Logger log = LoggerFactory.getLogger(KnowledgeBaseResource.class);
	
	String methodName = null;
	IKnowledgeBaseService knowledgeBaseManager;
	String id = null;
	KnowledgeType type = null;
	KnowledgeType relatedType = null;
	KnowledgeType affiliatedType = null;
	String requestUserID = null;
	
	
	@Override  
    public void doInit() {    
		knowledgeBaseManager = 
                (IKnowledgeBaseService)getContext().getAttributes().
                get(IKnowledgeBaseService.class.getCanonicalName());
		methodName = (String) getRequestAttributes().get("methodname");
		id = (String) getRequestAttributes().get("id");
		requestUserID = (String) getRequestAttributes().get("user_id");
		
		try {
			if(getRequestAttributes().get("domain")!=null)
				type = parseKnowledgeType((String) getRequestAttributes().get("domain")
					,(String) getRequestAttributes().get("type"));
			else{
				String domain = getQuery().getValues("domain");
				String subType= (String) getRequestAttributes().get("type");
				if(domain==null)
					domain=getDefaultDomain(subType);
				type = parseKnowledgeType(domain,subType);
			}
		} catch (Exception e) {
			type=null;
		}
		try {
			if(getRequestAttributes().get("related_domain")!=null)
				relatedType =  parseKnowledgeType((String) getRequestAttributes().get("related_domain")
					,(String) getRequestAttributes().get("related_type"));
			else{
				String domain = getQuery().getValues("related_domain");
				String subType= (String) getRequestAttributes().get("related_type");
				if(domain==null)
					domain=getDefaultDomain(subType);
				relatedType = parseKnowledgeType(domain,subType);
			}
		} catch (Exception e) {
			relatedType=null;
		}
		try {
			if(getRequestAttributes().get("affiliated_domain")!=null)
				affiliatedType = //KnowledgeType.valueOf(( (String) getRequestAttributes().get("affiliated_type")).toUpperCase());
					parseKnowledgeType((String) getRequestAttributes().get("affiliated_domain")
							,(String) getRequestAttributes().get("affiliated_type"));
			else{
				String domain = getQuery().getValues("affiliated_domain");
				String subType= (String) getRequestAttributes().get("affiliated_type");
				if(domain==null)
					domain=getDefaultDomain(subType);
				affiliatedType = parseKnowledgeType(domain,subType);
			}
		} catch (Exception e) {
			affiliatedType = null;
		}
    } 

	String getDefaultDomain(String subType){
		if(subType==null)
			return null;
		if( subType.equals("vm") || 
			subType.equals("user") || 
			subType.equals("tenant") || 
			subType.equals("port") ){
				return "cloud";
		}
		else if( subType.equals("switch") || 
				subType.equals("topology") || 
				subType.equals("device") ){
				return "network";
		}

		return null;
		
	}
	
	KnowledgeType parseKnowledgeType(String domain,String subType){
		try {
			StringBuffer buffer=new StringBuffer();
			buffer.append(domain.toUpperCase()).append('_').append(subType.toUpperCase());
			return KnowledgeType.valueOf(buffer.toString());
		} catch (Exception e) {
			return null;//KnowledgeType.UNDEFINED;
		}
	}
	
	
	//liuwenmao
	@Get("json")
    public Object handleGetRequest() {

    	if(this.type == null){
            return "{\"status\" : \"error\", \"result\" : \"knowledge type missing. \"}";
    	}
    	Date d1,d2;
    	d1= new Date();
    	JsonFactory jasonFactory = new JsonFactory();
        StringWriter writer = new StringWriter();
    	
    	if(this.id != null){
    		if(this.relatedType!=null){
    			// get related type
    			try {
    				JsonGenerator generator = jasonFactory.createGenerator(writer);
                	//{
    				generator.writeStartObject();
                	//"status":"ok",
                	generator.writeStringField("status", "ok");
                	//"result":{
                	generator.writeObjectFieldStart("result");
                	KnowledgeEntity relatedEntity = knowledgeBaseManager.queryRelatedEntity(
                			type,KnowledgeEntityAttribute.ID, id, this.relatedType);
            		for(Entry<KnowledgeEntityAttribute, Serializable> entry:relatedEntity.attributeMap.entrySet()){
                    	generator.writeStringField(entry.getKey().toString().toLowerCase(), entry.getValue().toString());
                	}
                	//}
                	generator.writeEndObject();
                	generator.close();
                	
				} catch (IOException e) {
                    log.error("json conversion failed: ", e.getMessage());
                    return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "+e.getMessage()+" \"}"; 
                }catch (Exception e) {
                    log.error("getting knowledge failed: ", e.getMessage());
                    return "{\"status\" : \"error\", \"result\" : \"getting knowledge entity failed: "+e.getMessage()+"\"}"; 
                }   
    		}
    		else if(this.affiliatedType!=null){
    			// get related type
    			try {
    				JsonGenerator generator = jasonFactory.createGenerator(writer);
                	//{
    				generator.writeStartObject();
                	//"status":"ok",
                	generator.writeStringField("status", "ok");
                	//"result":[
//                	generator.writeObjectFieldStart("result");
                	generator.writeArrayFieldStart("entities");
           
                	Map<String, KnowledgeEntity>affiliatedEntityList = knowledgeBaseManager.queryAffliatedEntity
                			(type,KnowledgeEntityAttribute.ID, id, this.affiliatedType);
                	if(affiliatedEntityList!=null){
                		for(KnowledgeEntity entity:affiliatedEntityList.values()){
                			//{
                			generator.writeStartObject();
                			for(Entry<KnowledgeEntityAttribute, Serializable> entry:entity.attributeMap.entrySet()){
                            	generator.writeStringField(entry.getKey().toString().toLowerCase(), entry.getValue().toString());
                        	}
                			//}
                			generator.writeEndObject();
                		}
                		
                	}  
                	//]
            		generator.writeEndArray();
                	generator.close();
                	
				} catch (IOException e) {
                    log.error("json conversion failed: ", e.getMessage());
                    return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "+e.getMessage()+" \"}"; 
                }catch (Exception e) {
                    log.error("getting knowledge failed: ", e.getMessage());
                    return "{\"status\" : \"error\", \"result\" : \"getting knowledge entity failed: "+e.getMessage()+"\"}"; 
                }   
    		}
    		else{
				try{
                	JsonGenerator generator = jasonFactory
                            .createGenerator(writer);
                	generator.writeStartObject();
                	generator.writeStringField("status", "ok");
                	generator.writeObjectFieldStart("result");
                	KnowledgeEntity entity = knowledgeBaseManager.queryEntity(type, 
                			KnowledgeEntityAttribute.ID, id);
                	d2 = new Date();
                	log.warn("-----------------get entity time used:{}",d2.getTime()-d1.getTime());
                	if(entity!= null){
                		generator.writeObjectFieldStart("entity");
                    	for(Entry<KnowledgeEntityAttribute, Serializable> entry:entity.attributeMap.entrySet()){
                        	generator.writeStringField(entry.getKey().toString().toLowerCase(), entry.getValue().toString());
                    	}
                    	generator.writeStringField("type", entity.getType().toString());

                    	generator.writeObjectFieldStart("affiliated");
                    	for(Entry<KnowledgeType, Map<String, KnowledgeEntity>> entry:entity.affiliatedEntityListMap.entrySet()){
                    		generator.writeArrayFieldStart(entry.getKey().toString());
                    		Set<String> affiliatedEntities = entry.getValue().keySet();
                    		for(String affiliatedEntityId: affiliatedEntities)
                    			generator.writeString(affiliatedEntityId);
                    		generator.writeEndArray();
                    	}
                    	generator.writeEndObject();
                    	generator.writeObjectFieldStart("related");
                    	for(Entry<KnowledgeType, KnowledgeEntity> entry:entity.relatedEntityMap.entrySet()){
                    		generator.writeStringField(entry.getKey().toString(), entry.getValue().getId());
                    	}
                    	generator.writeEndObject();
                    	generator.writeEndObject();
                	}
                	generator.writeEndObject();
                	generator.writeEndObject();

                	generator.close();
                } catch (IOException e) {
                    log.error("json conversion failed: ", e.getMessage());
                    return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "+e.getMessage()+" \"}"; 
                }catch (Exception e) {
                    log.error("getting app failed: ", e.getMessage());
                    return "{\"status\" : \"error\", \"result\" : \"getting knowledge entity failed: "+e.getMessage()+"\"}"; 
                }   
                Date d3 = new Date();
            	log.warn("-----------------get entity time used:{}",d3.getTime()-d2.getTime());
    		}
    	}
    	else{
    		//TODO 0612
			if(type==KnowledgeType.SECURITY_DEV_TOPOLOGY){
				try {
					JsonGenerator generator = jasonFactory
                            .createGenerator(writer);
                	//{
					generator.writeStartObject();
                	//"start":"ok",
                	generator.writeStringField("status", "ok");
                	generator.writeStringField("hash_id", KnowledgeBaseManager.mapCreatedMark);
                	
                	//"result":{
                	generator.writeObjectFieldStart("result");
                	Map<String,KnowledgeEntity> routersMap = knowledgeBaseManager.retrieveEntityList(KnowledgeType.CLOUD_ROUTER);
            		// routers [
            		generator.writeArrayFieldStart("routers");
            		
             		for(KnowledgeEntity router: routersMap.values()){
             			if(requestUserID!=null && !isEntityAccessibleForUser(router,requestUserID) )
             				continue;
//             			if(ii==0)
//             				break;
             			//{
	             		generator.writeStartObject();
//    	                 	generator.writeStringField("type", entity.getType()==null?null:entity.getType().toString());
	                 	for(Entry<KnowledgeEntityAttribute, Serializable> entry:router.attributeMap.entrySet()){
	                     	generator.writeStringField(entry.getKey().toString().toLowerCase(), entry.getValue().toString());
	                 	}
	                 	// networks [
	                 	generator.writeArrayFieldStart("networks");
	                 	Map<String, KnowledgeEntity> networksMap = router.affiliatedEntityListMap.get(KnowledgeType.CLOUD_NETWORK);
	                 	if(networksMap!=null){
	                 		for(KnowledgeEntity network:networksMap.values()){

	                 			if(requestUserID!=null && !isEntityAccessibleForUser(network,requestUserID) )
	                 				continue;
	                 			//{
	                 			generator.writeStartObject();
	                 			for(Entry<KnowledgeEntityAttribute, Serializable> nwAttr:network.attributeMap.entrySet()){
        	                     	generator.writeStringField(nwAttr.getKey().toString().toLowerCase(), nwAttr.getValue().toString());
	                 			}
    	                     	// subnets [
    	                     	generator.writeArrayFieldStart("subnets");
    	                     	Map<String, KnowledgeEntity> subnetsMap = network.affiliatedEntityListMap.get(KnowledgeType.CLOUD_SUBNET);
    	                     	if(subnetsMap!=null){
    	                     		for(KnowledgeEntity subnet:subnetsMap.values()){
    	                     			if(requestUserID!=null && !isEntityAccessibleForUser(subnet,requestUserID) )
    		                 				continue;
    	                     			//{
    	                     			generator.writeStartObject();
    	                     			for(Entry<KnowledgeEntityAttribute, Serializable> subnetAttr:subnet.attributeMap.entrySet()){
                	                     	generator.writeStringField(subnetAttr.getKey().toString().toLowerCase(), subnetAttr.getValue().toString());
    	                     			}
            	                     	// security device [
    	                     			generator.writeArrayFieldStart("security_devices");
            	                     	Map<String, KnowledgeEntity> secDevsMap = subnet.affiliatedEntityListMap.get(KnowledgeType.SECURITY_DEVICE);
            	                     	if(secDevsMap!=null){
            	                     		for(KnowledgeEntity secDev:secDevsMap.values()){
            	                     			//{
            	                     			generator.writeStartObject();
            	                     			for(Entry<KnowledgeEntityAttribute, Serializable> secDevAttr:secDev.attributeMap.entrySet()){
            	                     				generator.writeStringField(secDevAttr.getKey().toString().toLowerCase(), secDevAttr.getValue().toString());
                	                     		}
            	                     			//}
            	                     			generator.writeEndObject();
            	                     		}
    	                     			}
            	                     	//],
            	                     	generator.writeEndArray();
            	                     	// virtual machines:[
            	                     	generator.writeArrayFieldStart("virtual_machines");
            	                     	Map<String, KnowledgeEntity> vmsMap = subnet.affiliatedEntityListMap.get(KnowledgeType.CLOUD_VM);
            	                     	if(vmsMap!=null){
            	                     		for(KnowledgeEntity vmEntity:vmsMap.values()){
            	                     			//{
            	                     			generator.writeStartObject();
            	                     			CloudVM vm = (CloudVM)vmEntity;
            	                     			generator.writeStringField("id",vm.getId());
            	                     			generator.writeStringField("name",vm.getName());
            	                     			String fixedIP=null,floatingIP=null;
            	                     			try {
            	                     				for( NetworkInfo nwif:vm.getNetworks().values()){
            	                     					for(AddressInfo adInfo:nwif.getAddresses()){
            	                     						if(adInfo!=null){
            	                     							if(adInfo.getIpsType().equals("fixed"))
                	                     							fixedIP=adInfo.getAddr();
            	                     							else if(adInfo.getIpsType().equals("floating"))
            	                     								floatingIP=adInfo.getAddr();
            	                     						}
            	                     					}
            	                     				}
												} catch (Exception e) {
													e.printStackTrace();
													fixedIP=null;
	            	                     			floatingIP=null;
												}
            	                     			generator.writeStringField("fixed_ip",fixedIP);
            	                     			generator.writeStringField("floating_ip",floatingIP);
            	                     			CloudPort cloudPort =  (CloudPort)vm.relatedEntityMap.get(KnowledgeType.CLOUD_PORT);
            	                     			if(cloudPort!=null){
            	                     				generator.writeStringField("mac",cloudPort.getMac());
            	                     			}
            	                     			else
            	                     				generator.writeStringField("mac",null);
            	                     			//}
            	                     			generator.writeEndObject();
            	                     		}
            	                     	}
            	                     	//]
            	                     	generator.writeEndArray();
            	                     	//}
            	                     	generator.writeEndObject();
    	                     		}
        	                 	}
    	                     	//]
    	                     	generator.writeEndArray();
    	                     	//}
    	                     	generator.writeEndObject();
	                 		}
	                 	}
	                 	// ]
	                 	generator.writeEndArray();
	                 	//}
	                 	generator.writeEndObject();
             		}
             		// ]
             		generator.writeEndArray();//entities
                	//}
                	generator.writeEndObject();
                	//}
                	generator.writeEndObject();
                 	generator.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try{
	             	JsonGenerator generator = jasonFactory
	                         .createGenerator(writer);
	             	generator.writeStartObject();
	             	generator.writeStringField("status", "ok");
	             	generator.writeObjectFieldStart("result");
	             	Map<String,KnowledgeEntity> entities = knowledgeBaseManager.retrieveEntityList(type);
	             	if(entities!= null){
	             		generator.writeArrayFieldStart("entities");
	             		for(KnowledgeEntity entity: entities.values()){             		
		             		generator.writeStartObject();
		                 	generator.writeStringField("type", entity.getType()==null?null:entity.getType().toString());
		                 	
		                 	for(Entry<KnowledgeEntityAttribute, Serializable> entry:entity.attributeMap.entrySet()){
		                     	generator.writeStringField(entry.getKey().toString().toLowerCase(), entry.getValue().toString());
		                 	}
		
		                 	generator.writeObjectFieldStart("affiliated");
		                 	for(Entry<KnowledgeType, Map<String, KnowledgeEntity>> entry:entity.affiliatedEntityListMap.entrySet()){
		                 		generator.writeArrayFieldStart(entry.getKey().toString());
		                 		Set<String> affiliatedEntities = entry.getValue().keySet();
		                 		for(String affiliatedEntityId: affiliatedEntities)
		                 			generator.writeString(affiliatedEntityId);
		                 		generator.writeEndArray();
		                 	}
		                 	generator.writeEndObject();
		                 	
		
		                 	generator.writeObjectFieldStart("related");
		                 	for(Entry<KnowledgeType, KnowledgeEntity> entry:entity.relatedEntityMap.entrySet()){
		                 		generator.writeStringField(entry.getKey().toString(), entry.getValue().getId());
		                 	}
		                 	generator.writeEndObject();//related
		                 	
		                 	generator.writeEndObject();//entity
	             		}
	             		generator.writeEndArray();//entities
	             	}
	             	generator.writeEndObject();
	             	generator.writeEndObject();

	             	generator.close();
	             } catch (IOException e) {
	                 log.error("json conversion failed: ", e.getMessage());
	                 return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "+e.getMessage()+" \"}"; 
	             }catch (Exception e) {
	            	 e.printStackTrace();
	                 log.error("getting app failed: ", e.getMessage());
	                 return "{\"status\" : \"error\", \"result\" : \"getting knowledge entity failed: "+e.getMessage()+"\"}"; 
	             }    
			}
    	}
        String resp =  writer.toString();
        return resp;
    }
	
	/**
	 * format:
	 * {""}
	 * 
	 * 
	 * 
	 * 
	 */
	@SuppressWarnings("unused")
	@Post
    public String handlePostRequest(String fmJson) {
    	String status = "";
    	KnowledgeEntity knowledgeEntity = null;
        try {
			Map<String, String> req = JsonUtils.decodeJsonToMap(fmJson);

        	//knowledgeEntity = new KnowledgeEntity();
        	//TODO create a new knowledge entity
        	status = "ok";
        } catch (IOException e) {
            log.error("Error parsing new app: " + fmJson, e);
            status = "error! Could not parse new app, see log for details.";
        } catch (Exception e) {
            log.error("Error creating new app: ", e);
            status = "error! Could not parse new app, see log for details."; 
        }

        // to be completed... invoke method
        
        //
        JsonFactory jasonFactory = new JsonFactory();
        StringWriter writer = new StringWriter();
        try{
        	ObjectMapper mapper =  new ObjectMapper();
        	JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject();
        	generator.writeStringField("status", status);        	
            mapper.writeValue(generator, knowledgeEntity); 
            generator.writeEndObject();
            generator.close();
        }
        catch (IOException e) {
            log.error("json conversion failed: ", e.getMessage());
            return "{\"status\" : \"error\", \"result\" : \"json conversion failed. \"}"; 
        }
        return writer.toString();
    }
	
	private boolean isEntityAccessibleForUser(KnowledgeEntity entity,String userID){
		CloudTenant tenant = (CloudTenant)entity.relatedEntityMap.get(KnowledgeType.CLOUD_TENANT);
		if(tenant==null)
			return false;
		KnowledgeEntity userEntity= tenant.affiliatedEntityListMap.get(KnowledgeType.CLOUD_USER).get(userID);
		return (userEntity!=null);
	}
	

}
