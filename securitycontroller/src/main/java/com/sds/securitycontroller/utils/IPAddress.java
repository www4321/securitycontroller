/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.utils;

public class IPAddress {
	
	int address;
	
	public IPAddress(int addr){
		this.address = addr;
	}
	
	String calc(){
		long mask[] = {0x000000FF,0x0000FF00,0x00FF0000,0xFF000000};    
        long num = 0;    
        StringBuffer ipInfo = new StringBuffer();    
        for(int i=0;i<4;i++){    
            num = (this.address & mask[i])>>(i*8);    
            if(i>0) ipInfo.insert(0,".");    
                ipInfo.insert(0,Long.toString(num,10));    
        }    
        return ipInfo.toString();    
	}
	
	@Override
	public String toString(){
		return calc();
	}

	
	
	public static boolean isIpInSubnet(String subnetMask,String IP,int masklen){
		try {
			String[] subnetMaskSegments = subnetMask.split("\\.");
			String[] ipPointSegments = IP.split("\\.");
			int subnetMaskValue=0,ipValue=0, leftMovBit=0;
			for(int i=3;i>=0;i--)
				subnetMaskValue += Integer.valueOf(subnetMaskSegments[i]) << (leftMovBit++)*8; 
			leftMovBit=0;
			for(int i=3;i>=0;i--)
				ipValue += Integer.valueOf(ipPointSegments[i]) << (leftMovBit++)*8;
			//System.out.printf("ipValue=%d, subnetMaskValue=%d", ipValue,subnetMaskValue);
			return ((subnetMaskValue & ipValue) ==subnetMaskValue);
			
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		
//		//TODO
//		try {
//			if(masklen==24){
//				String[] subnetMaskSegments = subnetMask.split("\\.");
//				String[] ipPointSegments = IP.split("\\.");
//				for(int i=0;i<3;i++){
//					if(!subnetMaskSegments[i].equals(ipPointSegments[i]))
//						return false;
//				}
//				return true;
//			}
//		} catch (Exception e) {
//			System.err.println(e.getMessage());
//		}
//		return false;
	}
	
	public static void main (String[] args){
		String mask="192.168.19.0";

		String IP="192.168.19.23";
		int masklen=24;
		System.out.println(isIpInSubnet(mask, IP, masklen));	
	}
}
