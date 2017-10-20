package com.sds.securitycontroller.command;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import com.sds.securitycontroller.utils.Cypher;

public class FlowCommandBase implements Serializable {

	private static final long serialVersionUID = 1L;
	protected String id;
	protected String commandName;
	protected short commandPriority;
	protected MatchArguments matchArguments;
	protected long idleTimeout;
	protected long hardTimeout;	
	
	public FlowCommandBase() {
		super();
	}
	
	public FlowCommandBase(String id, String flowName, short priority,
			MatchArguments matchArguments, long idleTimeout, long hardTimeout) {
		super();
		this.id = id;
		this.commandName = flowName;
		this.commandPriority = priority;
		this.matchArguments = matchArguments;
		this.idleTimeout = idleTimeout;
		this.hardTimeout = hardTimeout;
	}

	public MatchArguments getMatchArguments() {
		return matchArguments;
	}
	public void setMatchArguments(MatchArguments matchArguments) {
		this.matchArguments = matchArguments;
	}
	public String getId() {
		return id;
	}
	
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
					this.matchArguments.getTransportSource();
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
	
	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public int getCommandPriority() {
		return commandPriority;
	}

	public void setCommandPriority(short commandPriority) {
		this.commandPriority = commandPriority;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getFlowName() {
		return commandName;
	}
	public void setFlowName(String flowName) {
		this.commandName = flowName;
	}

	

	public long getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public long getHardTimeout() {
		return hardTimeout;
	}

	public void setHardTimeout(long hardTimeout) {
		this.hardTimeout = hardTimeout;
	}
	
}
