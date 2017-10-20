package com.sds.securitycontroller.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.log.LogQuery;
import com.sds.securitycontroller.log.Report.ReporterType;
import com.sds.securitycontroller.log.Report.TargetType;
import com.sds.securitycontroller.log.ReportItem;

public class ReportEventSubscription extends EventSubscription {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1900331360595616095L;
	protected static Logger logger = LoggerFactory.getLogger(ReportEventSubscription.class);
	
	private TargetType targetType;
	private ReporterType reporterType;
	private String details;
	private LogQuery logQuery = null;
	
	public ReportEventSubscription(String targetType,
			String reporterType, String details) {
        super();
        super.setSubscriptionType(SubscriptionType.REPORT);
        if(targetType != null){
            this.targetType = TargetType.valueOf(targetType);
        }

        if(reporterType != null){
            this.reporterType = ReporterType.valueOf(reporterType);
        }

        this.details = details;
		
	}
	
	public boolean CreateComparator(){
		ReportItem report = new ReportItem(this.details);
		if(report.DecodeLogJson() != null){
			logQuery = new LogQuery(report.getMaps());
			if(logQuery.InitComparator()){
				return true;
			}
			else
			{
				logger.error("Init log query error: {}", this.details);
				logQuery = null;
			}
			
		}
		
		return false;
	}
	
	public boolean testComparetor(){
		try{
			return this.CreateComparator();
		}
		finally{
			logQuery = null;
		}
	}
	
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public TargetType getTargetType() {
		return targetType;
	}
	public ReporterType getReporterType() {
		return reporterType;
	}
	public String getDetails() {
		return details;
	}
	
	public LogQuery getComparatorObject(){
		return this.logQuery;
	}
	
	@Override
	public String toString()
	{
		return String.format("EventSubscription: \nTargetType=%s, \nReporterType=%s, \ndetails=%s, \nSubscribedValueCategory=%s, \nSubscriptionType=%s, \nOperator=%s",
				this.targetType, this.reporterType, this.details,  this.subscribedValueCategory, this.subscriptionType, this.operator);
	}
}
