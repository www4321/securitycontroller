package com.sds.securitycontroller.securityfunction.securitydevicedriver;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.securityfunction.ErrorCode;
import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionRequestAndResponse;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;
import com.sds.securitycontroller.utils.http.HTTPHelper;
import com.sds.securitycontroller.utils.http.HTTPHelperResult;

public class WVSS_4sdnapi_0_0_0_1 extends SecurityDeviceDriver 
{
	protected static Logger log = LoggerFactory.getLogger(WVSS_4sdnapi_0_0_0_1.class);
	
	public WVSS_4sdnapi_0_0_0_1()
	{
		super();
		
		this.version = "WVSS-0.0.0.1";
		
		this.funcTypeSet.add("WEB_SCAN_TASK");
		
		this.supportedDeviceType.put(SecurityDeviceType.SD_WVSS, 
				new HashSet<String>()
				{
					private static final long serialVersionUID = -561636147801283761L;

					{
						add("0.0.0.1");
					}
				});
	}

	@Override
	public ErrorCode callSecurityFunction(SecurityDevice dev,
			SecurityFunctionRequestAndResponse reqAndRes) throws Exception {
		// TODO Auto-generated method stub
		switch(reqAndRes.request.secFuncType)
		{
		case "WEB_SCAN_TASK":
			return this.task(dev, reqAndRes);
		}
		
		reqAndRes.response.errorCode = ErrorCode.SECURITY_FUNCTION_TYPE_NOT_SUPPORTED;
		reqAndRes.response.errorString = "security function type is not supported";
		
		return reqAndRes.response.errorCode;
	}
	
	ErrorCode task(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes) throws Exception
	{
		switch(reqAndRes.request.opType)
		{
		case MODIFY:
			reqAndRes.response.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
			reqAndRes.response.errorString = "can not modify an task";
			break;
		case CREATE:
			return this.createTask(dev, reqAndRes);
		case GET:
			return this.getTask(dev, reqAndRes);
		case DELETE:
			return this.deleteTask(dev, reqAndRes);
		}
		return reqAndRes.response.errorCode;
	}
	
