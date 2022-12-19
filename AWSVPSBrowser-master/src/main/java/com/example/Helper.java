package com.example;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.example.dto.InstanceInfo;
import com.example.dto.MySqlInfo;
import com.example.dto.SoftwareInfo;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@Component
public class Helper {

	public static String installationStatus = "Software Installation Initiated";

	@Async
	public void rebootInstane(AmazonEC2 ec2, InstanceInfo instanceInfo) throws InterruptedException {
		RebootInstancesRequest request = new RebootInstancesRequest().withInstanceIds(instanceInfo.getInstanceId());
		RebootInstancesResult response = ec2.rebootInstances(request);
		System.err.println("Reboot response:" + response);
	}

	@Async
	public void createUser(AmazonEC2 ec2, InstanceInfo instanceInfo, String serverIp) throws Exception {
		installationStatus="Create User is in progress for user:"+instanceInfo.getUserName();
		Session session = getJschSession(serverIp);
		ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

		channelExec.setCommand("sudo useradd -m -p $(openssl passwd  -1 " + instanceInfo.getPassword()
				+ ") -s /bin/bash  -G sudo " + instanceInfo.getUserName() + ";"
				+ "sudo sed -i 's/PasswordAuthentication no/PasswordAuthentication Yes/' /etc/ssh/sshd_config;sudo apt-get update -y;"
				+ "sudo chmod 777 /home/"+instanceInfo.getUserName()+"/;sudo apt-get install expect -y");
		InputStream in = channelExec.getInputStream();
		channelExec.connect(5000);
		 byte[] tmp = new byte[1024];

			while (true) {
				
				  while (in.available() > 0) {
					  int i = in.read(tmp, 0, 1024); if (i < 0) break;
				  // count = Integer.parseInt(new String(tmp, 0, i).trim());
				  System.out.print(new String(tmp, 0, i)); }
				 
				if (channelExec.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: " + channelExec.getExitStatus());
					if (channelExec.getExitStatus() == 0) {
						installationStatus = "User Created Successfully";
					} else {
						installationStatus = "User Creation failed";
					}
					break;
				}
				/*
				 * try { Thread.sleep(1000); } catch (Exception ee) { }
				 */
			}
		Thread.sleep(2000);
		System.err.println("Closing Jsch Session Connection for user ");
		channelExec.disconnect();
		session.disconnect();
		rebootInstane(ec2, instanceInfo);
	}

	public Session getJschSession(String serverIp) throws Exception {
		JSch jsch = new JSch();
		int portNumber = 22;
		String pemFilePath = "D:\\AWS\\vps-test-key.pem";
		
		jsch.addIdentity(pemFilePath);
		//jsch.addIdentity("vps-test-key", pemFilePath.getBytes(), publicKey.getBytes(), (byte[])null);
		System.err.println("serverIp:" + serverIp);
		Session session = null;
		boolean notConnected = true;
		while (notConnected) {
			try {
				session = jsch.getSession("ubuntu", serverIp, portNumber);
				Properties properties = new Properties();
				properties.put("StrictHostKeyChecking", "no");
				session.setConfig(properties);
				session.connect(0);
				notConnected = false;
			} catch (Exception e) {
				Thread.sleep(2000);
				System.err.println("Trying to connect to session");
				e.printStackTrace();

			}
		}
		return session;
	}

	@Async
	public void installJava(String serverIp, String command) {
		try {
			installationStatus = "Java Installation is in progress";
			Session session = getJschSession(serverIp);
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(command);

			
			InputStream in = channelExec.getInputStream();
			channelExec.connect(5000);
			 byte[] tmp = new byte[1024];

			while (true) {
				
				  while (in.available() > 0) {
					  int i = in.read(tmp, 0, 1024); if (i < 0) break;
				  // count = Integer.parseInt(new String(tmp, 0, i).trim());
				  System.out.print(new String(tmp, 0, i)); }
				 
				if (channelExec.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: " + channelExec.getExitStatus());
					if (channelExec.getExitStatus() == 0) {
						installationStatus = "Java Installed Successfully";
					} else {
						installationStatus = "Java Installation failed";
					}
					break;
				}
				/*
				 * try { Thread.sleep(1000); } catch (Exception ee) { }
				 */
			}
			System.err.println("Closing Jsch Session Connection");
			channelExec.disconnect();
			session.disconnect();

		} catch (Exception e) {
			installationStatus = "Some error occured while installing java";
		}
	}

