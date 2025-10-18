package com.example.productcatalogservice.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.productcatalogservice.repositoty.elastic")
@EnableJpaRepositories(basePackages = "com.example.productcatalogservice.repositoty.jpa")
public class DataConfig {
}
