package com.sds.securitycontroller.knowledge.globaltraffic.analyzer;

import java.util.HashMap;

import  com.sds.securitycontroller.event.*;
import com.sds.securitycontroller.knowledge.globaltraffic.MatchPath;


public class flowFinishEventArgs extends EventArgs
{   private static final long serialVersionUID = 4179441214260457691L;
	public HashMap<String, MatchPath>  finished_flow= new HashMap<String, MatchPath>();
	public HashMap<String, MatchPath>  last_flow= new HashMap<String, MatchPath>();
	public flowFinishEventArgs()
	{
	}
	public flowFinishEventArgs(HashMap<String, MatchPath> Finished_flow,HashMap<String, MatchPath> Last_flow)
	{
	    this.finished_flow =  Finished_flow;
	    this.last_flow=Last_flow;
	}
	public HashMap<String,MatchPath> getlast_flow() 
	{
		return last_flow;
	}
	public void setlast_flow(HashMap<String, MatchPath>  Last_flow) 
	{
		this.last_flow =  Last_flow;
	}
	public HashMap<String,MatchPath> getfinished_flow() 
	{
		return finished_flow;
	}
	public void setfinished_flow(HashMap<String, MatchPath>  Finished_flow) 
	{
		this.finished_flow =  Finished_flow;
	}


}