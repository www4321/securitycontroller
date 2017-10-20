/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.core.message;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.sds.securitycontroller.protocol.DeviceMessageType;

public class BasicFactory implements DeviceMessageFactory{	
	public BasicFactory(){
		
	}
	public Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer){
		return null;
	}
	
	@Override
	public List<DeviceMessage> parseMessage(ChannelBuffer buffer){
		return null;
	}
	@Override
	public DeviceMessage getMessage(DeviceMessageType t) {
        DeviceMessage message = t.newInstance();

        //injectFactories(message);
        return message;
	}

}
