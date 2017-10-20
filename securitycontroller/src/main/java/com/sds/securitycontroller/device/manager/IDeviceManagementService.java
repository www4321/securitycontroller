/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.device.manager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.asset.manager.AssetManager;
import com.sds.securitycontroller.core.IDeviceMessageListener;
import com.sds.securitycontroller.device.BootDevice;
import com.sds.securitycontroller.device.DevRealtimeInfo;
import com.sds.securitycontroller.device.Device;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.event.manager.IRPCHandler;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.protocol.DeviceMessageType;

public interface IDeviceManagementService  extends ISecurityControllerService, IRPCHandler{

    /*
     * if an device already registered 
     */
    public String devRegistered(Device dev);    
	public boolean addDev(Device dev);	
    public boolean removeDev(Device dev);
    boolean updateDev(Device dev, JsonNode jn) throws IOException;
	public Device getDev(String id);
	public List<Device> getDevs();
 
	
	
    public boolean 				addDevRealtimeInfo(String appId);
	public boolean  			updateDevRealtimeInfo(String appId, JsonNode jn) throws IOException;
	public boolean 				removeDevRealtimeInfo(String appId); 	
	public DevRealtimeInfo 		getDevRealtimeInfo(String appId);
	public List<DevRealtimeInfo>  		getAllDevRealtimeInfo();
 
	
	
	
    /**
     * Adds an message listener
     * @param type The OFType the component wants to listen for
     * @param listener The component that wants to listen for the message
     */
    public void addDeviceMessageListener(DeviceMessageType type, IDeviceMessageListener listener);

    /**
     * Removes an OpenFlow message listener
     * @param type The OFType the component no long wants to listen for
     * @param listener The component that no longer wants to receive the message
     */
    public void removeDeviceMessageListener(DeviceMessageType type, IDeviceMessageListener listener);
	public boolean controlDev(String operation);
	public String getSnapshotdir();
	public String getMyIp() throws Exception;
	


	public AssetManager getAssetManager();
	
	//for boot agent
	public BootDevice bootSecurityDeviceInstance(String name, String type, String attach, String attachType, Map<String, Object> attrs) throws Exception;
	public BootDevice getBootDevice(String id);
	public boolean registerBootAgent(DeviceType type, String url);
	public List<BootDevice> getAllBootDevices();
	BootDevice getBootDevice(DeviceType deviceType);
}
