package com.sds.securitycontroller.storage;

public class QueryClauseItem {
	String key;
	Object value;
	OpType op;
	public enum OpType{
		LT,
		EQ,
		GT,
		LTE,
		GTE,
		NE,
		LIKE,
	}
	
	public QueryClauseItem(String key, Object value, OpType op){
		this.key = key;
		this.value = value;
		this.op = op;
	}

    public String parseOperator(OpType op){
    	String res;
    	switch (op){
    		case  EQ:
    			res = "=";
    			break;
    		case LT:
    			res = "<";
    			break;
    		case LTE:
    			res = "<=";
    			break;
    		case GT:
    			res = ">";
    			break;
    		case GTE:
    			res = ">=";
    			break;
    		case LIKE:
    			res = "LIKE";
    		default:
    			res = "=";
    			break;
    	}
    	return res;
    	
    }

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public OpType getOp() {
		return op;
	}
    
    
}
