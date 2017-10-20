/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.utils;

import java.util.Arrays;

/**
 * The class representing DPID.
 *
 * @author Sho Shimizu (sho.shimizu@gmail.com)
 */
public class DPID {
    public static final int DPID_LENGTH = 8;
    private byte[] address = new byte[DPID_LENGTH];

    public DPID(byte[] address) {
        this.address = Arrays.copyOf(address, DPID_LENGTH);
    }

    /**
     * Returns a MAC address instance representing the value of the specified {@code String}.
     * @param address the String representation of the MAC Address to be parsed.
     * @return a MAC Address instance representing the value of the specified {@code String}.
     * @throws IllegalArgumentException if the string cannot be parsed as a MAC address.
     */
    public static DPID valueOf(String address) {
        String[] elements = address.split(":");
        if (elements.length != DPID_LENGTH) {
            throw new IllegalArgumentException(
                    "Specified dpid must contain 32 hex digits" +
                    " separated pairwise by :'s.");
        }

        byte[] addressInBytes = new byte[DPID_LENGTH];
        for (int i = 0; i < DPID_LENGTH; i++) {
            String element = elements[i];
            addressInBytes[i] = (byte)Integer.parseInt(element, 16);
        }

        return new DPID(addressInBytes);
    }

    /**
     * Returns a MAC address instance representing the specified {@code byte} array.
     * @param address the byte array to be parsed.
     * @return a MAC address instance representing the specified {@code byte} array.
     * @throws IllegalArgumentException if the byte array cannot be parsed as a MAC address.
     */
    public static DPID valueOf(byte[] address) {
        if (address.length != DPID_LENGTH) {
            throw new IllegalArgumentException("the length is not " + DPID_LENGTH);
        }

        return new DPID(address);
    }

    /**
     * Returns a MAC address instance representing the specified {@code long} value.
     * The lower 48 bits of the long value are used to parse as a MAC address.
     * @param address the long value to be parsed. The lower 48 bits are used for a MAC address.
     * @return a MAC address instance representing the specified {@code long} value.
     * @throws IllegalArgumentException if the long value cannot be parsed as a MAC address.
     */
    public static DPID valueOf(long address) {
        byte[] addressInBytes = new byte[] {
                (byte)((address >> 56) & 0xff),
                (byte)((address >> 48) & 0xff),
                (byte)((address >> 40) & 0xff),
                (byte)((address >> 32) & 0xff),
                (byte)((address >> 24) & 0xff),
                (byte)((address >> 16) & 0xff),
                (byte)((address >> 8 ) & 0xff),
                (byte)((address >> 0) & 0xff)
        };

        return new DPID(addressInBytes);
    }
    
    public static long longValueOf(String address) {
    	String[] elements = address.split(":");
        if (elements.length != DPID_LENGTH) {
            throw new IllegalArgumentException(
                    "Specified dpid must contain 32 hex digits" +
                    " separated pairwise by :'s.");
        }

        byte[] addressInBytes = new byte[DPID_LENGTH];
        for (int i = 0; i < DPID_LENGTH; i++) {
            String element = elements[i];
            addressInBytes[i] = (byte)Integer.parseInt(element, 16);
        }
        return  convertToLong(addressInBytes);
    }

    /**
     * Returns the length of the {@code MACAddress}.
     * @return the length of the {@code MACAddress}.
     */
    public int length() {
        return address.length;
    }

    /**
     * Returns the value of the {@code MACAddress} as a {@code byte} array.
     * @return the numeric value represented by this object after conversion to type {@code byte} array.
     */
    public byte[] toBytes() {
        return Arrays.copyOf(address, address.length);
    }

    /**
     * Returns the value of the {@code MACAddress} as a {@code long}.
     * @return the numeric value represented by this object after conversion to type {@code long}.
     */
    public long toLong() {
        long mac = 0;
        for (int i = 0; i < DPID_LENGTH; i++) {
            long t = (address[i] & 0xffL) << ((DPID_LENGTH - 1 - i) * 8);
            mac |= t;
        }
        return mac;
    }
    
    static long convertToLong(byte[] address) {
        long mac = 0;
        for (int i = 0; i < DPID_LENGTH; i++) {
            long t = (address[i] & 0xffL) << ((DPID_LENGTH - 1 - i) * 8);
            mac |= t;
        }
        return mac;
    }
    

    /**
     * Returns {@code true} if the MAC address is the broadcast address.
     * @return {@code true} if the MAC address is the broadcast address.
     */
    public boolean isBroadcast() {
        for (byte b : address) {
            if (b != -1) // checks if equal to 0xff
                return false;
        }
        return true;
    }

    /**
     * Returns {@code true} if the MAC address is the multicast address.
     * @return {@code true} if the MAC address is the multicast address.
     */
    public boolean isMulticast() {
        if (isBroadcast()) {
            return false;
        }
        return (address[0] & 0x01) != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof DPID)) {
            return false;
        }

        DPID other = (DPID)o;
        return Arrays.equals(this.address, other.address);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.address);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (byte b: address) {
            if (builder.length() > 0) {
                builder.append(":");
            }
            builder.append(String.format("%02X", b & 0xFF));
        }
        return builder.toString();
    }
    
    public static void main(String[] args){
//    	DPID dpid= valueOf("00:00:00:1e:08:09:64:36");
    	long a = longValueOf("00:00:00:1e:08:09:64:36");//dpid.toLong();
    	System.out.println(a);
    	
    }
}
