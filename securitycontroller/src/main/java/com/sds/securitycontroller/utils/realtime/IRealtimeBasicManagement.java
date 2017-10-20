package com.sds.securitycontroller.utils.realtime;

import java.util.List;

public interface IRealtimeBasicManagement {
    public boolean 				addRealtimeBasic(String obj_id, String type);
	public boolean 				updateRealtimeBasic(String obj_id, String type, RealtimeBasic rb);
	public boolean 				removeRealtimeBasic(String obj_id, String type); 	
	public RealtimeBasic 		getRealtimeBasic(String obj_id, String type);
	public List<RealtimeBasic>  		getAllRealtimeBasic(String type);
	
}