	ErrorCode getTask(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes)
	{
		do
		{
			String url = dev.getBaseURL() + "tasks/" + reqAndRes.request.args.get("task_id") + "/status";
			log.debug("[getTask] Query url " + url);
			HTTPHelperResult response = HTTPHelper.httpGet(url, new HashMap<String, String>());
			
			if(response.getCode() == -1)
			{
				reqAndRes.response.errorCode = ErrorCode.COMMUNICATE_WITH_DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			else if(response.getCode() != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			
			log.debug("[getTask] device's response is " + response.getMsg());
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(response.getMsg());
			}
			catch (Exception e)
			{
				log.error("[getTask] parse device response exception, " + e.getMessage());
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			int code = root.path("code").asInt();
			if(code != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = root.path("result").path("msg").asText();
				log.error("[getTask] device failed");
				break;
			}
			
			JsonNode progressInJson = root.path("result").path("progress");
			if(!progressInJson.isInt())
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = "response field progress should be an integer";
				log.error("[getTask] device response missing field progress or progress is not an integer");
				break;
			}
			
			reqAndRes.response.result.put("progress", progressInJson.asInt());
		}while(false);
		
		return reqAndRes.response.errorCode;
	}
	
	ErrorCode deleteTask(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes)
	{
		do
		{
			String url = dev.getBaseURL() + "tasks/" + reqAndRes.request.args.get("task_id");
			log.debug("[deleteTask] Query url " + url);
			HTTPHelperResult response = HTTPHelper.httpDelete(url, new HashMap<String, String>());
			
			if(response.getCode() == -1)
			{
				reqAndRes.response.errorCode = ErrorCode.COMMUNICATE_WITH_DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			else if(response.getCode() != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			
			log.debug("[deleteTask] device's response is " + response.getMsg());
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(response.getMsg());
			}
			catch (Exception e)
			{
				log.error("[deleteTask] parse device response exception, " + e.getMessage());
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			int code = root.path("code").asInt();
			if(code != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = root.path("result").path("msg").asText();
				log.error("[deleteTask] failed");
				break;
			}
		}while(false);
		
		return reqAndRes.response.errorCode;
	}
	
	@SuppressWarnings("unchecked")
	ErrorCode createTask(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes)
	{
		do
		{
			String protocol = (String)((Map<String, Object>)reqAndRes.request.args.get("target")).get("protocol");
			String host = (String)((Map<String, Object>)reqAndRes.request.args.get("target")).get("host");
			int port = (int)((Map<String, Object>)reqAndRes.request.args.get("target")).get("port");
			
			String target = protocol + "://" + host + ":" + port;
			String name = "SDN-WEBSCAN[" + target + "]";
			String type = (String)reqAndRes.request.args.get("type");
			int template = -1;
			String body = null;
			
			switch(type)
			{
			case "template":
				if(!reqAndRes.request.args.containsKey("type_parameter"))
				{
					log.error("[createTask] missing field type_parameter");
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing field type_parameter";
					break;
				}
				Map<String, Object> params = (Map<String, Object>)reqAndRes.request.args.get("type_parameter");
				if(!params.containsKey("template"))
				{
					log.error("[createTask] missing field type_parameter:template");
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing field type_parameter:template";
					break;
				}
				try
				{
					template = Integer.parseInt((String)params.get("template"));
				}
				catch(Exception e)
				{
					log.error("[createTask] invalid field type_parameter:template, should be an integer string");
					reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
					reqAndRes.response.errorString = "invalid field type_parameter:template, " + e.getMessage();
					break;
				}
				break;
			case "fast":
			case "slow":
			case "verify":
			default:
				log.error("[createTask] un-supported type " + type);
				reqAndRes.response.errorCode = ErrorCode.DEVICE_DOES_NOT_SUPPORT;
				reqAndRes.response.errorString = "device does not support type " + type;
				break;
			}
			
			if(reqAndRes.response.errorCode != ErrorCode.SUCCESS)
			{
				break;
			}
			
			/**
			 * The POST Body should be sth. like this:
			 * 	[
			 * 		[
			 * 			[
			 * 				["target", "", "http://xxx.xxx.xxx:yyy"], 
			 * 				["name", "", "blablablabla..."], 
			 * 				["template", "", 0]
			 * 			]
			 * 		]
			 * 	]
			 */

			try
			{
				JsonFactory jsonFactory = new JsonFactory();
				StringWriter writer = new StringWriter();
				JsonGenerator generator = jsonFactory.createGenerator(writer);
				//[
				generator.writeStartArray();
					//[
					generator.writeStartArray();
						//[
						generator.writeStartArray();
						
							//[
							generator.writeStartArray();
							generator.writeString("target");
							generator.writeString("");
							generator.writeString(target);
							//]
							generator.writeEndArray();
							
							//[
							generator.writeStartArray();
							generator.writeString("name");
							generator.writeString("");
							generator.writeString(name);
							//]
							generator.writeEndArray();
							
							//[
							generator.writeStartArray();
							generator.writeString("template");
							generator.writeString("");
							generator.writeNumber(template);
							//]
							generator.writeEndArray();
							
						//]
						generator.writeEndArray();
					//]
					generator.writeEndArray();
				//]
				generator.writeEndArray();
				generator.close();
				
				body = writer.toString();
			}
			catch(Exception e)
			{
				reqAndRes.response.errorCode = ErrorCode.JSON_WRITE_ERROR;
				reqAndRes.response.errorString = "create json string failed, " + e.getMessage();
				log.error("[createTask] create json exception, " + e.getMessage());
				e.printStackTrace();
				break;
			}
			
			String url = dev.getBaseURL() + "tasks";
			log.debug("[createTask] Querying url " + url);
			log.debug("[createTask] POST BODY: " + body);
			
			HTTPHelperResult response = HTTPHelper.httpPost(url, body, new HashMap<String, String>());
			
			log.debug("[createTask] Device response: " + response.getMsg());
			
			if(response.getCode() == -1)
			{
				reqAndRes.response.errorCode = ErrorCode.COMMUNICATE_WITH_DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			else if(response.getCode() != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			
			log.debug("[createTask] devices's response is " + response.getMsg());
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(response.getMsg());
			}
			catch(Exception e)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			int code = root.path("code").asInt();
			if(code != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}

			JsonNode result = root.path("result");
			if(result.isMissingNode())
			{
				log.error("[createTask] response missing field result");
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = "response missing field result";
				break;
			}
			
			JsonNode taskIdInJson = result.path("taskId");
			if(taskIdInJson.isMissingNode())
			{
				log.error("[createTask] response missing field result:taskId");
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = "response missing field result:taskId";
				break;
			}
			
			if(!taskIdInJson.isInt())
			{
				log.error("[createTask] response missing field result:taskId");
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = "response missing field result:taskId";
				break;
			}
			
			int task_id = taskIdInJson.asInt();
			reqAndRes.response.result.put("task_id", task_id);
		}while(false);
		
		return reqAndRes.response.errorCode;
	}

	@Override
	public boolean isSecurityDeviceTypeSupported(SecurityDeviceType devType) {
		// TODO Auto-generated method stub
		if(this.supportedDeviceType.containsKey(devType))
		{
			return true;
		}
		return false;
	}
}
