package com.sds.securitycontroller.cloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CloudVM {

	public enum Status{
		ACTIVE,
		
	}
	public class AddressInfo{
		int version;
		String addr;
		String ipsType;
		public AddressInfo(int version, String addr, String ipsType){
			this.version = version;
			this.addr = addr;
			this.ipsType = ipsType;
		}
		public int getVersion(){
			return this.version;
		}
		public String getAddr(){
			return this.addr;
		}
		public String getIpsType(){
			return this.ipsType;
		}
	}
	
	public class NetworkInfo{
		List<AddressInfo> addresses = new ArrayList<AddressInfo>();
		public void addAddress(int version, String addr, String ipsType){
			AddressInfo info = new AddressInfo(version, addr, ipsType);
			this.addresses.add(info);
		}
		public List<AddressInfo> getAddresses(){
			return this.addresses;
		}
 
	}
	
	String id;
	String name;
	String tenantId;
	String userId;
	Map<String, NetworkInfo> networks;
	
	public CloudVM(String id, String name, String userId, String tenantId, Map<String, NetworkInfo> networks){
		this.id = id;
		this.name = name;
		this.userId = userId;
		this.tenantId = tenantId;
		this.networks = networks;
	}
	public String getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public String getTenantId(){
		return this.tenantId;
	}
	public String getUserId(){
		return this.userId;
	}
	public Map<String, NetworkInfo> getNetworks(){
		return this.networks;
	}

}
