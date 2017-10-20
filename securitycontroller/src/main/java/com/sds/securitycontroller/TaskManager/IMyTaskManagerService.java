package com.sds.securitycontroller.TaskManager;


import java.util.List;
import java.util.Map;

import com.sds.securitycontroller.module.ISecurityControllerService;

public interface IMyTaskManagerService extends ISecurityControllerService{
	public boolean addOrder(MyTask aorder);
	public boolean removeOrder(MyTask aorder);	
	public MyTask getOrder(String id);//get an order by it's id
	public List<MyTask> getAllOrders(String from, int size);
	public List<MyTask> getAllOrders();
	public boolean updateMyOrder(String id, Map<String,Object> values);

		


}
