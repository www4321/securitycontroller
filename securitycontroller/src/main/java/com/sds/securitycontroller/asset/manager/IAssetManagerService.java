/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.asset.manager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sds.securitycontroller.asset.Asset;
import com.sds.securitycontroller.common.Entity;
import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IAssetManagerService  extends ISecurityControllerService{
	public void deleteFlowAssets();
	public Asset getFlowAssetEntity(Entity snode, Entity node);
	

	public boolean addAsset(Asset asset);
	public Map<String,Object> buildAsset(Map<String,Object> valueMap)throws IOException;
	public Asset getAsset(String id);
	public boolean removeAsset(String id);
	public String assetExists(Map<String,Object> query);
	public String applyAssetId();
	public List<Asset> getAllAsset();
	public boolean updateAsset(String id, Map<String,Object> values);
}
