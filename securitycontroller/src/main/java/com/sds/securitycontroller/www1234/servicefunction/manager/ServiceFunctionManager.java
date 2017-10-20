package com.sds.securitycontroller.www1234.servicefunction.manager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.www1234.servicefunction.ServiceFunctionDevice;
import com.sds.securitycontroller.www1234.sfc.ServiceFunction;

public class ServiceFunctionManager implements IServiceFunctionService,ISecurityControllerModule{
	
	
	private String ncHost = null;
	private String url = "/wm/device/";
	private Map<ServiceFunction,Set<ServiceFunctionDevice>> deviceMap;
	private List<ServiceFunctionDevice> serviceFunctionDevices;
	protected IRestApiService restApi;
	protected static Logger log = LoggerFactory.getLogger(ServiceFunctionManager.class);
	
	public Map<ServiceFunction, Set<ServiceFunctionDevice>> getDeviceMap() {
		return deviceMap;
	}

	public void setDeviceMap(Map<ServiceFunction, Set<ServiceFunctionDevice>> deviceMap) {
		this.deviceMap = deviceMap;
	}
	@Override
	public List<ServiceFunctionDevice> getAllSF(){
		Set<ServiceFunction> keyset = deviceMap.keySet();
		Iterator<ServiceFunction> keyiter = keyset.iterator();
		ServiceFunction sf =null;
		while(keyiter.hasNext()){
			sf = keyiter.next();
			serviceFunctionDevices.addAll(deviceMap.get(sf));
		}
		return serviceFunctionDevices;
	}
	
	@Override
	public void init(SecurityControllerModuleContext context) throws SecurityControllerModuleException {
		GlobalConfig config = GlobalConfig.getInstance();
		this.restApi = context.getServiceImpl(IRestApiService.class);
		deviceMap = new HashMap<ServiceFunction, Set<ServiceFunctionDevice>>();
		if(config.ncHost!=null)
			ncHost = config.ncHost;
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		ServiceFunctionDeviceRoutable routable = new ServiceFunctionDeviceRoutable();
		restApi.addRestletRoutable(routable);
		deviceMap.put(ServiceFunction.DDOS, new HashSet<ServiceFunctionDevice>());
		deviceMap.put(ServiceFunction.FIREWALL, new HashSet<ServiceFunctionDevice>());
		deviceMap.put(ServiceFunction.WAF, new HashSet<ServiceFunctionDevice>());
		deviceMap.put(ServiceFunction.IPS, new HashSet<ServiceFunctionDevice>());
		serviceFunctionDevices = new ArrayList<ServiceFunctionDevice>();
	}

	
	/* implements methods of IServiceFunctionService */
	
	@Override
	public ServiceFunctionDevice getSFDevice(ServiceFunction sf) {
		return deviceMap.get(sf).iterator().next();
	}

	@Override
	public Set<ServiceFunctionDevice> getSFDevices(ServiceFunction sf) {
		return deviceMap.get(sf);
	}

	@Override
	public Map<ServiceFunction, Set<ServiceFunctionDevice>> getDevices() {
		return deviceMap;
	}
	@Override
	public void addSFDevice(ServiceFunctionDevice sfDevice) {
		ServiceFunction sf = sfDevice.getSF();
		this.deviceMap.get(sf).add(sfDevice);
		log.info("\n Network Function:[type={},in_ip={},out_ip={},\n in_mac={},out_mac={},\n in_switchport={},out_switchport={}.",
				sfDevice.getSF(),sfDevice.getIn_ip(),sfDevice.getOut_ip(),sfDevice.getIn_mac(),sfDevice.getOut_mac(),
				sfDevice.getIn_switchPort(),sfDevice.getOut_switchPort());
	}
	@Override
	public void removeSFDevice(ServiceFunctionDevice sfDevice){
		ServiceFunction sf = sfDevice.getSF();
		if(this.deviceMap.get(sf).contains(sfDevice)){
			this.deviceMap.get(sf).remove(sfDevice);
		}
	}
	@Override
	public NodePortTuple getSwitchPort(String mac){
		NodePortTuple result = null;
		String url = this.ncHost+this.url;
		ObjectMapper mapper = new ObjectMapper();
		URL datasource;
		try {
			datasource = new URL(url);
			JsonNode rootNode = mapper.readTree(datasource);
			Iterator<JsonNode> ite = rootNode.iterator();
			JsonNode node=null;
			while(ite.hasNext()){
				node = ite.next();
				if(node.path("mac").get(0).asText().equals(mac)){
					String switchDPID = node.path("attachmentPoint").get(0).path("switchDPID").asText();
					String switchPort = node.path("attachmentPoint").get(0).path("port").asText();
					result = new NodePortTuple(switchDPID,Integer.parseInt(switchPort));
					break;
				}	
			}
			return result;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;	
	}
	/* implements methods of ISecurityControllerModule */
	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services =
                new ArrayList<Class<? extends ISecurityControllerService>>(1);
        services.add(IServiceFunctionService.class);
		return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
        m.put(IServiceFunctionService.class, this);
	return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		 Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
		 l.add(IRestApiService.class);
	     return l;
	}

	
}
