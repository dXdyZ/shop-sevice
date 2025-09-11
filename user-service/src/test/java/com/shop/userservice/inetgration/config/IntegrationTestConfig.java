package com.shop.userservice.inetgration.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class IntegrationTestConfig {

    /**
     * Простой тестовый JwtDecoder, который "декодирует" строку токена
     * и в зависимости от ее значения возвращает Jwt с разными ролями.
     *
     * Использование в тесте:
     *   headers.setBearerAuth("test-token");      // ROLE_USER
     *   headers.setBearerAuth("admin-token");     // ROLE_ADMIN (и можно добавить USER при желании)
     *
     * При необходимости можно расширить логику парсинга.
     */
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return tokenValue -> {
            Instant now = Instant.now();

            // Базовые (не "бизнес") роли/клеймы - можете менять под себя
            List<String> springSecRoles = getStrings(tokenValue);

            return Jwt.withTokenValue(tokenValue)
                    .header("alg", "RS256")
                    .header("typ", "Bearer")
                    .header("kid", "test-key-id")
                    .claim("exp", now.plusSeconds(3600).getEpochSecond())
                    .claim("iat", now.getEpochSecond())
                    .claim("jti", "test-jwt-id")
                    .claim("iss", "http://localhost:8080/realms/shop")
                    .claim("aud", "account")
                    .claim("sub", "401bd388-7359-454b-b142-cfc9c559951c")
                    .claim("typ", "Bearer")
                    .claim("azp", "user-service")
                    .claim("sid", "fbe5fac3-2b47-4f83-9592-2887c8c929e7")
                    .claim("acr", "1")
                    .claim("allowed-origins", List.of("http://localhost:9090"))
                    .claim("realm_access", Map.of(
                            "roles", List.of(
                                    "offline_access",
                                    "default-roles-shop",
                                    "uma_authorization",
                                    "app-user" // эти realm роли можете оставить как есть; они не конвертятся в ROLE_ у вас
                            )
                    ))
                    .claim("resource_access", Map.of(
                            "account", Map.of(
                                    "roles", List.of(
                                            "manage-account",
                                            "manage-account-links",
                                            "view-profile"
                                    )
                            )
                    ))
                    .claim("scope", "openid profile email")
                    .claim("email_verified", true)
                    .claim("name", "User User")
                    // Вот этот список реально используется в вашем UserJwtConverter
                    .claim("spring_sec_roles", springSecRoles)
                    .claim("preferred_username", "user")
                    .claim("given_name", "User")
                    .claim("family_name", "User")
                    .claim("email", "user@gmail.com")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build();
        };
    }

    private static @NotNull List<String> getStrings(String tokenValue) {
        List<String> springSecRoles = new ArrayList<>(List.of(
                "offline_access",
                "default-roles-shop",
                "uma_authorization"
        ));

        // По умолчанию пользовательская роль
        if ("test-token".equals(tokenValue)) {
            springSecRoles.add("app-user");
        } else if ("admin-token".equals(tokenValue)) {
            // Админский токен. Решите — добавить ли и user.
            springSecRoles.add("app-admin");
            // Если хотите чтобы админ умел и как user:
            // springSecRoles.add("app-user");
        } else {
            // Fallback: анонимный/пользовательский сценарий (можно кидать исключение, если нужно)
            springSecRoles.add("app-user");
        }
        return springSecRoles;
    }
}
