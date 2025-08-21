package com.shop.userservice.keycloak;

import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KeycloakEmailService {
    private static final Marker NOTIFY = MarkerFactory.getMarker("NOTIFY");
    private static final Marker ERROR = MarkerFactory.getMarker("ERROR");

    private static final long SLOW_THRESHOLD_MS = 5000L;

    private final Keycloak keycloak;
    private final MeterRegistry meterRegistry;
    private final String realm;

    public KeycloakEmailService(Keycloak keycloak, MeterRegistry meterRegistry,
                                @Value("${keycloak.realms.service-realms.realm}") String realm) {
        this.keycloak = keycloak;
        this.meterRegistry = meterRegistry;
        this.realm = realm;

    }

    @Async
    @Retry(name = "keycloakVerifyEmail", fallbackMethod = "sendVerifyEmailFallback")
    public void sendVerifyEmail(String userId, String username, String email) {
        meterRegistry.timer("keycloak_verify_email_duration_second", "service", "keycloak", "action", "verifyEmail")
                .record(() -> {
                    try {

                        long start = System.currentTimeMillis();

                        keycloak.realm(realm)
                                .users()
                                .get(userId)
                                .sendVerifyEmail();

                        long elapsed = System.currentTimeMillis() - start;

                        if (elapsed > SLOW_THRESHOLD_MS) {
                            log.warn(NOTIFY, "service=Keycloak | action=verifyEmail | SLOW | ms={} | username={} | email={} | userUUID={}",
                                    elapsed, username, email, userId);
                        } else {
                            log.debug(NOTIFY, "service=Keycloak | action=verifyEmail | ms={} | username={} | email={} | userUUID={}",
                                    elapsed, username, email, userId);
                        }

                    } catch (RuntimeException exception) {
                        throw exception;
                    }
                });
    }

    public void sendVerifyEmailFallback(String userId, String username, String email, Throwable throwable) {
        log.error(ERROR, "service=Keycloak | verifyEmail FAILED after retries | username={} | email={} | userId={} | cause={}",
                username, email, userId, throwable.getMessage());
    }
}
