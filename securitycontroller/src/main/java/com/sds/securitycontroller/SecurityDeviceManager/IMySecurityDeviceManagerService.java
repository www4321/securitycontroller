package com.sds.securitycontroller.SecurityDeviceManager;

import java.util.List;
import java.util.Map;

import com.sds.securitycontroller.module.ISecurityControllerService;


public interface IMySecurityDeviceManagerService extends ISecurityControllerService{
	public boolean addSecurityDevice(MySecurityDevice device);
	public boolean removeSecurityDevice(MySecurityDevice device);	
	public MySecurityDevice getSecurityDevice(String id);//get an order by it's id
	public List<MySecurityDevice> getAllSecurityDevices(String from, int size);
	public List<MySecurityDevice> getAllSecurityDevices();
	public boolean updateSecurityDevice(String id, Map<String,Object> values);
	public void addList(MySecurityDevice scanDevice);
	public List<MySecurityDevice> getList();
	public void remove(String ip);

}
