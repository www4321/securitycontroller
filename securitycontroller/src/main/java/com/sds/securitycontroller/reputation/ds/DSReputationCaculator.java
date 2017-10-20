package com.sds.securitycontroller.reputation.ds;

import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.reputation.ReputationEntity;
import com.sds.securitycontroller.reputation.determined.DeterminedReputation;
import com.sds.securitycontroller.reputation.manager.IReputationCalculator;

public class DSReputationCaculator implements IReputationCalculator{
	private static DSReputationCaculator instance;
	private DSReputationCaculator (){
		
	}
	
	public static DSReputationCaculator getInstance(){
		if(instance == null)
			instance = new DSReputationCaculator();
		return instance;
	}
	
	@Override
	public ReputationEntity initReputationEntity(String id, Object target, KnowledgeType knowledgeType){
		return new ReputationEntity(id, target, knowledgeType, new DeterminedReputation());
	}

}
