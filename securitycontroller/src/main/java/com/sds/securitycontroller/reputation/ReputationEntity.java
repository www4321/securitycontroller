/** 
*    BUPT. 
**/ 
package com.sds.securitycontroller.reputation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.log.Report;
import com.sds.securitycontroller.reputation.Reputation.ReputationLevel;

/*
 * 
 * */

public class ReputationEntity {

	
	protected Reputation reputation;
	protected String subjectId;
	protected Object sourceObject;
	protected KnowledgeType type;

    protected static Logger log = LoggerFactory.getLogger(ReputationEntity.class);

    public ReputationEntity(String reputationId, Object sourceObject, 
    		KnowledgeType type, Reputation reputation){
    	this.subjectId = reputationId;
    	this.sourceObject = sourceObject;
    	this.type = type;
    	this.reputation = reputation;
    }
    

    public ReputationEntity(String reputationId, Object sourceObject, 
    		KnowledgeType type){
    	this.reputation = new DeterminedReputation();
    }
    
	public Reputation getReputation() {
		return this.reputation;
	}
	
	public String getReputationId(){
		return this.subjectId;
	}

	public KnowledgeType getType() {
		return type;
	}

	public Object getSourceObject() {
		return sourceObject;
	}

	public ReputationLevel getReputationLevel(){
		return this.reputation.getTrustLevel();
	}
	public boolean updateReputation(Report report){
		return this.reputation.updateReputation(report);		
	}	
}
