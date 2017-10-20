/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.reputation.ds;

import com.sds.securitycontroller.log.Report;
import com.sds.securitycontroller.reputation.Reputation;

public class DSReputation extends Reputation{

	double belief;
	double plausibility;
	double undetermination;
	
	public double getBelief(){
		return this.belief;
	}
	

	public double getPlausibility(){
		return this.plausibility;
	}
	

	public double getUndetermination(){
		return this.undetermination;
	}

	@Override
	public ReputationLevel getTrustLevel(){
		if(getBelief()>0.5 && getUndetermination()>0.8)
			return ReputationLevel.HIGHLY_TRUSTED;
		else if(getBelief()>0.3 && getUndetermination()>0.5)
			return ReputationLevel.FAIR_TRUSTED;
		else if(getBelief()>0.1 && getUndetermination()>0.3)
			return ReputationLevel.LOW_TRUSTED;
		return ReputationLevel.UNTRUSTED;
		
	}


	@Override
	public boolean updateReputation(Report report) {
		//TODO how to update
		this.belief *= 0.7;
		return false;
	}
}
