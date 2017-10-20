package com.sds.securitycontroller.securityfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionRequestAndResponse;
import com.sds.securitycontroller.securityfunction.manager.SecurityFunctionManager.SecurityFunctionInitializeContext;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.utils.MapUtils;

public class WebScanSecurityFunction extends SecurityFunction {
	private static Logger log = LoggerFactory.getLogger(WebScanSecurityFunction.class);

	public WebScanSecurityFunction()
	{
		super();
	}
	
	@Override
	public ErrorCode initialize(SecurityFunctionInitializeContext secFuncInitCtx)
			throws Exception {
		ErrorCode ret = ErrorCode.SUCCESS;
		
		do
		{
			ret = super.initialize(secFuncInitCtx);
			if(ret != ErrorCode.SUCCESS)
			{
				break;
			}
			
			this.essentialSecFuncList.add("WEB_SCAN_TASK");
			
			String tableName = "t_secfunc_webscan_task";
			Map<String, String> tableColumns = new HashMap<String, String>() {
				private static final long serialVersionUID = 1232L;

				{
					put("id", "INTEGER NOT NULL");
					put("task_id", "VARCHAR(11)");
					put("app_id", "VARCHAR(16)");
					put("tenant_id", "VARCHAR(16)");
					put("device_id", "VARCHAR(32)");
					put("protocol", "VARCHAR(8)");
					put("host", "VARCHAR(64)");
					put("port", "int(11)");
					put("scan_type", "VARCHAR(16)");
					put("scan_type_parameter", "VARCHAR(256)");
					put("error_code", "VARCHAR(32)");
					put("error_string", "VARCHAR(128)");
					put("PRIMARY_KEY", "id");
					put("EXTRA", "ENGINE=InnoDB DEFAULT CHARSET=utf8");
				}
			};
			storageSource.createTable(tableName, tableColumns);
			
			
			this.storageSource.setTablePrimaryKeyName(this.getTableName("task"), "id");
			
			this.securityFunctionName = "webscan";
		}while(false);
		
		return ErrorCode.SUCCESS;
	}

	@Override
	public ErrorCode callSecurityFunction(
			SecurityFunctionRequestAndResponse reqAndRes) throws Exception {
		switch(reqAndRes.request.secFuncType)
		{
		case "WEB_SCAN_TASK":
			return this.task(reqAndRes);
		}
		return reqAndRes.response.errorCode;
	}
	
