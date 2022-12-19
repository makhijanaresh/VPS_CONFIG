package com.example;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.util.Base64;
import com.example.dto.InstanceInfo;
import com.example.dto.MySqlInfo;
import com.example.dto.PortDetails;
import com.example.dto.SecurityGroupDetail;
import com.example.dto.SoftwareInfo;

@RestController
@CrossOrigin
public class SSHController {

	@Autowired
	private Helper helper;
	static AmazonEC2 ec2 = null;
	static Map<String, UserInstanceDetail> userInstanceDetailMap = new HashMap<>();
	static {
		BasicAWSCredentials awsCreds = new BasicAWSCredentials("access_key",
				"secret_key");
		ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

	}

	@PostMapping("/createInstance")
	public String createInstance(@RequestBody InstanceInfo instanceInfo) throws Exception {
		// createKeyPair(instanceInfo.getUserName());
		String ubuntuImageId = "ami-07ffb2f4d65357b42";
		RunInstancesRequest request;
		request = new RunInstancesRequest();
		request.setImageId(ubuntuImageId);
		request.setInstanceType(InstanceType.T2Micro);
		request.setKeyName("vps-test-key");
		request.setMinCount(1);
		request.setMaxCount(1);
		request.setMinCount(1);
		if (instanceInfo.getSoftwaresToInstall() != null && !instanceInfo.getSoftwaresToInstall().isEmpty()) {
			request.setUserData(generateUserData(instanceInfo.getSoftwaresToInstall()));
		}
		if (instanceInfo.getSecirutyGroupName() == null || instanceInfo.getSecirutyGroupName().isEmpty()) {
			Collection<String> collection = Arrays
					.asList(createSecurityGroup(instanceInfo.getUserName(), instanceInfo.getSecurityGroupDetail()));
			request.setSecurityGroupIds(collection);
		} else {
			request.setSecurityGroups(Arrays.asList(instanceInfo.getSecirutyGroupName()));
		}
		RunInstancesResult result = ec2.runInstances(request);
		Thread.sleep(120000);
		System.err.println("result:" + result);
		instanceInfo.setInstanceId(result.getReservation().getInstances().get(0).getInstanceId());
		String publicIp = ec2
				.describeInstances(new DescribeInstancesRequest()
						.withInstanceIds(result.getReservation().getInstances().get(0).getInstanceId()))
				.getReservations().stream().map(Reservation::getInstances).flatMap(List::stream).findFirst()
				.map(Instance::getPublicIpAddress).orElse(null);
		UserInstanceDetail uid = new UserInstanceDetail();
		uid.setUserName(instanceInfo.getUserName());
		uid.setIp(publicIp);
		uid.setInstanceId(result.getReservation().getInstances().get(0).getInstanceId());
		userInstanceDetailMap.put(instanceInfo.getUserName(), uid);
		try {
			helper.createUser(ec2, instanceInfo, publicIp);

			System.err.println("After reboot call");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Instance Created to get IP wait for 10 min and click refresh button";
	}

	@GetMapping("/getInstanceInfo/{username}")
	public InstanceInfo getInstanceInfo(@PathVariable("username") String userName) {

		String instanceId = userInstanceDetailMap.get(userName).getInstanceId();
		InstanceInfo info = new InstanceInfo();
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		request.setInstanceIds(Arrays.asList(instanceId));

		List<Reservation> reservations = ec2.describeInstances(request).getReservations();
		Optional<Instance> optional = null;
		for (Reservation reservation : reservations) {
			optional = reservation.getInstances().stream().filter(inst -> inst.getInstanceId().equals(instanceId))
					.findFirst();

		}

		if (optional.isPresent()) {
			Instance instance = optional.get();
			info.setInstanceState(instance.getState().getName());
			String publicIp = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId))
					.getReservations().stream().map(Reservation::getInstances).flatMap(List::stream).findFirst()
					.map(Instance::getPublicIpAddress).orElse(null);
			info.setIp(publicIp);
		}

