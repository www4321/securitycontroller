package com.sds.securitycontroller.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sds.securitycontroller.storage.CommonTool;


class ManyTreeNode<Type extends Object>{
	
	private Object data;
	private List<Object> childList;
	
	public ManyTreeNode(Type d){
		this.data = d;
		this.childList = new ArrayList<Object>();
	}
	
	public Object getData(){
		return this.data;
	}
	
	public List<Object> getChildList(){
		return this.childList;
	}
	
	public void setChildList(List<Object> obj){
		this.childList = obj;
	}
}




public class LogQuery {
	
	protected static Logger log = LoggerFactory.getLogger(QueryItem.class);
	
	protected enum Operator{
		NQ,
		EQ,
		LT,
		LE,
		GT,
		GE,
		AND,
		OR,
		IN,
		NI,
	}
	
	class QueryItem{
		
		protected Map<Operator,String> operMap = new HashMap<Operator, String>(){
			private static final long serialVersionUID = -8131748649027664519L;

		{
				  put(Operator.NQ,	"$ne"); //({"name":{"$ne":"stephen1"}})
				  put(Operator.EQ,	""); // {"name":"stephen","age":35}
				  put(Operator.LT, "$lt");
				  put(Operator.LE, "$lte");
				  put(Operator.GT, "$gt");
				  put(Operator.GE, "$gte"); //{"age":{"GTe":18, "LTe":40}}
				  put(Operator.AND, ""); //{"age":{"GTe":18, "LTe":40}}
				  put(Operator.OR, "$or"); //{"OR", []} , {"OR": [{"name":"stephen1"}, {"age":35}]}
				  put(Operator.IN, "$in"); //":{"IN":["stephen","stephen1"]}})
				  put(Operator.NI, "$nin"); //{"category":{"$nin":["stephen","stephen1"]}})
		}};  

		
		protected String key = "";
		protected String value = null;
		protected ManyTreeNode<Object> operTree = null;
		
		public QueryItem(String k, String v){
			this.key = k.trim();
			this.value = v.trim();
		}
		
		@SuppressWarnings("unchecked")
		public ManyTreeNode<Object> createTree(){
			try{
				if(this.value.contains(Operator.OR.toString())){
					String[] result = this.getOption(Operator.OR, this.value);
					if(result.length >= 1){
						operTree = new ManyTreeNode<Object>(Operator.OR);
							for(String str: result){
								operTree.getChildList().add(this.createAndTree(str));
							}
					}
					else{
						System.out.println("Format wrong");
					}
				}
				else if(this.value.contains(Operator.AND.toString())){
					operTree = (ManyTreeNode<Object>)this.createAndTree(this.value);
				}
				else{
					operTree = this.processSingle(this.value);
				}
				return operTree;
			}
			catch(Exception e){
				log.error("CreateQuery error: {}", e.getMessage());
				return null;
			}
		}
		
		public Object createQuery(){
		
			try{
				this.operTree = this.createTree();
				return this.SerializeTree(this.operTree, true);
			}
			catch(Exception e){
				log.error("CreateQuery error: {}", e.getMessage());
				return null;
			}
			
		}
		
		
		protected  Object createAndTree(String andStr){
			String[] result = this.getOption(Operator.AND, andStr);
			if(result.length == 1){
				return andStr.trim();
			}
			else
			{
				ManyTreeNode<Object> root = new ManyTreeNode<Object>(Operator.AND);
				for(String str: result){
					Object obj = this.processOneItem(str);
					obj = obj == null ? str.trim() : obj;
					root.getChildList().add(obj);
				}
				
				return root;
			}
		}
		
		@SuppressWarnings("unchecked")
		protected ManyTreeNode<Object> processSingle(String input){
			ManyTreeNode<Object> node;
			Object obj = this.processOneItem(input);
			if(obj == null){
				node = new ManyTreeNode<Object>(Operator.EQ);
				node.getChildList().add(input);
			}
			else{
				node = (ManyTreeNode<Object>)obj;
			}
			
			return node;
		}
		

		protected Object convertValueToObject(String value){
			try{
				int len = value.length();
				if(value.charAt(0) == '\'' && value.charAt(len-1) == '\''){
					return value.substring(1, len-1);
				}
				else{
					return Integer.parseInt(value);
				}
			}
			catch (Exception e){
				return value;
			}
					
		}
		
