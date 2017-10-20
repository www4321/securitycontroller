package com.sds.securitycontroller.log.manager;

import java.text.SimpleDateFormat;

import com.sds.securitycontroller.event.EventArgs;

public class ConsoleLogEventArgs extends EventArgs {
		
	private static final long serialVersionUID = -8509986760668289351L;
	private String hostName;
	private String logString;
	private String module;
	private int lineNo;
	private LogLevel logLevel;
	
	public ConsoleLogEventArgs(LogLevel logLevel,String hostName, String module,
			 String logString ) {
		super();
		this.hostName = hostName;
		this.logString = logString;
		this.module = module;
		this.logLevel = logLevel;
	}
	
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getLogString() {
		return logString;
	}
	public void setLogString(String logString) {
		this.logString = logString;
	}
	public LogLevel getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public int getLine() {
		return lineNo;
	}
	public void setLine(int line) {
		this.lineNo = line;
	}
	@Override
	public String toString() {
		 SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		return  formatter.format(time)+" " + logLevel + " "
				+ hostName + "->" + module + ": " + logString;
	}
}
