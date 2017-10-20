package com.sds.securitycontroller.command;

import java.io.Serializable;

public class RedirectDeviceInterface implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3919842305252003343L;
    String mac;
    String ap;
    FlowAction actions;

    public RedirectDeviceInterface(String mac, String ap) {
        super();
        this.mac = mac;
        this.ap = ap;
    }
    public String getMac() {
		return mac;
	}
    public void setMac(String mac) {
		this.mac = mac;
	}
    public String getAp() {
		return ap;
	}

    public FlowAction getActions() {
        return actions;
    }

    public void setActions(FlowAction actions) {
        this.actions = actions;
    }

    public void setAp(String ap) {
		this.ap = ap;
	}
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub


    }

}
