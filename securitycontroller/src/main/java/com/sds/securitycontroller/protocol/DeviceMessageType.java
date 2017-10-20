/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.protocol;

import java.lang.reflect.Constructor;

import com.sds.securitycontroller.core.message.DeviceMessage;

public enum DeviceMessageType {
    HELLO               (0, DeviceMessageHello.class, new Instantiable<DeviceMessage>() {
                            @Override
                            public DeviceMessage instantiate() {
                                return new DeviceMessageHello();
                            }}),
    ERROR               (1, DeviceMessageError.class, new Instantiable<DeviceMessage>() {
                            @Override
                            public DeviceMessage instantiate() {
                                return new DeviceMessageError();
                            }}),
    ECHO_REQUEST        (2, DeviceMessageEchoRequest.class, new Instantiable<DeviceMessage>() {
                            @Override
                            public DeviceMessage instantiate() {
                                return new DeviceMessageEchoRequest();
                            }}),
    ECHO_RESPONSE        (3, DeviceMessageEchoResponse.class, new Instantiable<DeviceMessage>() {
                            @Override
                            public DeviceMessage instantiate() {
                                return new DeviceMessageEchoResponse();
                            }});
    
    
    static DeviceMessageType[] mapping;

    protected Class<? extends DeviceMessage> clazz;
    protected Constructor<? extends DeviceMessage> constructor;
    protected Instantiable<DeviceMessage> instantiable;
    protected byte type;

    /**
     * Store some information about the OpenFlow type, including wire protocol
     * type number, length, and derived class
     *
     * @param type Wire protocol number associated with this DeviceMessageType
     * @param clazz The Java class corresponding to this type of OpenFlow
     *              message
     * @param instantiator An Instantiator<DeviceMessage> implementation that creates an
     *          instance of the specified DeviceMessage
     */
    DeviceMessageType(int type, Class<? extends DeviceMessage> clazz, Instantiable<DeviceMessage> instantiator) {
        this.type = (byte) type;
        this.clazz = clazz;
        this.instantiable = instantiator;
        try {
            this.constructor = clazz.getConstructor(new Class[]{});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failure getting constructor for class: " + clazz, e);
        }
        DeviceMessageType.addMapping(this.type, this);
    }

    /**
     * Adds a mapping from type value to DeviceMessageType enum
     *
     * @param i OpenFlow wire protocol type
     * @param t type
     */
    static public void addMapping(byte i, DeviceMessageType t) {
        if (mapping == null)
            mapping = new DeviceMessageType[32];
        DeviceMessageType.mapping[i] = t;
    }

    /**
     * Remove a mapping from type value to DeviceMessageType enum
     *
     * @param i OpenFlow wire protocol type
     */
    static public void removeMapping(byte i) {
        DeviceMessageType.mapping[i] = null;
    }

    /**
     * Given a wire protocol OpenFlow type number, return the DeviceMessageType associated
     * with it
     *
     * @param i wire protocol number
     * @return DeviceMessageType enum type
     */

    static public DeviceMessageType valueOf(Byte i) {
        return DeviceMessageType.mapping[i];
    }

    /**
     * @return Returns the wire protocol value corresponding to this DeviceMessageType
     */
    public byte getTypeValue() {
        return this.type;
    }

    /**
     * @return return the DeviceMessage subclass corresponding to this DeviceMessageType
     */
    public Class<? extends DeviceMessage> toClass() {
        return clazz;
    }

    /**
     * Returns the no-argument Constructor of the implementation class for
     * this DeviceMessageType
     * @return the constructor
     */
    public Constructor<? extends DeviceMessage> getConstructor() {
        return constructor;
    }

    /**
     * Returns a new instance of the DeviceMessage represented by this DeviceMessageType
     * @return the new object
     */
    public DeviceMessage newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<DeviceMessage> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable the instantiable to set
     */
    public void setInstantiable(Instantiable<DeviceMessage> instantiable) {
        this.instantiable = instantiable;
    }
}