		protected Object processOneItem(String strNode){
			for(Operator oper : Operator.values()){
				if(strNode.indexOf(oper.toString()) >= 0){
					//
					String[] re = this.getOption(oper, strNode);
					ManyTreeNode<Object> root = new ManyTreeNode<Object>(oper);
					for(String str: re){
						String newStr = str.trim();
						if(!newStr.isEmpty()){
							switch (oper) {
							case LT:
							case LE:
							case GT:
							case GE:
								try {
									if (this.key == "time") {
										root.getChildList().add(CommonTool.GetDateFromStr(newStr));
									} else {
										int i = Integer.parseInt(newStr);
										root.getChildList().add(i);
									}
								} catch (Exception e) {
//									e.printStackTrace();
									return null;
								}
								break;
							default:
								root.getChildList().add(newStr);
							}
						}
					}
					
					return root;
				}
			}
			
			return null;
		}
		
		
		protected String[] getOption(Operator oper, String input){
			
			String[] strArr = input.split(oper.toString());
			if(strArr.length <=1){
				return new String[] {input};
			}
			else{
				return strArr;
			}	
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected Object SerializeTree(ManyTreeNode<Object> root, boolean bOnce){
			if(root.getChildList().size() < 0){
				return null;
			}
						
			BasicDBObject queryObj = new BasicDBObject();
			switch((Operator)root.getData()){ 
			case NQ:
				queryObj.put(this.key, new BasicDBObject(this.operMap.get(root.getData()), root.getChildList().get(0)));
				return queryObj;
			case EQ:
				queryObj.put(this.key,  root.getChildList().get(0));
				return queryObj;
			case LT:
				queryObj.put(this.operMap.get(root.getData()),  root.getChildList().get(0));
				return bOnce ? new BasicDBObject(this.key, queryObj) : queryObj;
			case LE:
				queryObj.put(this.operMap.get(root.getData()),  root.getChildList().get(0));
				return bOnce ? new BasicDBObject(this.key, queryObj) : queryObj;
			case GT:
				queryObj.put(this.operMap.get(root.getData()),  root.getChildList().get(0));
				return bOnce ? new BasicDBObject(this.key, queryObj) : queryObj;
			case GE:
				queryObj.put(this.operMap.get(root.getData()),  root.getChildList().get(0));
				return bOnce ? new BasicDBObject(this.key, queryObj) : queryObj;
			case AND:
				BasicDBObject temp = new BasicDBObject();
				for(Object obj : root.getChildList()){
					Object re = this.SerializeTree((ManyTreeNode<Object>)obj, false);
					temp.putAll((Map)re);
				}
				queryObj.put(this.key, temp);
				return queryObj;
			case OR:
				if(root.getChildList().get(0) instanceof String){
					ManyTreeNode<Object> newTree = new ManyTreeNode<Object>(Operator.IN);
					newTree.setChildList(root.getChildList());
					return this.SerializeTree(newTree, false);
				}
				else{
					List<DBObject> orList = new ArrayList<DBObject>();
					for(Object obj : root.getChildList()){
						Object re = this.SerializeTree((ManyTreeNode<Object>)obj, false);
						orList.add((DBObject) re);
					}
					queryObj.put(this.operMap.get(root.getData()), orList);
					return queryObj;
				}
			case IN:
			case NI:
				if(root.getChildList().size() > 0){
					if(root.getChildList().get(0) instanceof String){
						BasicDBObject inObj = new BasicDBObject(this.operMap.get(root.getData()), root.getChildList());
						return new BasicDBObject(this.key, inObj);
					}
					else{
						List<DBObject> inList1 = new ArrayList<DBObject>();
						for(Object obj : root.getChildList()){
							BasicDBObject re = (BasicDBObject) this.SerializeTree((ManyTreeNode<Object>)obj, false);
							inList1.add(re);
						}
						queryObj.put(this.operMap.get(root.getData()), inList1);
						return new BasicDBObject(this.key, queryObj);
					}
				}
				else{
					return null;
				}
			}
			
			return null;
		}
		
		 protected Object ConvertStr2Int(Object num){
			try {
				if (num instanceof Integer){
					return num;
				}
				else{
					return  Integer.parseInt((String) num);
				}
			} catch (Exception e) {
				return null;
			}
		}
		
		@SuppressWarnings("unchecked")
		protected boolean CompareTree(ManyTreeNode<Object> root, boolean bOnce, Object compareObject) {
			try {
				if (root.getChildList().size() < 0) {
					return false;
				}

				// BasicDBObject queryObj = new BasicDBObject();
				boolean bReturn = false;
				Object tmpObj;
				switch ((Operator) root.getData()) {
				case NQ:
					return !compareObject.equals(root.getChildList().get(0));
				case EQ:
					return compareObject.equals(root.getChildList().get(0));
				case LT:
					tmpObj = this.ConvertStr2Int(compareObject);
					return tmpObj == null ? false : (int) tmpObj < (int) (root.getChildList().get(0));
				case LE:
					tmpObj = this.ConvertStr2Int(compareObject);
					return tmpObj == null ? false : (int) tmpObj <= (int) (root.getChildList().get(0));
				case GT:
					tmpObj = this.ConvertStr2Int(compareObject);
					return tmpObj == null ? false : (int) tmpObj > (int) (root.getChildList().get(0));
				case GE:
					tmpObj = this.ConvertStr2Int(compareObject);
					return tmpObj == null ? false : (int) tmpObj >= (int) (root.getChildList().get(0));
				case AND:
					for (Object obj : root.getChildList()) {
						bReturn = this.CompareTree((ManyTreeNode<Object>) obj,
								false, compareObject);
						if (!bReturn) {
							return false;
						}
					}
					return true;
				case OR:
					if (root.getChildList().get(0) instanceof String) {
						ManyTreeNode<Object> newTree = new ManyTreeNode<Object>(
								Operator.IN);
						newTree.setChildList(root.getChildList());
						return this.CompareTree(newTree, false, compareObject);
					} else {
						for (Object obj : root.getChildList()) {
							bReturn = this.CompareTree((ManyTreeNode<Object>) obj, false, compareObject);
							if (bReturn) {
								return true;
							}
						}

						return false;
					}
				case IN:
					if (root.getChildList().size() > 0) {
						return root.getChildList().contains(compareObject); // ["a", "b"]
					} else {
						return false;
					}
				case NI:
					if (root.getChildList().size() > 0) {
						return !root.getChildList().contains(compareObject); // ["a", "b"]
					} else {
						return true;
					}
				}

				return false;

			} catch (Exception e) {
				return false;
			}
		}
		
		public boolean CompareObject(Object compareObj){
			if(this.operTree == null){
				log.error("Operator tree is null in compare object");
				return false;
			}
			boolean ret = this.CompareTree(this.operTree, false, compareObj);
			return ret;
		}
		
	
	} //end of QueryItem class
	
