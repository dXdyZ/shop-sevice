package com.shop.userservice.keycloak;

import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.ws.rs.ProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class KeycloakEmailService {
    private static final Marker NOTIFY = MarkerFactory.getMarker("NOTIFY");
    private static final Marker ERROR = MarkerFactory.getMarker("ERROR");
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

    private static final long SLOW_THRESHOLD_MS = 5000L;

    private final Keycloak keycloak;
    private final Timer verifyEmail;
    private final String realm;

    public KeycloakEmailService(Keycloak keycloak, MeterRegistry meterRegistry,
                                @Value("${keycloak.realms.service-realms.realm}") String realm) {
        this.keycloak = keycloak;
        this.realm = realm;
        this.verifyEmail = Timer.builder("keycloak_verify_email_duration_second")
                .description("Duration of Keycloak verify email calls")
                .tags("service", "keycloak", "action", "verifyEmail")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .sla(
                        Duration.ofMillis(100),
                        Duration.ofMillis(300),
                        Duration.ofMillis(500),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(3),
                        Duration.ofSeconds(4),
                        Duration.ofSeconds(5),
                        Duration.ofSeconds(6),
                        Duration.ofSeconds(8),
                        Duration.ofSeconds(10)
                )
                .register(meterRegistry);
    }

    @Async
    @Retry(name = "keycloakVerifyEmail", fallbackMethod = "sendVerifyEmailFallback")
    public void sendVerifyEmail(String userId, String username, String email) {
        try {
            long start = System.currentTimeMillis();

            verifyEmail.record(() -> {
                keycloak.realm(realm)
                        .users()
                        .get(userId)
                        .sendVerifyEmail();
            });

            long elapsed = System.currentTimeMillis() - start;

            if (elapsed > SLOW_THRESHOLD_MS) {
                log.warn(NOTIFY, "service=Keycloak | action=verifyEmail | SLOW | ms={} | username={} | email={} | userUUID={}",
                        elapsed, username, email, userId);
            } else {
                log.debug(NOTIFY, "service=Keycloak | action=verifyEmail | ms={} | username={} | email={} | userUUID={}",
                        elapsed, username, email, userId);
            }

            log.info(AUDIT,  "action=verifyEmail | userId={} | username={} | status=SUCCESS", userId, username);

        } catch (ProcessingException exception) {
            throw exception;
        }
    }

    public void sendVerifyEmailFallback(String userId, String username, String email, Throwable throwable) {
        log.error(ERROR, "service=Keycloak | verifyEmail FAILED after retries | username={} | email={} | userId={} | cause={}",
                username, email, userId, throwable.getMessage());
    }
}
