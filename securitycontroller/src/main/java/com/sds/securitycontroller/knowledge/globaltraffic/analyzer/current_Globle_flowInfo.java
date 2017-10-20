package com.sds.securitycontroller.knowledge.globaltraffic.analyzer;

import java.util.HashMap;

import com.sds.securitycontroller.knowledge.globaltraffic.MatchPath;

public class current_Globle_flowInfo {
	HashMap<String, MatchPath>  Flowfinish=new HashMap<String, MatchPath>() ;//存储结束的流
	HashMap<String, MatchPath>  Flowlast=new HashMap<String, MatchPath>() ;//存储还存在的流
	public HashMap<String,MatchPath> Flowfinish() 
	{
		return Flowfinish;
	}
	public void setFlowfinish(HashMap<String, MatchPath>flowfinish) 
	{
		this.Flowfinish =  flowfinish;
	}
	public HashMap<String,MatchPath> getFlowlast() 
	{
		return Flowlast;
	}
	public void setFlowlast(HashMap<String, MatchPath>  flowlast) 
	{
		this.Flowlast =  flowlast;
	}
	current_Globle_flowInfo(HashMap<String, MatchPath>  flowfinish,HashMap<String, MatchPath>  flowlast)
	{
		this.Flowfinish=flowfinish;
		this.Flowlast=flowlast;
	}
}