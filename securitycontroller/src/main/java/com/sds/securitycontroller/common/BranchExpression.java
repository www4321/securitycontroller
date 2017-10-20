package com.sds.securitycontroller.common;

import java.util.List;
import java.util.Map;

public class BranchExpression implements IExpression{
	
	//IExpression是一个树的结构，BranchExpression是树中的中间节点
	
	
	public enum ExpressOperator{
		AND,
		OR
	}
	
	private List<IExpression> subExpressions;
	private ExpressOperator subOp;
	
	public BranchExpression(List<IExpression> subExpressions, ExpressOperator subOp){
		this.subExpressions = subExpressions;
		this.subOp = subOp;
	}

	public List<IExpression> getSubExpressions() {
		return subExpressions;
	}

	public ExpressOperator getSubOp() {
		return subOp;
	}

	@Override
	public MatchResult match(Map<String, Object> object) throws Exception{
		MatchResult result = MatchResult.NOT_FOUND;
		for(IExpression expression: this.subExpressions){
			MatchResult t = expression.match(object);
			if(t == MatchResult.UNMATCH && this.subOp == ExpressOperator.AND)
				return MatchResult.UNMATCH;
			else if(t == MatchResult.MATCH && this.subOp == ExpressOperator.OR)
				return MatchResult.MATCH;
			result = t;
		}
		return result;
	}
	
	String getExprsString(){
		StringBuilder sb = new StringBuilder();
		for(IExpression expr: this.subExpressions){
			sb.append(expr.toString());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1); //remove last comma
		return sb.toString();
	}
	
	@Override 
	public String toString(){
		return "{"
				+ "op : " + this.subOp +","
				+ "exprs: [" + getExprsString() + "]"
				+ "}";
	}
	
}
