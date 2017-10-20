package com.sds.securitycontroller.knowledge.globaltraffic;

import java.io.Serializable;

public class NodePortTuplePath implements Serializable{
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1678888888888888L;
	protected String nodeId; // switch DPID
	 protected short in_portId; // switch port id
	 protected short out_portId;
	 public NodePortTuplePath(String nodeId, int in_portId,int out_portId) {
	        this.nodeId = nodeId;
	        this.in_portId = (short) in_portId;
	        this.out_portId = (short) out_portId;
	    } 
	 public String getNodeId() {
	        return nodeId;
	    }
	    public void setNodeId(String nodeId) {
	        this.nodeId = nodeId;
	    }
	    
	    public short getIn_portId() {
			return in_portId;
		}
		public void setIn_portId(short in_portId) {
			this.in_portId = in_portId;
		}
		public short getOut_portId() {
			return out_portId;
		}
		public void setOut_portId(short out_portId) {
			this.out_portId = out_portId;
		}
		@Override
	    public int hashCode() {
	        final int prime = 31;
	        int result = 1;
	        result = prime * result + nodeId.hashCode();
	        result = prime * result + in_portId+out_portId;
	        return result;
	    }

	    @Override
		public boolean equals(Object obj)
	   {
	   if(obj instanceof NodePortTuplePath)
	   {
	    if(this.in_portId==(((NodePortTuplePath)obj).getIn_portId())&&this.nodeId.equals(((NodePortTuplePath)obj).getNodeId())
	    		&&this.out_portId==(((NodePortTuplePath)obj).getOut_portId()))
	       return true;
	   }
	   return false;
	   }
	    
	public NodePortTuple toNodePortTuple(){
		return new NodePortTuple(this.getNodeId(),this.getOut_portId());
	}
	 
	    @Override
	    public String toString(){
	     return"("+ nodeId +","+in_portId+","+out_portId+")";
	       
	    }
	   
	}
