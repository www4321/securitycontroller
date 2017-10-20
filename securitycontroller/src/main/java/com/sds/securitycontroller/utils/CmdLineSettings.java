/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.utils;

import org.kohsuke.args4j.Option;

/**
 * Expresses the port settings of OpenFlow controller.
 */
public class CmdLineSettings {
    public static final String DEFAULT_CONFIG_FILE = "securitycontroller.default.properties";

    @Option(name="-cf", aliases="--configFile", metaVar="FILE", usage="Security controller configuration file")
    private String configFile = DEFAULT_CONFIG_FILE;
    
    public String getModuleFile() {
    	return configFile;
    }
}
