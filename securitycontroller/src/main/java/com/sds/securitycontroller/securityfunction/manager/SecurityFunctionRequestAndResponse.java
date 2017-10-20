package com.sds.securitycontroller.securityfunction.manager;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.securityfunction.ErrorCode;
import com.sds.securitycontroller.securityfunction.OperationType;

public class SecurityFunctionRequestAndResponse implements Cloneable {

	public class SecurityFunctionRequest implements Cloneable {
		public String tenantID = null;
		public String appID = null;
	
		public String secFunc = null;
		public String secFuncType = null;
		public OperationType opType = null;
		
		public Map<String, Object> args = null;
		
		@Override
		public String toString()
		{
			return "[REQUEST]-->[tenantID:" + tenantID + ", appID:" + appID + ", secFunc:" + secFunc + ", secFuncType:" + secFuncType
					+ ", opType:" + opType + ", args:" + args + "]";
		}
		
		@Override
		public Object clone()
		{
			SecurityFunctionRequest _new = null;
			
			try
			{
				_new = new SecurityFunctionRequest();
				_new.tenantID = this.tenantID;
				_new.appID = this.appID;
				_new.secFunc = this.secFunc;
				_new.secFuncType = this.secFuncType;
				_new.opType = this.opType;
				
				if(this.args == null)
				{
					_new.args = null;
				}
				else
				{
					_new.args = new HashMap<String, Object>();
					Iterator<String> it = this.args.keySet().iterator();
					while(it.hasNext())
					{
						String key = it.next();
						Object value = this.args.get(key);
						_new.args.put(key, value);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				_new = null;
			}
			
			return _new;
		}
	}
	
	public class SecurityFunctionResponse implements Cloneable {
		public ErrorCode errorCode = null;
		public String errorString = null;
		
		public HashMap<String, Object> result = null;
		
		public SecurityFunctionResponse()
		{
			this.errorCode = ErrorCode.SUCCESS;
			this.result = new HashMap<String, Object>();
		}
		
		@Override
		public Object clone()
		{
			SecurityFunctionResponse _new = null;
			
			try
			{
				_new = new SecurityFunctionResponse();
				_new.errorCode = this.errorCode;
				_new.errorString = this.errorString;
				
				Iterator<String> it = this.result.keySet().iterator();
				while(it.hasNext())
				{
					String key = it.next();
					Object value = this.result.get(key);
					_new.result.put(key, value);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				_new = null;
			}
			
			return _new;
		}
	}
	
	public interface IRequestParser
	{
		public ErrorCode parseRequest(SecurityFunctionRequestAndResponse reqAndRes) throws Exception;
	}
	
	public IRequestParser requestParser = null;
	public SecurityFunctionRequest request = null;
	public SecurityFunctionResponse response =  null;
	
	public SecurityFunctionRequestAndResponse()
	{
	}
	
	public SecurityFunctionRequestAndResponse(String secFunc, String secFuncType) {
		this.request = new SecurityFunctionRequest();
		this.response = new SecurityFunctionResponse();
		
		this.request.secFunc = secFunc;
		this.request.secFuncType = secFuncType;
	}
	
	@Override
	public Object clone()
	{
		SecurityFunctionRequestAndResponse _new = null;
		
		try
		{
			_new = new SecurityFunctionRequestAndResponse();
			_new.request = (SecurityFunctionRequest)this.request.clone();
			_new.response = (SecurityFunctionResponse)this.response.clone();
			_new.requestParser = this.requestParser;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return _new;
	}
	
	public String getJsonResponse()
	{
		try
		{
			StringWriter writer = new StringWriter();
			ObjectMapper mapper = new ObjectMapper();
			JsonGenerator generator = mapper.getFactory().createGenerator(writer);
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			HashMap<String, Object> data = new HashMap<String, Object>();
			
			map.put("opt_status", this.response.errorCode.getIntegerCode());
			map.put("head", new HashMap<String, Object>());
			map.put("data", data);
			
			if(this.response.errorCode == ErrorCode.SUCCESS)
			{
				data.put("status", "success");
				if(this.response.result == null)
				{
					data.put("result", new HashMap<String, Object>());
				}
				else
				{
					data.put("result", this.response.result);
				}
			}
			else
			{
				data.put("status", "error");
				data.put("error_code", this.response.errorCode);
				if(this.response.errorString != null && !this.response.errorString.equalsIgnoreCase(""))
				{
					data.put("error_string", this.response.errorString);
				}
			}
			
			generator.writeObject(map);
			writer.close();
			
			return writer.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return "{\"head\" : {}, \"data\" : { \"status\" : \"error\", \"error_code\" : \"CONVERT_JSON_STRING_EXCEPION\" } }";
		
	}
	
	@SuppressWarnings("unchecked")
	public ErrorCode parseRequest(String httpMethod, Object request, IRequestParser requestParser) {
		//ErrorCode ret = ErrorCode.SUCCESS;
		
		do
		{
			try
			{
				switch(httpMethod)
				{
				case "POST": 
					{
						this.request.opType = OperationType.CREATE;
						
						ObjectMapper mapper = new ObjectMapper();
						JsonNode root = mapper.readTree((String)request);
						
						JsonNode headInJson = root.path("head");
						if(headInJson.isMissingNode())
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_HEAD;
							this.response.errorString = "missing field head";
							break;
						}
						
						JsonNode appIDInJson = headInJson.path("appID");
						if(appIDInJson.isMissingNode())
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_APP_ID;
							this.response.errorString = "missing field appID";
							break;
						}
						this.request.appID = appIDInJson.asText();
						
						JsonNode tenantIDInJson = headInJson.path("tenantID");
						if(tenantIDInJson.isMissingNode())
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_TENANT_ID;
							this.response.errorString = "missing field tenantID";
							break;
						}
						this.request.tenantID = tenantIDInJson.asText();
						
						JsonNode dataInJson = root.path("data");
						if(dataInJson.isMissingNode())
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_DATA;
							this.response.errorString = "missing field data";
							break;
						}
						
						Map<String,Object> data = mapper.readValue(dataInJson.toString(), new TypeReference<Map<String,Object>>() { });
						this.request.args = data;
					}
					break;
				case "PUT": 
					{
						this.request.opType = OperationType.MODIFY;
						
						ObjectMapper mapper = new ObjectMapper();
						JsonNode root = mapper.readTree((String)request);
						
						JsonNode headInJson = root.path("head");
						if(headInJson.isMissingNode())
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_HEAD;
							this.response.errorString = "missing field head";
							break;
						}
						
						JsonNode appIDInJson = headInJson.path("appID");
						if(appIDInJson.isMissingNode())
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_APP_ID;
							this.response.errorString = "missing field appID";
							break;
						}
						this.request.appID = appIDInJson.asText();
						
						JsonNode tenantIDInJson = headInJson.path("tenantID");
						if(tenantIDInJson.isMissingNode())
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_TENANT_ID;
							this.response.errorString = "missing field tenantID";
							break;
						}
						this.request.tenantID = tenantIDInJson.asText();
						
						JsonNode dataInJson = root.path("data");
						if(dataInJson.isMissingNode())
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_DATA;
							this.response.errorString = "missing field data";
							break;
						}
						
						Map<String,Object> data = mapper.readValue(dataInJson.toString(), new TypeReference<Map<String,Object>>() { });
						this.request.args = data;
					}
					break;
				case "DELETE": 
					{
						this.request.opType = OperationType.DELETE;
						Map<String, Object> map = (Map<String, Object>)request;
						
						if(!map.containsKey("appID"))
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_APP_ID;
							this.response.errorString = "missing field appID";
							break;
						}
						this.request.appID = (String)map.get("appID");
						
