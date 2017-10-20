/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.cloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sds.securitycontroller.event.ISubscriptionResult;
import com.sds.securitycontroller.knowledge.KnowledgeEntity;
import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
import com.sds.securitycontroller.knowledge.KnowledgeType;

public class CloudVM extends KnowledgeEntity implements ISubscriptionResult{

//	public CloudNetwork network;
//	public CloudTenant tenant;
//	public CloudUser user;
//	public CloudPort port;
	
	private static final long serialVersionUID = -8714413892155020850L;
	KnowledgeType[] affiliatedEntityTypes = {KnowledgeType.CLOUD_VIFPORT};
	public enum Status{
		ACTIVE,
	}
	public class AddressInfo implements java.io.Serializable{
		private static final long serialVersionUID = 823668852995860038L;
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
	
	public class NetworkInfo  implements java.io.Serializable{
		private static final long serialVersionUID = -3762405850565496976L;
		List<AddressInfo> addresses = new ArrayList<AddressInfo>();
		public void addAddress(int version, String addr, String ipsType){
			AddressInfo info = new AddressInfo(version, addr, ipsType);
			this.addresses.add(info);
		}
		public List<AddressInfo> getAddresses(){
			return this.addresses;
		}
 
	}
	
	String name;
	String tenantId;
	String userId;
	Map<String, NetworkInfo> networks;
	
	public CloudVM(){};
	public CloudVM(String id, String name, String userId, String tenantId, Map<String, NetworkInfo>  networks){
		this.type = KnowledgeType.CLOUD_VM;
		this.id = id;
		this.name = name;
		this.userId = userId;
		this.tenantId = tenantId;
		this.networks = networks;

		super.initAffiliates(affiliatedEntityTypes);
		this.attributeMap.put(KnowledgeEntityAttribute.ID, id);
		this.attributeMap.put(KnowledgeEntityAttribute.NAME, name);	
	}
	@Override
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
	public void setNetworks(Map<String, NetworkInfo> networks) {
		this.networks = networks;
		String ipsStr="";
		for(NetworkInfo nwif:networks.values()){
			for(AddressInfo adInfo: nwif.addresses){
				ipsStr+= adInfo.ipsType+":"+adInfo.addr+" ";
			}
		}
		this.attributeMap.put(KnowledgeEntityAttribute.IP_ADDRESS, ipsStr);
	}

}
