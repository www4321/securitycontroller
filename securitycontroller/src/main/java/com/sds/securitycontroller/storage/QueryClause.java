package com.sds.securitycontroller.storage;

import java.util.Arrays;
import java.util.List;

import com.sds.securitycontroller.storage.QueryClauseItem.OpType;

public class QueryClause {
	public enum  QueryClauseType{
		CLAUSE_ITEMS,
		STRING,
		EMPTY,
	}
	
	QueryClauseType type;
	
	List<QueryClauseItem> items;
	QueryClauseItemType op;
	public enum QueryClauseItemType{
		AND,
		OR,
	}
	
	String condition;
	
	//return object class
	IDBObject dbobject;
	
	String tableName;	
	//return columns, null for all
	String[] columnNames;
	//like limit..
	String extra;
	RowOrdering ordering;
		
	public QueryClause(String tableName){
		this.tableName = tableName;
		this.type = QueryClauseType.EMPTY;		
	}
	

	public QueryClause(List<QueryClauseItem> items,  String tableName, String[] columnNames, RowOrdering ordering){
		this(items, QueryClauseItemType.AND, tableName, columnNames, ordering);
		
	}
	
	public QueryClause(List<QueryClauseItem> items, QueryClauseItemType op, String tableName, String[] columnNames, RowOrdering ordering){
		this.type = QueryClauseType.CLAUSE_ITEMS;
		this.items = items;
		this.op = op;
		this.tableName = tableName;
		this.columnNames = columnNames;
		this.ordering = ordering;
		
	}
	
	public QueryClause(String condition, String tableName, String[] columnNames, RowOrdering ordering){
		this.type = QueryClauseType.STRING;
		this.condition = condition;
		this.tableName = tableName;
		this.columnNames = columnNames;
		this.ordering = ordering;
		
	}
	public QueryClause(String condition, String tableName, String[] columnNames, RowOrdering ordering, String extra){
		this.type = QueryClauseType.STRING;
		this.condition = condition;
		this.tableName = tableName;
		this.columnNames = columnNames;
		this.extra = extra;
		this.ordering = ordering;
	}

	public QueryClause(String key, Object value, String tableName, String[] columnNames, RowOrdering ordering){		
		this(Arrays.asList(new QueryClauseItem(key, value, OpType.EQ)), QueryClauseItemType.AND, tableName, columnNames, ordering);
		
	}

	public QueryClauseItemType getOp(){
		return this.op;
	}

	public QueryClauseType getType() {
		return type;
	}

	public void setType(QueryClauseType type) {
		this.type = type;
	}

	public List<QueryClauseItem> getItems() {
		return items;
	}

	
	public String getCondition() {
		return condition;
	}


	public void setCondition(String condition) {
		this.condition = condition;
	}


	public void setItems(List<QueryClauseItem> items) {
		this.items = items;
	}
	
	public IDBObject getDbobject() {
		return dbobject;
	}

	public void setDbobject(IDBObject dbobject) {
		this.dbobject = dbobject;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public RowOrdering getOrdering() {
		return ordering;
	}

	public void setOrdering(RowOrdering ordering) {
		this.ordering = ordering;
	}
	
	public String getExtra() {
		return extra;
	}


	@Override
	public String toString(){
		if(this.type == QueryClauseType.EMPTY)
			return "";
		else if(this.type == QueryClauseType.STRING)
			return this.condition;
		else if(this.type == QueryClauseType.CLAUSE_ITEMS)
			return getItemString();
		else
			return null;
		
	}
	
	
	
	
	
	protected Object convertString(Object obj){
		if("now()".equals(obj) || "NOW()".equals(obj))				
			return obj;
		else if(obj instanceof String || obj instanceof Enum)
			return "'" + obj + "'";
		else
			return obj;
	}
	private String getItemString(){		 
		String result = "";        
        for(QueryClauseItem item: this.items){
        	if(!result.isEmpty())
        		result += " and ";
        	if(item.getOp() == OpType.EQ){
        		result += (item.getKey() + "=" + convertString(item.getValue()));
        	}
        	else if(item.getOp() == OpType.GT){
        		result += (item.getKey() + ">" + convertString(item.getValue()));
        	}
        	else if(item.getOp() == OpType.LT){
        		result += (item.getKey() + "<" + convertString(item.getValue()));
        	}
        	else if(item.getOp() == OpType.NE){
        		result += (item.getKey() + "<>" + convertString(item.getValue()));
        	}
        	else{        		
        		continue;
        	}            	
        }
        return result;
	}
	
	
}
