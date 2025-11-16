package com.power.base.dao.timeser;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(InfluxDBClient.class)
@ConditionalOnProperty(prefix = "power.timeser.influx", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(InfluxTimeSeriesProperties.class)
public class InfluxTimeSeriesConfig {

    @Bean
    @ConditionalOnMissingBean
    public InfluxDBClient influxDBClient(InfluxTimeSeriesProperties properties) {
        char[] token = properties.getToken() == null ? new char[0] : properties.getToken().toCharArray();
        return InfluxDBClientFactory.create(properties.getUrl(), token, properties.getOrg(), properties.getBucket());
    }
}


