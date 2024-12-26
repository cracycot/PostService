package org.example.utils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
    @Value("${s3.accesskey}")
    private String accessKey;
    @Value("${s3.secretkey}")
    private String secretKey;
    private final String endpointUrl = "https://s3.selcdn.ru";

    @Bean
    public AmazonS3 s3Client() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTPS);

        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AmazonS3ClientBuilder.EndpointConfiguration(endpointUrl, "ru-1")
                )
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfig)
                .build();
    }
}
