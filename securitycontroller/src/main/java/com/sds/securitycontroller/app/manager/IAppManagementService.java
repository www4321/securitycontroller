/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.app.manager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.app.AppRealtimeInfo;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IAppManagementService extends  ISecurityControllerService{

	/**
     * Returns an unmodifiable map of all connected apps.
     * @return the set of connected apps
     */
    public Map<String, App> getConnectedApps();

    
    /**
     * Add an app to the app list.
     * This happens either when an app first connects (and the controller is
     * not in the slave role) or when the role of the controller changes from
     * slave to master.
     *
     * @param sd the app that has been added
     */
    
    public String appRegistered(App app);//if an app already registered
    
     
	public boolean addApp(App app);
	public boolean removeApp(App app);	
	public boolean updateApp(App app, JsonNode jn) throws  IOException;
	public App getApp(String id);//get an app by it's id
	public List<App> getAllApps(String from, int size);
	public List<App> getAllApps();
		
	
	public boolean 					addAppRealtimeInfo(String appId);
	public boolean  				updateAppRealtimeInfo(String appId, JsonNode jn) throws IOException;
	public boolean 					removeAppRealtimeInfo(String appId); 	
	public AppRealtimeInfo 			getAppRealtimeInfo(String appId);
	public List<AppRealtimeInfo>	getAllAppRealtimeInfo();

	//package management
	public String getPackageVersion(String guid);
	public String getPackagedir();
	public String getSnapshotdir();
	public void fillUpdateInfo(App app);
  
			
}
