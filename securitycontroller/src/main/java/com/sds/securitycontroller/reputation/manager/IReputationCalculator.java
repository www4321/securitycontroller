package com.sds.securitycontroller.reputation.manager;

import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.reputation.ReputationEntity;

public interface IReputationCalculator {
	ReputationEntity initReputationEntity(String id, Object target, KnowledgeType knowledgeType);

}
