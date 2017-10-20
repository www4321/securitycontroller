package com.sds.securitycontroller.securityfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionRequestAndResponse;
import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionManager.SecurityFunctionInitializeContext;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClause.QueryClauseType;
import com.sds.securitycontroller.storage.QueryClauseItem;
import com.sds.securitycontroller.utils.Base64Utils;
import com.sds.securitycontroller.utils.MapUtils;

public class WebProtectSecurityFunction extends SecurityFunction {
	private static Logger log = LoggerFactory.getLogger(WebProtectSecurityFunction.class);
	private static HashMap<String, String> defaultPolicyTemplate = null;
	
	public WebProtectSecurityFunction()
	{
		super();
	}
	
	@Override
	public ErrorCode initialize(SecurityFunctionInitializeContext secFuncInitCtx) throws Exception
	{
		ErrorCode ret = ErrorCode.SUCCESS;
		
		do
		{
			ret = super.initialize(secFuncInitCtx);
			if(ret != ErrorCode.SUCCESS)
			{
				break;
			}
			
			this.essentialSecFuncList.add("WEB_PROTECT_POLICY_TEMPLATE");
			this.essentialSecFuncList.add("WEB_PROTECT_DEVICE_COMMAND");
			this.essentialSecFuncList.add("WEB_PROTECT_REVERSE_PROXY");
			this.essentialSecFuncList.add("WEB_PROTECT_WHITE_LIST");
			
			this.essentialParamSet.add("domain");
			this.essentialParamSet.add("protocol");
			this.essentialParamSet.add("ip");
			this.essentialParamSet.add("port");
			
			this.storageSource = secFuncInitCtx.stroageSource;
						
			String tableName = "t_secfunc_webprotect";
			Map<String, String> tableColumns = new HashMap<String, String>() {
				private static final long serialVersionUID = 1232L;

				{
					put("webprotect_key", "VARCHAR(128)");
					put("device_id", "VARCHAR(32)");
					put("app_id", "VARCHAR(16)");
					put("website_domain", "VARCHAR(64)");
					put("website_protocol", "VARCHAR(8)");
					put("website_ip", "VARCHAR(16)");
					put("website_port", "Integer");
					put("PRIMARY_KEY", "webprotect_key");
					put("EXTRA", "ENGINE=InnoDB DEFAULT CHARSET=utf8");
				}
			};
			storageSource.createTable(tableName, tableColumns);
			
			tableName = "t_secfunc_webprotect_policy_template";
			tableColumns = new HashMap<String, String>() {
				private static final long serialVersionUID = 1232L;

				{
					put("webprotect_key", "VARCHAR(128)");
					put("policy_id", "VARCHAR(16)");
					put("template", "VARCHAR(1024)");
					put("error_code", "VARCHAR(32)");
					put("error_string", "VARCHAR(128)");
					put("PRIMARY_KEY", "webprotect_key");
					put("EXTRA", "ENGINE=InnoDB DEFAULT CHARSET=utf8");
				}
			};
			storageSource.createTable(tableName, tableColumns);


			tableName = "t_secfunc_webprotect_reverse_proxy";
			tableColumns = new HashMap<String, String>() {
				private static final long serialVersionUID = 1232L;

				{
					put("id", "int(10) AUTO_INCREMENT");
					put("webprotect_key", "VARCHAR(128)");
					put("policy_id", "VARCHAR(16)");
					put("website_protocol", "VARCHAR(8)");
					put("proxy_ip", "VARCHAR(16)");
					put("proxy_port", "int(11)");
					put("error_code", "VARCHAR(32)");
					put("error_string", "VARCHAR(128)");
					put("PRIMARY_KEY", "id,webprotect_key");
					put("EXTRA", "ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8");
				}
			};
			storageSource.createTable(tableName, tableColumns);

			tableName = "t_secfunc_webprotect_white_list";
			tableColumns = new HashMap<String, String>() {
				private static final long serialVersionUID = 1232L;

				{
					put("white_list_key", "varchar(134)");
					put("webprotect_key", "VARCHAR(128)");
					put("except_policy_id", "VARCHAR(16)");
					put("site_id", "VARCHAR(16)");
					put("src_ip", "VARCHAR(16)");
					put("dst_port", "VARCHAR(8)");
					put("domain", "VARCHAR(64)");
					put("uri", "VARCHAR(4096)");
					put("event_type", "VARCHAR(8)");
					put("policy_id", "VARCHAR(16)");
					put("rule_id", "VARCHAR(16)");
					put("PRIMARY_KEY", "white_list_key");
					put("EXTRA", "ENGINE=InnoDB DEFAULT CHARSET=utf8");
				}
			};
			storageSource.createTable(tableName, tableColumns);

			this.storageSource.setTablePrimaryKeyName(this.getTableName("webprotect"), "webprotect_key");
			this.storageSource.setTablePrimaryKeyName(this.getTableName("policy_template"), "webprotect_key");
			this.storageSource.setTablePrimaryKeyName(this.getTableName("white_list"), "white_list_key");
			this.storageSource.setTablePrimaryKeyName(this.getTableName("reverse_proxy"), "webprotect_key");
			
			this.securityFunctionName = "webprotect";
			
			WebProtectSecurityFunction.defaultPolicyTemplate = new HashMap<String, String>();
			WebProtectSecurityFunction.defaultPolicyTemplate.put("xss", "enabled");
			WebProtectSecurityFunction.defaultPolicyTemplate.put("sql", "enabled");
		}while(false);
		
		return ret;
	}

