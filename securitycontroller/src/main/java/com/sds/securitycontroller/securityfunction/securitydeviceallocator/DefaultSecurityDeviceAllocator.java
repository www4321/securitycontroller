package com.sds.securitycontroller.securityfunction.securitydeviceallocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClause.QueryClauseType;
import com.sds.securitycontroller.storage.QueryClauseItem;

public class DefaultSecurityDeviceAllocator implements ISecurityDeviceAllocator
{
	private static Logger log = LoggerFactory.getLogger(DefaultSecurityDeviceAllocator.class);
	private ISecurityFunction securityFunction = null;
	private ISecurityFunctionManagerService manager = null;
	private ArrayList<_Device> deviceList = null;
	private HashMap<String, _Device> devices = null;
	private IStorageSourceService storageSource = null;
	private String tableName = "t_secfunc_allocator_default";
	
	private static class _Device implements IDBObject
	{
		private static final long serialVersionUID = 1L;
		public SecurityDevice secDev = null;
		public int reference = 0;
		public DefaultSecurityDeviceAllocator outer;

		@SuppressWarnings("unused")
		public _Device(){}
		
		public _Device(SecurityDevice secDev,DefaultSecurityDeviceAllocator outer)
		{
			this.secDev = secDev;
			this.reference = 0;
            this.outer=outer;
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
			
			if(outer.storageSource.updateOrInsertEntity(outer.tableName, this) <= 0)
			{
				log.error("[DefaultSecurityDeviceAllocator::_Device][ref] update device info in database failed");
				return -1;
			}
			
			log.debug("[DefaultSecurityDeviceAllocator::_Device] device {} reference increased to {}", this.toString(), this.reference);
			
			return this.reference;
		}
		