	private ErrorCode task(SecurityFunctionRequestAndResponse reqAndRes) throws Exception
	{
		do
		{
			SecurityDevice dev = null;
			TaskItem item = null;
			
			switch(reqAndRes.request.opType)
			{
			case MODIFY:
				reqAndRes.response.errorCode = ErrorCode.OPERATION_NOT_SUPPORTED;
				reqAndRes.response.errorString = "can not modify this resource";
				break;
			case CREATE:
				{
					//CREATE时，需要新生成一个TaskItem，并且新分配一个设备
					item = new TaskItem();
					
					//检查参数
					if(item.fill(reqAndRes) != ErrorCode.SUCCESS)
					{
						break;
					}
					
					dev = this.getNewDevice(reqAndRes, reqAndRes.request.secFuncType);
					if(dev == null)
					{
						log.error("[WEB_SCAN_TASK] Allocate device failed for " + item.toString());
						
						reqAndRes.response.errorCode = ErrorCode.CANNOT_FIND_DEVICE_INSUFFICIENT_RESOURCE;
						reqAndRes.response.errorString = "can not allocate device for this request";
						break;
					}
					else
					{
						item.device_id = dev.device_id;
						log.debug("[WEB_SCAN_TASK] Allocate device " + dev.device_id + " success for " + item.toString());
					}
					
					reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
					
					if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
					{
						HashMap<String, Object> result = reqAndRes.response.result;
						if(!result.containsKey("task_id"))
						{
							reqAndRes.response.errorCode = ErrorCode.DEVICE_RESPONSE_INVALID;
							reqAndRes.response.errorString = "device's response can not be recognized";
							break;
						}
						item.task_id = String.valueOf(result.get("task_id"));
						if(!this.setTaskItem(item))
						{
							reqAndRes.response.errorCode = ErrorCode.DATABASE_UPDATE_DATA_FAILED;
							reqAndRes.response.errorString = "can not insert entry to database";
							break;
						}
						result.put("id", item.id);
						result.put("device_id", dev.device_id);
						
						/**
						 * "result" : {
						 * 		"task_id" : "xxxxx",      真实任务ID
						 * 		"id" : "kfewk",           SC分配的ID
						 * 		"device_id" : "fewkewf"   设备ID
						 * 	}
						 */
					}
				}
				break;
			case GET:
			case DELETE:
				{
					//不是CREATE时，需要从数据库获取任务的相关信息
					if(!reqAndRes.request.args.containsKey("id"))
					{
						reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
						reqAndRes.response.errorString = "missing field id";
						break;
					}
					
					int id = -1;
					try
					{
						id = Integer.parseInt((String)reqAndRes.request.args.get("id"));
					}
					catch(Exception e)
					{
						e.printStackTrace();
						reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
						reqAndRes.response.errorString = "field id is invalid, " + e.getMessage();
						break;
					}
					String app_id = reqAndRes.request.appID;
					String tenant_id = reqAndRes.request.tenantID;
					
					item = this.getTaskItem(id);
					if(item == null)
					{
						reqAndRes.response.errorCode = ErrorCode.RESOURCE_DOES_NOT_EXIST;
						reqAndRes.response.errorString = "can not find this task";
						break;
					}
					
					if(!item.app_id.equalsIgnoreCase(app_id))
					{
						reqAndRes.response.errorCode = ErrorCode.PERMISSION_DENIED;
						reqAndRes.response.errorString = "permission denied, app_id does not match";
						break;
					}
					
					if(!item.tenant_id.equalsIgnoreCase(tenant_id))
					{
						reqAndRes.response.errorCode = ErrorCode.PERMISSION_DENIED;
						reqAndRes.response.errorString = "permission denied, tenant_id does not match";
						break;
					}
					
					//dev = this.getSecurityDevice(item.device_id);
					dev = this.devAllocator.findSecurityDevice(item.device_id);
					if(dev == null)
					{
						log.error("[WEB_SCAN_TASK:GET] Get device " + item.device_id + " from cache failed for " + item.toString());
						reqAndRes.response.errorCode = ErrorCode.DEVICE_DOES_NOT_EXIST;
						reqAndRes.response.errorString = "can not find the corresponding device";
					}
					else
					{
						log.debug("[WEB_SCAN_TASK:GET] Get device " + item.device_id + " from cache success for " + item.toString());
					}
					
					if(item.error_code != ErrorCode.SUCCESS)
					{
						reqAndRes.response.errorCode = item.error_code;
						reqAndRes.response.errorString = item.error_string;
						break;
					}
					
					reqAndRes.request.args.put("task_id", item.task_id);

					reqAndRes.response.errorCode = dev.callSecurityFunction(reqAndRes);
					if(reqAndRes.request.opType == OperationType.DELETE)
					{//DELETE database entry
						if(reqAndRes.response.errorCode == ErrorCode.SUCCESS)
						{
							if(!this.deleteTaskItem(item))
							{
								reqAndRes.response.errorCode = ErrorCode.DATABASE_DELETE_DATA_FAILED;
								reqAndRes.response.errorString = "delete from database failed";
								break;
							}
						}
					}
				}
				break;
			}
		}while(false);
		
		return reqAndRes.response.errorCode;
	}
	
