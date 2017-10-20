package com.sds.securitycontroller.common;

import java.util.Map;

public class LeafExpression implements IExpression{
	//IExpression是一个树的结构，LeafExpression是树中的叶节点

	public enum LeafExpressionOperator{
		LT,//less than
		GT,//greater than
		EQ,//equal
		NLT,//not less than...
		NGT,
		NEQ,
	}
	
	public enum LeafExpressionType{
		STRING,
		INTEGER,
		DOUBLE,
	}
	
	private String attribute;
	private LeafExpressionOperator op;
	private Object value;
	private LeafExpressionType type;
	
	public LeafExpression(String attribute, LeafExpressionOperator op, Object value) throws Exception{
		this.attribute = attribute;
		this.op = op;
		this.value = value;
		if(value instanceof String)
			this.type = LeafExpressionType.STRING;
		else if(value instanceof Integer)
			this.type = LeafExpressionType.INTEGER;
		else if(value instanceof Float || value instanceof Double)
			this.type = LeafExpressionType.DOUBLE;
		else
			throw new Exception("type " + value.getClass().toString() + "not supported");
	}

	public String getAttribute() {
		return attribute;
	}

	public LeafExpressionOperator getOp() {
		return op;
	}

	public Object getValue() {
		return value;
	}

	public LeafExpressionType getType() {
		return type;
	}


	//positive for being complied
	//0 for not found,
	//negative for not being complied
	@Override
	public MatchResult match(Map<String, Object> object) throws Exception {
		if(object.containsKey(this.attribute)){
			Object objectValue = object.get(this.attribute);
			if(this.type == LeafExpressionType.STRING){
				if(this.op == LeafExpressionOperator.EQ)
					return objectValue.equals(this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
				else
					throw new Exception("Unsupported operation "+ this.op +" for string operation.");
			}
			else if(this.type == LeafExpressionType.INTEGER){
				if(this.op == LeafExpressionOperator.EQ)
					return objectValue.equals(this.value)?MatchResult.MATCH : MatchResult.UNMATCH;
				else if(this.op == LeafExpressionOperator.GT)
					return ((int)objectValue>(int)this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
				else if(this.op == LeafExpressionOperator.LT)
					return ((int)objectValue<(int)this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
				else if(this.op == LeafExpressionOperator.NEQ)
					return ((int)objectValue!=(int)this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
				else if(this.op == LeafExpressionOperator.NGT)
					return ((int)objectValue<=(int)this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
				else if(this.op == LeafExpressionOperator.NLT)
					return ((int)objectValue>=(int)this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
			}
			else if(this.type == LeafExpressionType.DOUBLE){
				if(this.op == LeafExpressionOperator.EQ)
					return objectValue.equals(this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
				else if(this.op == LeafExpressionOperator.GT)
					return ((double)objectValue>(double)this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
				else if(this.op == LeafExpressionOperator.LT)
					return ((double)objectValue<(double)this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
				else if(this.op == LeafExpressionOperator.NEQ)
					return ((double)objectValue!=(double)this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
				else if(this.op == LeafExpressionOperator.NGT)
					return ((double)objectValue<=(double)this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
				else if(this.op == LeafExpressionOperator.NLT)
					return ((double)objectValue>=(double)this.value)? MatchResult.MATCH : MatchResult.UNMATCH;
			}
			throw new Exception("Unsupported type "+ this.type +" for expression evalution.");
			
		}
		else
			return MatchResult.NOT_FOUND;
	}
	
	@Override
	public String toString(){
		return "{ attr: '"+ this.attribute + "',"
				+" op: "+ this.op + ","
				+" value: '"+ this.value + "',"
				+" type: "+ this.type + ""
				+ "}";
	}
	
	
}