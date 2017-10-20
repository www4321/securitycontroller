/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.log;

import java.util.List;

import com.sds.securitycontroller.event.EventArgs;

public class ReportEventArgs  extends EventArgs{

	private static final long serialVersionUID = 801208652545058941L;
	public Report report;
	public List<ReportItem> reportList;
	
	public ReportEventArgs(Report report){
		this.report = report;
	}

	public ReportEventArgs(List<ReportItem> reportItems){
		this.reportList = reportItems;
	}
}