						if(!map.containsKey("tenantID"))
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_TENANT_ID;
							this.response.errorString = "missing field tenantID";
							break;
						}
						this.request.tenantID = (String)map.get("tenantID");
						
						this.request.args = map;
					}
					break;
				case "GET": 
					{
						this.request.opType = OperationType.GET;
						Map<String, Object> map = (Map<String, Object>)request;
						
						if(!map.containsKey("appID"))
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_APP_ID;
							this.response.errorString = "missing field appID";
							break;
						}
						this.request.appID = (String)map.get("appID");
						
						if(!map.containsKey("tenantID"))
						{
							this.response.errorCode = ErrorCode.MISSING_FIELD_TENANT_ID;
							this.response.errorString = "missing field tenantID";
							break;
						}
						this.request.tenantID = (String)map.get("tenantID");
						
						this.request.args = map;
					}
					break;
				default:
					this.response.errorCode = ErrorCode.INVALID_FIELD_OPTYPE;
					this.response.errorString = "invalid operation type";
					break;
				}
				
				if(this.response.errorCode != ErrorCode.SUCCESS)
				{
					break;
				}
				
				this.response.errorCode = requestParser.parseRequest(this);
				if(this.response.errorCode != ErrorCode.SUCCESS);
				{
					break;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				this.response.errorCode = ErrorCode.PARSE_REQUEST_FAILED;
				this.response.errorString = "parse request exception, " + e.getMessage();
			}
		}while(false);
		
		return this.response.errorCode;
	}
}
