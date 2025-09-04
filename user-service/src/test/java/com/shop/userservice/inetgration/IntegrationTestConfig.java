package com.shop.userservice.inetgration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class IntegrationTestConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return token -> {
            // Текущее время для расчета временных меток
            Instant now = Instant.now();

            return Jwt.withTokenValue("integration-test-token")
                    // Заголовки
                    .header("alg", "RS256")
                    .header("typ", "Bearer")
                    .header("kid", "test-key-id")

                    // Claims из вашего реального токена
                    .claim("exp", now.plusSeconds(3600).getEpochSecond())  // Expiration time
                    .claim("iat", now.getEpochSecond())                     // Issued at
                    .claim("jti", "test-jwt-id")                            // JWT ID
                    .claim("iss", "http://localhost:8080/realms/shop")      // Issuer (ВАЖНО: должен совпадать с application.yml)
                    .claim("aud", "account")                                // Audience
                    .claim("sub", "401bd388-7359-454b-b142-cfc9c559951c")   // Subject (user ID)
                    .claim("typ", "Bearer")                                 // Token type
                    .claim("azp", "user-service")                           // Authorized party
                    .claim("sid", "fbe5fac3-2b47-4f83-9592-2887c8c929e7")   // Session ID
                    .claim("acr", "1")                                      // Authentication context class
                    .claim("allowed-origins", List.of("http://localhost:9090"))

                    // Роли пользователя (ВАЖНО для @PreAuthorize и hasRole())
                    .claim("realm_access", Map.of(
                            "roles", List.of(
                                    "offline_access",
                                    "default-roles-shop",
                                    "uma_authorization",
                                    "app-user"  // Эта роль будет использоваться для проверок доступа
                            )
                    ))

                    // Роли для ресурса account
                    .claim("resource_access", Map.of(
                            "account", Map.of(
                                    "roles", List.of(
                                            "manage-account",
                                            "manage-account-links",
                                            "view-profile"
                                    )
                            )
                    ))

                    // Scopes
                    .claim("scope", "openid profile email")

                    // Информация о пользователе
                    .claim("email_verified", true)
                    .claim("name", "User User")
                    .claim("spring_sec_roles", List.of(  // Spring Security roles
                            "offline_access",
                            "default-roles-shop",
                            "uma_authorization",
                            "app-user"
                    ))
                    .claim("preferred_username", "user")  // Используется в Spring Security
                    .claim("given_name", "User")
                    .claim("family_name", "User")
                    .claim("email", "user@gmail.com")

                    // Временные метки
                    .issuedAt(now)                          // iat
                    .expiresAt(now.plusSeconds(3600))       // exp
                    .build();
        };
    }
}
