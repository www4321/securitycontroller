#!/bin/sh
jarfile="target/securitycontroller-storm-1.1.jar"
#jarfile="jars/securitycontroller-storm-fm-ADS-built-in-1.11.jar"
#tZZjarfile="securitycontroller-storm-fm-ADS-built-in-1.1.jar"
#jarfile="scstorm.jar"
configfile=config/securitycontroller.default.properties.storm-fm
/usr/src/storm-0.8.2/bin/storm jar $jarfile  com.sds.securitycontroller.core.Main -cf $configfile
