/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.asset.manager;

import org.restlet.resource.Delete;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.app.App;

public class AssetManagerResource extends ServerResource {
	protected static Logger log = LoggerFactory.getLogger(AssetManagerResource.class);
	
	String id = null;
	App app = null;
	IAssetManagerService assetmanager = null;
	
	@Override  
    public void doInit() {    
		assetmanager = 
                (IAssetManagerService)getContext().getAttributes().
                get(IAssetManagerService.class.getCanonicalName());    }  
	
	@Delete
    public String deleteFlows() {
		assetmanager.deleteFlowAssets();
        // no known options found
        return "{\"status\" : \"ok\" \"}";
    }
	
}