	public SecurityDevice getSecurityDeviceFromDeviceManagerByDeviceID(String deviceID) throws Exception
	{
		SecurityDevice dev = null;
		dev = new SecurityDevice();
		dev.base_url = "/4sdnapi";
		dev.device_id = "87312981238973127";
		dev.ip = "10.65.200.206";
		dev.port = 9999;
		dev.protocol = "http";
		dev.rest_version = "WVSS-4sdnapi-0.0.0.1";
		
		if(this.probe(dev) != ErrorCode.SUCCESS)
		{
			//设备没有被识别，把设备还给设备管理器，把dev置为null，然后准备再次获取设备
			//free dev to DeviceManager
			dev = null;
		}
		
		return dev;
	}
	
	private SecurityDevice getNewDevice(SecurityFunctionRequestAndResponse reqAndRes, String secFuncType) throws Exception
	{
		SecurityDevice dev = null;
		
		dev = this.devAllocator.allocate(SecurityDeviceType.SD_WVSS, 0, null, null, null);
		
		if(dev != null)
		{
			log.debug("[getSecurityDeviceFromDeviceManagerBySecFuncType] get device success");
			
			if(this.probe(dev) != ErrorCode.SUCCESS)
			{
				log.error("[getSecurityDeviceFromDeviceManagerBySecFuncType] Probe device failed");
				dev = null;
			}
		}
		
		return dev;
	}
	
	private boolean setTaskItem(TaskItem item)
	{
		int retry = 3;
		do{
			long now = System.currentTimeMillis();
			item.id = (int)now;
			int n = this.storageSource.insertEntity(this.getTableName("task"), item);
		if(n>0)
			return true;
		}while(retry>=0);
		return false;
	}
	
	private TaskItem getTaskItem(int id)
	{
		return (TaskItem)this.storageSource.getEntity(this.getTableName("task"), id, TaskItem.class);
	}
	
	private boolean deleteTaskItem(TaskItem item)
	{
		if(this.storageSource.deleteEntity(this.getTableName("task"), item.id) <= 0)
		{
			return false;
		}
		return true;
	}
	
	private String getTableName(String type)
	{
		switch(type)
		{
		case "task":
			return "t_secfunc_webscan_task";
		}
		
		log.warn("[getTableName] " + type);
		
		return null;
	}

	@Override
	public ErrorCode getSupportedSecurityDeviceType(
			ArrayList<SecurityDeviceType> secDevTypeList) throws Exception {
		secDevTypeList.add(SecurityDeviceType.SD_WVSS);
		return ErrorCode.SUCCESS;
	}

	class TaskItem  implements IDBObject
	{
		private static final long serialVersionUID = 4392618884052277838L;
		public int id = -1;
		public String task_id = null;
		public String app_id = null;
		public String tenant_id = null;
		public String device_id = null;
		public String protocol = null;
		public String host = null;
		public int port = 0;
		public String scan_type = null;
		public String scan_type_parameter = null;
		public ErrorCode error_code = null;
		public String error_string = null;
		
		public Map<String, Object> scan_type_parameter_map = null;
		
		public TaskItem()
		{
		}
		
		public TaskItem(int id, String task_id, String app_id, String tenant_id, String device_id, 
				String protocol, String host, int port, String scan_type, String scan_type_parameter, 
				ErrorCode error_code, String error_string)
		{
			this.id = id;
			this.task_id = task_id;
			this.app_id = app_id;
			this.tenant_id = tenant_id;
			this.device_id = device_id;
			this.protocol = protocol;
			this.host = host;
			this.port = port;
			this.scan_type = scan_type;
			this.scan_type_parameter = scan_type_parameter;
			this.error_code = error_code;
			this.error_string = error_string;
		}
		
		@Override
		public String toString()
		{
			return this.app_id + this.tenant_id + this.protocol + this.host + this.port + this.scan_type;
		}
		
