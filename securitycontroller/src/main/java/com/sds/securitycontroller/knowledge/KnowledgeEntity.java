/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class KnowledgeEntity implements Serializable{

	private static final long serialVersionUID = -62478609227970899L;
	public Map<KnowledgeType, KnowledgeEntity> relatedEntityMap;//对应一对一
	public Map<KnowledgeType, Map<String, KnowledgeEntity>> affiliatedEntityListMap;//从属一对多
	public Map<KnowledgeEntityAttribute, Serializable> attributeMap;
	
	public KnowledgeEntity(){
		relatedEntityMap =new HashMap<KnowledgeType, KnowledgeEntity>();
		affiliatedEntityListMap= new HashMap<KnowledgeType, Map<String, KnowledgeEntity>>();  
		attributeMap = new HashMap<KnowledgeEntityAttribute, Serializable>();  
		
	}
	protected void initAffiliates(KnowledgeType[] affiliatedEntityTypes){
		for(int i=0;i<affiliatedEntityTypes.length;i++)
			this.affiliatedEntityListMap.put(affiliatedEntityTypes[i],new HashMap<String,KnowledgeEntity>());
	}
	protected KnowledgeType type;
	protected String id;

	public String getId() {
		return this.id;
	}

	public KnowledgeType getType() {
		return this.type;
	} 
	public void setType(KnowledgeType type) {
		this.type = type;
	} 
}
