package com.power.base.rest.integration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.power.base")
@EntityScan(basePackages = "com.power.base.dao.rdbms.jpa.persistence")
@EnableJpaRepositories(basePackages = "com.power.base.dao.rdbms.jpa.repository")
public class TestApplication {
}