	@Async
	public void installTomcat(AmazonEC2 ec2, SoftwareInfo tomcatInfo, String serverIp) {
		installationStatus="Tomcat Installation is in progress";
		downloadJarFile(serverIp, tomcatInfo);
		modifySecurityGroup(ec2, tomcatInfo.getUserName(), tomcatInfo.getConnectorPort());

	}

	// https://dlcdn.apache.org/tomcat/tomcat-10/v10.0.12/bin/apache-tomcat-10.0.12.tar.gz
	private void downloadJarFile(String serverIp, SoftwareInfo tomcatInfo) {
		try {
			String rgx = "([^\\/]+$)";
			Matcher m = Pattern.compile(rgx).matcher(tomcatInfo.getTarPath());
			if (m.find()) {
				String finalVersion = m.group(1);
				finalVersion = finalVersion.substring(0, finalVersion.length() - 7);
				tomcatInfo.setVersion(finalVersion);
			}
			String userName = tomcatInfo.getUserName();
			Session session = getJschSession(serverIp);
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			String finalCommand = "cd /home/" + userName + "/;sudo wget " + tomcatInfo.getTarPath() + ";"
					+ "sudo tar xvzf " + tomcatInfo.getVersion() + ".tar.gz" + ";"
					+ "sudo sed -i 's/<Connector port=\"8080\"/<Connector port=\"" + tomcatInfo.getConnectorPort()
					+ "\"/' /home/" + userName + "/" + tomcatInfo.getVersion() + "/conf/server.xml;"
					+ "sudo sed -i 's/<Server port=\"8005\"/<Server port=\"" + tomcatInfo.getServerPort()
					+ "\"/' /home/" + userName + "/" + tomcatInfo.getVersion() + "/conf/server.xml;"
					+ "sudo chown "
					+ userName + " -R  /home/" + userName + "/*";

			channelExec.setCommand(finalCommand);
			Thread.sleep(2000);
			InputStream in = channelExec.getInputStream();
			channelExec.connect(5000);
			 byte[] tmp = new byte[1024];

				while (true) {
					
					  while (in.available() > 0) {
						  int i = in.read(tmp, 0, 1024); if (i < 0) break;
					  // count = Integer.parseInt(new String(tmp, 0, i).trim());
					  System.out.print(new String(tmp, 0, i)); }
					 
					if (channelExec.isClosed()) {
						if (in.available() > 0)
							continue;
						System.out.println("exit-status: " + channelExec.getExitStatus());
						if (channelExec.getExitStatus() == 0) {
							installationStatus = "Tomcat Installed Successfully";
						} else {
							installationStatus = "Tomcat Installation failed";
						}
						break;
					}
					/*
					 * try { Thread.sleep(1000); } catch (Exception ee) { }
					 */
				}
			Thread.sleep(2000);
			System.err.println("Closing Jsch Session Connection");
			channelExec.disconnect();
			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void modifySecurityGroup(AmazonEC2 ec2, String userName, String port) {
		AuthorizeSecurityGroupIngressRequest ingress = new AuthorizeSecurityGroupIngressRequest();
		ingress.withGroupName(userName + "my-security-group");
		IpPermission ipPermission = new IpPermission();
		List<IpPermission> ipPermissions = new ArrayList<>();
		IpRange ip_range = new IpRange().withCidrIp("0.0.0.0/0");

		ipPermission = new IpPermission().withIpProtocol("tcp").withToPort(Integer.parseInt(port))
				.withFromPort(Integer.parseInt(port)).withIpv4Ranges(ip_range);
		ipPermissions.add(ipPermission);
		ingress.withIpPermissions(ipPermissions);
		ec2.authorizeSecurityGroupIngress(ingress);

	}

	@Async
	public void installMysql(AmazonEC2 ec2, MySqlInfo mysqlInfo) {
		installationStatus = "Mysql Installation is in progress";
		modifySecurityGroup(ec2, mysqlInfo.getUserName(), mysqlInfo.getPort());
		String additionalUserName = mysqlInfo.getAdditionalUser();
		String additionalPassword = mysqlInfo.getAdditionalUserPassword();
		copyExpFile(mysqlInfo.getIp(), mysqlInfo.getUserName(), mysqlInfo.getPassword());

		try {

			Session session = getJschSession(mysqlInfo.getIp());
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			StringBuilder command = new StringBuilder("");
			command.append(" cd /home/" + mysqlInfo.getUserName() + "/;");
			command.append("sudo chmod 777 autoexpectfile.exp;");
			command.append(" sudo apt update -y;");
			command.append("sudo apt-get install expect -y;");
			command.append("sudo apt-get install mysql-server -y;");
			command.append("sleep 5;");
			command.append("expect autoexpectfile.exp;");
			command.append("sudo sed -i 's/root/" + mysqlInfo.getRootPassword() + "/' autoexpectfile.exp;");
			command.append("sleep 3;");
			command.append("sudo sed -i 's/127.0.0.1/0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf;");
			command.append("sudo sed -i 's/3306/" + mysqlInfo.getPort() + "/' /etc/mysql/mysql.conf.d/mysqld.cnf;");
			command.append("sudo sed -i 's/# port/port/' /etc/mysql/mysql.conf.d/mysqld.cnf;");
			command.append("sudo service mysql restart;");
			command.append("sleep 3;");
			command.append("sudo mysql -e '");
			if (mysqlInfo.isAllowRemoteLogin()) {
				command.append("create user \"" + additionalUserName + "\"@\"%\" identified by \"" + additionalPassword
						+ "\"'");
			} else {
				command.append("create user \"" + additionalUserName + "\"@\"localhost\" identified by \""
						+ additionalPassword + "\"'");
			}
			System.out.println(command.toString());
			channelExec.setCommand(command.toString());
			InputStream in = channelExec.getInputStream();
			channelExec.connect(5000);
			 byte[] tmp = new byte[1024];
			
			while (true) {

				  while (in.available() > 0) {
					  int i = in.read(tmp, 0, 1024); if (i < 0) break;
				  System.out.print(new String(tmp, 0, i)); }
				 
				if (channelExec.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: " + channelExec.getExitStatus());
					if (channelExec.getExitStatus() == 0) {
						installationStatus = "Mysql Installed Successfully";
					} else {
						installationStatus = "Mysql Installation failed";
					}
					break;
				}
			}
			System.err.println("Closing Jsch Session Connection");
			channelExec.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void copyExpFile(String serverIp, String userName, String password) {
		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(userName, serverIp);
			session.setPassword(password);
			Properties properties = new Properties();
			properties.put("StrictHostKeyChecking", "no");
			session.setConfig(properties);
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.put("D:\\YouTubeVideos\\Part1\\AWS-Java-SpringBoot\\Workspace\\AWSVPSProjects\\AWSVPSBrowser-master\\autoexpectfile.exp",
					"/home/" + userName + "/");
			sftpChannel.exit();
			channel.disconnect();
			session.disconnect();
			do {
				Thread.sleep(1000);
			} while (channel.isEOF() == false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void testCommand(String ip, String command) {
		try {
			// TODO Auto-generated method stub
			Session session = getJschSession(ip);
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(command);
			// channelExec.setInputStream(System.in);
			// channelExec.setErrStream(System.err);
			InputStream in = channelExec.getInputStream();
			channelExec.connect(5000);
			Thread.sleep(2000);
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					// count = Integer.parseInt(new String(tmp, 0, i).trim());
					System.out.print(new String(tmp, 0, i));
				}
				if (channelExec.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: " + channelExec.getExitStatus());
					break;
				}
				/*
				 * try { Thread.sleep(1000); } catch (Exception ee) { }
				 */
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void generateFiles(String serverIp) {
		// TODO Auto-generated method stub
		try {
			Session session = getJschSession(serverIp);
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			StringBuilder command = new StringBuilder("");
			command.append(" cd /home/ubuntu;");
			command.append("sudo touch install.sh;");
			command.append("sudo chmod 777 install.sh;");
			command.append("sudo chmod 777 autoexpectfile.exp;");
			command.append("echo sudo apt update -y>install.sh;");
			command.append("echo sudo apt-get install mysql-server -y>>install.sh;");

			System.out.println(command.toString());
			channelExec.setCommand(command.toString());
			channelExec.connect(5000);
			System.err.println("Closing Jsch Session Connection");
			channelExec.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
