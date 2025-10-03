package com.example.productcatalogservice.elastic;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EsIndexInitializer {
    private static final String INDEX = "product_v1";
    private final ElasticsearchOperations operations;

    @PostConstruct
    public void init() {
        if (!operations.indexOps(IndexCoordinates.of(INDEX)).exists()) {
/*
        TODO разобраться и может улучить этот код
        String json = new String(new ClassPathResource("es/products-settings.json").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        operations.indexOps(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.of(INDEX)).create(json);
        log.info("Created index {}", INDEX);
 */
        }
    }
}