		public int deref()
		{
			this.reference--;
			assert(this.reference > 0);
			
			if(this.reference == 0)
			{
				if(outer.storageSource.deleteEntity(outer.tableName, this.secDev.device_id) <= 0)
				{
					log.error("[DefaultSecurityDeviceAllocator::_Device][deref] delete device from database failed");
				}
				else
				{
					log.debug("[DefaultSecurityDeviceAllocator::_Device][deref] device {} reference decreased to 0, delete it from database", this.toString());
				}
				
				return 0;
			}
			
			if(outer.storageSource.updateOrInsertEntity(outer.tableName, this) <= 0)
			{
				log.error("[DefaultSecurityDeviceAllocator::_Device][deref] update device info in database failed");
				return -1;
			}
			
			log.debug("[DefaultSecurityDeviceAllocator::_Device] device {} reference decreased to {}", this.toString(), this.reference);
			
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
			ISecurityFunction securityFunction, IStorageSourceService storageSource) {
		ErrorCode ret = ErrorCode.SUCCESS;
		
		this.deviceList = new ArrayList<_Device>();
		this.devices = new HashMap<String, _Device>();
		this.manager = manager;
		this.securityFunction = securityFunction;
		this.storageSource = storageSource;
		
		String tableName = "t_secfunc_allocator_default";
		Map<String, String> tableColumns = new HashMap<String, String>() {
			private static final long serialVersionUID = 1232L;

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
		
		List<_Device> allDevices = this.getAllSecurityDevicesFromDB();
		Iterator<_Device> devIt = allDevices.iterator();
		while(devIt.hasNext())
		{
			_Device dev = devIt.next();
			this.addSecurityDevice(dev);
		}
		
		return ret;
	}
	
	@Override
	public SecurityDevice allocate(SecurityDeviceType deviceType, int flag,
			String appID, String tenantID, Object reserved) {
		_Device dev = null;
		SecurityDevice secDev = null;
		
		do
		{
			secDev = this.manager.allocateSecurityDevice(deviceType);
			if(secDev == null)
			{
				log.error("[DefaultSecurityDeviceAllocator] allocate failed from DeviceManager");
				log.debug("[DefaultSecurityDeviceAllocator] try to return an existing device in the cache");
				
				if(this.devices.size() > 0)
				{
					dev = this.getMinReferenceDevice(deviceType);
					
					if(dev == null)
					{
						log.error("[DefaultSecurityDeviceAllocator] can not find " + deviceType.toString() + " device in cache");
					}
					else
					{
						dev.ref();
						log.debug("[DefaultSecurityDeviceAllocator] find {} from cache", dev.toString());
					}
				}
				else
				{
					log.error("[DefaultSecurityDeviceAllocator] no available devices in cache");
				}
			}
			else
			{
				if(this.devices.containsKey(secDev.device_id))
				{
					log.error("[DefaultSecurityDeviceAllocator] FATAL ERROR: newly allocated device already exists in cache!!!!!!");
					this.manager.freeSecurityDevice(secDev);
					dev = null;
					break;
				}
				
				dev = new _Device(secDev,this);
				if(dev.ref() <= 0)
				{
					log.error("[DefaultSecurityDeviceAllocator] allocate success, but insert into database failed, so free the device");
					this.manager.freeSecurityDevice(secDev);
					dev = null;
					break;
				}
				
				try
				{
					if(this.securityFunction.probe(dev.secDev) == ErrorCode.SUCCESS)
					{
						this.addSecurityDevice(dev);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					log.error("[DefaultSecurityDeviceAllocator] allocate success, probe exception, " + e.getMessage());
					dev.deref();
					this.manager.freeSecurityDevice(dev.secDev);
					dev = null;
					break;
				}
			}
		}while(false);
		
		return dev == null ? null : dev.secDev;
	}
	
	@Override
	public void free(SecurityDevice device) {
		_Device dev = this.devices.get(device.device_id);
		
		if(dev == null)
		{
			log.warn("[DefaultSecurityDeviceAllocator] try to free device {} which is not cached", device.device_id);
			return;
		}
		
		if(dev.deref() == 0)
		{
			this.removeSecurityDevice(dev);
			this.manager.freeSecurityDevice(dev.secDev);
		}
	}
	
	private _Device getMinReferenceDevice(SecurityDeviceType type)
	{
		_Device dev = null;
		Iterator<_Device> it = this.deviceList.iterator();
		
		while(it.hasNext())
		{
			_Device tmp = it.next();
			if(tmp.secDev.type == type)
			{
				if(dev == null)
				{
					dev = tmp;
				}
				else if(dev.reference > tmp.reference)
				{
					dev = tmp;
				}
			}
		}
		
		return dev;
	}
	
	private void addSecurityDevice(_Device dev)
	{
		this.deviceList.add(dev);
		this.devices.put(dev.secDev.device_id, dev);
	}
	
	private void removeSecurityDevice(_Device dev)
	{
		this.deviceList.remove(dev);
		this.devices.remove(dev.secDev.device_id);
	}
	
	private List<_Device> getAllSecurityDevicesFromDB()
	{
		ArrayList<_Device> list = new ArrayList<_Device>();
		
		QueryClause qc = new QueryClause(new ArrayList<QueryClauseItem>(), this.tableName, null, null);
		qc.setType(QueryClauseType.EMPTY);
		@SuppressWarnings("unchecked")
		List<_Device> dbResult = (List<_Device>)this.storageSource.executeQuery(qc, _Device.class);
		
		if(dbResult != null && dbResult.size() > 0)
		{
			Iterator<_Device> it = dbResult.iterator();
			while(it.hasNext())
			{
				list.add(it.next());
			}
		}
		
		return list;
	}
	
	@Override
	public SecurityDevice findSecurityDevice(String deviceID) {
		if(this.devices.containsKey(deviceID))
		{
			_Device dev = this.devices.get(deviceID);
			try
			{
				if(dev.secDev.driver == null)
				{
					if(this.securityFunction.probe(dev.secDev) == ErrorCode.SUCCESS)
					{
						return dev.secDev;
					}
				}
				else
				{
					return dev.secDev;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				log.error("[DefaultSecurityDeviceAllocator] probe device " + dev.toString() + " exception, " + e.getMessage());
			}
		}
		return null;
	}
	
	@Override
	public List<SecurityDevice> getAllSecurityDevices()
	{
		ArrayList<SecurityDevice> list = new ArrayList<SecurityDevice>();
		Iterator<_Device> it = this.deviceList.iterator();
		
		while(it.hasNext())
		{
			list.add(it.next().secDev);
		}
		
		return list;
	}
}
