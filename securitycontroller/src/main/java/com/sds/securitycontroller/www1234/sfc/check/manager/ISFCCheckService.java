package com.sds.securitycontroller.www1234.sfc.check.manager;

import java.util.List;



import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.www1234.sfc.SFCInstance;

public interface ISFCCheckService extends ISecurityControllerService {
	/**
	 * Check Service Function chain according to abstract SFC logic (SFCInstancce).
	 * @param sfcInstance
	 * @return true if flows are steered by a series of rules installed by abstract SFC.
	 *         else, return false.
	 */
	public boolean sfcCheck(SFCInstance sfcInstance);

	public List<SFCInstance> getSFCInstances();

	public void SetSFCInstances(List<SFCInstance> sfcInstances);

	public void sfcCheckenable();

	boolean sfcCheckbySwitchPort(String sfc_id);
	
	
	public List<ConflictFlowInfo> getConflictRules(String sfc_id);
}
