package com.sds.securitycontroller.securityfunction.securitydevicedriver;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.command.DeviceCommand;
import com.sds.securitycontroller.command.ResolvedCommand;
import com.sds.securitycontroller.policy.AtomPolicy;
import com.sds.securitycontroller.policy.DevicePolicyObject;
import com.sds.securitycontroller.policy.PolicyAction;
import com.sds.securitycontroller.policy.PolicyActionArgs;
import com.sds.securitycontroller.policy.PolicyActionType;
import com.sds.securitycontroller.policy.PolicyInfo;
import com.sds.securitycontroller.policy.PolicySubject;
import com.sds.securitycontroller.policy.PolicySubject.PolicySubjectType;
import com.sds.securitycontroller.policy.resolver.PolicyResolver;
import com.sds.securitycontroller.securityfunction.ErrorCode;
import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionRequestAndResponse;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;
import com.sds.securitycontroller.utils.http.HTTPHelper;
import com.sds.securitycontroller.utils.http.HTTPHelperResult;

public class WAF_4sdnapi_0_0_0_1 extends SecurityDeviceDriver
{
	protected static Logger log = LoggerFactory.getLogger(WAF_4sdnapi_0_0_0_1.class);
	protected static int PROXY_PORT_BASE = 10000;
	private static HashSet<String> supportedPolicyTemplates = null;
	protected PolicyResolver policyResolver=new PolicyResolver();//policy Resolver
	
	public static HashSet<String> getSupportedPolicyTemplates() {
		return supportedPolicyTemplates;
	}

	public static void setSupportedPolicyTemplates(
			HashSet<String> supportedPolicyTemplates) {
		WAF_4sdnapi_0_0_0_1.supportedPolicyTemplates = supportedPolicyTemplates;
	}

	public static int getPROXY_PORT_BASE() {
		return PROXY_PORT_BASE;
	}

	public static void setPROXY_PORT_BASE(int pROXY_PORT_BASE) {
		PROXY_PORT_BASE = pROXY_PORT_BASE;
	}

	public WAF_4sdnapi_0_0_0_1()
	{
		super();
		
		this.version = "WAF-0.0.0.1";
		
		this.funcTypeSet.add("WEB_PROTECT_POLICY_TEMPLATE");
		//this.funcTypeSet.add("WEB_PROTECT_POLICY_TEMPLATE_DEFAULT");
		this.funcTypeSet.add("WEB_PROTECT_DEVICE_COMMAND");
		this.funcTypeSet.add("WEB_PROTECT_REVERSE_PROXY");
		this.funcTypeSet.add("WEB_PROTECT_WHITE_LIST");

		
		this.supportedDeviceType.put(SecurityDeviceType.SD_WAF, 
				new HashSet<String>()
				{
					private static final long serialVersionUID = 5342229672498079064L;

					{
						add("0.0.0.1");
					}
				});
		
		WAF_4sdnapi_0_0_0_1.supportedPolicyTemplates = new HashSet<String>();
		WAF_4sdnapi_0_0_0_1.supportedPolicyTemplates.add("xss");
		WAF_4sdnapi_0_0_0_1.supportedPolicyTemplates.add("sql");
		WAF_4sdnapi_0_0_0_1.supportedPolicyTemplates.add("anti_leech");
		WAF_4sdnapi_0_0_0_1.supportedPolicyTemplates.add("upload_limit");
		WAF_4sdnapi_0_0_0_1.supportedPolicyTemplates.add("download_limit");
		WAF_4sdnapi_0_0_0_1.supportedPolicyTemplates.add("anti_spider");
	}

	@Override
	public ErrorCode callSecurityFunction(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes) throws Exception {
		switch(reqAndRes.request.secFuncType)
		{
		case "WEB_PROTECT_POLICY_TEMPLATE":
			return this.policyTemplate(dev, reqAndRes);
		case "WEB_PROTECT_WHITE_LIST":
			return this.whiteList(dev, reqAndRes);
		case "WEB_PROTECT_DEVICE_COMMAND":
			return this.deviceCommand(dev, reqAndRes);
		case "WEB_PROTECT_REVERSE_PROXY":
			return this.reverseProxy(dev, reqAndRes);
		}
		
		reqAndRes.response.errorCode = ErrorCode.SECURITY_FUNCTION_TYPE_NOT_SUPPORTED;
		reqAndRes.response.errorString = "security function type is not supported";
		
		return reqAndRes.response.errorCode;
	}