		return info;
	}

	private String generateUserData(String softwaresToInstall) {
		String userData = "";
		userData = userData + "#!/bin/bash \n";
		userData = userData + "sudo apt-get update -y \n";
		String rest = Stream.of(softwaresToInstall.split(",")).map(str -> "\n" + str).reduce("", String::concat);
		userData += rest;
		System.err.println(userData);

		String base64UserData = null;
		try {
			base64UserData = new String(Base64.encode(userData.getBytes("UTF-8")), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.err.println("base64UserData:" + base64UserData);
		return base64UserData;

	}

	public String createSecurityGroup(String userName, SecurityGroupDetail sgd) {
		CreateSecurityGroupRequest create_request = new CreateSecurityGroupRequest()
				.withGroupName(userName + "my-security-group").withDescription(sgd.getDescription());

		String securityGroupId = null;
		CreateSecurityGroupResult securityResult = ec2.createSecurityGroup(create_request);
		securityGroupId = securityResult.getGroupId();
		System.err.println("Security result:" + securityResult);

		List<PortDetails> ports = sgd.getPortDetails();
		List<IpPermission> ipPermissions = new ArrayList<>();
		IpPermission ipPermission = new IpPermission();
		for (PortDetails port : ports) {
			IpRange ip_range = new IpRange().withCidrIp(port.getIp());

			ipPermission = new IpPermission().withIpProtocol(port.getProtocol())
					.withToPort(Integer.parseInt(port.getPort())).withFromPort(Integer.parseInt(port.getPort()))
					.withIpv4Ranges(ip_range);
			ipPermissions.add(ipPermission);
		}
		System.out.println("Successfully created security group named:" + sgd.getName());

		AuthorizeSecurityGroupIngressRequest auth_request = new AuthorizeSecurityGroupIngressRequest()
				.withGroupName(userName + "my-security-group").withIpPermissions(ipPermissions);

		AuthorizeSecurityGroupIngressResult result = ec2.authorizeSecurityGroupIngress(auth_request);
		System.err.println("Result:" + result);

		System.out.printf("Successfully added ingress policy to security group %s", sgd.getName());
		return securityGroupId;
	}

	@PostMapping("/changestate")
	public String changeState(@RequestBody InstanceInfo instanceInfo) {

		try {
			String instanceId = userInstanceDetailMap.get(instanceInfo.getUserName()).getInstanceId();
			if (instanceInfo.getInstanceState() != null && instanceInfo.getInstanceState().equalsIgnoreCase("start")) {
				StartInstancesRequest request;
				request = new StartInstancesRequest().withInstanceIds(instanceId);
				ec2.startInstances(request);
			} else if (instanceInfo.getInstanceState() != null
					&& instanceInfo.getInstanceState().equalsIgnoreCase("stop")) {
				StopInstancesRequest request;
				request = new StopInstancesRequest().withInstanceIds(instanceId);
				ec2.stopInstances(request);
			}
			System.err.println("going to sleep");
			System.err.println("woken up");
		} catch (Exception e) {
			e.printStackTrace();
			return "Some Error Occured";
		}
		return "State Changed";
	}

	@GetMapping("/java/{version}/{serverip}")
	public String installjava(@PathVariable("version") String version, @PathVariable("serverip") String serverIp) {
		helper.installJava(serverIp, version);
		return "Installed Successfully";
	}

	@PostMapping("/tomcat")
	public String installTomcat(@RequestBody SoftwareInfo tomcatInfo) {
		String publicIp = ec2
				.describeInstances(new DescribeInstancesRequest()
						.withInstanceIds(userInstanceDetailMap.get(tomcatInfo.getUserName()).getInstanceId()))
				.getReservations().stream().map(Reservation::getInstances).flatMap(List::stream).findFirst()
				.map(Instance::getPublicIpAddress).orElse(null);
		helper.installTomcat(ec2, tomcatInfo, publicIp);
		return "Tomcat Installation Started";
	}

	@PostMapping("/java")
	public String installJava(@RequestBody SoftwareInfo softInfo) {

		String publicIp = ec2
				.describeInstances(new DescribeInstancesRequest()
						.withInstanceIds(userInstanceDetailMap.get(softInfo.getUserName()).getInstanceId()))
				.getReservations().stream().map(Reservation::getInstances).flatMap(List::stream).findFirst()
				.map(Instance::getPublicIpAddress).orElse(null);

		helper.installJava(publicIp, softInfo.getJavaCommand());
		return "Java Installation Started";
	}

	@PostMapping("/mysql")
	public String installMySql(@RequestBody MySqlInfo mysqlInfo) {
		String publicIp = ec2
				.describeInstances(new DescribeInstancesRequest()
						.withInstanceIds(userInstanceDetailMap.get(mysqlInfo.getUserName()).getInstanceId()))
				.getReservations().stream().map(Reservation::getInstances).flatMap(List::stream).findFirst()
				.map(Instance::getPublicIpAddress).orElse(null);
		mysqlInfo.setIp(publicIp);
		helper.installMysql(ec2, mysqlInfo);
		return "Mysql Installation Started";
	}

	@GetMapping("/installationStatus")
	public String getInstallationStatus() {
		return Helper.installationStatus;
	}
}
