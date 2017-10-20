package com.sds.securitycontroller.access.manager;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sds.securitycontroller.access.manager.Policy.PolicyResult;
import com.sds.securitycontroller.access.manager.Policy.PolicyType;
import com.sds.securitycontroller.access.radac.RiskControlPolicy;
import com.sds.securitycontroller.app.manager.AppManagerResource;
import com.sds.securitycontroller.common.ExpressionUtils;
import com.sds.securitycontroller.common.IExpression;
import com.sds.securitycontroller.utils.JsonRequest;
import com.sds.securitycontroller.utils.JsonResponse;

public class AccessControlPolicyManageResource extends ServerResource {
	protected static Logger log = LoggerFactory
			.getLogger(AppManagerResource.class);
	String policyId = null;
	IAccessControlManagementService aclManager = null;
	JsonRequest request = null;
	JsonResponse response = new JsonResponse();

	@Override
	public void doInit() {
		aclManager = (IAccessControlManagementService) getContext()
				.getAttributes().get(
						IAccessControlManagementService.class
								.getCanonicalName());
		policyId = (String) getRequestAttributes().get("acl");
	}

	@Get("json")
	public Object handleGetRequest() {

		JsonFactory jasonFactory = new JsonFactory();
		StringWriter writer = new StringWriter();
		if (this.policyId != null) {
			// return an acl
			try {
				JsonGenerator generator = jasonFactory.createGenerator(writer);
				generator.writeStartObject();
				generator.writeStringField("status", "ok");

				Policy policy = this.aclManager.getACLPolicy(policyId);

				generator.writeObjectFieldStart("policy");
				Map<String, Object> elements = policy.getDBElements();
				for (Entry<String, Object> element : elements.entrySet()) {
					generator.writeStringField(element.getKey(),
							(String) element.getValue());
				}
				generator.writeEndObject();
				generator.writeEndObject();
				generator.close();
			} catch (IOException e) {
				log.error("json conversion failed: ", e.getMessage());
				return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "
						+ e.getMessage() + " \"}";
			} catch (Exception e) {
				log.error("getting acl failed: ", e.getMessage());
				return "{\"status\" : \"error\", \"result\" : \"getting acl failed: "
						+ e.getMessage() + "\"}";
			}
		} else {
			// return all acls
			try {
				JsonGenerator generator = jasonFactory.createGenerator(writer);
				generator.writeStartObject();
				generator.writeStringField("status", "ok");

				List<Policy> policies = this.aclManager.getACLPolicies();
				generator.writeArrayFieldStart("policies");

				for (Policy policy : policies) {
					generator.writeObjectFieldStart("policy");
					Map<String, Object> elements = policy.getDBElements();
					for (Entry<String, Object> element : elements.entrySet()) {
						generator.writeStringField(element.getKey(),
								(String) element.getValue());

					}
					generator.writeEndObject();
				}
				generator.writeEndArray();
				generator.writeEndObject();
				generator.close();
			} catch (IOException e) {
				log.error("json conversion failed: ", e.getMessage());
				return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "
						+ e.getMessage() + " \"}";
			} catch (Exception e) {
				log.error("getting acl failed: ", e.getMessage());
				return "{\"status\" : \"error\", \"result\" : \"getting acl failed: "
						+ e.getMessage() + "\"}";
			}
		}
		return writer.toString();
	}

