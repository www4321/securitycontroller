/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.core;


import java.util.concurrent.ConcurrentHashMap;

public class SecurityControllerContext {
    protected ConcurrentHashMap<String, Object> storage =
            new ConcurrentHashMap<String, Object>();

    public ConcurrentHashMap<String, Object> getStorage() {
        return storage;
    }
}
