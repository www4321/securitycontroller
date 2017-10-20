package com.sds.securitycontroller.knowledge.cloud.agent;

//import com.sds.securitycontroller.knowledge.cloud.agent.OpenstackClient.AuthRequestBean.Auth;
//import com.sds.securitycontroller.knowledge.cloud.agent.OpenstackClient.AuthRequestBean.Auth.PasswordCredentials;

public class AuthRequestBean {
	public class Auth{
		public String tenantName;
		public class PasswordCredentials{
			public String username;
			public String getUsername() {
				return username;
			}
			public void setUsername(String username) {
				this.username = username;
			}
			public String getPassword() {
				return password;
			}
			public void setPassword(String password) {
				this.password = password;
			}
			public String password;
		}
		public PasswordCredentials passwordCredentials = new PasswordCredentials();

		public String getTenantName() {
			return tenantName;
		}
		public void setTenantName(String tenantName) {
			this.tenantName = tenantName;
		}
		public PasswordCredentials getPasswordCredentials() {
			return passwordCredentials;
		}
		public void setPasswordCredentials(PasswordCredentials passwordCredentials) {
			this.passwordCredentials = passwordCredentials;
		}
	}
	public Auth auth = new Auth();
	public Auth getAuth() {
		return auth;
	}
	public void setAuth(Auth auth) {
		this.auth = auth;
	}	
}
