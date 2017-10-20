/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.command;

import java.io.Serializable;

public class APInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String mac = null;
	public String ap = null;//接入点（attachment point），格式为：IP%%portname,如192.168.19.1:tap1
	public boolean noMatchInport;
	public boolean modSrcMac;
	public boolean highPriority;
	public APInfo(String mac, String ap, boolean noMatchInport,
			boolean modSrcMac, boolean highPriority) {
		super();
		this.mac = mac;
		this.ap = ap;
		this.noMatchInport = noMatchInport;
		this.modSrcMac = modSrcMac;
		this.highPriority = highPriority;
	}
}