	protected Map<String, Object> queryMap = null;
	protected List<String> tableNameList = null;
	
	public LogQuery(Map<String, Object> query){
		this.queryMap = query;
	}
	
	
	public LogQuery(){}
	
	
	public Object processQuery(){
		if(this.queryMap == null){
			return null;
		}
		try{
			BasicDBObject temp = new BasicDBObject();
			for(String key : this.queryMap.keySet()){
				String value = this.queryMap.get(key).toString();
				if(!value.isEmpty() && !value.equals("*")){
					QueryItem item = new QueryItem(key, value);
					Map<?, ?> queryMap = (Map<?, ?>)item.createQuery();
					temp.putAll(queryMap);
					
					if(key == ReportItem.keyCategory){
						this.createTalbeNameList(key, queryMap.get(ReportItem.keyCategory));
					}
					
				}
			}
		
			return temp;
			
		}catch(Exception e){
			log.error("Process query: {}", e.getMessage());
			return null;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected void createTalbeNameList(String key, Object obj){
		try{
			
				this.tableNameList = new ArrayList<String>();
				do{
					if (obj instanceof String){
						this.tableNameList.add((String) obj);
						break;
					}
					else if(obj instanceof List<?>){
						this.tableNameList.addAll((Collection<? extends String>) obj);
						break;
					}
					else if(obj instanceof Map<?,?>){
						Map<String, Object> local = (Map<String, Object>)obj;
						for(Object o : local.keySet()){
							this.createTalbeNameList(key, local.get(o));
						}
						break;
					}else{
						break;
					}
				}
				while(true);
		}
		catch(Exception e){
			log.error("Create table name list failed {}", e.getMessage());
		}
		
	}
	
	public List<String> getTableNameList(){
		return this.tableNameList;
	}
	
	protected Map<String, QueryItem> comparatorMap = new HashMap<String, QueryItem>();
	public boolean InitComparator(){
		if(this.queryMap == null){
			return false;
		}
		try{
			for(String key : this.queryMap.keySet()){
				String value = this.queryMap.get(key).toString();
				if(!value.isEmpty() && !value.equals("*")){
					QueryItem item = new QueryItem(key, value);
					if(item.createTree() == null){
						log.error("Create comparator tree error");
						return false;
					}
					
					this.comparatorMap.put(key, item);
				}
			}
			
			//log.error(this.comparatorMap.toString());
			
			return true;
			
		}catch(Exception e){
			log.error("Process query: {}", e.getMessage());
			return false;
		}
		
	}
	public boolean CompareReportItem(ReportItem report){
		for(String key: this.comparatorMap.keySet()){
			QueryItem query = this.comparatorMap.get(key);
			if(report.getMaps().containsKey(key)){
				
				Object value = report.getMaps().get(key);
				switch(key){
					case ReportItem.keyTime:
							try {
								value = CommonTool.GetDateFromStr((String) value);
							} catch (Exception e) {
								return false;
							}
							
							break;
					default:
							break;
				}		
				
				if(!query.CompareObject(value)){
					return false;
				}
			}
			else{
				return false;
			}
			
			
		}
		
		return true;
	}
}