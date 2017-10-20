/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.core.message;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;

import com.sds.securitycontroller.protocol.DeviceMessageType;


public interface  DeviceMessageFactory {

    public DeviceMessage getMessage(DeviceMessageType t);

    /**
     * Attempts to parse and return a OFMessages contained in the given
     * ChannelBuffer, beginning at the ChannelBuffer's position, and ending at the
     * after the first parsed message
     * @param data the ChannelBuffer to parse for an OpenFlow message
     * @return a list of OFMessage instances
     * @throws MessageParseException 
     */
    public List<DeviceMessage> parseMessage(ChannelBuffer data) throws MessageParseException;

    /**
     * Retrieves an OFActionFactory
     * @return an OFActionFactory
     */
    //public OFActionFactory getActionFactory();
}
