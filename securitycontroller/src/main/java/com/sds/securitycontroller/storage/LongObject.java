package com.sds.securitycontroller.storage;

import java.util.Map;

public class LongObject implements IDBObject{

	private static final long serialVersionUID = -7510771769562005518L;
	private Long value;
	
	public LongObject(){		
	}
	
	public LongObject(Long value){
		this.value = value;
	}

	@Override
	public Map<String, Object> getDBElements() {
		return null;
	}

	@Override
	public Object getFieldValueByKey(String key) {
		return null;
	}

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {
		return new LongObject((Long)resultSet.getValueByColumnIndex(1));
	}
	
	public Long getValue(){
		return this.value;
	}

}