	ErrorCode policyTemplate(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes)
			throws Exception {
		//HashMap<String, Object> result = (HashMap<String, Object>)reqAndRes.response.result;
		//HashMap<String, Object> args = (HashMap<String, Object>)reqAndRes.request.args;
		String websiteName = (String)reqAndRes.request.args.get("webprotect_key");
		String websiteID = this.getWebsiteID(dev, reqAndRes, websiteName);
		
		if(websiteID == null)
		{
			return reqAndRes.response.errorCode;
		}
		
		switch(reqAndRes.request.opType)
		{
		case DELETE:
			//删除就是把所有的模板都禁用了，设置一个空的Map，程序会自动将所有支持的策略都禁用
			reqAndRes.request.args.put("template", new HashMap<String, String>());
		case CREATE:
		case MODIFY:
			if(!this.applyPolicyTemplate(dev, reqAndRes, websiteID))
			{
				log.error("[policyTemplate:" + reqAndRes.request.opType.toString() + "] applyPolicyTemplate failed, errorCode: " + reqAndRes.response.errorCode + ", errorString: " + reqAndRes.response.errorString);
				break;
			}
			reqAndRes.response.errorCode = ErrorCode.SUCCESS;
			break;
		default:
			break;
		}
		
		return reqAndRes.response.errorCode;
	}

	ErrorCode whiteList(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes) throws Exception {
		switch(reqAndRes.request.opType)
		{
		case CREATE:
			{
				if(!this.createExceptPolicy(dev, reqAndRes))
				{
					log.error("[whiteList:CREATE] failed, errorCode: " + reqAndRes.response.errorCode + ", errorString: " + reqAndRes.response.errorString);
				}
			}
			break;
		case DELETE:
			{
				if(!this.deleteExceptPolicy(dev, reqAndRes))
				{
					log.error("[whiteList:DELETE] failed, errorCode: " + reqAndRes.response.errorCode + ", errorString: " + reqAndRes.response.errorString);
				}
			}
			break;
		default:
			reqAndRes.response.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
			reqAndRes.response.errorString = "operation is not supported";
			break;
		}
		
		return reqAndRes.response.errorCode;
	}
	
