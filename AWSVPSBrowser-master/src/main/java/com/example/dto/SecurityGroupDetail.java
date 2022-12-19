package com.example.dto;

import java.util.List;

import com.amazonaws.services.ec2.model.IpPermission;

public class SecurityGroupDetail {

	private String id;
	private String name;
	private String description;
	private String vpcId;
	List<PortDetails> portDetails;
	private List<IpPermission> ipPermission;

	public List<PortDetails> getPortDetails() {
		return portDetails;
	}

	public void setPortDetails(List<PortDetails> portDetails) {
		this.portDetails = portDetails;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVpcId() {
		return vpcId;
	}

	public void setVpcId(String vpcId) {
		this.vpcId = vpcId;
	}

	public List<IpPermission> getIpPermission() {
		return ipPermission;
	}

	public void setIpPermission(List<IpPermission> ipPermission) {
		this.ipPermission = ipPermission;
	}

	@Override
	public String toString() {
		return "SecurityGroupDetail [id=" + id + ", name=" + name + ", description=" + description + ", vpcId=" + vpcId
				+ ", portDetails=" + portDetails + ", ipPermission=" + ipPermission + "]";
	}

	
	
	

}
