package com.example.productcatalogservice.integration.elastic;

import com.example.productcatalogservice.service.elastic.ProductDataProvider;
import com.example.productcatalogservice.service.elastic.ProductSearchService;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Testcontainers
@DataElasticsearchTest
@ActiveProfiles("integration")
@Import(ProductSearchService.class)
class ProductSearchServiceIntegrationTest {

    @Container
    static final GenericContainer<?> elastic = new GenericContainer<>("elasticsearch:9.1.4")
            .withExposedPorts(9200)
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("xpack.security.http.ssl.enabled", "false")
            .withEnv("bootstrap.memory_lock", "true")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("config/synonyms_ru_en.txt"),
                    "/usr/share/elasticsearch/config/analysis/synonyms_ru_en.txt"
            )
            .waitingFor(
                    Wait.forHttp("/")
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofSeconds(60))
            );


    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        Integer elasticPort = elastic.getMappedPort(9200);

        registry.add("spring.elasticsearch.uris", () -> "http://localhost:" + elasticPort);
        registry.add("spring.elasticsearch.connection-timeout", () -> "2s");
        registry.add("spring.elasticsearch.socket-timeout", () -> "30s");
    }


    @MockitoBean
    private ProductDataProvider productDataProvider;

    @Autowired
    private ProductSearchService productSearchService;

    private static Integer elasticPort = elastic.getMappedPort(9200);
    private static String elasticHost = elastic.getHost();

    @BeforeAll
    static void initIndex() {
        RestClient restClient = RestClient.builder(new HttpHost(elasticHost, elasticPort, "http")).build();

        try (InputStream inputStream = ProductSearchServiceIntegrationTest.class.getClassLoader().getResourceAsStream("config/product_V1-mapping.json")) {
            String mapper = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            Request request = new Request("PUT", "/product_v1");
            request.setJsonEntity(mapper);

            restClient.performRequest(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                restClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void testContainer() {

    }
}