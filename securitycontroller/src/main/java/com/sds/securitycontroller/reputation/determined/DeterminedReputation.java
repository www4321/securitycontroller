/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.reputation.determined;

import com.sds.securitycontroller.log.Report;
import com.sds.securitycontroller.reputation.Reputation;

public class DeterminedReputation extends Reputation{

	double value;
	public double getValue(){
		return this.value;
	}
	

	public void setValue(double value){
		this.value = value;
	}
	
	public DeterminedReputation(){
		this.value = 0.9f; //init value
	}


	@Override
	public ReputationLevel getTrustLevel(){
		if(getValue()>0.7f)
			return ReputationLevel.HIGHLY_TRUSTED;
		else if(getValue()>0.5)
			return ReputationLevel.FAIR_TRUSTED;
		else if(getValue()>0.3)
			return ReputationLevel.LOW_TRUSTED;
		return ReputationLevel.UNTRUSTED;
		
	}


	@Override
	public boolean updateReputation(Report report) {
		this.value *= 0.7f;
		return false;
	}
	
}
