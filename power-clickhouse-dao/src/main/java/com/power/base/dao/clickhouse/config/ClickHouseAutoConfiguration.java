package com.power.base.dao.clickhouse.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for ClickHouse DAO.
 * Enables configuration properties and auto-wiring of services.
 */
@Configuration
@EnableConfigurationProperties(ClickHouseProperties.class)
@ConditionalOnProperty(prefix = "clickhouse", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ClickHouseAutoConfiguration {
    // Configuration class - properties are enabled via @EnableConfigurationProperties
}

