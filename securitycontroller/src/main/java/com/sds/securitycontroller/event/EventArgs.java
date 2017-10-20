/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event;

import java.io.Serializable;
import java.util.Date;

public class EventArgs  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7406561189524650627L;
	protected Date time = new Date();

	public Date getTime() {
		return time;
	}
	
}
