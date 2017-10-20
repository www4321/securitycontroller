package com.sds.securitycontroller.command;

import java.io.Serializable;

public class RestoreCommand extends FlowCommandBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5857228308251590567L;
	public RestoreCommand(String id){
		this.id=id;
	}

}