	@Override
	public ErrorCode callSecurityFunction(SecurityFunctionRequestAndResponse reqAndRes) throws Exception {
		// TODO Auto-generated method stub
		String webprotect_key = this.getWebprotectKey(reqAndRes);
		
		reqAndRes.request.args.put("webprotect_key", webprotect_key);
		
		//调用设备接口前做一些准备工作
		switch(reqAndRes.request.secFuncType)
		{
		case "WEB_PROTECT_POLICY_TEMPLATE": 
			{
				WebprotectItem item = this.getWebprotectItem(reqAndRes);
				if(item == null)
				{
					log.error("[WEB_PROTECT_POLICY_TEMPLATE] WebprotectItem does not exist for " + this.getWebprotectKey(reqAndRes));
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
					reqAndRes.response.errorString = "website has not been protected, create reverse proxy first";
					break;
				}
				
				//SecurityDevice dev = this.getSecurityDevice(item.device_id);
				SecurityDevice dev = this.devAllocator.findSecurityDevice(item.device_id);
				if(dev == null)
				{
					log.error("[WEB_PROTECT_POLICY_TEMPLATE] Get device " + item.device_id + " from cache failed for " + webprotect_key);
					reqAndRes.response.errorCode = ErrorCode.CANNOT_FIND_DEVICE;
					reqAndRes.response.errorString = "can not find device";
					break;
				}
				
				reqAndRes.response.errorCode = this.policyTemplate(dev, reqAndRes);
			}
			break;
		case "WEB_PROTECT_DEVICE_COMMAND":
			{
				WebprotectItem item = this.getWebprotectItem(reqAndRes);
				if(item == null)
				{
					log.error("[WEB_PROTECT_DEVICE_COMMAND] WebprotectItem does not exist for " + this.getWebprotectKey(reqAndRes));
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
					reqAndRes.response.errorString = "website has not been protected, create reverse proxy first";
					break;
				}
				
				//SecurityDevice dev = this.getSecurityDevice(item.device_id);
				SecurityDevice dev = this.devAllocator.findSecurityDevice(item.device_id);
				if(dev == null)
				{
					log.error("[WEB_PROTECT_DEVICE_COMMAND] Get device " + item.device_id + " from cache failed for " + webprotect_key);
					reqAndRes.response.errorCode = ErrorCode.CANNOT_FIND_DEVICE;
					reqAndRes.response.errorString = "can not find device";
					break;
				}
				
				reqAndRes.response.errorCode = this.deviceCommand(dev, reqAndRes);
			}
			break;
		case "WEB_PROTECT_REVERSE_PROXY":
			{
				SecurityDevice dev = null;
				
				//先看这个站点之前有没有在SC注册过
				WebprotectItem item = this.getWebprotectItem(reqAndRes);
				if(item == null)
				{//没注册过，则从DeviceManager新申请一个设备
					if(reqAndRes.request.opType == OperationType.CREATE)
					{//只有当方法是新建时，才能从DeviceManager申请新设备
						dev = this.getNewDevice(reqAndRes, reqAndRes.request.secFuncType);
						if(dev == null)
						{//申请失败
							log.error("[WEB_PROTECT_REVERSE_PROXY] Allocate device failed for " + webprotect_key);
							reqAndRes.response.errorCode = ErrorCode.CANNOT_FIND_DEVICE_INSUFFICIENT_RESOURCE;
							reqAndRes.response.errorString = "can not allocate device for this request";
							break;
						}
						else
						{
							log.debug("[WEB_PROTECT_REVERSE_PROXY] Allocate device " + dev.device_id + " success for " + webprotect_key);
						}
					}
					else
					{
						reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
						reqAndRes.response.errorString = "website has not been protected, create reverse proxy first";
						break;
					}
				}
				else
				{
					//注册过，则从缓存中读取这个设备对象
					//dev = this.getSecurityDevice(item.device_id);
					if(reqAndRes.request.opType == OperationType.CREATE)
					{
						log.error("[WEB_PROTECT_REVERSE_PROXY] Invalid operation POST");
						reqAndRes.response.errorCode = ErrorCode.RESOURCE_ALREADY_EXIST;
						reqAndRes.response.errorString = "reverse proxy already exists";
						break;
					}
					else
					{
						dev = this.devAllocator.findSecurityDevice(item.device_id);
						if(dev == null)
						{//没读到，说明由于某些原因(SC重启)，设备对象没有被加载进来，则需要使用这个deviceID从DeviceManager重新获取该设备对象
							log.error("[WEB_PROTECT_REVERSE_PROXY] Get device " + item.device_id + " from cache failed for " + webprotect_key);
							reqAndRes.response.errorCode = ErrorCode.CANNOT_FIND_DEVICE;
							reqAndRes.response.errorString = "can not find device";
							break;
						}
						else
						{
							log.debug("[WEB_PROTECT_REVERSE_PROXY] Get device " + dev.device_id + " from cache success for " + webprotect_key);
						}
					}
				}
				
				reqAndRes.response.errorCode = this.reverseProxy(dev, reqAndRes);
			}
			break;
		case "WEB_PROTECT_WHITE_LIST":
			{
				WebprotectItem item = this.getWebprotectItem(reqAndRes);
				if(item == null)
				{
					log.error("[WEB_PROTECT_WHITE_LIST] WebprotectItem does not exist for " + this.getWebprotectKey(reqAndRes));
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
					reqAndRes.response.errorString = "website has not been protected, create reverse proxy first";
					break;
				}
				
				//SecurityDevice dev = this.getSecurityDevice(item.device_id);
				SecurityDevice dev = this.devAllocator.findSecurityDevice(item.device_id);
				if(dev == null)
				{
					log.error("[WEB_PROTECT_WHITE_LIST] Get device " + item.device_id + " from cache failed for " + webprotect_key);
					reqAndRes.response.errorCode = ErrorCode.CANNOT_FIND_DEVICE;
					reqAndRes.response.errorString = "can not find device";
					break;
				}
				
				reqAndRes.response.errorCode = this.whiteList(dev, reqAndRes);
			}
			break;
		case "ALL":
			{
				if(reqAndRes.request.opType != OperationType.GET)
				{//必须是GET
					reqAndRes.response.errorCode = ErrorCode.INVALID_FIELD_OPTYPE;
					reqAndRes.response.errorString = "only GET is permitted";
					break;
				}
				
				Map<String, Object> args = reqAndRes.request.args;
				String appID = reqAndRes.request.appID;
				String tenantID = reqAndRes.request.tenantID;
				String domain = (String)args.get("domain");
				String protocol = (String)args.get("protocol");
				String ip = (String)args.get("ip");
				int port = -1;
				if(args.get("port") instanceof String)
				{
					try
					{
						port = Integer.parseInt((String)args.get("port"));
					}
					catch(Exception e)
					{
						port = -1;
					}
				}
				else
				{
					port = (int)args.get("port");
				}
				
				if(!appID.equalsIgnoreCase("*") 
						&& !tenantID.equalsIgnoreCase("*") 
						&& !domain.equalsIgnoreCase("*") 
						&& !protocol.equalsIgnoreCase("*")
						&& !ip.equalsIgnoreCase("*")
						&& port != -1)
				{//精确查询
					WebprotectItem item = this.getWebprotectItem(reqAndRes);
					if(item == null)
					{
						log.error("[ALL] WebprotectItem does not exist for " + this.getWebprotectKey(reqAndRes));
						reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
						reqAndRes.response.errorString = "website has not been protected, create reverse proxy first";
						break;
					}
					
					HashMap<String, Object> result = reqAndRes.response.result;
					HashMap<String, Object> reverseProxyMap = new HashMap<String, Object>();
					HashMap<String, Object> policyTemplateMap = new HashMap<String, Object>();
					HashMap<String, Object> whiteListMap = new HashMap<String, Object>();
					
					ReverseProxyItem reverseProxyItem = this.getReverseProxyItem(reqAndRes);
					if(reverseProxyItem != null)
					{
						reverseProxyMap.put("proxy_ip", reverseProxyItem.proxy_ip);
						reverseProxyMap.put("proxy_port", reverseProxyItem.proxy_port);
						reverseProxyMap.put("proxy_protocol", reverseProxyItem.proxy_protocol);
						reverseProxyMap.put("error_code", reverseProxyItem.error_code);
					}
					
					PolicyTemplateItem policyTemplateItem = this.getPolicyTemplateItem(reqAndRes);
					if(policyTemplateItem != null)
					{
						policyTemplateMap.put("template", policyTemplateItem.template);
						policyTemplateMap.put("error_code", policyTemplateItem.error_code);
					}
					
					List<WhiteListItem> whiteListItems = this.getWhiteListItems(reqAndRes);
					if(whiteListItems.size() != 0)
					{
						ArrayList<HashMap<String, String>> tmp = new ArrayList<HashMap<String, String>>();
						
						Iterator<WhiteListItem> it = whiteListItems.iterator();
						while(it.hasNext())
						{
							WhiteListItem whiteListItem = it.next();
							HashMap<String, String> map = new HashMap<String, String>();
							
							map.put("except_policy_id", whiteListItem.except_policy_id);
							map.put("site_id", whiteListItem.site_id);
							map.put("src_ip", whiteListItem.src_ip);
							map.put("dst_port", whiteListItem.dst_port);
							map.put("domain", whiteListItem.domain);
							map.put("uri", whiteListItem.uri);
							map.put("event_type", whiteListItem.event_type);
							map.put("policy_id", whiteListItem.policy_id);
							map.put("rule_id", whiteListItem.rule_id);
							
							tmp.add(map);
						}
						
						whiteListMap.put("white_list", tmp);
					}
					
					result.put("reverse_proxy", reverseProxyMap);
					result.put("policy_template", policyTemplateMap);
					result.put("white_list", whiteListMap);
					
					reqAndRes.response.errorCode = ErrorCode.SUCCESS;
				}
				else
				{//模糊查询
					ArrayList<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
					
					if(!appID.equalsIgnoreCase("*"))
					{
						items.add(new QueryClauseItem("app_id", appID, QueryClauseItem.OpType.EQ));
					}
					
					if(!tenantID.equalsIgnoreCase("*"))
					{
						items.add(new QueryClauseItem("tenant_id", tenantID, QueryClauseItem.OpType.EQ));
					}
					
					if(!domain.equalsIgnoreCase("*"))
					{
						items.add(new QueryClauseItem("website_domain", domain, QueryClauseItem.OpType.EQ));
					}
					
					if(!protocol.equalsIgnoreCase("*"))
					{
						items.add(new QueryClauseItem("website_protocol", protocol, QueryClauseItem.OpType.EQ));
					}
					
					if(!ip.equalsIgnoreCase("*"))
					{
						items.add(new QueryClauseItem("website_ip", ip, QueryClauseItem.OpType.EQ));
					}
					
					if(port != -1)
					{
						items.add(new QueryClauseItem("website_port", port, QueryClauseItem.OpType.EQ));
					}
					
					QueryClause qc = new QueryClause(items, this.getTableName("webprotect"), null, null);
					if(items.size() == 0)
					{
						qc.setType(QueryClauseType.EMPTY);
					}
					@SuppressWarnings("unchecked")
					List<WebprotectItem> dbResult = (List<WebprotectItem>)this.storageSource.executeQuery(qc, WebprotectItem.class);
					
					if(dbResult.size() == 0)
					{
						reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
						reqAndRes.response.errorString = "no website matches the condition";
						break;
					}
					
					ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
					Iterator<WebprotectItem> it = dbResult.iterator();
					while(it.hasNext())
					{
						WebprotectItem item = it.next();
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("app_id", item.app_id);
						map.put("tenant_id", item.tenant_id);
						map.put("website_domain", item.website_domain);
						map.put("website_protocol", item.website_protocol);
						map.put("website_ip", item.website_ip);
						map.put("website_port", item.website_port);
						
						result.add(map);
					}
					
					reqAndRes.response.result.put("websites", result);
					
					System.out.println(result);
				}
			}
			break;
		default:
			reqAndRes.response.errorCode = ErrorCode.SECURITY_FUNCTION_TYPE_NOT_SUPPORTED;
			break;
		}
		
		return reqAndRes.response.errorCode;
	}

