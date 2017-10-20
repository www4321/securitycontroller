/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.networkcontroller;

public class TopologyLink implements java.io.Serializable ,Cloneable{

	private static final long serialVersionUID = 1292509525067945624L;
	String src_switch;
	int src_port;
	String dst_switch;
	int dst_port;
	String type;
	String direction;
	int flag=0;
	
	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public TopologyLink(String src_switch,int src_port,String dst_switch,int dst_port,String type,String direction){
		this.src_switch = src_switch;
		this.src_port = src_port;
		this.dst_switch = dst_switch;
		this.dst_port = dst_port;
		this.type = type;
		this.direction = direction;
	}
	public TopologyLink(String src_switch,int src_port,String dst_switch,int dst_port,String type,String direction,int flag){
		this.src_switch = src_switch;
		this.src_port = src_port;
		this.dst_switch = dst_switch;
		this.dst_port = dst_port;
		this.type = type;
		this.direction = direction;
		this.flag=flag;
	}
	public String getSrc_switch() {
		return src_switch;
	}

	public int getSrc_port() {
		return src_port;
	}

	public String getDst_switch() {
		return dst_switch;
	}

	public int getDst_port() {
		return dst_port;
	}

	public String getType() {
		return type;
	}

	public String getDirection() {
		return direction;
	}
	@Override
    public String toString(){
     return"("+ src_switch +","+src_port+","+dst_switch+","+dst_port+","+flag+")";
       
    }
	@Override
	public Object clone() 
	  { 
	   Object o=null; 
	  try
	   { 
	   o=super.clone();//Object 中的clone()识别出你要复制的是哪一个对象。 
	   } 
	  catch(CloneNotSupportedException e) 
	   { 
	    System.out.println(e.toString()); 
	   } 
	  return o; 
	  }  
	
}
