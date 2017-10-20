package com.sds.securitycontroller.common;

import java.util.Map;

public interface IExpression {
	
	
	//positive for being complied
	//0 for not found,
	//negative for not being complied
	MatchResult match(Map<String, Object> object) throws Exception;

}
