package com.sds.securitycontroller.utils.realtime;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sds.securitycontroller.app.AppRealtimeInfo;
import com.sds.securitycontroller.storage.IAbstractResultSet;
import com.sds.securitycontroller.storage.IDBObject;

public class RealtimeBasic implements IDBObject {
	private static final long serialVersionUID = 7489398638341839475L;
	protected SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	protected static Logger log = LoggerFactory
			.getLogger(AppRealtimeInfo.class);
	protected static Map<String, Method> dbFieldMapping;

	protected String obj_id = null;
	protected String type = "APP";
	protected String state = null;
	protected int update_time = 0;
	protected int start_time = 0;
	private int cpu = 0;
	private int memory_used = 0;
	private int memory_total = 0;
	private int disk_used = 0;
	private int disk_total = 0;

	public RealtimeBasic(String obj_id, String type,
			String state,
			int update_time, // seconds, Date().getTime()/1000
			int start_time, int cpu, int memory_used, int memory_total,
			int disk_used, int disk_total) {
		this.setObj_id(obj_id);
		this.setType(type);
		this.setState(state);
		this.setUpdate_time(update_time);
		this.start_time = start_time;
		this.setCpu(cpu);
		this.setMemory_used(memory_used);
		this.setMemory_total(memory_total);
		this.setDisk_used(disk_used);
		this.setDisk_total(disk_total);

	}

	public RealtimeBasic(RealtimeBasic rb) {
		this.setObj_id(rb.obj_id);
		this.setType(rb.type);
		this.setState(rb.state);
		this.setUpdate_time(rb.update_time);
		this.start_time = rb.start_time;
		this.setCpu(rb.getCpu());
		this.setMemory_used(rb.getMemory_used());
		this.setMemory_total(rb.getMemory_total());
		this.setDisk_used(rb.getDisk_used());
		this.setDisk_total(rb.getDisk_total());

	}

	public RealtimeBasic() {
		this("", "", "INIT", 0, 0, 0, 0, 0, 0, 0);
	}

	@Override
	@JsonIgnore
	public Map<String, Object> getDBElements() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("obj_id", this.getObj_id());
		map.put("type", this.getType());
		map.put("state", this.getState());
		map.put("update_time", this.getUpdate_time());
		map.put("start_time", this.getStart_time());
		map.put("cpu", this.getCpu());
		map.put("memory_used", this.getMemory_used());
		map.put("memory_total", this.getMemory_total());
		map.put("disk_used", this.getDisk_used());
		map.put("disk_total", this.getDisk_total());

		return map;
	}

	@Override
	@JsonIgnore
	public Object getFieldValueByKey(String key) {
		if (dbFieldMapping == null) {
			dbFieldMapping = new HashMap<String, Method>();
			Class<? extends RealtimeBasic> cla = this.getClass();
			try {
				dbFieldMapping.put("obj_id", cla.getDeclaredMethod("obj_id"));
				dbFieldMapping.put("type", cla.getDeclaredMethod("type"));
				dbFieldMapping.put("state", cla.getDeclaredMethod("state"));
				dbFieldMapping.put("update_time",
						cla.getDeclaredMethod("update_time"));
				dbFieldMapping.put("start_time",
						cla.getDeclaredMethod("start_time"));
				dbFieldMapping.put("cpu", cla.getDeclaredMethod("cpu"));
				dbFieldMapping.put("memory_used",
						cla.getDeclaredMethod("memory_used"));
				dbFieldMapping.put("memory_total",
						cla.getDeclaredMethod("memory_total"));
				dbFieldMapping.put("disk_used",
						cla.getDeclaredMethod("disk_used"));
				dbFieldMapping.put("disk_total",
						cla.getDeclaredMethod("disk_total"));

			} catch (NoSuchMethodException | SecurityException e) {
				log.error("getFieldValueByKeys error: " + e.getMessage());
				return null;
			}
		}
		Method m = dbFieldMapping.get(key);
		try {
			return m.invoke(this, new Object[0]);
		} catch (Exception e) {
			log.error("getFieldValueByKeys error: " + e.getMessage());
			return null;
		}
	}

	@Override
	public IDBObject mapRow(IAbstractResultSet resultSet) {

		return new RealtimeBasic(resultSet.getString("obj_id"),
				resultSet.getString("type"), resultSet.getString("state"),
				resultSet.getInt("update_time"),
				resultSet.getInt("start_time"), resultSet.getInt("cpu"),
				resultSet.getInt("memory_used"),
				resultSet.getInt("memory_total"),
				resultSet.getInt("disk_used"), resultSet.getInt("disk_total")

		);
	}

	protected String getLife() {
		int now = (int) (System.currentTimeMillis() / 1000L);
		int dif = now - this.start_time;
		int day = dif / (24 * 3600);
		int hour = dif % (24 * 3600) / 3600;
		int min = dif % 3600 / 60;
		int sec = dif % 60;

		return "[" + day + "]D [" + hour + "]H [" + min + "]M [" + sec + "]S";
	}

	public void updateInfo(String state, int start_time, int cpu,
			int memory_used, int memory_total, int disk_used, int disk_total) {
		this.state = state;
		this.start_time = start_time;
		this.setCpu(cpu);
		this.setMemory_used(memory_used);
		this.setMemory_total(memory_total);
		this.setDisk_used(disk_used);
		this.setDisk_total(disk_total);
	}

	public String getObj_id() {
		return obj_id;
	}

	public void setObj_id(String obj_id) {
		this.obj_id = obj_id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public long getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(int update_time) {
		this.update_time = update_time;
	}

	public long getStart_time() {
		return start_time;
	}

	public void setStart_time(int start_time) {
		this.start_time = start_time;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public int getMemory_used() {
		return memory_used;
	}

	public void setMemory_used(int memory_used) {
		this.memory_used = memory_used;
	}

	public int getMemory_total() {
		return memory_total;
	}

	public void setMemory_total(int memory_total) {
		this.memory_total = memory_total;
	}

	public int getDisk_used() {
		return disk_used;
	}

	public void setDisk_used(int disk_used) {
		this.disk_used = disk_used;
	}

	public int getDisk_total() {
		return disk_total;
	}

	public void setDisk_total(int disk_total) {
		this.disk_total = disk_total;
	}
}
