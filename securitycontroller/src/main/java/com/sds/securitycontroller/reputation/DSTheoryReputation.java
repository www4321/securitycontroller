/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.reputation;

import com.sds.securitycontroller.log.Report;

public class DSTheoryReputation extends Reputation{

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
