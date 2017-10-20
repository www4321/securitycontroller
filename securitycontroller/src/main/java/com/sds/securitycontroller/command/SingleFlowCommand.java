package com.sds.securitycontroller.command;

import java.io.UnsupportedEncodingException;

import com.sds.securitycontroller.utils.Cypher;

public class SingleFlowCommand extends FlowCommandBase {

	/**
	 * 
	 */
	public SingleFlowCommand(){
		super();
	}
	
	private static final long serialVersionUID = 1L;
//	String swIPAddress;
//	String inPortName;
	
	long dpid;
	short inPort;
	
	public SingleFlowCommand(String id, String flowName, short priority,
			MatchArguments matchArguments, long idleTimeout, long hardTimeout,
			long dpid, short inport) {
		super(id, flowName, priority, matchArguments, idleTimeout, hardTimeout);
		this.dpid = dpid;
		this.inPort = inport;
	}

	@Override
	public String generateId(){
		if(this.id == null){
			String raw = ""+ this.matchArguments.getInputPort() +
					this.matchArguments.getDataLayerDestination()+
					this.matchArguments.getDataLayerSource()+
					this.matchArguments.getDataLayerType()+
					this.matchArguments.getNetworkDestination()+
					this.matchArguments.getNetworkSource()+
					this.matchArguments.getNetworkProtocol()+
					this.matchArguments.getTransportDestination()+
					this.matchArguments.getTransportSource()+
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
	
	public long getDpid() {
		return dpid;
	}
	public void setDpid(long dpid) {
		this.dpid = dpid;
	}
	public int getInPort() {
		return inPort;
	}
	public void setInPort(short inport) {
		this.inPort = inport;
	}
}
