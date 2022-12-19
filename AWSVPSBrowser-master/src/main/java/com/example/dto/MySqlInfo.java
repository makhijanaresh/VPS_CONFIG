package com.example.dto;

public class MySqlInfo {

	private String rootPassword;
	private String port;
	private String userName;
	private String password;
	private String additionalUser;
	private String additionalUserPassword;
	private boolean allowRemoteLogin;
	private String ip;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRootPassword() {
		return rootPassword;
	}

	public void setRootPassword(String rootPassword) {
		this.rootPassword = rootPassword;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAdditionalUser() {
		return additionalUser;
	}

	public void setAdditionalUser(String additionalUser) {
		this.additionalUser = additionalUser;
	}

	public String getAdditionalUserPassword() {
		return additionalUserPassword;
	}

	public void setAdditionalUserPassword(String additionalUserPassword) {
		this.additionalUserPassword = additionalUserPassword;
	}

	public boolean isAllowRemoteLogin() {
		return allowRemoteLogin;
	}

	public void setAllowRemoteLogin(boolean allowRemoteLogin) {
		this.allowRemoteLogin = allowRemoteLogin;
	}

}
