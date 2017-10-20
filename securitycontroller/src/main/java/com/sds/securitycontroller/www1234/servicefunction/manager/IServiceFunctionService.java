package com.sds.securitycontroller.www1234.servicefunction.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.www1234.servicefunction.ServiceFunctionDevice;
import com.sds.securitycontroller.www1234.sfc.ServiceFunction;

public interface IServiceFunctionService extends ISecurityControllerService {
	/**
	 * get a ServiceFunctionDevice according to ServiceFunction type;
	 * @param sf
	 * @return ServiceFunctionDevice
	 */
	public ServiceFunctionDevice getSFDevice(ServiceFunction sf);
	/**
	 * get all ServiceFunctionDevices according to ServiceFunction type;
	 * @param sf
	 * @return
	 */
	public Set<ServiceFunctionDevice> getSFDevices(ServiceFunction sf);
	/**
	 * get a map which store the information of ServiceFunction type and ServiceFunctionDevices;
	 * @return Map<ServiceFunction,Set<ServiceFunctionDevice>>
	 */
	public Map<ServiceFunction,Set<ServiceFunctionDevice>> getDevices();
	/**
	 * add a new ServiceFunctionDevice
	 * @param sfDevice
	 */
	public void addSFDevice(ServiceFunctionDevice sfDevice);
	/**
	 * remove a old ServiceFunctionDevice
	 * @param sfDevice
	 */
	public void removeSFDevice(ServiceFunctionDevice sfDevice);
	/**
	 * get SwitchPort information according to device MAC. 
	 * @param mac
	 * @return
	 */
	public NodePortTuple getSwitchPort(String mac);
	
	public List<ServiceFunctionDevice> getAllSF();
}
