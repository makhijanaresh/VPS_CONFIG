package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class InstanceInfo {

	private String ip;
	private String userName;
	private String password;
	private String accessKey;
	private String secretKey;
	private String pemFileName;
	private String instanceId;
	private String instanceState;
	private String secirutyGroupName;
	private String softwaresToInstall;
	private SecurityGroupDetail securityGroupDetail;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceState() {
		return instanceState;
	}

	public void setInstanceState(String instanceState) {
		this.instanceState = instanceState;
	}

	
	public String getSecirutyGroupName() {
		return secirutyGroupName;
	}

	public void setSecirutyGroupName(String secirutyGroupName) {
		this.secirutyGroupName = secirutyGroupName;
	}

	
	public String getSoftwaresToInstall() {
		return softwaresToInstall;
	}

	public void setSoftwaresToInstall(String softwaresToInstall) {
		this.softwaresToInstall = softwaresToInstall;
	}

	
	public SecurityGroupDetail getSecurityGroupDetail() {
		return securityGroupDetail;
	}

	public void setSecurityGroupDetail(SecurityGroupDetail securityGroupDetail) {
		this.securityGroupDetail = securityGroupDetail;
	}

	
	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	
	
	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	
	public String getPemFileName() {
		return pemFileName;
	}

	public void setPemFileName(String pemFileName) {
		this.pemFileName = pemFileName;
	}

}
