package com.geonews.integration;

import com.geonews.integration.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class IntegrationApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApiApplication.class, args);
    }
}
