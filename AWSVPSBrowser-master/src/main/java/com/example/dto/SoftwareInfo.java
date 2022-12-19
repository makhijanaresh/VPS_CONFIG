package com.example.dto;

public class SoftwareInfo {

	private String tarPath;
	private String connectorPort;
	private String serverPort;
	private String userName;
	private String version;

	private String javaCommand;
	
	
	

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getJavaCommand() {
		return javaCommand;
	}

	public void setJavaCommand(String javaCommand) {
		this.javaCommand = javaCommand;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTarPath() {
		return tarPath;
	}

	public void setTarPath(String tarPath) {
		this.tarPath = tarPath;
	}

	
	public String getConnectorPort() {
		return connectorPort;
	}

	public void setConnectorPort(String connectorPort) {
		this.connectorPort = connectorPort;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
