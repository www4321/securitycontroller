package com.sds.securitycontroller.cloud.manager;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudAgentResource extends ServerResource {
	protected static Logger log = LoggerFactory.getLogger(CloudAgentResource.class);
	
	@Get("json")
    public Object handleRequest() {
        ICloudAgentService caService =
                (ICloudAgentService)getContext().getAttributes().
                    get(ICloudAgentService.class.getCanonicalName());
    	String item = (String) getRequestAttributes().get("item");
        //list all tenants  
        if (item.toLowerCase().equals("tenant")) {
            return caService.getTenants();
        }else if  (item.toLowerCase().equals("user")) {
            return caService.getUsers();
        }else if  (item.toLowerCase().equals("subnet")) {
            return caService.getSubnets();
        }else if  (item.toLowerCase().equals("vm")) {
            return caService.getVMs();
        }
        // no known options found
        return "{\"status\" : \"failure\", \"details\" : \"invalid id: "+item+" \"}";
    }

}
