package com.sds.securitycontroller.directory.registry;

import java.io.IOException;
import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.directory.ServiceInfo;
import com.sds.securitycontroller.utils.JsonRequest;
import com.sds.securitycontroller.utils.JsonResponse;

public class RegistryServiceManageResource extends ServerResource{
	protected static Logger log = LoggerFactory.getLogger(RegistryServiceManageResource.class);
	String serviceId = null;
	IRegistryManagementService regManager = null;
	JsonRequest request=null;
	JsonResponse response=new JsonResponse();

	
	@Override  
    public void doInit() {    
        regManager = 
                (IRegistryManagementService)getContext().getAttributes().
                get(IRegistryManagementService.class.getCanonicalName());
        serviceId = (String) getRequestAttributes().get("id");
        
    }  
	

	@Get("json")
    public Object handleGetRequest() {		 
		do{
			if(null != serviceId){
				ServiceInfo service = regManager.findService(serviceId);
				if (null == service) {
					response.setMessage(404, "no such service");
					break;
				}
				try {					
					response.buildData("service", service);					 
				} catch (IOException e) {
					log.error("write json response failed: ", e);
					response.setMessage(404,"write json response failed");
					break;
				}				
			}else{
				List<ServiceInfo> services = regManager.getAllServices();
				try {
					response.buildData("services", services);				 
				} catch (IOException e) {
					log.error("write json response failed: ", e);
					response.setMessage(404,"write json response failed");	
					break;
				}				
			}
			response.setCode(200);
		}while(false);
		
		return response.toString();
	}
}