	boolean deleteExceptPolicy(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes)
	{
		boolean ret = false;
		
		do
		{
			String url = dev.getBaseURL() + "strategy/except?policyid=" + (String)reqAndRes.request.args.get("except_policy_id");
			
			log.debug("[deleteExceptPolicy] Query url " + url);
			
			HTTPHelperResult response = HTTPHelper.httpDelete(url, new HashMap<String, String>());
			
			log.debug("[deleteExceptPolicy] device's response is " + response.getMsg());
			
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
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(response.getMsg());
			}
			catch (Exception e)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			int opt_status = root.path("OPT_STATUS").asInt();
			if(opt_status != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = root.path("DATA").path("msg").asText();
				break;
			}
			
			ret = true;
		}while(false);
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	boolean createExceptPolicy(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes)
	{
		boolean ret = false;
		
		do
		{
			String url = dev.getBaseURL() + "strategy/except";
			String body = null;
			Map<String, Object> args = (Map<String, Object>) reqAndRes.request.args.get("white_list");
			
			try
			{
				StringWriter writer = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				JsonGenerator generator = mapper.getFactory().createGenerator(writer);
				
				HashMap<String, Object> bodyMap = new HashMap<String, Object>();
				bodyMap.put("site_id", args.get("site_id"));
				bodyMap.put("src_ip", args.get("src_ip"));
				bodyMap.put("dst_port", args.get("dst_port"));
				bodyMap.put("domain", args.get("domain"));
				bodyMap.put("uri", args.get("uri"));
				bodyMap.put("event_type", args.get("event_type"));
				bodyMap.put("policy_id", args.get("policy_id"));
				bodyMap.put("rule_id", args.get("rule_id"));
				
				generator.writeObject(bodyMap);
				writer.close();
				
				body = writer.toString();
			}
			catch(Exception e)
			{
				log.error("[createExceptPolicy] create json exception, " + e.getMessage());
				e.printStackTrace();
				reqAndRes.response.errorCode = ErrorCode.JSON_WRITE_ERROR;
				reqAndRes.response.errorString = "create json string failed, " + e.getMessage();
				break;
			}
			
			log.debug("[createExceptPolicy] Querying url " + url);
			log.debug("[createExceptPolicy] POST body: " + body);
			DeviceCommand deviceCommand=new DeviceCommand(url, "POST",new HashMap<String,String>(),body);
			List<DeviceCommand> httpCommandList=new LinkedList<DeviceCommand>(); 
			httpCommandList.add(deviceCommand);
			HTTPHelperResult response=null;
			try {
				response=generateWAFpolicy(reqAndRes, PolicyActionType.HTTP,httpCommandList);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			log.debug("[createExceptPolicy] Response: " + response.getMsg());
			
			if(response.getCode() == -1)
			{
				reqAndRes.response.errorCode = ErrorCode.COMMUNICATE_WITH_DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(response.getMsg());
			}
			catch (Exception e)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			if(response.getCode() != 200)
			{
				//设备应用例外策略失败时，可能已经把策略创建好了，这里要检查设备是否返回策略号，如果返回，则需要删掉该策略
				log.debug("[createExceptPolicy] Device create policy failed, so, try to get the policy_id and delete it from device");
				
				//先设置错误返回码
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				
				//下面尝试获取设备可能生成的策略号，并删除该策略，这个过程是否会出错，不用关心
				JsonNode policyIdInJson = root.path("DATA").path("policy_id");
				if(policyIdInJson.isMissingNode())
				{
					//没找到策略号，说明设备应该没有生成策略
					log.debug("[createExceptPolicy] Can not find policy_id");
					break;
				}
				
				String policy_id = policyIdInJson.asText();
				log.debug("[createExceptPolicy] policy_id find: " + policy_id + ", delete it now by calling deleteExceptPolicy");
				
				SecurityFunctionRequestAndResponse newReqAndRes = (SecurityFunctionRequestAndResponse)reqAndRes.clone();
				if(newReqAndRes == null)
				{
					log.debug("[createExcetpPolicy] clone SecurityFunctionRequestAndResponse to delete except policy failed");
					break;
				}
				else
				{
					newReqAndRes.request.args.put("except_policy_id", policy_id);
					if(!this.deleteExceptPolicy(dev, newReqAndRes))
					{
						log.error("[createExcetpPolicy] delete except policy failed, reason: " + newReqAndRes.response.errorCode);
					}
					else
					{
						log.debug("[createExcetpPolicy] delete except policy success");
					}
				}
			}
			else
			{
				//设备正确生成并应用了例外策略
				int opt_status = root.path("OPT_STATUS").asInt();
				if(opt_status != 200)
				{
					reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
					reqAndRes.response.errorString = root.path("DATA").path("msg").asText();
					break;
				}
				
				String except_policy_id = root.path("DATA").path("policy_id").asText();
				reqAndRes.response.result.put("except_policy_id", except_policy_id);
				
				ret = true;
			}
		}while(false);
		
		return ret;
	}
	
	ErrorCode deviceCommand(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes)
			throws Exception {
		// TODO Auto-generated method stub
		return ErrorCode.SUCCESS;
	}
	
	ErrorCode reverseProxy(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> args = reqAndRes.request.args;
		String websiteName = (String)args.get("webprotect_key");
		
		switch(reqAndRes.request.opType)
		{
		case CREATE:
			{
				if(!this.createWebsite(dev, reqAndRes, websiteName))
				{
					log.error("[reverseProxy] create Website failed, errorCode: " + reqAndRes.response.errorCode + ", errorString: " + reqAndRes.response.errorString);
					return reqAndRes.response.errorCode;
				}
				
				reqAndRes.response.errorCode = ErrorCode.SUCCESS;
			}
			break;
		case DELETE:
			{
				if(!this.deleteWebsite(dev, reqAndRes, websiteName))
				{
					log.error("[reverseProxy:DELETE] delete website failed, errorCode: " + reqAndRes.response.errorCode + ", errorString: " + reqAndRes.response.errorString);
					break;
				}
				
				reqAndRes.response.errorCode = ErrorCode.SUCCESS;
			}
			break;
		default:
			reqAndRes.response.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
			reqAndRes.response.errorString = reqAndRes.request.opType.toString() + " is not supported";
			log.error("[reverseProxy:" + reqAndRes.request.opType.toString() + "] not supported");
			break;
		}
		
		return reqAndRes.response.errorCode;
	}
	
	@SuppressWarnings("static-access")
	private boolean applyPolicyTemplate(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes, String websiteID)
	{
		boolean ret = false;
		String url = dev.getBaseURL() + "website/strategy";
		
		do
		{
			HashMap<String, String> finalTemplate = new HashMap<String, String>();
			String tenantID = reqAndRes.request.tenantID;
			String postBody = null;
			
			try
			{
				reqAndRes.request.args.put("wesite_id", websiteID);
				
				StringWriter writer = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				JsonGenerator generator = mapper.getFactory().createGenerator(writer);
				
				/**
				 * The POST Body Should be sth. like this: 
				 * 	{
				 *		"website_id" : "xxxx", 
				 *		"tenant_id" : "xxxx", 
				 *		"template" : {
				 *			"xss" : "enabled", 
				 *			"sql" : "enabled", 
				 *			"anti_leech" : "enabled", 
				 *			"upload_limit" : "enabled", 
				 *			"download_limit" : "enabled", 
				 *			"anti_spider" : "enabled"
				 *		}
				 *	}
				 */
				
				@SuppressWarnings("unchecked")
				HashMap<String, String> template = (HashMap<String, String>)reqAndRes.request.args.get("template");
				
				Iterator<String> supportedPolicyTemplatesIt = WAF_4sdnapi_0_0_0_1.supportedPolicyTemplates.iterator();
				while(supportedPolicyTemplatesIt.hasNext())
				{
					String supportedPolicyTemplate = supportedPolicyTemplatesIt.next();
					
					if(template.containsKey(supportedPolicyTemplate))
					{
						finalTemplate.put(supportedPolicyTemplate, template.get(supportedPolicyTemplate));
					}
					else
					{
						finalTemplate.put(supportedPolicyTemplate, "disabled");
					}
				}
				
				HashMap<String, Object> postBodyMap = new HashMap<String, Object>();
				postBodyMap.put("website_id", websiteID);
				postBodyMap.put("tenant_id", tenantID);
				postBodyMap.put("template", finalTemplate);
				
				generator.writeObject(postBodyMap);
				writer.close();
				
				postBody = writer.toString();
				
			}
			catch(Exception e)
			{
				log.error("[applyPolicyTemplate] create json exception, " + e.getMessage());
				e.printStackTrace();
				reqAndRes.response.errorCode = ErrorCode.JSON_WRITE_ERROR;
				reqAndRes.response.errorString = "create json string failed, " + e.getMessage();
				break;
			}

			log.debug("[applyPolicyTemplate] Querying url " + url);
			log.debug("[applyPolicyTemplate] POST BODY: " + postBody);
			
			DeviceCommand deviceCommand=new DeviceCommand(url, "POST",new HashMap<String,String>(), postBody);
			List<DeviceCommand> httpCommandList=new LinkedList<DeviceCommand>(); 
			httpCommandList.add(deviceCommand);
			HTTPHelperResult response=null;
			try {
				response = generateWAFpolicy(reqAndRes, PolicyActionType.HTTP,httpCommandList);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			log.debug("[applyPolicyTemplate] device's response is " + response.getMsg());
			
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
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(response.getMsg());
			}
			catch (Exception e)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			int opt_status = root.path("OPT_STATUS").asInt();
			if(opt_status != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = root.path("DATA").path("msg").asText();
				break;
			}
			
			ret = true;
			if(reqAndRes.response.result != null)
			{
				reqAndRes.response.result.put("template", finalTemplate);
				reqAndRes.response.result.put("policy_id", "");
			}
			
			log.debug("[applyPolicyTemplate] success");
		}while(false);
		
		return ret;
	}
	
	private String getWebsiteID(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes, String websiteName)
	{
		String ret = null;
		String url = dev.getBaseURL() + "website/strategy?website=" + websiteName;
		
		log.debug("[getWebsiteID] Querying url " + url);
		
		do
		{
			HTTPHelperResult response = HTTPHelper.httpGet(url, new HashMap<String, String>());
			
			log.debug("[getWebsiteID] Device's response: " + response.getMsg());
			
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
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(response.getMsg());
			}
			catch (Exception e)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			JsonNode data = root.path("DATA");
			if(data.isMissingNode())
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = "missing field DATA";
				break;
			}
			if(!data.isArray())
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = "field DATA should be an array";
				break;
			}
			
