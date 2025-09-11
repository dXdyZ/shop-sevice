package com.shop.userservice.inetgration.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration")
@Import(IntegrationTestConfig.class)
public abstract class BasePostgresIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

//    @Container
//    static final GenericContainer<?> keycloak = new GenericContainer<>("quay.io/keycloak/keycloak:latest")
//            .withExposedPorts(8080)
//            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
//            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
//            .withCopyFileToContainer(
//                    MountableFile.forClasspathResource("config/realm-export.json"),
//                    "/opt/keycloak/data/import/realm-test.json"
//            )
//            .withCommand("start-dev --import-realm")
//            .waitingFor(
//                    Wait.forHttp("/realms/master")
//                            .forStatusCode(200)
//                            .withStartupTimeout(Duration.ofSeconds(90))
//            );
//
//    @DynamicPropertySource
//    static void dynamicProperty(DynamicPropertyRegistry registry) {
//        String kcBaseUrl = "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080);
//
//        registry.add("keycloak.server-url", () -> kcBaseUrl);
//        registry.add("keycloak.realms.service-realms.realm", () -> "shop");
//        registry.add("keycloak.realms.admin-realms.realm", () -> "master");
//        registry.add("keycloak.realms.admin-realms.username", () -> "admin");
//        registry.add("keycloak.realms.admin-realms.password", () -> "admin");
//        registry.add("keycloak.realms.admin-realms.client.client-id", () -> "admin-cli");
//        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> kcBaseUrl + "/realms/shop");
//    }
}

