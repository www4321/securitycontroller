/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.manager;

import java.util.Map;

import com.sds.securitycontroller.event.manager.IRPCHandler;
import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IKnowledgeBaseService extends ISecurityControllerService, IRPCHandler{

	KnowledgeEntity queryEntity(KnowledgeType queryEntityType,
			KnowledgeEntityAttribute queryAttrType,String queryAttrValue);
	
	Map<String,KnowledgeEntity> retrieveEntityList(KnowledgeType queryEntityType);
	
	KnowledgeEntity queryRelatedEntity(KnowledgeType inputEntityType,
			KnowledgeEntityAttribute inputAttrType,String inputAttrValue, 
			KnowledgeType outputEntityType);
	
	Map<String, KnowledgeEntity> queryAffliatedEntity(KnowledgeType inputEntityType,
			KnowledgeEntityAttribute inputAttrType,String inputAttrValue, 
			KnowledgeType outputEntityType);
	
	
	boolean isEntitiesRelated(KnowledgeType typeA,
			KnowledgeEntityAttribute attrTypeA,String attrValueA,
			KnowledgeType typeB,KnowledgeEntityAttribute attrTypeB,String attrValueB);
}
