package com.sds.securitycontroller.cloud;

import com.sds.securitycontroller.device.DeviceFactory.DeviceType;

//handle the relations with a cloud platform
public interface ICloudPlugin {
	public int newVm(DeviceType type, StringBuffer vmid); 
	public int getIp(String vmid, StringBuffer ip);
	public int powerOn(String vmid);
	public int powerOff(String vmid);
	public int powerReset(String vmid);
	public int getStatus(String vmid);
	public int deleteVm(String vmid);
 

}

/*
 * 0: ok,
 * -1: local error(http) 
 * 1: NOT found
 * 2: other error
 * 
 * 
 */

/*
-2:method not supported 
-1:error
1: pending： 等待被创建
2: running： 运行中
3: stopped： 已关机
4: suspended： 由于欠费, 已被暂停使用
5: terminated： 已被删除, 但处于此状态的主机在2小时之内仍可以被恢复为 running 状态
6: ceased： 已被彻底删除, 处于此状态的主机无法恢复



7: creating： 创建中, 由 pending 状态变成 running 状态
8: starting： 启动中, 由 stopped 状态变成 running 状态
9: stopping： 关闭中, 由 running 状态变成 stopped 状态
10:restarting： 重启中
11:suspending： 欠费暂停中, 由 running/stopped 状态变成 suspended 状态
12:resuming： 恢复中, 由 suspended 状态变成 running 状态
13:terminating： 删除中, 由 running/stopped/suspended 状态变成 terminated 状态
14:recovering： 恢复中, 由 terminated 状态变成 running 状态
15:resetting： 操作系统重置中


*/