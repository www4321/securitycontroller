package com.sds.securitycontroller.data;

public class FeildItem {
	protected String type;
	protected String correspondence;
	
	//public FeildItem(){};
	public FeildItem( String type, String co){
		this.type = type;
		this.correspondence = co;
	};
	
	
	public String getType(){
		return this.type;
	}
	
	public String getCorrespondence(){
		return this.correspondence;
	}
}
