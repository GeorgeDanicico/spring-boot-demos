package com.george.gist.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class S3Config {

    @Value("${spring.aws.access-key}")
    private String awsAccessKey;
    @Value("${spring.aws.secret-key}")
    private String awsSecretKey;
    @Value("${spring.aws.region}")
    private String awsRegion;
    @Value("${spring.aws.s3-endpoint}")
    private String awsS3Endpoint;

    @Bean
    public S3Client s3Client() {
        var credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);

        return S3Client
            .builder()
            .endpointOverride(URI.create(awsS3Endpoint))
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build())   
            .build();
    }
}
