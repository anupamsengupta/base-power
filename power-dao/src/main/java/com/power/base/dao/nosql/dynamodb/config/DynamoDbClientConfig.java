package com.power.base.dao.nosql.dynamodb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;

@Configuration
@ConditionalOnClass(DynamoDbClient.class)
@ConditionalOnProperty(name = "power.dynamodb.enabled", havingValue = "true")
public class DynamoDbClientConfig {

    @Value("${power.dynamodb.region:}")
    private String region;

    @Value("${power.dynamodb.endpoint:}")
    private String endpoint;

    @Bean
    @ConditionalOnMissingBean
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClientBuilder builder = DynamoDbClient.builder();
        if (region != null && !region.isBlank()) {
            builder = builder.region(Region.of(region));
        }
        if (endpoint != null && !endpoint.isBlank()) {
            builder = builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }
}

