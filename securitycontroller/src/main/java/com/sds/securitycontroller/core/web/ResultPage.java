package com.sds.securitycontroller.core.web;

import java.util.List;

public class ResultPage<T> {
	public List<T> allList;
	public int allCount;
	public List<T> getAllList() {
		return allList;
	}
	public void setAllList(List<T> allList) {
		this.allList = allList;
	}
	public int getAllCount() {
		return allCount;
	}
	public void setAllCount(int allCount) {
		this.allCount = allCount;
	}
	
}
