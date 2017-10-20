/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.reputation;

import com.sds.securitycontroller.log.Report;

/*
 *  
 * */
public abstract class Reputation {
	public enum ReputationType{
		DETERMINED,
		DSTHEORY,
		OTHER
	}
	
	public enum ReputationLevel{
		HIGHLY_TRUSTED,
		FAIR_TRUSTED,
		LOW_TRUSTED,
		UNTRUSTED
	}
	
	ReputationType type;
	public ReputationType getType(){
		return this.type;
	}
	
	@Override
	public String toString(){
		return getTrustLevel().toString();
	}

	public abstract ReputationLevel getTrustLevel();
	public abstract boolean updateReputation(Report report);
}
