package com.sds.securitycontroller.TaskManager;

import com.sds.securitycontroller.event.EventArgs;

public class TaskEventArgs extends EventArgs{
	private static final long serialVersionUID = 801208652545058940L;
	private MyTask task;
	public MyTask getOrder() {
		return task;
	}
	public void setOrder(MyTask task) {
		this.task = task;
	}
	public TaskEventArgs(MyTask task)
	{
		this.task=task;
	}

}
