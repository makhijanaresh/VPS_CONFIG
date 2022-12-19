/*
 * package com.example;
 * 
 * import org.springframework.beans.factory.annotation.Value; import
 * org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration;
 * 
 * import com.amazonaws.auth.AWSCredentials; import
 * com.amazonaws.auth.AWSStaticCredentialsProvider; import
 * com.amazonaws.auth.BasicAWSCredentials; import
 * com.amazonaws.services.ec2.AmazonEC2; import
 * com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
 * 
 * @Configuration public class EC2Config {
 * 
 * @Value("${access.key}") private String accessKey;
 * 
 * @Value("${secret.key}") private String secretKey;
 * 
 * public AWSCredentials credentials() { AWSCredentials credentials = new
 * BasicAWSCredentials(accessKey, secretKey); return credentials; }
 * 
 * 
 * 
 * @Bean public AmazonEC2 amazonEc2() { return
 * AmazonEC2ClientBuilder.standard().withCredentials(new
 * AWSStaticCredentialsProvider(credentials())).build(); } }
 */