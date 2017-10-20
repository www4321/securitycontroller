/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.reputation;

import com.sds.securitycontroller.log.Report;

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
	public ReputationLevel getTrustLevel() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean updateReputation(Report report) {
		// TODO Auto-generated method stub
		return false;
	}
}
