package com.sds.securitycontroller.command;

import java.io.Serializable;

public class RedirectDeviceInfo implements Serializable {

	public RedirectDeviceInfo(String deviceid, int tag,
			RedirectDeviceInterface ingress, RedirectDeviceInterface outgress) {
//		super();
		this.deviceid = deviceid;
		this.tag = tag;
		this.ingress = ingress;
		this.egress = outgress;
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 5124625652440056080L;

	String deviceid;
	int tag;
	RedirectDeviceInterface ingress;
	RedirectDeviceInterface egress;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


	public String getDeviceid() {
		return deviceid;
	}


	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}


	public int getTag() {
		return tag;
	}


	public void setTag(int tag) {
		this.tag = tag;
	}


	public RedirectDeviceInterface getIngress() {
		return ingress;
	}


	public void setIngress(RedirectDeviceInterface ingress) {
		this.ingress = ingress;
	}


	public RedirectDeviceInterface getEgress() {
		return egress;
	}


	public void setEgress(RedirectDeviceInterface egress) {
		this.egress = egress;
	}

}
