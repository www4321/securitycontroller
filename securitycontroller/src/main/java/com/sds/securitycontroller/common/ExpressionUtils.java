package com.sds.securitycontroller.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.common.BranchExpression.ExpressOperator;
import com.sds.securitycontroller.common.LeafExpression.LeafExpressionOperator;
import com.sds.securitycontroller.common.LeafExpression.LeafExpressionType;

public class ExpressionUtils {
	protected static Logger log = LoggerFactory.getLogger(ExpressionUtils.class);

	public static IExpression parseExpressiony(String exprJson) {
		IExpression expr = null;
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode root = mapper.readValue(exprJson, JsonNode.class);
			if (root.path("exprs") == null) {
				// branch expr
				List<IExpression> subExpressions = new ArrayList<IExpression>();
				ExpressOperator op = Enum.valueOf(ExpressOperator.class, root.path("op").asText());
				String exprsJson = root.path("exprs").asText(); 
				ObjectMapper mapper1 = new ObjectMapper();
				Iterator<JsonNode> exprsIter = mapper1.readValue(exprsJson, JsonNode.class).iterator();
				while(exprsIter.hasNext()){
					JsonNode exprNode = exprsIter.next();
					String subExprJson = exprNode.asText();
					IExpression subExpr = parseExpressiony(subExprJson);
					subExpressions.add(subExpr);
				}
				expr = new BranchExpression(subExpressions, op);
			} else {
				// leaf expr
				String attribute = root.path("attr").asText();
				LeafExpressionOperator op = Enum.valueOf(LeafExpressionOperator.class, root.path("op").asText());
				String valueString = root.path("value").asText();
				Object value = null;
				LeafExpressionType type = Enum
						.valueOf(LeafExpressionType.class, root.path("type")
								.textValue());
				if (type == LeafExpressionType.DOUBLE)
					value = Double.valueOf(valueString);
				else if (type == LeafExpressionType.INTEGER)
					value = Integer.valueOf(valueString);
				else
					value = valueString;
				expr = new LeafExpression(attribute, op, value);
			}
			return expr;
		} catch (Exception e) {
			log.error("Error parse policy expression: ", e);
			e.printStackTrace();
			return null;
		}
	}
}
