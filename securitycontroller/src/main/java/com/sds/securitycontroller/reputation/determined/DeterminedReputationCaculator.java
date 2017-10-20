package com.sds.securitycontroller.reputation.determined;

import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.reputation.ReputationEntity;
import com.sds.securitycontroller.reputation.manager.IReputationCalculator;

public class DeterminedReputationCaculator implements IReputationCalculator{

	private static DeterminedReputationCaculator instance;
	private DeterminedReputationCaculator (){
		
	}
	
	public static DeterminedReputationCaculator getInstance(){
		if(instance == null)
			instance = new DeterminedReputationCaculator();
		return instance;
	}
	
	
	
	@Override
	public ReputationEntity initReputationEntity(String id, Object target, KnowledgeType knowledgeType){
		return new ReputationEntity(id, target, knowledgeType, new DeterminedReputation());
		
	}
	

}