	@Override
	public ErrorCode getSupportedSecurityDeviceType(
			ArrayList<SecurityDeviceType> secDevTypeList) throws Exception {
		// TODO Auto-generated method stub
		secDevTypeList.add(SecurityDeviceType.SD_WAF);
		return ErrorCode.SUCCESS;
	}
	
	protected String getWebprotectKey(SecurityFunctionRequestAndResponse reqAndRes)
	{
		return reqAndRes.request.appID + reqAndRes.request.tenantID + reqAndRes.request.args.get("domain")
				+ reqAndRes.request.args.get("protocol") + reqAndRes.request.args.get("ip") + reqAndRes.request.args.get("port");
	}
	
	protected WebprotectItem getWebprotectItem(SecurityFunctionRequestAndResponse reqAndRes)
	{
		WebprotectItem item = null;
		log.debug("[getWebprotectItem] getting WebprotectItem for {}", this.getWebprotectKey(reqAndRes));
		item = (WebprotectItem)this.storageSource.getEntity(this.getTableName("webprotect"), this.getWebprotectKey(reqAndRes), WebprotectItem.class);
		if(item == null)
		{
			log.error("[getWebprotectItem] failed for {}", this.getWebprotectKey(reqAndRes));
		}
		else
		{
			log.debug("[getWebprotectItem] success for {}", this.getWebprotectKey(reqAndRes));
		}
		return item;
	}
	
	protected boolean setWebprotectItem(WebprotectItem item)
	{
		log.debug("[setWebprotectItem] setting WebprotectItem for {}", item.webprotect_key);
		if(this.storageSource.updateOrInsertEntity(this.getTableName("webprotect"), item) <= 0)
		{
			log.error("[setWebprotectItem] failed for {}", item.webprotect_key);
			return false;
		}
		else
		{
			log.debug("[setWebprotectItem] success for {}", item.webprotect_key);
			return true;
		}
	}
	
	protected boolean deleteWebprotectItem(WebprotectItem item)
	{
		log.debug("[deleteWebprotectItem] deleting WebprotectItem for {}", item.webprotect_key);
		if(this.storageSource.deleteEntity(this.getTableName("webprotect"), item.webprotect_key) <= 0)
		{
			log.error("[deleteWebprotectItem] failed for {}", item.webprotect_key);
			return false;
		}
		else
		{
			log.debug("[deleteWebprotectItem] success for {}", item.webprotect_key);
			return true;
		}
	}
	
	protected ReverseProxyItem getReverseProxyItem(SecurityFunctionRequestAndResponse reqAndRes)
	{
		ReverseProxyItem item = null;
		log.debug("[getReverseProxyItem] getting ReverseProxyItem for {}", this.getWebprotectKey(reqAndRes));
		item = (ReverseProxyItem)this.storageSource.getEntity(this.getTableName("reverse_proxy"), this.getWebprotectKey(reqAndRes), ReverseProxyItem.class);
		if(item == null)
		{
			log.error("[getReverseProxyItem] failed for {}", this.getWebprotectKey(reqAndRes));
		}
		else
		{
			log.debug("[getReverseProxyItem] success for {}", this.getWebprotectKey(reqAndRes));
		}
		return item;
	}
	
	protected boolean setReverseProxyItem(ReverseProxyItem item)
	{
		log.debug("[setReverseProxyItem] setting ReverseProxyItem for {}", item.webprotect_key);
		if(this.storageSource.updateOrInsertEntity(this.getTableName("reverse_proxy"), item) <= 0)
		{
			log.error("[setReverseProxyItem] failed for {}", item.webprotect_key);
			return false;
		}
		else
		{
			log.debug("[setReverseProxyItem] success for {}", item.webprotect_key);
			return true;
		}
	}
	
	protected boolean deleteReverseProxyItem(ReverseProxyItem item)
	{
		log.debug("[deleteReverseProxyItem] deleting ReverseProxyItem for {}", item.webprotect_key);
		if(this.storageSource.deleteEntity(this.getTableName("reverse_proxy"), item.webprotect_key) <= 0)
		{
			log.error("[deleteReverseProxyItem] failed for {}", item.webprotect_key);
			return false;
		}
		else
		{
			log.debug("[deleteReverseProxyItem] success for {}", item.webprotect_key);
			return true;
		}
	}
	
	protected PolicyTemplateItem getPolicyTemplateItem(SecurityFunctionRequestAndResponse reqAndRes)
	{
		PolicyTemplateItem item = null;
		log.debug("[getPolicyTemplateItem] getting PolicyTemplateItem for {}", this.getWebprotectKey(reqAndRes));
		item = (PolicyTemplateItem)this.storageSource.getEntity(this.getTableName("policy_template"), this.getWebprotectKey(reqAndRes), PolicyTemplateItem.class);
		if(item == null)
		{
			log.error("[getPolicyTemplateItem] failed for {}", this.getWebprotectKey(reqAndRes));
		}
		else
		{
			log.debug("[getPolicyTemplateItem] success for {}", this.getWebprotectKey(reqAndRes));
		}
		return item;
	}
	
	protected boolean setPolicyTemplateItem(PolicyTemplateItem item)
	{
		log.debug("[setPolicyTemplateItem] setting PolicyTemplateItem for {}", item.webprotect_key);
		if(this.storageSource.updateOrInsertEntity(this.getTableName("policy_template"), item) <= 0)
		{
			log.error("[setPolicyTemplateItem] failed for {}", item.webprotect_key);
			return false;
		}
		else
		{
			log.debug("[setPolicyTemplateItem] success for {}", item.webprotect_key);
			return true;
		}
	}
	
	protected boolean deletePolicyTemplateItem(PolicyTemplateItem item)
	{
		log.debug("[deletePolicyTemplateItem] deleting PolicyTemplateItem for {}", item.webprotect_key);
		if(this.storageSource.deleteEntity(this.getTableName("policy_template"), item.webprotect_key) <= 0)
		{
			log.error("[deletePolicyTemplateItem] failed for {}", item.webprotect_key);
			return false;
		}
		else
		{
			log.debug("[deletePolicyTemplateItem] success for {}", item.webprotect_key);
			return true;
		}
	}
	
	protected List<WhiteListItem> getWhiteListItems(SecurityFunctionRequestAndResponse reqAndRes)
	{
		ArrayList<WhiteListItem> result = new ArrayList<WhiteListItem>();
		
		log.debug("[getWhiteListItems] getting WhiteListItems for {}", this.getWebprotectKey(reqAndRes));
		do
		{
			ArrayList<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
			String webprotect_key = this.getWebprotectKey(reqAndRes);
			items.add(new QueryClauseItem("webprotect_key", webprotect_key, QueryClauseItem.OpType.EQ));
			
			QueryClause qc = new QueryClause(items, this.getTableName("white_list"), null, null);
			
			@SuppressWarnings("unchecked")
			List<WhiteListItem> dbResult = (List<WhiteListItem>)this.storageSource.executeQuery(qc, WhiteListItem.class);
			if(dbResult.size() == 0)
			{
				log.error("[getWhiteListItems] failed for {}", this.getWebprotectKey(reqAndRes));
				break;
			}

			Iterator<WhiteListItem> it = dbResult.iterator();
			while(it.hasNext())
			{
				WhiteListItem item = it.next();
				result.add(item);
			}
			log.debug("[getWhiteListItems] {} item success for {}", result.size(), this.getWebprotectKey(reqAndRes));
		}while(false);
		
		return result;
	}
	
	protected WhiteListItem getWhiteListItem(SecurityFunctionRequestAndResponse reqAndRes, String except_policy_id)
	{
		WhiteListItem item = null;
		log.debug("[getWhiteListItem] getting WhiteListItem for {}", this.getWebprotectKey(reqAndRes) + except_policy_id);
		item = (WhiteListItem)this.storageSource.getEntity(this.getTableName("white_list"), 
				this.getWebprotectKey(reqAndRes) + except_policy_id, WhiteListItem.class);
		if(item == null)
		{
			log.error("[getWhiteListItem] failed for {}", this.getWebprotectKey(reqAndRes) + except_policy_id);
		}
		else
		{
			log.debug("[getWhiteListItem] success for {}", this.getWebprotectKey(reqAndRes) + except_policy_id);
		}
		return item;
	}
	