			Iterator<JsonNode> it = data.iterator();
			while(it.hasNext())
			{
				JsonNode node = it.next();
				JsonNode sid = node.path("sid");
				if(sid.isMissingNode())
				{
					reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
					reqAndRes.response.errorString = "missing field sid";
					break;
				}
				
				ret = sid.asText();
				break;
			}
		}while(false);
		
		return ret;
	}
	
	private boolean deleteWebsite(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes, String websiteName)
	{
		boolean ret = true;
		
		String url = dev.getBaseURL() + "clouds/" + websiteName + "/website?ws=" + websiteName;
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Cookie", "USERNAME=SDN");
		
		log.debug("[deleteWebsite] Query url " + url);
		
		do
		{
			HTTPHelperResult response = HTTPHelper.httpDelete(url, header);
			
			log.debug("[deleteWebsite] device's response is " + response.getMsg());
			
			if(response.getCode() == -1)
			{
				reqAndRes.response.errorCode = ErrorCode.COMMUNICATE_WITH_DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			if(response.getCode() != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(response.getMsg());
			}
			catch (Exception e)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			int opt_status = root.path("OPT_STATUS").asInt();
			if(opt_status != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = root.path("DATA").path("msg").asText();
				break;
			}
		}while(false);
		
		//不管上面执行的结果如何，站点组都需要删除
		if(ret)
		{
			ret = this.deleteWebsiteGroup(dev, reqAndRes, websiteName);
		}
		else
		{
			this.deleteWebsiteGroup(dev, reqAndRes, websiteName);
		}
		
		return ret;
	}
	
	private boolean deleteWebsiteGroup(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes, String websiteGroupName)
	{
		boolean ret = true;
		
		String url = dev.getBaseURL() + "clouds/" + websiteGroupName;
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Cookie", "USERNAME=SDN");
		
		log.debug("[deleteWebsiteGroup] Query url " + url);
		
		do
		{
			HTTPHelperResult response = HTTPHelper.httpDelete(url, header);
			
			log.debug("[deleteWebsiteGroup] device's response is " + response.getMsg());
			
			if(response.getCode() == -1)
			{
				reqAndRes.response.errorCode = ErrorCode.COMMUNICATE_WITH_DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			if(response.getCode() != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(response.getMsg());
			}
			catch (Exception e)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			int opt_status = root.path("OPT_STATUS").asInt();
			if(opt_status != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = root.path("DATA").path("msg").asText();
				break;
			}
			
			ret = true;
		}while(false);
		
		return ret;
	}
	
	private boolean createWebsite(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes, String websiteName)
	{
		boolean ret = false;

		int port = WAF_4sdnapi_0_0_0_1.PROXY_PORT_BASE + (int)reqAndRes.request.args.get("id");
		String real_ip = (String)reqAndRes.request.args.get("ip");
		int real_port = (int)reqAndRes.request.args.get("port");
		String domain = (String)reqAndRes.request.args.get("domain");
		String protocol = (String)reqAndRes.request.args.get("protocol");
		String ssl = "false";
		do
		{
			if(protocol.equalsIgnoreCase("https"))
			{
			}
			
			String ip = this.getIntfaceIP(dev, reqAndRes, "eth1");
			if(ip == null)
			{
				break;
			}
			
			if(domain.equalsIgnoreCase("") || domain.equalsIgnoreCase(real_ip))
			{
				domain = ip;
			}
			
			if(!this.createWebsiteGroup(dev, reqAndRes, websiteName))
			{
				break;
			}
			
			String postBody = null;
			try
			{
				
				StringWriter writer = new StringWriter();
				JsonFactory jsonFactory = new JsonFactory();
				JsonGenerator generator = jsonFactory.createGenerator(writer);
				
				/**
				 * The POST Body Should be sth. like this:
				 * 	{
				 *		"SITE_LIST": [
				 *			{
				 *				"ACTIVE": "true",
				 *				"DESC": "MACHI-REST-API-TEST",
				 *				"IP": "10.65.100.151",
				 *				"PORT": "12222",
				 *				"NAME": "mc-test",
				 *				"WEBACCESS_ENABLED": "false",
				 *				"HOST": [
				 *					{
				 *						"DOMAIN": "10.65.100.151",
				 *						"SSL": "false",
				 *						"SERVER": [
				 *							{
				 *								"REAL_IP": "10.65.100.43",
				 *								"REAL_PORT": "80"
				 *							}
				 *						]
				 *					}
				 *				]
				 *			}
				 *		]
				 *	}
				 */
				generator.writeStartObject();
					generator.writeArrayFieldStart("SITE_LIST");
						generator.writeStartObject();
							generator.writeStringField("ACTIVE", "true");
							generator.writeStringField("DESC", "SDN-REVERSE-PROXY");
							generator.writeStringField("IP", ip);
							generator.writeStringField("PORT", String.valueOf(port));
							generator.writeStringField("NAME", websiteName);
							generator.writeStringField("WEBACCESS_ENABLED", "false");
							generator.writeArrayFieldStart("HOST");
								generator.writeStartObject();
									generator.writeStringField("DOMAIN", domain);
									generator.writeStringField("SSL", ssl);
									generator.writeArrayFieldStart("SERVER");
										generator.writeStartObject();
											generator.writeStringField("REAL_IP", real_ip);
											generator.writeStringField("REAL_PORT", String.valueOf(real_port));
										generator.writeEndObject();
									generator.writeEndArray();
								generator.writeEndObject();
							generator.writeEndArray();
						generator.writeEndObject();
					generator.writeEndArray();
				generator.writeEndObject();
				
				generator.close();
				
				//System.out.println(writer.toString());
				
				/**
				 * WHAT THE FUCK, THE INTERFACE CURRUPTED, MLGB!!!!!!!!!!!!!!!!!
				 * 
				 * "RESULT": "Configfile Saving error"
				 * 
				 * RESOLVED: Turn off the xsd checking on WAF
				 * 
				 */
				
				postBody = writer.toString();
				//postBody = generateWAFpolicy(reqAndRes, PolicyActionType.WAF_REVERSE_PROXY_CREATE_WEBSITE);
			}
			catch(Exception e)
			{
				log.error("[createWebsite] create json exception, " + e.getMessage());
				e.printStackTrace();
				reqAndRes.response.errorCode = ErrorCode.JSON_WRITE_ERROR;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			String url = dev.getBaseURL() + "clouds/" + websiteName + "/website";
			HashMap<String, String> header = new HashMap<String, String>();
			header.put("Cookie", "USERNAME=SDN");
			
			log.debug("[createWebsite] Querying url " + url);
			log.debug("[createWebsite] POST BODY: " + postBody);
			DeviceCommand deviceCommand=new DeviceCommand(url, "POST",header, postBody);
			List<DeviceCommand> httpCommandList=new LinkedList<DeviceCommand>(); 
			httpCommandList.add(deviceCommand);
			HTTPHelperResult response=null;
			try {
				response = generateWAFpolicy(reqAndRes, PolicyActionType.HTTP,httpCommandList);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			log.debug("[createWebsite] device's response is " + response.getMsg());
			
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
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(response.getMsg());
			}
			catch (Exception e)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			int opt_status = root.path("OPT_STATUS").asInt();
			if(opt_status != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = root.path("DATA").path("msg").asText();
				break;
			}
			
			/**
			 * It seems that there should be some more code here: add proxy ip and port to result
			 */
			if(reqAndRes.response.result != null)
			{
				reqAndRes.response.result.put("proxy_ip", ip);
				reqAndRes.response.result.put("proxy_port", port);
				reqAndRes.response.result.put("proxy_protocol", protocol);
				reqAndRes.response.result.put("policy_id", "");
			}
			
			ret = true;
			
		}while(false);
		
		return ret;
	}

	
	/***
	 * 根据安全设备类型生产对应的策略解析字符串
	 * author xingpanning
	 * @param reqAndRes
	 * @param policyActionType
	 * @return 解析后的字符串
	 * @throws Exception
	 */
	public HTTPHelperResult generateWAFpolicy(
			SecurityFunctionRequestAndResponse reqAndRes,PolicyActionType policyActionType,List<DeviceCommand> httpCommandList ) throws Exception {
		DevicePolicyObject object=new DevicePolicyObject();
		object.setDeviceArgs(reqAndRes);
		AtomPolicy atomPolicy=new AtomPolicy(object, 
				new PolicyAction(policyActionType,
						new PolicyActionArgs()));
		AtomPolicy[] policies=new AtomPolicy[1];//原子策略数组
		ResolvedCommand resolvedCommand=new ResolvedCommand();
		resolvedCommand.setHttpCommandList(httpCommandList);
		atomPolicy.setResolvedCommand(resolvedCommand);
		policies[0] = atomPolicy;
		PolicyInfo policyInfo=new PolicyInfo(reqAndRes.request.appID, policies, false);
		policyInfo.setSubject(new PolicySubject("", PolicySubjectType.SECURITY_DEVICE));
		policyInfo.setActionType(policyActionType);
		String strJson =policyResolver.generateNewPolicy(policyInfo);
		return HTTPHelperResult.fromJson(strJson);
	}
	
	private boolean createWebsiteGroup(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes, String websiteGroupName)
	{
		boolean ret = false;
		String url = dev.getBaseURL() + "clouds/" + websiteGroupName;
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Cookie", "USERNAME=SDN");
		
		log.debug("[createWebsiteGroup] Querying device " + url + " to create website group: " + websiteGroupName);
		
		do
		{
			HTTPHelperResult response = HTTPHelper.httpPost(url, "", header);
			
			log.debug("[createWebsiteGroup] device's response is " + response.getMsg());
			
			if(response.getCode() == -1)
			{
				reqAndRes.response.errorCode = ErrorCode.COMMUNICATE_WITH_DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			if(response.getCode() != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = response.getMsg();
				break;
			}
			
			ret = true;
		}while(false);
		
		if(!ret)
		{
			log.error("[createWebsiteGroup] failed for device " + url + ", errorCode: " + reqAndRes.response.errorCode + ", errorString: " + reqAndRes.response.errorString);
		}
		else
		{
			log.debug("[createWebsiteGroup] success for device " + url);
		}
		
		return ret;
	}
	
	/**
	 * Device Response:
	 * 
	 * {
	 *    "DATA":
	 *    [
	 *        {
	 *            "STATUS": "active",
	 *            "DUPLEX_MODE": "auto",
	 *            "SPEED": 0,
	 *            "MTU": 1500,
	 *            "HREF": "/interfaces/eth0",
	 *            "INTERFACE": "eth0",
	 *            "IP_ADDRESS":
	 *            [
	 *                {
	 *                    "IP": "10.64.100.119",
	 *                    "MASK": "255.255.0.0"
	 *                }
	 *            ]
	 *        },
	 *        {
	 *            "STATUS": "active",
	 *            "DUPLEX_MODE": "auto",
	 *            "SPEED": 0,
	 *            "MTU": 1500,
	 *            "HREF": "/interfaces/eth1",
	 *            "INTERFACE": "eth1",
	 *            "IP_ADDRESS":
	 *            [
	 *                {
	 *                    "IP": "10.65.100.151",
	 *                    "MASK": "255.255.0.0"
	 *                }
	 *            ]
	 *        },
	 *        {
	 *            "STATUS": "active",
	 *            "DUPLEX_MODE": "auto",
	 *            "SPEED": 0,
	 *            "MTU": 1500,
	 *            "HREF": "/interfaces/eth2",
	 *            "INTERFACE": "eth2",
	 *            "IP_ADDRESS":
	 *            [
	 *            ]
	 *        }
	 *    ],
	 *    "OPT_STATUS": 200
     * }
	 * @param dev
	 * @param reqAndRes
	 * @param intf
	 * @return
	 */
	private String getIntfaceIP(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes, String intf)
	{
		String url = dev.getBaseURL() + "interfaces";
		String interfaceIP = null;
		boolean bFound = false;
		boolean bActive = false;
		
		log.debug("[getIntfaceIP] Querying device: " + url);
		
		do
		{
			HTTPHelperResult httpResponse = HTTPHelper.httpGet(url, new HashMap<String, String>());
			
			log.debug("[getIntfaceIP] Device's response: " + httpResponse.getMsg());
			
			if(httpResponse.getCode() == -1)
			{
				reqAndRes.response.errorCode = ErrorCode.COMMUNICATE_WITH_DEVICE_ERROR;
				reqAndRes.response.errorString = httpResponse.getMsg();
				break;
			}
			else if(httpResponse.getCode() != 200)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_ERROR;
				reqAndRes.response.errorString = httpResponse.getMsg();
				break;
			}
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try
			{
				root = mapper.readTree(httpResponse.getMsg());
			}
			catch (Exception e)
			{
				log.error("getInterfaceIP] parse json response exception, " + e.getMessage());
				e.printStackTrace();
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = e.getMessage();
				break;
			}
			
			JsonNode data = root.path("DATA");
			if(data.isMissingNode())
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = "missing field DATA";
				break;
			}
			if(!data.isArray())
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = "field DATA is not an array";
				break;
			}
			
			Iterator<JsonNode> it = data.iterator();
			while(it.hasNext())
			{
				JsonNode node = it.next();
				
				if(!node.path("INTERFACE").asText().equalsIgnoreCase(intf))
				{
					continue;
				}
				bFound = true;
				
				if(!node.path("STATUS").asText().equalsIgnoreCase("active"))
				{
					log.error("[getIntfaceIP] eth1 is not active");
					continue;
				}
				bActive = true;
				
				JsonNode ipArray = node.path("IP_ADDRESS");
				if(ipArray.isMissingNode())
				{
					reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
					reqAndRes.response.errorString = "missing field DATA/IP_ADDRESS";
					break;
				}
				if(!ipArray.isArray())
				{
					reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
					reqAndRes.response.errorString = "field DATA/IP_ADDRESS is not an array";
					break;
				}
				
				Iterator<JsonNode> ipIt = ipArray.iterator();
				while(ipIt.hasNext())
				{
					JsonNode ip = ipIt.next();
					if(ip.path("IP").isMissingNode())
					{
						continue;
					}
					
					interfaceIP = ip.path("IP").asText();
				}
				
				if(interfaceIP == null)
				{
					reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
					reqAndRes.response.errorString = "missing field DATA/IP_ADDRESS[" + intf + "]/IP";
				}
				
				break;
			}
		}while(false);
		
		if(interfaceIP == null)
		{
			if(!bFound)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = "interface not found";
			}
			else if(!bActive)
			{
				reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
				reqAndRes.response.errorString = "interface is not active";
			}
			else
			{
				if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
				{
					reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
					reqAndRes.response.errorString = "Unknown reason";
				}
			}
			log.error("[getInterfaceIP] failed for device " + url + " on interface " + intf + ", errorCode: " + reqAndRes.response.errorCode + ", errorString: " + reqAndRes.response.errorString);
		}
		else
		{
			log.debug("[getInterfaceIP] success, " + intf + "'s ip is " + interfaceIP);
		}
		
		return interfaceIP;
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
