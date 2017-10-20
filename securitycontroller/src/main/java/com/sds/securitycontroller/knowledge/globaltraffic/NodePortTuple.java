package com.sds.securitycontroller.knowledge.globaltraffic;

import java.io.Serializable;

public class NodePortTuple implements Serializable{
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1112414L;
	protected String nodeId; // switch DPID
	 protected short portId; // switch port id
	 
	 public NodePortTuple(String nodeId, int portId) {
	        this.nodeId = nodeId;
	        this.portId = (short) portId;
	    } 
	 public String getNodeId() {
	        return nodeId;
	    }
	    public void setNodeId(String nodeId) {
	        this.nodeId = nodeId;
	    }
	    public int getPortId() {
	        return portId;
	    }
	    public void setPortId(short portId) {
	        this.portId = portId;
	    
	    }
	    @Override
	    public int hashCode() {
	        final int prime = 31;
	        int result = 1;
	        result = prime * result + nodeId.hashCode();
	        result = prime * result + portId;
	        return result;
	    }

	    @Override
		public boolean equals(Object obj){
	    	if(obj instanceof NodePortTuple){
	    		if(this.portId==(((NodePortTuple)obj).getPortId())&&this.nodeId.equals(((NodePortTuple)obj).getNodeId()))
	    			return true;
	    	}
	    	return false;
	   }
		
	 
	    @Override
	    public String toString(){
	     return"("+ nodeId +","+portId+")";
	       
	    }
	   
	}

