package com.sds.securitycontroller.access.radac;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.access.manager.AccessControlContext;
import com.sds.securitycontroller.access.manager.AccessControlPolicy;
import com.sds.securitycontroller.access.manager.Policy;
import com.sds.securitycontroller.access.manager.Policy.PolicyResult;
import com.sds.securitycontroller.access.manager.SubjectOperation;
import com.sds.securitycontroller.access.radac.RiskStatus.RiskLevel;
import com.sds.securitycontroller.asset.Asset;
import com.sds.securitycontroller.asset.Asset.AssetLevel;
import com.sds.securitycontroller.asset.manager.IAssetManagerService;
import com.sds.securitycontroller.common.Entity;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.reputation.Reputation.ReputationLevel;
import com.sds.securitycontroller.reputation.ReputationEntity;
import com.sds.securitycontroller.reputation.manager.IReputationManagementService;

public class RAdACManager {
	private static RAdACManager instance = null;
	List<AccessControlPolicy> accessControlPolicies = null;
	List<RiskControlPolicy> riskControlPolicies = null;
	PolicyResult defaultPolicyResult = PolicyResult.DENY;

	SecurityControllerModuleContext moduleContext;
	protected static Logger log = LoggerFactory.getLogger(RAdACManager.class);

	private RAdACManager(SecurityControllerModuleContext context,
			List<Policy> policies) {
		this.moduleContext = context;
		this.accessControlPolicies = new ArrayList<AccessControlPolicy>();
		this.riskControlPolicies = new ArrayList<RiskControlPolicy>();
		for (Policy policy : policies) {
			if (policy instanceof AccessControlPolicy) {
				this.accessControlPolicies.add((AccessControlPolicy) policy);
			} else if (policy instanceof RiskControlPolicy) {
				this.riskControlPolicies.add((RiskControlPolicy) policy);
			} else
				log.error("Unknown policy type: " + policy.getClass());
		}
	}

	public static RAdACManager getInstance(
			SecurityControllerModuleContext context, List<Policy> policies) {
		if (instance == null)
			instance = new RAdACManager(context, policies);
		return instance;
	}

	public boolean allowAccess(Entity subject, Entity object,
			SubjectOperation operation, AccessControlContext context)
			throws Exception {

		IReputationManagementService reputationManager = moduleContext
				.getServiceImpl(IReputationManagementService.class, this);
		ReputationEntity subjectReputation = reputationManager
				.getReputationEntity(subject.getId());

		IAssetManagerService assetManager = moduleContext.getServiceImpl(
				IAssetManagerService.class, this);
		Asset assetEntity = assetManager.getFlowAssetEntity(subject, object);


		// evaluate the risk
		RiskStatus risk = evaluateRisk(subjectReputation, assetEntity,
				context);

		PolicyResult complied = PolicyResult.DEFAULT;
		for (RiskControlPolicy policy : this.riskControlPolicies) {
			complied = policy.compliedWith(subject, object, risk);

			if (complied == PolicyResult.DENY && !policy.getAllowOverride()) {
				log.info("Operation denied for {} {} {}", subject, operation,
						object);
				break;
			} else if (complied == PolicyResult.ALLOW
					&& !policy.getRequireVerification()) {
				log.info("Operation allow for {} {} {}", subject, operation,
						object);
				break;
			}
			if (complied == PolicyResult.DEFAULT)
				continue;
		}

		for (AccessControlPolicy policy : this.accessControlPolicies) {
			complied = policy.compliedWith(subject, object, operation, risk);

			if (complied == PolicyResult.DENY) {
				log.info("Operation denied for {} {} {}", subject, operation,
						object);
				break;
			} else if (complied == PolicyResult.ALLOW) {
				log.info("Operation allow for {} {} {}", subject, operation,
						object);
				break;
			}
			if (complied == PolicyResult.DEFAULT)
				continue;
		}

		if (complied == PolicyResult.DEFAULT)
			complied = this.defaultPolicyResult;
		if (complied == PolicyResult.ALLOW)
			return true;
		else
			return false;
	}

	private RiskStatus evaluateRisk(ReputationEntity subjectReputation,
			Asset assetEntity, AccessControlContext context) {
		RiskStatus riskStatus = null;
		//Object subject = subjectReputation.getSourceObject();
		ReputationLevel subjectReputationLevel = subjectReputation
				.getReputationLevel();
		AssetLevel assetLevel = assetEntity.getLevel();
		
		double subjectFactor = 0.3f;
		if(subjectReputationLevel == ReputationLevel.HIGHLY_TRUSTED)
			subjectFactor = 1.0f;
		else if(subjectReputationLevel == ReputationLevel.FAIR_TRUSTED)
			subjectFactor = 0.7f;
		else if(subjectReputationLevel == ReputationLevel.LOW_TRUSTED)
			subjectFactor = 0.5f;
		else if(subjectReputationLevel == ReputationLevel.UNTRUSTED)
			subjectFactor = 0.3f;
		int subjectRisk = RiskLevel.EXTREMELY_LOW;
		if(subjectFactor >0.8f)
			subjectRisk = RiskLevel.EXTREMELY_LOW;
		else if(subjectFactor >0.6f)
			subjectRisk = RiskLevel.LOW;
		else if(subjectFactor >0.45f)
			subjectRisk = RiskLevel.MEDIUM;
		else if(subjectFactor >0.2f)
			subjectRisk = RiskLevel.HIGH;
		else
			subjectRisk = RiskLevel.EXTREMELY_HIGH;
		
		
		double assetFactor = 0.3f;
		if(assetLevel == AssetLevel.HIGH)
			assetFactor = 0.6f;
		else if(assetLevel == AssetLevel.MEDIUM)
			assetFactor = 0.8f;
		else if(assetLevel == AssetLevel.LOW)
			assetFactor = 1.0f;
		int assetRisk = RiskLevel.EXTREMELY_LOW;
		//如果有漏洞库，可以在此增加一个因素
		if(assetFactor >0.8f)
			assetRisk = RiskLevel.EXTREMELY_LOW;
		else if(assetFactor >0.6f)
			assetRisk = RiskLevel.LOW;
		else if(assetFactor >0.45f)
			assetRisk = RiskLevel.MEDIUM;
		else if(assetFactor >0.2f)
			assetRisk = RiskLevel.HIGH;
		else
			assetRisk = RiskLevel.EXTREMELY_HIGH;
		
		//TODO,此处有环境的因素
		int envRisk = RiskLevel.EXTREMELY_LOW;

		double riskValue = subjectFactor*assetFactor;
		int overallRisk = RiskLevel.EXTREMELY_LOW;
		if(riskValue >0.8f)
			overallRisk = RiskLevel.EXTREMELY_HIGH;
		else if(riskValue >0.6f)
			overallRisk = RiskLevel.HIGH;
		else if(riskValue >0.45f)
			overallRisk = RiskLevel.MEDIUM;
		else if(riskValue >0.2f)
			overallRisk = RiskLevel.LOW;
		else
			overallRisk = RiskLevel.EXTREMELY_LOW;
		
		riskStatus = new RiskStatus(subjectRisk, assetRisk, envRisk, overallRisk);
		return riskStatus;
	}

}
