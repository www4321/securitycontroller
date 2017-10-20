package com.sds.securitycontroller.www1234.servicefunction;
import com.sds.securitycontroller.knowledge.globaltraffic.NodePortTuple;
import com.sds.securitycontroller.www1234.sfc.ServiceFunction;
public class ServiceFunctionDevice {
	private ServiceFunction SF;
	private String in_ip;
	private String in_mac;
	private String out_ip;
	private String out_mac;
	private NodePortTuple in_switchPort;
	private NodePortTuple out_switchPort;
	
	public ServiceFunction getSF() {
		return SF;
	}

	public void setSF(ServiceFunction sF) {
		SF = sF;
	}

	public String getIn_ip() {
		return in_ip;
	}

	public void setIn_ip(String in_ip) {
		this.in_ip = in_ip;
	}

	public String getIn_mac() {
		return in_mac;
	}

	public void setIn_mac(String in_mac) {
		this.in_mac = in_mac;
	}

	public String getOut_ip() {
		return out_ip;
	}

	public void setOut_ip(String out_ip) {
		this.out_ip = out_ip;
	}

	public String getOut_mac() {
		return out_mac;
	}

	public void setOut_mac(String out_mac) {
		this.out_mac = out_mac;
	}

	public NodePortTuple getIn_switchPort() {
		return in_switchPort;
	}

	public void setIn_switchPort(NodePortTuple in_switchPort) {
		this.in_switchPort = in_switchPort;
	}

	public NodePortTuple getOut_switchPort() {
		return out_switchPort;
	}

	public void setOut_switchPort(NodePortTuple out_switchPort) {
		this.out_switchPort = out_switchPort;
	}

	public ServiceFunctionDevice(ServiceFunction sF, String in_ip, String in_mac, String out_ip, String out_mac,
			NodePortTuple in_switchPort, NodePortTuple out_switchPort) {
		super();
		SF = sF;
		this.in_ip = in_ip;
		this.in_mac = in_mac;
		this.out_ip = out_ip;
		this.out_mac = out_mac;
		this.in_switchPort = in_switchPort;
		this.out_switchPort = out_switchPort;
	}
	/*
	 * The set of collections remove duplicate elements according to equals method and the hashCode method.
	 * So, it's necessary to override the two methods.
	 * */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ServiceFunctionDevice))
			return false;
		ServiceFunctionDevice p = (ServiceFunctionDevice)obj;

		return this.SF.equals(p.SF) && this.in_ip.equals(p.in_ip) && 
			   this.in_mac.equals(p.in_mac)&& this.out_ip.equals(p.out_ip) && this.out_mac.equals(p.out_mac)
			   && this.in_switchPort.equals(p.in_switchPort) && this.out_switchPort.equals(p.out_switchPort);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
        int result = 1;
        result = prime * result + SF.hashCode()+in_mac.hashCode()+out_mac.hashCode();
        result = prime * result + in_switchPort.hashCode()+out_switchPort.hashCode();
        return result;
		
	}

	@Override
	public String toString() {
		return "ServiceFunctionDevice [SF=" + SF + ", in_ip=" + in_ip + ", in_mac=" + in_mac + ", out_ip=" + out_ip
				+ ", out_mac=" + out_mac + ", in_switchPort=" + in_switchPort + ", out_switchPort=" + out_switchPort
				+ "]";
	}
}
