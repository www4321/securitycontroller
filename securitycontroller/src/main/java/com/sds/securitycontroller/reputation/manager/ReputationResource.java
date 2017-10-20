/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.reputation.manager;

import java.io.IOException;
import java.io.StringWriter;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sds.securitycontroller.reputation.Reputation;
import com.sds.securitycontroller.reputation.Reputation.ReputationType;
import com.sds.securitycontroller.reputation.ReputationEntity;
import com.sds.securitycontroller.reputation.determined.DeterminedReputation;
import com.sds.securitycontroller.reputation.ds.DSReputation;

public class ReputationResource extends ServerResource implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log = LoggerFactory.getLogger(ReputationResource.class);
	String id = null;
	IReputationManagementService reputationManager = null;
	

	@Override  
    public void doInit() {    
        reputationManager = 
                (IReputationManagementService)getContext().getAttributes().
                get(IReputationManagementService.class.getCanonicalName());
        id = (String) getRequestAttributes().get("id");
       
    }  
	
	@Get
	public String getReputation() {
		String status = "ok";
		ReputationEntity entity = reputationManager.getReputationEntity(id);

		JsonFactory jasonFactory = new JsonFactory();
		StringWriter writer = new StringWriter();
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject();
			generator.writeStringField("status", status);
			if(entity != null){
				generator.writeStringField("id", entity.getReputationId());
				Reputation reputation = entity.getReputation();
				generator.writeStringField("trust", reputation.toString());
				generator.writeString("reputation");
				generator.writeStartObject();

				if(reputation.getType() == ReputationType.DETERMINED){
					DeterminedReputation dr = (DeterminedReputation)reputation;
					generator.writeNumberField("value", dr.getValue());
				}
				else if(reputation.getType() == ReputationType.DSTHEORY){
					DSReputation ur = (DSReputation)reputation;
					generator.writeNumberField("belief", ur.getBelief());
					generator.writeNumberField("plausibility", ur.getPlausibility());
					generator.writeNumberField("undetermination", ur.getUndetermination());
				}
				generator.writeEndObject();
			}
			generator.writeEndObject();
			generator.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "{\"status\" : \"error\", \"details\" : \"json conversion failed. \"}";
		}
		return writer.toString();
	}


}
