/** 
*    Copyright 2014 BUPT. 
**/ 
/**
 *    Copyright 2011, Big security device Networks, Inc. 
 *    Originally created by David Erickson, Stanford University
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package com.sds.securitycontroller.core;

import java.util.List;
import java.util.Map;

import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.protocol.DeviceMessageType;


/**
 * The interface exposed by the core bundle that allows you to interact
 * with connected security devices.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface ISecurityControllerProviderService extends
        ISecurityControllerService {

    /**
     * A value stored in the SecurityController context containing a parsed packet
     * representation of the payload of a packet-in message. 
     */
    public static final String CONTEXT_PI_PAYLOAD = 
            "net.SecurityControllercontroller.core.ISecurityControllerProvider.piPayload";

    /**
     * Return a non-modifiable list of all current listeners
     * @return listeners
     */
    public Map<DeviceMessageType, List<IDeviceMessageListener>> getListeners();


    
    /**
     * Add an info provider of a particular type
     * @param type
     * @param provider
     */
    public void addInfoProvider(String type, IInfoProvider provider);

   /**
    * Remove an info provider of a particular type
    * @param type
    * @param provider
    */
   public void removeInfoProvider(String type, IInfoProvider provider);
   
   /**
    /**
     * Gets the ID of the controller
     */
    public String getControllerId();
    
    /**
     * Terminate the process
     */
    public void terminate();

    /**
     * Gets the BasicFactory
     * @return an OpenFlow message factory
     */
    //public BasicFactory getSecurityMessageFactory();

    /**
     * Run the main I/O loop of the Controller.
     */
    public void run();

   /**
    * Return information of a particular type (for rest services)
    * @param type
    * @return
    */
   public Map<String, Object> getControllerInfo(String type);
   
   
   /**
    * Return the controller start time in  milliseconds
    * @return
    */
   public long getSystemStartTime();
   
   public void setScheduler(IEventManagerService scheduler);
   
   

}
