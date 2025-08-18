package com.shop.userservice.keycloak;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    private final String serverUrl;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminRealms;
    private final String adminClientId;

    public KeycloakConfig(@Value("${keycloak.server-url}") String serverUrl,
                          @Value("${keycloak.realms.admin-realms.username}") String adminUsername,
                          @Value("${keycloak.realms.admin-realms.password}") String adminPassword,
                          @Value("${keycloak.realms.admin-realms.realm}") String adminRealms,
                          @Value("${keycloak.realms.admin-realms.client.client-id}") String adminClientId) {
        this.serverUrl = serverUrl;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminRealms = adminRealms;
        this.adminClientId = adminClientId;
    }

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(adminRealms)
                .clientId(adminClientId)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }
}
