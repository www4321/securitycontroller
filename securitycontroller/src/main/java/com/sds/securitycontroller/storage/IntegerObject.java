package com.sds.securitycontroller.storage;

import java.util.Map;

public class IntegerObject implements IDBObject{

	private static final long serialVersionUID = -7510771769560005518L;
	private Integer value;
	
	public IntegerObject(){		
	}
	
	public IntegerObject(Integer value){
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
		return new IntegerObject((Integer)resultSet.getValueByColumnIndex(1));
	}
	
	public Integer getValue(){
		return this.value;
	}

}
