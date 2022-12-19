package com.helper.project;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class VersionController {

	@GetMapping("/versions")
	public Map<String, List<VersionDto>> getVersion() {

		Map<String, List<VersionDto>> versionMap = new HashMap<>();

		versionMap.put("tomcat", generateTomcatVersions());
		versionMap.put("java", generateJavaVersions());
		return versionMap;
	}

	public List<VersionDto>  generateTomcatVersions() {
		List<VersionDto> tomcatVersions = new LinkedList<>();

		VersionDto version9 = new VersionDto();
		version9.setVersion("tomcat9");
		version9.setUrl(" https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.54/bin/apache-tomcat-9.0.54.tar.gz");

		VersionDto version8 = new VersionDto();
		version8.setVersion("tomcat8");
		version8.setUrl(" https://dlcdn.apache.org/tomcat/tomcat-8/v8.5.72/bin/apache-tomcat-8.5.72.tar.gz");

		VersionDto version10 = new VersionDto();
		version10.setVersion("tomcat10");
		version10.setUrl(" https://dlcdn.apache.org/tomcat/tomcat-10/v10.0.27/bin/apache-tomcat-10.0.27.tar.gz");

		tomcatVersions.add(version8);
		tomcatVersions.add(version9);
		tomcatVersions.add(version10);
		
		return tomcatVersions;
	}
	
	public List<VersionDto> generateJavaVersions()
	{
		List<VersionDto> javaVersions=new LinkedList<>();
		
		VersionDto java17=new VersionDto();
		java17.setJavaVersion("Java17");
		String java17command="sudo apt-get update -y;sudo apt-get install openjdk-17-jdk -y;";
		java17command+="sudo update-java-alternatives -s $(sudo update-java-alternatives -l | grep 17 | cut -d ' ' -f1) || echo '.'";
		java17.setJavaCommand(java17command);
		
		VersionDto java18=new VersionDto();
		java18.setJavaVersion("Java18");
		String java16Command="sudo apt-get update -y;sudo apt-get install openjdk-18-jdk -y;";
		java16Command+="sudo update-java-alternatives -s $(sudo update-java-alternatives -l | grep 18 | cut -d ' ' -f1) || echo '.'";;
		java18.setJavaCommand(java16Command);
		
		javaVersions.add(java17);
		javaVersions.add(java18);
		
		return javaVersions;
	}
}
