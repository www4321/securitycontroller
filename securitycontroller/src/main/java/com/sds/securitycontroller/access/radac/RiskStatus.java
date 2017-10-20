package com.sds.securitycontroller.access.radac;

public class RiskStatus {

	public class RiskLevel {
		public static final int EXTREMELY_LOW = 0;
		public static final int LOW = 1;
		public static final int MEDIUM = 2;
		public static final int HIGH = 3;
		public static final int EXTREMELY_HIGH = 4;
		public static final int UNSET = 5;
	}
	
	int subjectRisk;
	int objectRisk; 
	int envtRisk;
	int overallRisk;
	
	RiskStatus(int subjectRisk, int objectRisk, int envtRisk, int overallRisk){
		this.subjectRisk = subjectRisk;
		this.objectRisk = objectRisk;
		this.envtRisk = envtRisk;
		this.overallRisk = overallRisk;		
	}

	public int getSubjectRisk() {
		return subjectRisk;
	}

	public int getObjectRisk() {
		return objectRisk;
	}

	public int getEnvtRisk() {
		return envtRisk;
	}

	public int getOverallRisk() {
		return overallRisk;
	}
	
}
