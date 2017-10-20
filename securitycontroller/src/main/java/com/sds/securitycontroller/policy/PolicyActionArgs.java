package com.sds.securitycontroller.policy;

import java.io.Serializable;

public class PolicyActionArgs implements Serializable {

	private static final long serialVersionUID = 9028614703125248147L;
	protected short priority;
	boolean force;


	public PolicyActionArgs() {
	}

	public short getPriority() {
		return priority;
	}

	public void setPriority(short priority) throws PriorityException {
		if(priority<=0)
			throw new PriorityException("Priority can not less than or equal to 0");
		this.priority = priority;
	}

	public boolean isForce() {
		return this.force;
	}
}
