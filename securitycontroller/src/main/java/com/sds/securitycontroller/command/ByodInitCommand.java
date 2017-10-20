package com.sds.securitycontroller.command;

import java.io.UnsupportedEncodingException;

import com.sds.securitycontroller.utils.Cypher;

public class ByodInitCommand extends FlowCommandBase {

	private static final long serialVersionUID = -7837328124912961795L;


	public ByodInitCommand(long dpid, short inPort, String serverIp,
			String serverMac, String network, short mask) {
		super();
		this.dpid = dpid;
		this.inPort = inPort;
		this.serverIp = serverIp;
		this.serverMac = serverMac;
		this.network = network;
		this.mask = mask;
	}


	long dpid;
	short inPort;
	String serverIp;
	String serverMac;
	String network;
	short mask;

	@Override
	public String generateId(){
		if(this.id == null){
			String raw = ""+ this.dpid +
					this.inPort;
			byte[] rawbytes = null;
			try {
				rawbytes = raw.getBytes("UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			this.id = Cypher.getMD5(rawbytes);
		}
		return this.id;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


	public long getDpid() {
		return dpid;
	}


	public void setDpid(long dpid) {
		this.dpid = dpid;
	}




	public String getServerIp() {
		return serverIp;
	}


	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}


	public String getServerMac() {
		return serverMac;
	}


	public void setServerMac(String serverMac) {
		this.serverMac = serverMac;
	}


	public String getNetwork() {
		return network;
	}


	public void setNetwork(String network) {
		this.network = network;
	}


	public short getMask() {
		return mask;
	}


	public void setMask(short mask) {
		this.mask = mask;
	}


	public short getInPort() {
		return inPort;
	}


	public void setInPort(short inPort) {
		this.inPort = inPort;
	}

}