	protected boolean setWhiteListItem(WhiteListItem item)
	{
		log.debug("[setWhiteListItem] setting WhiteListItem for {}", item.white_list_key);
		if(this.storageSource.updateOrInsertEntity(this.getTableName("white_list"), item) <= 0)
		{
			log.error("[setWhiteListItem] failed for {}", item.white_list_key);
			return false;
		}
		else
		{
			log.debug("[setWhiteListItem] success for {}", item.white_list_key);
			return true;
		}
	}
	
	protected boolean deleteWhiteListItem(WhiteListItem item)
	{
		log.debug("[deleteWhiteListItem] deleting WhiteListItem for {}", item.white_list_key);
		if(this.storageSource.deleteEntity(this.getTableName("white_list"), item.webprotect_key + item.except_policy_id) <= 0)
		{
			log.error("[deleteWhiteListItem] failed for {}", item.white_list_key);
			return false;
		}
		else
		{
			log.debug("[deleteWhiteListItem] success for {}", item.white_list_key);
			return true;
		}
	}
	
	protected void deleteWhiteListItems(List<WhiteListItem> items)
	{
		log.debug("[deleteWhiteListItems] deleting WhiteListItems");
		Iterator<WhiteListItem> it = items.iterator();
		while(it.hasNext())
		{
			WhiteListItem item = it.next();
			this.deleteWhiteListItem(item);
		}
		log.debug("[deleteWhiteListItems] delete WhiteListItems END");
	}
	
	protected SecurityDevice getNewDevice(SecurityFunctionRequestAndResponse reqAndRes, String secFuncType) throws Exception
	{
		SecurityDevice dev = null;
		
		dev = this.devAllocator.allocate(SecurityDeviceType.SD_WAF, 0, null, null, null);
		
		if(dev != null)
		{//获取到设备则需要更新数据库
			log.debug("[getSecurityDeviceFromDeviceManagerBySecFuncType] Allocate device success");
			
			if(dev.driver == null)
			{
				if(this.probe(dev) != ErrorCode.SUCCESS)
				{
					log.error("[getSecurityDeviceFromDeviceManagerBySecFuncType] Probe device failed");
					//free dev to DeviceManager
					return null;
				}
			}
			
			WebprotectItem item = this.getWebprotectItem(reqAndRes);
			if(item == null)
			{//说明这是第一次，需要在数据库里新建一条信息
				item = new WebprotectItem();
				item.webprotect_key = this.getWebprotectKey(reqAndRes);
				item.app_id = reqAndRes.request.appID;
				item.tenant_id = reqAndRes.request.tenantID;
				item.website_domain = (String)reqAndRes.request.args.get("domain");
				item.website_ip = (String)reqAndRes.request.args.get("ip");
				item.website_port = (int)reqAndRes.request.args.get("port");
				item.website_protocol = (String)reqAndRes.request.args.get("protocol");
				item.device_id = dev.device_id;
			}
			else
			{//说明这不是第一次，需要更新数据库的信息
				item.device_id = dev.device_id;
			}
			
			if(this.setWebprotectItem(item))
			{
				log.debug("[Webprotect] Database updated success for " + item.webprotect_key + ", deviceID is " + item.device_id);
			}
			else
			{
				log.debug("[Webprotect] Database updated failed for " + item.webprotect_key);
				//free dev to DeviceManager
				dev = null;
			}
		}
		
		return dev;
	}
	
	private String getTableName(String type)
	{
		switch(type)
		{
		case "webprotect":
			return "t_secfunc_webprotect";
		case "policy_template":
			return "t_secfunc_webprotect_policy_template";
		case "reverse_proxy":
			return "t_secfunc_webprotect_reverse_proxy";
		case "white_list":
			return "t_secfunc_webprotect_white_list";
		}

		log.warn("[getTableName] " + type);
		return null;
	}

