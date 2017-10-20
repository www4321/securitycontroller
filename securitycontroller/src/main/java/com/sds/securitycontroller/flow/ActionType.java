/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.flow;


public enum ActionType {
    OUTPUT,
    SET_VLAN_ID,
    SET_VLAN_PCP,
    STRIP_VLAN,
    SET_DL_SRC,
    SET_DL_DST,
    SET_NW_SRC,
    SET_NW_DST,
    SET_NW_TOS,
    SET_TP_SRC,
    SET_TP_DST,
    OPAQUE_ENQUEUE,
    VENDOR,
}