		@SuppressWarnings("unchecked")
		public ErrorCode fill(SecurityFunctionRequestAndResponse reqAndRes)
		{
			do
			{
				this.app_id = reqAndRes.request.appID;
				this.tenant_id = reqAndRes.request.tenantID;
				
				Map<String, Object> args = reqAndRes.request.args;
				
				if(!args.containsKey("target"))
				{
					//没有包含target的请求，那必须得包含id
					if(!args.containsKey("id"))
					{
						//既不包含target，也不包含id，说明参数有问题
						reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
						reqAndRes.response.errorString = "missing field target or id";
						break;
					}
					else
					{
						try
						{
							this.id = Integer.parseInt((String)reqAndRes.request.args.get("id"));
						}
						catch(Exception e)
						{
							reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
							reqAndRes.response.errorString = "invaid field id, " + e.getMessage();
							break;
						}
					}
				}
				else
				{
					if(!args.containsKey("type"))
					{
						reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
						reqAndRes.response.errorString = "missing field type";
						break;
					}
					
					Map<String, Object> target = (Map<String, Object>)args.get("target");
					if(!(target instanceof Map))
					{
						reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
						reqAndRes.response.errorString = "argument target should be an array";
					}
					
					if(!target.containsKey("protocol"))
					{
						reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
						reqAndRes.response.errorString = "missing field target:protocol";
						break;
					}
					if(!(target.get("protocol") instanceof String))
					{
						reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
						reqAndRes.response.errorString = "field target:protocol should be a string";
						break;
					}
					
					if(!target.containsKey("host"))
					{
						reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
						reqAndRes.response.errorString = "missing field target:host";
						break;
					}
					if(!(target.get("host") instanceof String))
					{
						reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
						reqAndRes.response.errorString = "field target:host should be a string";
						break;
					}
					
					if(!target.containsKey("port"))
					{
						reqAndRes.response.errorCode = ErrorCode.MISSING_ARGUMENT;
						reqAndRes.response.errorString = "missing field target:port";
						break;
					}
					if(!(target.get("port") instanceof Integer))
					{
						reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
						reqAndRes.response.errorString = "field target:port should be an integer";
						break;
					}
					
					this.protocol = (String)target.get("protocol");
					this.host = (String)target.get("host");
					this.port = (int)target.get("port");
					this.scan_type = (String)args.get("type");
					
					if(args.containsKey("type_parameter"))
					{
						Object parameter = args.get("type_parameter");
						if(!(parameter instanceof Map<?, ?>))
						{
							reqAndRes.response.errorCode = ErrorCode.INVALID_ARGUMENT;
							reqAndRes.response.errorString = "invalid field target:type_parameter, should be a dictionary";
							break;
						}
						this.scan_type_parameter_map = (Map<String, Object>)parameter;
						this.scan_type_parameter = MapUtils.MapToString(this.scan_type_parameter_map);
					}
				}
			}while(false);
			
			return reqAndRes.response.errorCode;
		}

		@Override
		public Map<String, Object> getDBElements() {
			Map<String, Object> map = null;
			
			map = new HashMap<String, Object>();
			map.put("task_id", this.task_id);
			map.put("app_id", this.app_id);
			map.put("tenant_id", this.tenant_id);
			map.put("device_id", this.device_id);
			map.put("protocol", this.protocol);
			map.put("host", this.host);
			map.put("port", this.port);
			map.put("scan_type", this.scan_type);
			map.put("scan_type_parameter", this.scan_type_parameter);
			map.put("error_code", "");
			map.put("error_string", "");
			
			return map;
		}

		@Override
		public Object getFieldValueByKey(String key) {
			return null;
		}

		@Override
		public IDBObject mapRow(IAbstractResultSet resultSet) {
			return new TaskItem(resultSet.getInt("id"), 
					resultSet.getString("task_id"), 
					resultSet.getString("app_id"), 
					resultSet.getString("tenant_id"), 
					resultSet.getString("device_id"), 
					resultSet.getString("protocol"), 
					resultSet.getString("host"), 
					resultSet.getInt("port"), 
					resultSet.getString("scan_type"), 
					resultSet.getString("scan_type_parameter"), 
					ErrorCode.SUCCESS, 
					"");
		}
		
	}
}