	ErrorCode policyTemplate(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes) throws Exception
	{
		//调用实际的设备接口
		String webprotect_key = this.getWebprotectKey(reqAndRes);
		
		Map<String, Object> args = reqAndRes.request.args;
		
		switch(reqAndRes.request.opType)
		{
		case CREATE:
			{
				PolicyTemplateItem item = this.getPolicyTemplateItem(reqAndRes);
				if(item != null)
				{
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_ALREADY_EXIST;
					reqAndRes.response.errorString = "policy template already exists, re-create is not permitted";
					break;
				}
				
				if(!args.containsKey("template"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument template";
					break;
				}
				if(!(args.get("template") instanceof Map))
				{
					reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
					reqAndRes.response.errorString = "argument template is not a dictionary";
					break;
				}
				
				item = new PolicyTemplateItem();
				item.webprotect_key = webprotect_key;
				item.template = MapUtils.MapToString((Map<?, ?>)args.get("template"));
				item.error_code = ErrorCode.PROCESS_IN_PROGRESS.toString();
				item.error_string = "";
				
				reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
				
				item.error_code = reqAndRes.response.errorCode.toString();
				
				if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
				{
					log.debug("[policyTemplate:CREATE] success for " + webprotect_key);
					
					if(reqAndRes.response.result.containsKey("template"))
					{
						item.template = MapUtils.MapToString((Map<?, ?>)reqAndRes.response.result.get("template"));
					}
					
					//只有成功的情况，才存入数据库
					if(!this.setPolicyTemplateItem(item))
					{
						//存入数据库失败，则把所有的相关数据都删掉
						log.error("[policyTemplate:CREATE] update data failed for " + webprotect_key);
						reqAndRes.response.errorCode = ErrorCode.DATABASE_UPDATE_DATA_FAILED;
						reqAndRes.response.errorString = "insert data to database failed";
						
						log.debug("[policyTemplate:CREATE] delete policy template on device and database");
						
						log.debug("[policyTemplate:CREATE] try to delete policy template on device");
						SecurityFunctionRequestAndResponse newReqAndRes = (SecurityFunctionRequestAndResponse)reqAndRes.clone();
						if(newReqAndRes != null)
						{
							log.debug("[policyTemplate:CREATE] clone reqAndRes success");
							newReqAndRes.request.opType = OperationType.DELETE;
							newReqAndRes.request.args.remove("template");
							newReqAndRes.response.errorCode = dev.callSecurityFunction(newReqAndRes);
							if(newReqAndRes.response.errorCode == ErrorCode.SUCCESS)
							{
								log.debug("[policyTemplate:CREATE] delete policy template on device success");
							}
							else
							{
								log.error("[policyTemplate:CREATE] delete policy template on device failed, errorCode: " 
													+ newReqAndRes.response.errorCode.toString() 
													+ ", errorString: " 
													+ newReqAndRes.response.errorString);
							}
						}
						else
						{
							log.error("[policyTemplate:CREATE] clone reqAndRes failed");
						}
						
						log.debug("[policyTemplate:CREATE] try to delete policy template from database");
						if(!this.deletePolicyTemplateItem(item))
						{
							log.error("[policyTemplate:CREATE] delete policy template from database failed");
						}
						else
						{
							log.debug("[policyTemplate:CREATE] delete policy template from database success");
						}
						break;
					}
				}
				else
				{
					log.error("[policyTemplate:CREATE] failed for " + webprotect_key + ", reason: " + reqAndRes.response.errorCode.toString());
				}
			}
			break;
		case GET:
			{
				PolicyTemplateItem item = this.getPolicyTemplateItem(reqAndRes);
				if(item == null)
				{
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
					reqAndRes.response.errorString = "no policy template applied for the website, apply policy template first";
					break;
				}
				
				reqAndRes.response.result.put("template", MapUtils.StringToMap(item.template));
				reqAndRes.response.result.put("error_code", item.error_code);
				
				reqAndRes.response.errorCode = ErrorCode.SUCCESS;
			}
			break;
		case MODIFY:
			{
				PolicyTemplateItem item = this.getPolicyTemplateItem(reqAndRes);
				if(item == null)
				{
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
					reqAndRes.response.errorString = "no policy template applied for the website, apply policy template first";
					break;
				}
				
				if(!args.containsKey("template"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument template";
					break;
				}
				if(!(args.get("template") instanceof Map))
				{
					reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
					reqAndRes.response.errorString = "argument template is not a dictionary";
					break;
				}
				
				Map<String, Object> original_template = MapUtils.StringToMap(item.template);
				
				reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
				
				item.error_code = reqAndRes.response.errorCode.toString();
				
				if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
				{
					log.debug("[policyTemplate:MODIFY] success for " + webprotect_key);
					if(!((Map<String, Object>)reqAndRes.response.result).containsKey("policy_id"))
					{
						log.error("[policyTemplate:MODIFY] success for " + webprotect_key + ", but device's response could not be recognized");
						reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
						reqAndRes.response.errorString = "device's response could not be recognized";
					}
					else
					{
						item.policy_id = (String)((Map<String, Object>)reqAndRes.response.result).get("policy_id");
					}
					
					if(reqAndRes.response.result.containsKey("template"))
					{
						item.template = MapUtils.MapToString((Map<?,?>)reqAndRes.response.result.get("template"));
					}
					else
					{
						log.warn("[policyTemplate:MODIFY] driver response missing field template, save template in user request to database");
						item.template = MapUtils.MapToString((Map<?,?>)args.get("template"));
					}
					
					//只有在修改成功的情况下，才更新数据库
					if(!this.setPolicyTemplateItem(item))
					{
						log.error("[policyTemplate:MODIFY] update data failed for " + webprotect_key);
						reqAndRes.response.errorCode = ErrorCode.DATABASE_UPDATE_DATA_FAILED;
						reqAndRes.response.errorString = "update policy template in database failed";
						
						log.debug("[policyTemplate:MODIFY] trying to recover previous policy template on device");
						SecurityFunctionRequestAndResponse newReqAndRes = (SecurityFunctionRequestAndResponse)reqAndRes.clone();
						if(newReqAndRes != null)
						{
							log.debug("[policyTemplate:MODIFY] clone reqAndRes success for recovering policy template");
							newReqAndRes.request.args.remove("template");
							newReqAndRes.request.args.put("template", original_template);
							newReqAndRes.response.errorCode = dev.callSecurityFunction(newReqAndRes);
							
							if(newReqAndRes.response.errorCode != ErrorCode.SUCCESS)
							{
								log.error("[policyTemplate:MODIFY] call device to recover policy template failed, errorCode: {}, errorString: {}", 
										newReqAndRes.response.errorCode.toString(), newReqAndRes.response.errorString);
							}
							else
							{
								log.debug("[policyTemplate:MODIFY] call device to recover policy template success");
							}
						}
						else
						{
							log.error("[policyTemplate:MODIFY] clone reqAndRes failed for recovering policy template");
						}
						
						break;
					}
				}
				else
				{
					log.error("[policyTemplate:MODIFY] failed for " + webprotect_key + ", reason: " + reqAndRes.response.errorCode.toString());
				}
			}
			break;
		case DELETE:
			{
				PolicyTemplateItem item = this.getPolicyTemplateItem(reqAndRes);
				if(item == null)
				{
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
					reqAndRes.response.errorString = "no policy template applied for the website, apply policy template first";
					break;
				}
				
				reqAndRes.request.args.put("policy_id", item.policy_id);
				reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
				
				item.error_code = reqAndRes.response.errorCode.toString();
				
				if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
				{
					log.debug("[policyTemplate:DELETE] success for " + webprotect_key);
					if(!this.deletePolicyTemplateItem(item))
					{
						log.error("[policyTemplate:DELETE] delete from database failed for " + webprotect_key);
						reqAndRes.response.errorCode = ErrorCode.DATABASE_DELETE_DATA_FAILED;
						reqAndRes.response.errorString = "delete policy template from database failed";
						break;
					}
				}
				else
				{
					log.error("[policyTemplate:DELETE] failed for " + webprotect_key + ", reason: " + reqAndRes.response.errorCode.toString());
				}
			}
			break;
		}
		
		return reqAndRes.response.errorCode;
	}
	
	@SuppressWarnings("unchecked")
	ErrorCode whiteList(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes) throws Exception
	{
		String webprotect_key = this.getWebprotectKey(reqAndRes);
		
		Map<String, Object> args = reqAndRes.request.args;
		
		switch(reqAndRes.request.opType)
		{
		case CREATE:
			{
				if(!args.containsKey("white_list"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument white_list";
					break;
				}
				if(!(args.get("white_list") instanceof Map))
				{
					reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
					reqAndRes.response.errorString = "argument white_list should be an array";
					break;
				}
				
				Map<String, Object> params = (Map<String, Object>)args.get("white_list");
				if(!params.containsKey("site_id"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument site_id";
					break;
				}
				if(!(params.get("site_id") instanceof String))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "argument white_list:site_id should be a string";
					break;
				}
				
				if(!params.containsKey("src_ip"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument src_ip";
					break;
				}
				if(!(params.get("src_ip") instanceof String))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "argument white_list:src_ip should be a string";
					break;
				}
				
				if(!params.containsKey("dst_port"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument dst_port";
					break;
				}
				if(!(params.get("dst_port") instanceof String))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "argument white_list:dst_port should be a string";
					break;
				}
				
				if(!params.containsKey("domain"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument domain";
					break;
				}
				if(!(params.get("domain") instanceof String))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "argument white_list:domain should be a string";
					break;
				}
				
				if(!params.containsKey("uri"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument uri";
					break;
				}
				if(!(params.get("uri") instanceof String))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "argument white_list:uri should be a string";
					break;
				}
				
				if(!params.containsKey("event_type"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument event_type";
					break;
				}
				if(!(params.get("event_type") instanceof String))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "argument white_list:event_type should be a string";
					break;
				}
				
				if(!params.containsKey("policy_id"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument policy_id";
					break;
				}
				if(!(params.get("policy_id") instanceof String))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "argument white_list:policy_id should be a string";
					break;
				}
				
				if(!params.containsKey("rule_id"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing argument rule_id";
					break;
				}
				if(!(params.get("rule_id") instanceof String))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "argument white_list:rule_id should be a string";
					break;
				}
				
				reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
				
				if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
				{
					log.debug("[whiteList:CREATE] success for " + webprotect_key);
					if(!((Map<String, Object>)reqAndRes.response.result).containsKey("except_policy_id"))
					{
						reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
						reqAndRes.response.errorString = "device's response could not be recognized";
						log.error("[whiteList:CREATE] success for " + webprotect_key + ", but device's response could not be recognized");
					}
					else
					{
						WhiteListItem item = new WhiteListItem();
						item.webprotect_key = webprotect_key;
						item.site_id = (String)params.get("site_id");
						item.src_ip = (String)params.get("src_ip");
						item.dst_port = (String)params.get("dst_port");
						item.domain = (String)params.get("domain");
						item.uri = (String)params.get("uri");
						item.event_type = (String)params.get("event_type");
						item.policy_id = (String)params.get("policy_id");
						item.rule_id = (String)params.get("rule_id");
						item.except_policy_id = (String)((Map<String, Object>)reqAndRes.response.result).get("except_policy_id");
						item.white_list_key = item.webprotect_key + item.except_policy_id;
						
						//只有成功的情况下，才把数据存入数据库
						if(!this.setWhiteListItem(item))
						{
							log.error("[whiteList:CREATE] insert data failed for " + webprotect_key);
							reqAndRes.response.errorCode = ErrorCode.DATABASE_UPDATE_DATA_FAILED;
							reqAndRes.response.errorString = "insert white list item to database failed";
							
							log.debug("[whiteList:CREATE] delete white list item on device and database");
							
							log.debug("[whiteList:CREATE] trying to delete white list item on device");
							SecurityFunctionRequestAndResponse newReqAndRes = (SecurityFunctionRequestAndResponse)reqAndRes.clone();
							if(newReqAndRes != null)
							{
								log.debug("[whiteList:CREATE] clone reqAndRes for deleting white list item success");
								newReqAndRes.request.opType = OperationType.DELETE;
								newReqAndRes.request.args.put("except_policy_id", item.except_policy_id);
								newReqAndRes.response.errorCode = dev.callSecurityFunction(newReqAndRes);
								
								if(newReqAndRes.response.errorCode == ErrorCode.SUCCESS)
								{
									log.debug("[whiteList:CREATE] delete white list item on device success");
								}
								else
								{
									log.error("[whiteList:CREATE] delete white list item on device failed, errorCode: " 
											+ newReqAndRes.response.errorCode.toString()
											+ ", errorString: "
											+ newReqAndRes.response.errorString);
								}
							}
							else
							{
								log.error("[whiteList:CREATE] clone reqAndRes for deleting white list item failed");
							}
							
							log.debug("[whiteList:CREATE] trying to delete white list item from database");
							if(!this.deleteWhiteListItem(item))
							{
								log.error("[whiteList:CREATE] delete white list item from database failed");
							}
							else
							{
								log.debug("[whiteList:CREATE] delete white list item from database success");
							}
							break;
						}
						else
						{
							log.debug("[whiteList:CREATE] insert white list item into database success");
						}
					}
				}
				else
				{
					log.error("[whiteList:CREATE] failed for " + webprotect_key + ", reason: " + reqAndRes.response.errorCode.toString());
				}
			}
			break;
		case GET:
			{
				if(args.containsKey("except_policy_id"))
				{
					WhiteListItem item = this.getWhiteListItem(reqAndRes, (String)args.get("except_policy_id"));
					if(item == null)
					{
						reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
						reqAndRes.response.errorString = "can not find the white list";
						break;
					}
					
					HashMap<String, Object> result = reqAndRes.response.result;
					result.put("except_policy_id", item.except_policy_id);
					result.put("site_id", item.site_id);
					result.put("src_ip", item.src_ip);
					result.put("dst_port", item.dst_port);
					result.put("domain", item.domain);
					result.put("uri", item.uri);
					result.put("event_type", item.event_type);
					result.put("policy_id", item.policy_id);
					result.put("rule_id", item.rule_id);
					
					reqAndRes.response.errorCode = ErrorCode.SUCCESS;
				}
				else
				{
					List<WhiteListItem> items = this.getWhiteListItems(reqAndRes);
					if(items.size() == 0)
					{
						reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
						reqAndRes.response.errorString = "can not find the white list";
						break;
					}
					
					HashMap<String, Object> result = reqAndRes.response.result;
					ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
					
					Iterator<WhiteListItem> it = items.iterator();
					while(it.hasNext())
					{
						WhiteListItem item = it.next();
						HashMap<String, String> map = new HashMap<String, String>();
						
						map.put("except_policy_id", item.except_policy_id);
						map.put("site_id", item.site_id);
						map.put("src_ip", item.src_ip);
						map.put("dst_port", item.dst_port);
						map.put("domain", item.domain);
						map.put("uri", item.uri);
						map.put("event_type", item.event_type);
						map.put("policy_id", item.policy_id);
						map.put("rule_id", item.rule_id);
						
						list.add(map);
					}
					
					result.put("white_list", list);
					reqAndRes.response.errorCode = ErrorCode.SUCCESS;
				}
			}
			break;
		case MODIFY:
			{
				reqAndRes.response.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
				reqAndRes.response.errorString = "white list can not be modified";
//				WhiteListItem item = this.getWhiteListItem(reqAndRes);
//				if(item == null)
//				{
//					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
//					break;
//				}
//				
//				if(!args.containsKey("whitelist"))
//				{
//					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
//					break;
//				}
//				if(!(args.get("whitelist") instanceof List))
//				{
//					reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
//					break;
//				}
//				
//				reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
//				
//				item.error_code = reqAndRes.response.errorCode.toString();
//				
//				if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
//				{
//					log.debug("[whiteList:MODIFY] success for " + webprotect_key);
//					if(!((Map<String, Object>)reqAndRes.response.result).containsKey("policy_id"))
//					{
//						reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
//						log.error("[whiteList:MODIFY] success for " + webprotect_key + ", but device's response could not be recognized");
//					}
//					else
//					{
//						item.policy_id = (String)((Map<String, Object>)reqAndRes.response.result).get("policy_id");
//					}
//					
//					item.white_list = MapUtils.ListToString((List<?>)args.get("whitelist"));
//					
//					if(!this.setWhiteListItem(item))
//					{
//						log.error("[whiteList:MODIFY] update data failed for " + webprotect_key);
//						reqAndRes.response.errorCode = ErrorCode.DATABASE_UPDATE_DATA_FAILED;
//						break;
//					}
//				}
//				else
//				{
//					log.error("[whiteList:MODIFY] failed for " + webprotect_key + ", reason: " + reqAndRes.response.errorCode.toString());
//				}
			}
			break;
		case DELETE:
			{
				if(!args.containsKey("except_policy_id"))
				{
					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
					reqAndRes.response.errorString = "missing field except_policy_id";
					break;
				}
				
				WhiteListItem item = this.getWhiteListItem(reqAndRes, (String)args.get("except_policy_id"));
				if(item == null)
				{
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
					reqAndRes.response.errorString = "can not find the white list";
					break;
				}
				
				reqAndRes.request.args.put("except_policy_id", item.except_policy_id);
				reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
				
				if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
				{
					log.debug("[whiteList:DELETE] success for " + webprotect_key);
					if(!this.deleteWhiteListItem(item))
					{
						log.error("[whiteList:DELETE] delete from database failed for " + webprotect_key);
						reqAndRes.response.errorCode = ErrorCode.DATABASE_DELETE_DATA_FAILED;
						reqAndRes.response.errorString = "delete white list from database failed";
						break;
					}
				}
				else
				{
					log.error("[whiteList:DELETE] failed for " + webprotect_key + ", reason: " + reqAndRes.response.errorCode.toString());
				}
			}
			break;
		}
		
		return reqAndRes.response.errorCode;
	}
	
	ErrorCode deviceCommand(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes) throws Exception
	{
		//调用实际的设备接口
		return ErrorCode.SUCCESS;
	}
	
	ErrorCode reverseProxy(SecurityDevice dev, SecurityFunctionRequestAndResponse reqAndRes) throws Exception
	{
		String webprotect_key = this.getWebprotectKey(reqAndRes);
		
		switch(reqAndRes.request.opType)
		{
		case CREATE:
			{
				ReverseProxyItem item = this.getReverseProxyItem(reqAndRes);
				if(item != null && item.error_code.equalsIgnoreCase(ErrorCode.SUCCESS.toString()))
				{
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_ALREADY_EXIST;
					reqAndRes.response.errorString = "the website has already been protected";
					break;
				}
				
				item = new ReverseProxyItem();
				item.webprotect_key = webprotect_key;
				item.error_code = ErrorCode.PROCESS_IN_PROGRESS.toString();
				item.error_string = "";
				
				if(!this.setReverseProxyItem(item))
				{
					log.error("[reverseProxy:CREATE] insert data failed for " + webprotect_key);
					reqAndRes.response.errorCode = ErrorCode.DATABASE_UPDATE_DATA_FAILED;
					reqAndRes.response.errorString = "insert reverse proxy item to database failed";
					break;
				}
				
				//从数据库再取出来一次，这次对象里就有数据库自动生成的ID了，然后拿ID确定反向代理的端口号
				item = this.getReverseProxyItem(reqAndRes);
				if(item == null)
				{
					log.error("[reverseProxy:CREATE] get reverse proxy item from database failed for " + webprotect_key);
					reqAndRes.response.errorCode = ErrorCode.DATABASE_UPDATE_DATA_FAILED;
					reqAndRes.response.errorString = "get reverse proxy item from database faield";
					break;
				}
				
				//id is used to determine the proxy port
				reqAndRes.request.args.put("id", item.id);
				
				//Call specific device driver
				reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
				
				item.error_code = reqAndRes.response.errorCode.toString();
				
				if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
				{
					//设备驱动操作成功时，记一条日志
					log.debug("[reverseProxy:CREATE] success for " + webprotect_key + ", now checking result and save it to database");
					HashMap<String, Object>result = reqAndRes.response.result;
					//检查返回值是否合法
					if(result.containsKey("proxy_ip") && result.containsKey("proxy_port") && result.containsKey("proxy_protocol"))
					{
						item.proxy_ip = (String)result.get("proxy_ip");
						item.proxy_port = (int)result.get("proxy_port");
						item.proxy_protocol = (String)result.get("proxy_protocol");
						item.policy_id = (String)result.get("policy_id");
						
						//将反向代理的结果存入数据库
						if(this.setReverseProxyItem(item))
						{
							//创建反向代理成功，记日志
							log.debug("[reverseProxy:CREATE] success for " + webprotect_key);
						}
						else
						{
							//存反向代理时失败，记日志
							log.error("[reverseProxy:CREATE] insert reverse proxy item into database failed for " + webprotect_key);
							reqAndRes.response.errorCode = ErrorCode.DATABASE_UPDATE_DATA_FAILED;
							reqAndRes.response.errorString = "insert reverse proxy item into database failed";
						}
					}
					else
					{
						//设备驱动的返回值不合法，记日志 
						log.error("[reverseProxy:CREATE] success for " + webprotect_key + ", but device's response could not be recognized");
						reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
						reqAndRes.response.errorString = "create reverse proxy success, but device's response could not be recognized";
					}
				}
				
				//判断上面的处理结果，如果成功，就下发策略模板；如果失败，就把生成的所有数据都删掉
				if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
				{
					log.debug("[reverseProxy:CREATE] try to apply default policy template to " + webprotect_key);
					SecurityFunctionRequestAndResponse newReqAndRes = (SecurityFunctionRequestAndResponse)reqAndRes.clone();
					if(newReqAndRes == null)
					{
						log.error("[reverseProxy:CREATE] clone reqAndRes for applying default policy template failed");
						reqAndRes.response.errorCode = ErrorCode.CLONE_FAILED;
						reqAndRes.response.errorString = "clone reqAndRes failed";
						break;
					}
					else
					{
						log.debug("[[reverseProxy:CREATE] clone reqAndRes success");
						newReqAndRes.request.secFuncType = "WEB_PROTECT_POLICY_TEMPLATE";
						newReqAndRes.request.args.put("template", WebProtectSecurityFunction.defaultPolicyTemplate);
						newReqAndRes.response.errorCode = this.callSecurityFunction(newReqAndRes);
						
						if(newReqAndRes.response.errorCode != ErrorCode.SUCCESS)
						{
							log.error("[reverseProxy:CREATE] apply default policy template failed, errorCode: " + newReqAndRes.response.errorCode + ", errorString: " + newReqAndRes.response.errorString);
						}
						else
						{
							if(newReqAndRes.response.result.containsKey("template"))
							{
								reqAndRes.response.result.put("template", newReqAndRes.response.result.get("template"));
							}
							log.debug("[reverseProxy:CREATE] apply default policy template success");
						}
					}
				}
				else
				{
					//走到这里，已经是程序运行出错了，所以这个块里的所有错误状态不会记录到reqAndRes.response.errorCode里，只是单纯的记日志
					log.debug("[reverseProxy:CREATE] try to delete website on device");
					SecurityFunctionRequestAndResponse newReqAndRes = (SecurityFunctionRequestAndResponse)reqAndRes.clone();
					if(newReqAndRes == null)
					{
						log.error("[reverseProxy:CREATE] clone reqAndRes for deleting website failed");
						break;
					}
					else
					{
						log.debug("[reverseProxy:CREATE] clone reqAndRes success");
						newReqAndRes.request.opType = OperationType.DELETE;
						
						//这里得调用设备驱动的callSecurityFunction，因为这里只是尝试
						newReqAndRes.response.errorCode = dev.callSecurityFunction(newReqAndRes);
						if(newReqAndRes.response.errorCode == ErrorCode.SUCCESS)
						{
							log.debug("[reverseProxy:CREATE] delete website success for " + webprotect_key);
						}
						else
						{
							log.error("[reverseProxy:CREATE] delete website failed for " + webprotect_key);
						}
						
						//下面将尝试把之前可能写入数据库的数据删掉
						log.debug("[reverseProxy:CREATE] trying to delete reverse proxy item in database");
						if(this.deleteReverseProxyItem(item))
						{
							log.debug("[reverseProxy:CREATE] delete reverse proxy item success");
						}
						else
						{
							log.error("[reverseProxy:CREATE] delete reverse pxory item failed");
						}
						
						log.debug("[reverseProxy:CREATE] trying to delete webprotect item in database");
						WebprotectItem webprotectItem = this.getWebprotectItem(reqAndRes);
						if(webprotectItem == null)
						{
							log.error("[reverseProxy:CREATE] can not find webprotect item " + webprotect_key + " in database");
						}
						else
						{
							if(this.deleteWebprotectItem(webprotectItem))
							{
								log.debug("[reverseProxy:CREATE] delete webprotect item success");
							}
							else
							{
								log.error("[reverseProxy:CREATE] delete webprotect item failed");
							}
						}
						
						log.debug("[reverseProxy:CREATE] free device {} to devAllocator", dev.toString());
						this.devAllocator.free(dev);
					}
				}
			}
			break;
		case GET:
			{
				ReverseProxyItem item = this.getReverseProxyItem(reqAndRes);
				if(item == null)
				{
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
					reqAndRes.response.errorString = "can not find the reverse proxy";
					break;
				}
				
				reqAndRes.response.result.put("proxy_protocol", item.proxy_protocol);
				reqAndRes.response.result.put("proxy_ip", item.proxy_ip);
				reqAndRes.response.result.put("proxy_port", item.proxy_port);
				reqAndRes.response.result.put("error_code", item.error_code);
				
				reqAndRes.response.errorCode = ErrorCode.SUCCESS;
			}
			break;
		case MODIFY:
			{
				//反向代理不支持修改操作
				reqAndRes.response.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
				reqAndRes.response.errorString = "Reverse Proxy can not be modified. Please DELETE it OR CREATE a new one instead.";
				
				log.error("[reverseProxy:MODIFY] Operation is not supported");
				
//				ReverseProxyItem item = this.getReverseProxyItem(reqAndRes);
//				if(item == null)
//				{
//					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
//					break;
//				}
//				
//				if(!args.containsKey("domain") || !args.containsKey("protocol") || !args.containsKey("ip") || !args.containsKey("port"))
//				{
//					reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
//					break;
//				}
//				
//				reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
//				
//				item.error_code = reqAndRes.response.errorCode.toString();
//				
//				if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
//				{
//					log.debug("[reverseProxy:MODIFY] success for " + webprotect_key);
//					HashMap<String, Object> result = (HashMap<String, Object>)reqAndRes.response.result;
//					if(!result.containsKey("proxy_ip") || !result.containsKey("proxy_port") || !result.containsKey("proxy_protocol") || !result.containsKey("policy_id"))
//					{
//						reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
//						log.error("[reverseProxy:MODIFY] success for " + webprotect_key + ", but device's response could not be recognized");
//					}
//					else
//					{
//						item.proxy_ip = (String)result.get("proxy_ip");
//						item.proxy_port = (int)result.get("proxy_port");
//						item.proxy_protocol = (String)result.get("proxy_protocol");
//						item.policy_id = (String)result.get("policy_id");
//					}
//					
//					//只有在修改成功的情况下，才更新数据库
//					if(!this.setReverseProxyItem(item))
//					{
//						log.error("[reverseProxy:MODIFY] update data failed for " + webprotect_key);
//						reqAndRes.response.errorCode = ErrorCode.DATABASE_UPDATE_DATA_FAILED;
//						break;
//					}
//				}
//				else
//				{
//					log.error("[reverseProxy:MODIFY] failed for " + webprotect_key + ", reason: " + reqAndRes.response.errorCode.toString());
//				}
			}
			break;
		case DELETE:
			{
				ReverseProxyItem item = this.getReverseProxyItem(reqAndRes);
				
				if(item != null)
				{
					reqAndRes.request.args.put("policy_id", item.policy_id);
					reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
					
					item.error_code = reqAndRes.response.errorCode.toString();
					
					if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
					{
						log.debug("[reverseProxy:DELETE] success for " + webprotect_key);
					}
					else
					{
						log.error("[reverseProxy:DELETE] failed on device for " + webprotect_key + ", reason: " + reqAndRes.response.errorCode.toString());
					}
					
					log.debug("[reverseProxy:DELETE] delete information for " + webprotect_key + " in database anyway");
					log.debug("[reverseProxy:DELETE] removing reverse proxy infomation from database");
					if(!this.deleteReverseProxyItem(item))
					{
						log.error("[reverseProxy:DELETE] remove reverse proxy infomation from database failed");
						reqAndRes.response.errorCode = ErrorCode.DATABASE_DELETE_DATA_FAILED;
						reqAndRes.response.errorString = "remove reverse proxy information from database failed";
					}
					else
					{
						log.debug("[reverseProxy:DELETE] remove reverse proxy success");
					}
				}
				else
				{
					log.debug("[reverseProxy:DELETE] reverse proxy item does not exist");
					reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
					reqAndRes.response.errorString = "can not find the reverse proxy";
				}
				
				//不管上面的操作是否成功，都把数据库里的相关项删掉
				PolicyTemplateItem policyTemplateItem = this.getPolicyTemplateItem(reqAndRes);
				if(policyTemplateItem != null)
				{
					log.debug("[reverseProxy:DELETE] removing policy template information from database");
					if(!this.deletePolicyTemplateItem(policyTemplateItem))
					{
						log.error("[reverseProxy:DELETE] remove policy template information from database failed");
						reqAndRes.response.errorCode = ErrorCode.DATABASE_DELETE_DATA_FAILED;
						reqAndRes.response.errorString = "remove policy template information from database failed";
					}
					else
					{
						log.debug("[reverseProxy:DELETE] remove policy template success");
					}
				}
				
				List<WhiteListItem> whiteListItems = this.getWhiteListItems(reqAndRes);
				if(whiteListItems.size() != 0)
				{
					log.debug("[reverseProxy:DELETE] removing white list information from database");
					
					//delete a dozen of items, so the return code is not concerned
					this.deleteWhiteListItems(whiteListItems);
					log.debug("[reverseProxy:DELETE] remove white list success");
				}
				
				WebprotectItem webprotectItem = this.getWebprotectItem(reqAndRes);
				if(webprotectItem != null)
				{
					log.debug("[reverseProxy:DELETE] removing webprotect information from database");
					if(!this.deleteWebprotectItem(webprotectItem))
					{
						log.error("[reverseProxy:DELETE] remove webprotect information from database failed");
						reqAndRes.response.errorCode = ErrorCode.DATABASE_UPDATE_DATA_FAILED;
						reqAndRes.response.errorString = "remove webprotect information from database failed";
					}
					else
					{
						log.debug("[reverseProxy:DELETE] remove webprotect success");
					}
				}
				
				log.debug("[reverseProxy:DELETE] free device {} to devAllocator", dev.toString());
				this.devAllocator.free(dev);
			}
			break;
		}
		
		return reqAndRes.response.errorCode;
	}
	
	class WebprotectItem implements IDBObject
	{
		private static final long serialVersionUID = 7890120798940216360L;
		public String webprotect_key = null;
		public String device_id = null;
		public String app_id = null;
		public String tenant_id = null;
		public String website_domain = null;
		public String website_protocol = null;
		public String website_ip = null;
		public int website_port = 0;
		
		public WebprotectItem()
		{
		}
		
		public WebprotectItem(String webprotect_key, String device_id, String app_id, String tenant_id, 
				String website_domain, String website_protocol, String website_ip, int website_port)
		{
			this.webprotect_key = webprotect_key;
			this.device_id = device_id;
			this.app_id = app_id;
			this.tenant_id = tenant_id;
			this.website_domain = website_domain;
			this.website_protocol = website_protocol;
			this.website_ip = website_ip;
			this.website_port = website_port;
		}
		
		@Override
		public Map<String, Object> getDBElements() {
			Map<String, Object> map = null;
			
			map = new HashMap<String, Object>();
			map.put("webprotect_key", this.webprotect_key);
			map.put("device_id", this.device_id);
			map.put("app_id", this.app_id);
			map.put("tenant_id", this.tenant_id);
			map.put("website_domain", this.website_domain);
			map.put("website_protocol", this.website_protocol);
			map.put("website_ip", this.website_ip);
			map.put("website_port", this.website_port);
			
			return map;
		}

		@Override
		public Object getFieldValueByKey(String key) {
			return null;
		}

		@Override
		public IDBObject mapRow(IAbstractResultSet resultSet) {
			return new WebprotectItem(
					resultSet.getString("webprotect_key"), 
					resultSet.getString("device_id"), 
					resultSet.getString("app_id"), 
					resultSet.getString("tenant_id"), 
					resultSet.getString("website_domain"), 
					resultSet.getString("website_protocol"), 
					resultSet.getString("website_ip"), 
					resultSet.getInt("website_port"));
		}
	}
	
	class PolicyTemplateItem implements IDBObject
	{
		private static final long serialVersionUID = 1933203376721075769L;
		public String webprotect_key = null;
		public String policy_id = null;
		public String template = null;
		public String error_code = null;
		public String error_string = null;
		
		public PolicyTemplateItem()
		{
		}
		
		public PolicyTemplateItem(String webprotect_key, String policy_id, String template, String error_code, String error_string)
		{
			this.webprotect_key = webprotect_key;
			this.policy_id = policy_id;
			this.template = template;
			this.error_code = error_code;
			this.error_string = error_string;
		}
		
		@Override
		public Map<String, Object> getDBElements() {
			Map<String, Object> map = null;
			
			map = new HashMap<String, Object>();
			map.put("webprotect_key", this.webprotect_key);
			map.put("policy_id", this.policy_id);
			map.put("template", this.template);
			map.put("error_code", this.error_code);
			map.put("error_string", this.error_string);
			
			return map;
		}

		@Override
		public Object getFieldValueByKey(String key) {
			return null;
		}

		@Override
		public IDBObject mapRow(IAbstractResultSet resultSet) {
			return new PolicyTemplateItem(
					resultSet.getString("webprotect_key"), 
					resultSet.getString("policy_id"), 
					resultSet.getString("template"), 
					resultSet.getString("error_code"), 
					resultSet.getString("error_string"));
		}
	}
	
	class ReverseProxyItem implements IDBObject
	{
		private static final long serialVersionUID = 3361446285618117569L;
		public int id = 0;
		public String webprotect_key = null;
		public String policy_id = null;
		public String proxy_protocol = null;
		public String proxy_ip = null;
		public int proxy_port = 0;
		public String error_code = null;
		public String error_string = null;
		
		public ReverseProxyItem()
		{
		}
		
		public ReverseProxyItem(int id, String webprotect_key, String policy_id, String proxy_protocol, String proxy_ip, 
				int proxy_port, String error_code, String error_string)
		{
			this.id = id;
			this.webprotect_key = webprotect_key;
			this.policy_id = policy_id;
			this.proxy_protocol = proxy_protocol;
			this.proxy_ip = proxy_ip;
			this.proxy_port = proxy_port;
			this.error_code = error_code;
			this.error_string = error_string;
		}
		
		@Override
		public Map<String, Object> getDBElements() {
			Map<String, Object> map = null;
			
			map = new HashMap<String, Object>();
			map.put("webprotect_key", this.webprotect_key);
			map.put("policy_id", this.policy_id);
			map.put("proxy_protocol", this.proxy_protocol);
			map.put("proxy_ip", this.proxy_ip);
			map.put("proxy_port", this.proxy_port);
			map.put("error_code", this.error_code);
			map.put("error_string", this.error_string);
			
			return map;
		}

		@Override
		public Object getFieldValueByKey(String key) {
			return null;
		}

		@Override
		public IDBObject mapRow(IAbstractResultSet resultSet) {
			return new ReverseProxyItem(
					resultSet.getInt("id"), 
					resultSet.getString("webprotect_key"), 
					resultSet.getString("policy_id"), 
					resultSet.getString("proxy_protocol"), 
					resultSet.getString("proxy_ip"), 
					resultSet.getInt("proxy_port"), 
					resultSet.getString("error_code"), 
					resultSet.getString("error_string"));
		}
	}
	
	class WhiteListItem implements IDBObject
	{
		private static final long serialVersionUID = -3926332917677108750L;
		public String white_list_key = null;   //white_list_key = webprotect_key + except_policy_id
		public String webprotect_key = null;
		public String except_policy_id = null;
		public String site_id = null;
		public String src_ip = null;
		public String dst_port = null;
		public String domain = null;
		public String uri = null;
		public String event_type = null;
		public String policy_id = null;
		public String rule_id = null;
		
		public WhiteListItem()
		{
		}
		
		public WhiteListItem(String white_list_key, String webprotect_key, String except_policy_id, String site_id, String src_ip, 
				String dst_port, String domain, String uri, String event_type, String policy_id, String rule_id)
		{
			this.white_list_key = white_list_key;
			this.webprotect_key = webprotect_key;
			this.except_policy_id = except_policy_id;
			this.site_id = site_id;
			this.src_ip = src_ip;
			this.dst_port = dst_port;
			this.domain = domain;
			this.uri = uri;
			this.event_type = event_type;
			this.policy_id = policy_id;
			this.rule_id = rule_id;
		}

		@Override
		public Map<String, Object> getDBElements() {
			Map<String, Object> map = null;
			
			map = new HashMap<String, Object>();
			map.put("white_list_key", this.white_list_key);
			map.put("webprotect_key", this.webprotect_key);
			map.put("except_policy_id", this.except_policy_id);
			map.put("site_id", this.site_id);
			map.put("src_ip", this.src_ip);
			map.put("dst_port", this.dst_port);
			map.put("domain", this.domain);
			map.put("uri", Base64Utils.base64Encode(this.uri));
			map.put("event_type", this.event_type);
			map.put("policy_id", this.policy_id);
			map.put("rule_id", this.rule_id);
			
			return map;
		}

		@Override
		public Object getFieldValueByKey(String key) {
			return null;
		}

		@Override
		public IDBObject mapRow(IAbstractResultSet resultSet) {
			return new WhiteListItem(
					resultSet.getString("white_list_key"), 
					resultSet.getString("webprotect_key"), 
					resultSet.getString("except_policy_id"), 
					resultSet.getString("site_id"), 
					resultSet.getString("src_ip"), 
					resultSet.getString("dst_port"), 
					resultSet.getString("domain"), 
					Base64Utils.base64Decode(resultSet.getString("uri")), 
					resultSet.getString("event_type"), 
					resultSet.getString("policy_id"), 
					resultSet.getString("rule_id"));
		}
	}
}