	@Post
	public String handlePostRequest(String fmJson) {

		String status = "";
		String errResult = "";
		Policy policy = null;

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {

			JsonNode root = mapper.readValue(fmJson, JsonNode.class);
			String subjectExprJson = root.path("subject_expr").asText();
			String objectExprJson = root.path("objec_exprt").asText();
			String attrsJson = root.path("attrs").asText();
			String resultJson = root.path("result").asText();
			String typeJson = root.path("type").asText();
			PolicyResult result = Enum.valueOf(PolicyResult.class, resultJson);
			PolicyType type = Enum.valueOf(PolicyType.class, typeJson);

			IExpression subjectExpr = ExpressionUtils
					.parseExpressiony(subjectExprJson);
			IExpression objectExpr = ExpressionUtils
					.parseExpressiony(objectExprJson);

			ObjectMapper mapper1 = new ObjectMapper();
			try {
				Long uuid = UUID.randomUUID().getMostSignificantBits();
				JsonNode attrsNode = mapper1.readValue(attrsJson,
						JsonNode.class);

				if (type == PolicyType.ACCESS) {
					SubjectOperation op = Enum.valueOf(SubjectOperation.class,
							attrsNode.path("sbj_op").asText());
					policy = new AccessControlPolicy(uuid.toString(),
							subjectExpr, objectExpr, op, result);
				} else if (type == PolicyType.RISK) {
					boolean override = attrsNode.path("override").asBoolean();
					boolean requireAuth = attrsNode.path("req_auth")
							.asBoolean();

					Map<String, Integer> risks = new HashMap<String, Integer>();
					Iterator<Entry<String, JsonNode>> iter = root.path(
							"accepted_risks").fields();
					while (iter.hasNext()) {
						Entry<String, JsonNode> entry = iter.next();
						risks.put(entry.getKey(), entry.getValue().asInt());
					}

					policy = new RiskControlPolicy(uuid.toString(),
							subjectExpr, objectExpr, result, override,
							requireAuth, risks);
				}

			} catch (Exception e) {
				log.error("Error parse access policy: ", e);
				e.printStackTrace();
			}

			boolean added = aclManager.addACLPolicy(policy);
			status = added ? "ok" : "failed";

		} catch (IOException e) {
			log.error("Error creating new device: " + fmJson, e);
			e.printStackTrace();
			status = "error";
			errResult = e.getMessage();
		} catch (Exception e) {
			log.error("Error creating new device: ", e);
			e.printStackTrace();
			status = "error";
			errResult = e.getMessage();
		}

		JsonFactory jasonFactory = new JsonFactory();
		StringWriter writer = new StringWriter();
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject();
			generator.writeStringField("status", status);
			if (policy != null) {
				generator.writeObjectFieldStart("result");
				generator.writeObjectFieldStart("policy");
				Map<String, Object> elements = policy.getDBElements();
				for (Entry<String, Object> element : elements.entrySet()) {
					generator.writeStringField(element.getKey(),
							(String) element.getValue());
				}
				generator.writeEndObject();
				generator.writeEndObject();
			} else
				generator.writeStringField("result", errResult);
			generator.writeEndObject();
			generator.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "{\"status\" : \"error\", \"result\" : \"json conversion failed. \"}";
		}
		return writer.toString();
	}

	@Put
	public String handlePutRequest(String fmJson) {

		String status = "";
		String errResult = "";
		Policy policy = null;

		if (this.policyId == null)
			return "{\"status\" : \"error\", \"result\" : \"policy id missing. \"}";

		policy = aclManager.getACLPolicy(this.policyId);
		if (policy == null)
			return "{\"status\" : \"error\", \"result\" : \"policy not found. \"}";

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {
			JsonNode root = mapper.readValue(fmJson, JsonNode.class);
			String subjectExprJson = root.path("subject_expr").asText();
			if (subjectExprJson != null) {
				IExpression subjectExpr = ExpressionUtils
						.parseExpressiony(subjectExprJson);
				policy.subjectExpression = subjectExpr;
			}

			String objectExprJson = root.path("objec_exprt").asText();
			if (objectExprJson != null) {
				IExpression objectExpr = ExpressionUtils
						.parseExpressiony(objectExprJson);
				policy.objectExpression = objectExpr;
			}
			String resultJson = root.path("result").asText();
			if (resultJson != null) {
				PolicyResult result = Enum.valueOf(PolicyResult.class,
						resultJson);
				policy.result = result;
			}

			String typeJson = root.path("type").asText();
			if (typeJson != null) {
				PolicyType type = Enum.valueOf(PolicyType.class, typeJson);
				policy.type = type;
			}

			String attrsJson = root.path("attrs").asText();
			if (attrsJson != null) {
				ObjectMapper mapper1 = new ObjectMapper();
				try {
					JsonNode attrsNode = mapper1.readValue(attrsJson,
							JsonNode.class);

					if (policy.type == PolicyType.ACCESS) {
						AccessControlPolicy ap = (AccessControlPolicy) policy;
						if (attrsNode.path("sbj_op").asText() != null) {
							ap.subjectOperation = Enum.valueOf(
									SubjectOperation.class,
									attrsNode.path("sbj_op").asText());
						}

					} else if (policy.type == PolicyType.RISK) {
						RiskControlPolicy rp = (RiskControlPolicy) policy;
						if (attrsNode.path("override") != null)
							rp.setAllowOverride(attrsNode.path("override")
									.asBoolean());
						if (attrsNode.path("req_auth") != null)
							rp.setRequireVerification(attrsNode
									.path("req_auth").asBoolean());

						if (attrsNode.path("accepted_risks") != null) {
							Map<String, Integer> acceptedRisks = new HashMap<String, Integer>();
							Iterator<Entry<String, JsonNode>> iter = root.path(
									"accepted_risks").fields();
							while (iter.hasNext()) {
								Entry<String, JsonNode> entry = iter.next();
								acceptedRisks.put(entry.getKey(), entry
										.getValue().asInt());
							}
							rp.setAcceptedRisks(acceptedRisks);
						}

					}

				} catch (Exception e) {
					log.error("Error parse access policy: ", e);
					e.printStackTrace();
				}
			}
			boolean updated = aclManager.updateACLPolicy(policy);
			status = updated ? "ok" : "error";

		} catch (IOException e) {
			log.error("Error updating policy: " + fmJson, e);
			e.printStackTrace();
			status = "error";
			errResult = e.getMessage();
		} catch (Exception e) {
			log.error("Error updating policy: ", e);
			e.printStackTrace();
			status = "error";
			errResult = e.getMessage();
		}

		JsonFactory jasonFactory = new JsonFactory();
		StringWriter writer = new StringWriter();
		try {
			JsonGenerator generator = jasonFactory.createGenerator(writer);
			generator.writeStartObject();
			generator.writeStringField("status", status);
			if (status.equals("ok")) {
				generator.writeObjectFieldStart("result");
				generator.writeObjectFieldStart("policy");
				Map<String, Object> elements = policy.getDBElements();
				for (Entry<String, Object> element : elements.entrySet()) {
					generator.writeStringField(element.getKey(),
							(String) element.getValue());
				}
				generator.writeEndObject();
				generator.writeEndObject();
			} else
				generator.writeStringField("result", errResult);
			generator.writeEndObject();
			generator.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "{\"status\" : \"error\", \"result\" : \"json conversion failed. \"}";
		}
		return writer.toString();
	}

	@Delete("json")
	public Object handleDeleteRequest() {

		JsonFactory jasonFactory = new JsonFactory();
		StringWriter writer = new StringWriter();
		if (this.policyId != null) {
			try {
				JsonGenerator generator = jasonFactory.createGenerator(writer);
				generator.writeStartObject();
				boolean deleted = aclManager.removeACLPolicy(policyId);
				if (deleted)
					generator.writeStringField("status", "ok");
				else
					generator.writeStringField("status", "error");
				generator.writeEndObject();
				generator.close();
			} catch (IOException e) {
				log.error("json conversion failed: ", e.getMessage());
				return "{\"status\" : \"error\", \"result\" : \"json conversion failed: "
						+ e.getMessage() + " \"}";
			} catch (Exception e) {
				log.error("deleting acl failed: ", e.getMessage());
				return "{\"status\" : \"error\", \"result\" : \"deleting acl failed: "
						+ e.getMessage() + "\"}";
			}
		}
		return writer.toString();
	}

}
