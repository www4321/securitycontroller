package com.sds.securitycontroller.www1234.sfc.manager;
// time : 2017.5.23  by www1234
import java.util.List;

import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuplePath;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.www1234.sfc.SFCInstance;
import com.sds.securitycontroller.www1234.sfc.SFCSwitchPortInstance;
import com.sds.securitycontroller.www1234.sfc.ServiceFunction;

public interface ISFCService extends ISecurityControllerService {
	/**
	 * Map Service Function Chain into underlying network according to SFCInstance.
	 * It will store path and the flow rules' name of Service Function Chain. the flow rules' name of Service Function Chain
	 * is used to delete a Service Function Chain.
	 * @param sfcInstance
	 * @return List<String>
	 */
	public void mapSFC(SFCInstance sfcInstance);
	
	public void mapSFCbySwPorts(SFCSwitchPortInstance sfcSwitchPortInstance);
	/**
	 * Delete a Service Function Chain by sfc_id.
	 * @param sfc_id
	 * @return return true if delete a Service Function Chain successfully.
	 */
	public boolean deleteSFCById(String sfc_id);
	/**
	 * Create Service Function Chain route by SFCInstance.
	 * @param sfcInstance
	 * @return
	 */
	public List<NodePortTuplePath> getRoute(SFCInstance sfcInstance);
	/**
	 * Get Service Function Chain route by sfc_id
	 * @param sfc_id
	 * @return
	 */
	public List<NodePortTuplePath> getRouteById(String sfc_id);
	/**
	 * get all SFC Instance.
	 * @return
	 */
	public List<SFCInstance> getSfcInstanceList();
	
	public SFCInstance getSfcInstancebyId(String sfc_id);
	
	public SFCSwitchPortInstance getSFCSwitchPortInstancebyId(String sfc_id);
	
	public List<NodePortTuplePath> getRoutebySwPort(SFCSwitchPortInstance sfcSwitchPortInstance);
	
}
