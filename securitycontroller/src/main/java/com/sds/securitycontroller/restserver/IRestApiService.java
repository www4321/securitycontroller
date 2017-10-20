/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.restserver;

import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IRestApiService extends ISecurityControllerService {
    /**
     * Adds a REST API
     * @param routeable
     */
    public void addRestletRoutable(RestletRoutable routable);

    /**
     * Runs the REST API server
     */
    public void run();
}
