package com.example.productcatalogservice.integration.elastic;

import com.example.productcatalogservice.dto.event.ProductCreateEvent;
import com.example.productcatalogservice.elastic_document.*;
import com.example.productcatalogservice.repositoty.elastic.ProductSearchRepository;
import com.example.productcatalogservice.service.elastic.ProductDataProvider;
import com.example.productcatalogservice.service.elastic.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.suggest.Completion;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Slf4j
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

    @Autowired
    private ProductSearchRepository productSearchRepository;

    private ProductDoc productDoc;

    @BeforeEach
    void initData() {
        BrandDoc brand = BrandDoc.builder()
                .id(1L)
                .publicId(UUID.randomUUID().toString())
                .name("Apple")
                .slug("apple")
                .isActive(true)
                .build();

        PrimaryCategoryDoc primary = PrimaryCategoryDoc.builder()
                .id(2L)
                .publicId(UUID.randomUUID().toString())
                .name("iPhone 14")
                .slug("iphone-14")
                .pathIds(List.of(1L))
                .pathSlug(List.of("electronics"))
                .build();

        CategoryDoc category = CategoryDoc.builder()
                .id(1L)
                .publicId(UUID.randomUUID().toString())
                .name("Electronics")
                .slug("electronics")
                .build();

        CategoryDoc primeCategory = CategoryDoc.builder()
                .id(2L)
                .publicId(UUID.randomUUID().toString())
                .name("iPhone 14")
                .slug("iphone-14")
                .build();


        List<CategoryDoc> categories = List.of(category, primeCategory);

        ProductAttributeDoc attribute = ProductAttributeDoc.builder()
                .attributeId(1L)
                .attributePublicId(UUID.randomUUID().toString())
                .attributeName("Color")
                .attributeSlug("color")
                .valueId(1L)
                .value("Red")
                .valueSlug("red")
                .filterable(true)
                .isActive(true)
                .build();

        List<ProductAttributeDoc> attributes = List.of(attribute);

        CustomAttributeDoc custom = CustomAttributeDoc.builder()
                .name("camera")
                .value("3")
                .build();

        List<CustomAttributeDoc> customs = List.of(custom);

        InventoryDoc inventory = InventoryDoc.builder()
                .quantity(100)
                .inStock(true)
                .lowStock(false)
                .build();

        SuggestDoc suggest = SuggestDoc.builder()
                .name(new Completion(List.of("iPhone 14", "iPhone 14 128GB", "Apple iPhone 14")))
                .brand(new Completion(List.of("Apple", "Apple Inc")))
                .build();

        productDoc = ProductDoc.builder()
                .id(1L)
                .publicId(UUID.randomUUID().toString())
                .name("iPhone 14")
                .description("The iPhone 14 is a 6.1-inch smartphone featuring a Super Retina XDR display, the A15 Bionic chip, and a dual-camera system with 12MP Main and Ultra Wide cameras.")
                .longDescription("The iPhone 14 is a 6.1-inch smartphone featuring a Super Retina XDR display, the A15 Bionic chip, and a dual-camera system with 12MP Main and Ultra Wide cameras. It was introduced in 2022 and includes features like Crash Detection and Emergency SOS via satellite. The device has a durable design, IP68 water and dust resistance, and supports wireless charging via MagSafe and Q")
                .brand(brand)
                .primaryCategory(primary)
                .categories(categories)
                .attributes(attributes)
                .customAttributes(customs)
                .basePrice(59999.99)
                .currency("RUB")
                .rating(4.7)
                .ratingCount(100L)
                .isActive(true)
                .isAvailable(true)
                .inventory(inventory)
                .suggest(suggest)
                .searchText("apple iphone 14")
                .build();
    }

    @BeforeAll
    static void initIndex() {
        Integer elasticPort = elastic.getMappedPort(9200);
        String elasticHost = elastic.getHost();

        try (RestClient restClient = RestClient.builder(new HttpHost(elasticHost, elasticPort, "http")).build();
             InputStream inputStream = ProductSearchServiceIntegrationTest.class.getClassLoader().getResourceAsStream("config/product_V1-mapping.json")) {
            try {
                String mapper = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                Request request = new Request("PUT", "/product_v1");
                request.setJsonEntity(mapper);

                restClient.performRequest(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void clearData() {
        productSearchRepository.deleteAll();
    }

    @Test
    void createIndexAndAlias_ShouldReturnSuccessfulResponse() throws IOException {
        String elasticHost = elastic.getHost();
        Integer elasticPort = elastic.getMappedPort(9200);

        RestClient restClient = RestClient.builder(new HttpHost(elasticHost, elasticPort, "http")).build();

        Request indexRequest = new Request("GET", "/product_v1");

        Response indexResponse = restClient.performRequest(indexRequest);

        String indexEntity = new String(indexResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

        Request aliasRequest = new Request("GET", "/_cat/aliases?v");

        Response aliasResponse = restClient.performRequest(aliasRequest);

        String aliasEntity = new String(aliasResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(200, indexResponse.getStatusLine().getStatusCode());
        assertEquals(200, aliasResponse.getStatusLine().getStatusCode());
        assertNotNull(indexEntity);
        assertNotNull(aliasEntity);
    }

    @Test
    void createProductSearchDocument_ShouldSuccessCreateDocument() throws IOException {
        var productId = 1L;
        var inventoryId = 1L;
        var productEvent = new ProductCreateEvent(productId, inventoryId);

        when(productDataProvider.getProductData(anyLong(), anyLong())).thenReturn(productDoc);

        ProductDoc result = productSearchService.createProductSearchDocument(productEvent);

        assertNotNull(result);
        assertEquals(productDoc, result);
    }
}