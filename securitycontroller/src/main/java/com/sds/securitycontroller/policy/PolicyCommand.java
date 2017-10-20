package com.sds.securitycontroller.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.sds.securitycontroller.directory.ModuleCommand;

public class PolicyCommand {
	
	public static ModuleCommand getCommand(){
		ModuleCommand c;
		c = new ModuleCommand("policy_resolver");
		c.setSubcommand(new ArrayList<String>(Arrays.asList("get-commands","del-commands")));
		c.setOptions(new HashSet<String>(Arrays.asList("--ignore-cases")));
		
		return c;
		
	}

}
