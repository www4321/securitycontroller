package com.sds.securitycontroller.securityfunction.securitydeviceallocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.securityfunction.ErrorCode;
import com.sds.securitycontroller.securityfunction.ISecurityFunction;
import com.sds.securitycontroller.securityfunction.manager.ISecurityFunctionManagerService;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDevice;
import com.sds.securitycontroller.securityfunction.securitydevice.SecurityDeviceType;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;
import com.sds.securitycontroller.storage.IStorageSourceService;

public class WebScanDeviceAllocator implements ISecurityDeviceAllocator
{
	private static Logger log = LoggerFactory.getLogger(WebScanDeviceAllocator.class);
	private IStorageSourceService storageSource = null;
	private String tableName = "t_secfunc_allocator_webscan";
	
	@SuppressWarnings("unused")
	private class _Device implements IDBObject
	{
		private static final long serialVersionUID = 1L;
		public SecurityDevice secDev = null;
		public int reference = 0;
		
		public _Device(){}
		
		public _Device(SecurityDevice secDev)
		{
			this.secDev = secDev;
			this.reference = 0;
		}
		
		public _Device(String protocol, 
						String ip, 
						int port, 
						String base_url, 
						String rest_version, 
						String device_id, 
						SecurityDeviceType type, 
						int reference)
		{
			this.secDev = new SecurityDevice(protocol, ip, port, base_url, rest_version, device_id, type);
			this.reference = reference;
		}
		
		@Override
		public String toString()
		{
			if(this.secDev == null)
			{
				return "NULL DEVICE";
			}
			
			return this.secDev.toString() + "[reference: " + this.reference + "]";
		}
		
		public int ref()
		{
			this.reference++;
			assert(this.reference > 0);
			
			if(storageSource.updateOrInsertEntity(tableName, this) <= 0)
			{
				log.error("[WebScanDeviceAllocator::_Device][ref] update device info in database failed");
				return -1;
			}
			
			log.debug("[WebScanDeviceAllocator::_Device] device {} reference increased to {}", this.toString(), this.reference);
			
			return this.reference;
		}
		
		public int deref()
		{
			this.reference--;
			assert(this.reference > 0);
			
			if(this.reference == 0)
			{
				if(storageSource.deleteEntity(tableName, this.secDev.device_id) <= 0)
				{
					log.error("[WebScanDeviceAllocator::_Device][deref] delete device from database failed");
				}
				else
				{
					log.debug("[WebScanDeviceAllocator::_Device][deref] device {} reference decreased to 0, delete it from database", this.toString());
				}
				
				return 0;
			}
			
			if(storageSource.updateOrInsertEntity(tableName, this) <= 0)
			{
				log.error("[WebScanDeviceAllocator::_Device][deref] update device info in database failed");
				return -1;
			}
			
			log.debug("[WebScanDeviceAllocator::_Device] device {} reference decreased to {}", this.toString(), this.reference);
			
			return this.reference;
		}

		@Override
		public Map<String, Object> getDBElements() {
			Map<String, Object> map = null;
			
			map = new HashMap<String, Object>();
			map.put("protocol", this.secDev.protocol);
			map.put("ip", this.secDev.ip);
			map.put("port", this.secDev.port);
			map.put("base_url", this.secDev.base_url);
			map.put("rest_version", this.secDev.rest_version);
			map.put("device_id", this.secDev.device_id);
			map.put("device_type", this.secDev.type.toString());
			map.put("reference", this.reference);
			
			return map;
		}

		@Override
		public Object getFieldValueByKey(String key) {
			return null;
		}

		@Override
		public IDBObject mapRow(IAbstractResultSet resultSet) {
			return new _Device(resultSet.getString("protocol"), 
					resultSet.getString("ip"), 
					resultSet.getInt("port"), 
					resultSet.getString("base_url"), 
					resultSet.getString("rest_version"), 
					resultSet.getString("device_id"), 
					SecurityDeviceType.valueOf(resultSet.getString("device_type")), 
					resultSet.getInt("reference"));
		}
	}
	
	@Override
	public ErrorCode initialize(ISecurityFunctionManagerService manager,
			ISecurityFunction securityFunction,
			IStorageSourceService storageSource) {
		ErrorCode ret = ErrorCode.SUCCESS;
		
		this.storageSource = storageSource;
		
		String tableName = "t_secfunc_allocator_webscan";
		Map<String, String> tableColumns = new HashMap<String, String>() {
			private static final long serialVersionUID = 12232L;

			{
				put("device_id", "VARCHAR(32)");
				put("device_type", "VARCHAR(32)");
				put("protocol", "VARCHAR(8)");
				put("ip", "VARCHAR(16)");
				put("port", "int(11)");
				put("base_url", "VARCHAR(64)");
				put("rest_version", "VARCHAR(32)");
				put("reference", "int(11)");
				put("PRIMARY_KEY", "device_id");
				put("EXTRA", "ENGINE=InnoDB DEFAULT CHARSET=utf8");
			}
		};
		storageSource.createTable(tableName, tableColumns);
		
		
		this.storageSource.setTablePrimaryKeyName(this.tableName, "device_id");
		
		return ret;
	}

	@Override
	public SecurityDevice allocate(SecurityDeviceType deviceType, int flag,
			String appID, String tenantID, Object reserved) {
		return null;
	}

	@Override
	public void free(SecurityDevice device) {
		
	}

	@Override
	public SecurityDevice findSecurityDevice(String deviceID) {
		return null;
	}

	@Override
	public List<SecurityDevice> getAllSecurityDevices() {
		return null;
	}
}
