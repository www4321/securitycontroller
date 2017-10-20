/** 
*    Copyright 2014 BUPT. 
**/ 
/**
*    Copyright 2011, Big Switch Networks, Inc. 
*    Originally created by David Erickson, Stanford University
* 
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

package com.sds.securitycontroller.event;

import java.sql.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.storage.IDBObject;

/** Predicate class to specify rows by equality or comparison operations
 * of column values. The Storage API uses the special column name of "id"
 * to specify the primary key values for the row.
 * 
 * @author rob
 */
public class OperatorEventSubscription  extends EventSubscription implements java.io.Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -6654560184918847410L;

	protected static Logger log = LoggerFactory.getLogger(OperatorEventSubscription.class);
    
    private String subscribedKey;
    private Comparable<?> value; 
    protected Class<?> valueType;
    
    
    public Class<?> getValueType() {
		return valueType;
	}

	public OperatorEventSubscription(String subscribedKey, Operator operator, Comparable<?> value, 
    		SubscribedValueCategory subscribedValueCategory,  SubscriptionType subscriptionType, 
    		Class<?> valueType) {
        this.subscribedKey = subscribedKey;
        this.operator = operator;
        this.value = value;
        this.subscribedValueCategory = subscribedValueCategory;
        this.subscriptionType = subscriptionType;
        this.valueType = valueType;
    }
    
    public String getSubscribedKey() {
        return subscribedKey;
    }
    
    public Comparable<?> getValue() {
        return value;
    }
	
    public static boolean Compare(Operator op, Object subscribedValue, Object value){
    	boolean res = false;
    	switch (op){
    		case EQ:
    			res = (subscribedValue.equals(value));
    			break;
    		case LT:
    			if(value instanceof Integer || value instanceof Short ) 
    				res = ((int)subscribedValue < (int)value);
    			else if(value instanceof Float || value instanceof Double ) 
    				res = ((float)subscribedValue < (float)value + 0.000001F);
    			break;
    		case LTE:
    			if(value instanceof Integer || value instanceof Short ) 
    				res = ((int)subscribedValue <= (int)value);
    			else if(value instanceof Float || value instanceof Double ) 
    				res = ((float)subscribedValue <= (float)value + 0.000001F);
    			break;
    		case GT:
    			if(value instanceof Integer || value instanceof Short ) 
    				res = ((int)subscribedValue > (int)value);
    			else if(value instanceof Float || value instanceof Double ) 
    				res = ((float)subscribedValue > (float)value + 0.000001F);
    			break;
    		case GTE:
    			if(value instanceof Integer || value instanceof Short ) 
    				res = ((int)subscribedValue >= (int)value);
    			else if(value instanceof Float || value instanceof Double ) 
    				res = ((float)subscribedValue >= (float)value - 0.000001F);
    			break;
    		default:
    			break;
    	}
    	return res;    			
    }
    
    public String getOperatorString(){
    	String op = "";
    	switch (this.operator){
			case EQ:
				op = "=";
				break;
			case LT:
				op = "<";
				break;
			case LTE:
				op = "<=";
				break;
			case GT:
				op = ">";
				break;
			case GTE:
				op = ">=";
				break;
			default:
				break;
    	}
    	return op;
    }
    
    public boolean compareObj(Object subscribedValue){
    	Object keyValue = ((IDBObject)subscribedValue).getFieldValueByKey(this.subscribedKey);
    	if(this.valueType == Integer.class)
    		return calcObj((int)keyValue, (int)subscribedValue, this.operator);
    	else if(this.valueType == Float.class)
    		return calcObj((float)keyValue, (float)subscribedValue, this.operator);
    	else if(this.valueType == Double.class)
    		return calcObj((double)keyValue, (double)subscribedValue, this.operator);
    	else if(this.valueType == String.class)
    		return calcObj((String)keyValue, (String)subscribedValue, this.operator);
    	return false;    	
    }
    

    public boolean compareObj(Object obj, Object refv){
    	Object objv = ((IDBObject)obj).getFieldValueByKey(this.subscribedKey);
    	if(this.valueType == Integer.class)
    		return calcObj((int)objv, (int)refv, this.operator);
    	else if(this.valueType == Float.class)
    		return calcObj((float)objv, (float)refv, this.operator);
    	else if(this.valueType == Double.class)
    		return calcObj((double)objv, (double)refv, this.operator);
    	else if(this.valueType == Long.class)
    		return calcObj((long)objv, (long)refv, this.operator);
    	else if(this.valueType == String.class)
    		return calcObj((String)objv, (String)refv, this.operator);
    	return false;    	
    }
    
    
    public boolean calcObj(String x1, String x2, Operator op){
    	if(op == Operator.EQ){
    		if(x1.charAt(0) == '\'' && x1.charAt(x1.length()-1) == '\'')
    			x1 = x1.substring(1, x1.length()-1);
    		if(x2.charAt(0) == '\'' && x2.charAt(x2.length()-1) == '\'')
    			x2 = x2.substring(1, x2.length()-1);
    		
    		return x1.equals(x2);
    	}
    	else
    		return false;
    }
    
    public boolean calcObj(int x1, int x2, Operator op){
    	boolean res = false;
    	switch (op){
		case EQ:
			res = (x1 == x2);
			break;
		case LT:
			res = (x1 < x2);
			break;
		case LTE:
			res = (x1 <= x2);
			break;
		case GT:
			res = (x1 > x2);
			break;
		case GTE:
			res = (x1 >= x2);
			break;
		default:
			break;
    	}
    	return res;
    }

    public boolean calcObj(float x1, float x2, Operator op){
    	boolean res = false;
    	switch (op){
		case EQ:
			res = (x1 == x2);
			break;
		case LT:
			res = (x1 < x2);
			break;
		case LTE:
			res = (x1 <= x2);
			break;
		case GT:
			res = (x1 > x2);
			break;
		case GTE:
			res = (x1 >= x2);
			break;
		default:
			break;
    	}
    	return res;
    }
    

    public boolean calcObj(double x1, double x2, Operator op){
    	boolean res = false;
    	switch (op){
		case EQ:
			res = (x1 == x2);
			break;
		case LT:
			res = (x1 < x2);
			break;
		case LTE:
			res = (x1 <= x2);
			break;
		case GT:
			res = (x1 > x2);
			break;
		case GTE:
			res = (x1 >= x2);
			break;
		default:
			break;
    	}
    	return res;
    }

    public boolean calcObj(long x1, long x2, Operator op){
    	boolean res = false;
    	switch (op){
		case EQ:
			res = (x1 == x2);
			break;
		case LT:
			res = (x1 < x2);
			break;
		case LTE:
			res = (x1 <= x2);
			break;
		case GT:
			res = (x1 > x2);
			break;
		case GTE:
			res = (x1 >= x2);
			break;
		default:
			break;
    	}
    	return res;
    }
    

    public Object calcValues(Object x1, Object x2, char op){
    	if(x1 instanceof Integer)
    		return calcObj((int)x1, (int)x2, this.operator);
    	else if(this.valueType == Float.class)
    		return calcObj((float)x1, (float)x2, this.operator);
    	else if(this.valueType == Double.class)
    		return calcObj((double)x1, (double)x2, this.operator);
    	log.error("Unknow operator: {}", op);
    	return 0;    	
    }
    
    
    public Set<Character> OPs = new HashSet<Character>(){
    	private static final long serialVersionUID = 1L;
    	{
    		this.add('+');
    		this.add('-');
    		this.add('*');
    		this.add('/');
    	}};
    
    public int calcObj(int x1, int x2, char op){
    	int res = 0;
    	switch (op){
		case '+':
			res = (x1 + x2);
			break;
		case '-':
			res = (x1 - x2);
			break;
		case '*':
			res = (x1 * x2);
			break;
		case '/':
			res = (x1 / x2);
			break;
		default:
			break;
    	}
    	return res;
    }
    
   

    public float calcObj(float x1, float x2, char op){
    	float res = 0;
    	switch (op){
			case '+':
				res = (x1 + x2);
				break;
			case '-':
				res = (x1 - x2);
				break;
			case '*':
				res = (x1 * x2);
				break;
			case '/':
				res = (x1 / x2);
				break;
			default:
				break;
    	}
    	return res;
    }
    

    public double calcObj(double x1, double x2, char op){
    	double res = 0;
    	switch (op){
			case '+':
				res = (x1 + x2);
				break;
			case '-':
				res = (x1 - x2);
				break;
			case '*':
				res = (x1 * x2);
				break;
			case '/':
				res = (x1 / x2);
				break;
			default:
				break;
    	}
    	return res;
    }
    

    public String calcObj(String x1, String x2, char op){
    	String res = "";
    	switch (op){
			case '+':
				res = (x1 + x2);
			default:
				break;
    	}
    	return res;
    }
    

    public Object getObjectValue(String objStr, Class<?> type, Object obj){
    	if(!(obj instanceof IDBObject)){
    		log.error("object is not an IDBObject instance: {}", obj);
    		return null;
    	}
    	IDBObject io = (IDBObject)obj;
    	if(objStr.indexOf("object.")>=0){
    		String prop = objStr.substring("object.".length());
    		Map<String,Object> v = io.getDBElements();
    		return v.get(prop);
    	}else{
    		if(type == Integer.class)
				return Integer.parseInt(objStr);
			else if(type == Long.class)
				return Long.parseLong(objStr);
			else if(type == Float.class)
				return Float.parseFloat(objStr);
			else if(type == Double.class)
				return Double.parseDouble(objStr);
    		return objStr;
    	}
    }
    
	public String calcSQLValueExpression(String s, Object obj){
		if(!(obj instanceof IDBObject)){
    		log.error("object is not an IDBObject instance: {}", obj);
    		return null;
    	}
		StringBuilder sb = new StringBuilder();
		String[] sp = s.split("%%");
		for(int i=0;i<sp.length;i++){
			
	    	IDBObject io = (IDBObject)obj;
	    	if(sp[i].indexOf("object.")>=0){
	    		String prop = sp[i].substring("object.".length());
	    		Map<String,Object> v = io.getDBElements();
	    		sb.append(v.get(prop));
	    	}
	    	else
	    		sb.append(sp[i]);
		}
		return sb.toString();
	}
    
	//calculating the combination of an object properties
	//s: a long expression
	//type: calculation type
	//obj: the object
	public Object calcValueExpression(String s, Class<?> type, Object obj){
		if(type == Date.class)
			return s;
		Object res = null;
		char op = '+';
		String[] sp = s.split(" ");
		for(int i=0;i<sp.length;i++){
			if(this.OPs.contains(sp[i].charAt(0))){
				op = sp[i].charAt(0);
				i++;//to the next one
			}
			Object v = getObjectValue(sp[i], type, obj);
					
			if(type == Integer.class)
				res = (res == null)?v: calcObj((int)res, (int)v, op);
			if(type == Long.class)
				res = (res == null)?v: calcObj((long)res, (long)v, op);
			else if(type == Float.class)
				res = (res == null)?v: calcObj((float)res, (float)v, op);
			else if(type == Double.class)
				res = (res == null)?v: calcObj((double)res, (double)v, op);
			else if(type == String.class){
				v =  "'"+v+"'";
				res = (res == null)?v: calcObj((String)res, (String)v, op);
			}
		}
		if(res == null)
			return s;
		else
			return res;
	}
    

}
