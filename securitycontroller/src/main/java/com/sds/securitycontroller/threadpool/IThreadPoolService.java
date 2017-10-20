/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.threadpool;

import java.util.concurrent.ScheduledExecutorService;

import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IThreadPoolService extends ISecurityControllerService {
    /**
     * Get the master scheduled thread pool executor maintained by the
     * ThreadPool provider.  This can be used by other modules as a centralized
     * way to schedule tasks.
     * @return
     */
    public ScheduledExecutorService getScheduledExecutor();
}
