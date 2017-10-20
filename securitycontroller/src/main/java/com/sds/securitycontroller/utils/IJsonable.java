package com.sds.securitycontroller.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;

public interface IJsonable {
	public String toJsonString() throws JsonGenerationException, IOException;

 
 
}
