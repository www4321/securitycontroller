package com.sds.securitycontroller.securityfunction.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.restlet.data.Parameter;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.sds.securitycontroller.securityfunction.ErrorCode;

public class SecurityFunctionResource extends ServerResource {
	//private static Logger log = LoggerFactory.getLogger(SecurityFunctionResource.class);
	private String secFunc = null;
	private String secFuncType = null;
	private SecurityFunctionManager mgr = null;
	
	@Override
	public void doInit()
	{
		this.secFunc = this.getAttribute("secFunc");
		this.secFuncType = this.getAttribute("secFuncType");
		this.mgr = SecurityFunctionManager.getInstance();
	}
	
	private String getExceptionResponse(String exception)
	{
		return "{ \"opt_status\" : 500, \"head\" : {}, \"data\" : { \"error_code\" : \"" + ErrorCode.SERVER_EXCEPTION.toString() + "\", \"exception\" : \"" + exception + "\"}}";
	}
	
	private void getURLArguments(Map<String, Object> map)
	{
		Iterator<Parameter> it = this.getQuery().iterator();
		while(it.hasNext())
		{
			Parameter param = it.next();
			map.put(param.getName(), param.getValue());
		}
	}
	
	@Get("json")
	public String handleGet()
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		try
		{
			this.getURLArguments(map);
			SecurityFunctionRequestAndResponse reqAndRes = 
					this.mgr.processRequest("GET", secFunc, secFuncType, map);
			return reqAndRes.getJsonResponse();
//			if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
//			{
//				return reqAndRes.getJsonResponse();
//				//return "SUCCESS " + reqAndRes.response.result;
//			}
//			else
//			{
//				return "ERROR " + reqAndRes.response.errorCode;
//			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return this.getExceptionResponse(e.getMessage());
		}
	}
	
	@Post
    public String handlePost(String fmJson)
	{
		try
		{
			SecurityFunctionRequestAndResponse reqAndRes
					= this.mgr.processRequest("POST", secFunc, secFuncType, fmJson);
			
			return reqAndRes.getJsonResponse();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return this.getExceptionResponse(e.getMessage());
		}
	}
	
	@Delete
    public String handleDelete()
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		try
		{
			this.getURLArguments(map);
			SecurityFunctionRequestAndResponse reqAndRes = 
					this.mgr.processRequest("DELETE", secFunc, secFuncType, map);
			return reqAndRes.getJsonResponse();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return this.getExceptionResponse(e.getMessage());
		}
	}
	
	@Put
	public String handlePut(String fmJson)
	{
		try
		{
			SecurityFunctionRequestAndResponse reqAndRes
					= this.mgr.processRequest("PUT", secFunc, secFuncType, fmJson);
			
			return reqAndRes.getJsonResponse();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return this.getExceptionResponse(e.getMessage());
		}
	}
}
