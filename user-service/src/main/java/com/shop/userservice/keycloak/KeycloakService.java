package com.shop.userservice.keycloak;

import com.shop.userservice.exception.KeycloakUserCreationException;
import com.shop.userservice.exception.UserDuplicateException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class KeycloakService {
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    private static final Marker APP_CALL = MarkerFactory.getMarker("APP_CALL");
    private static final Marker ERROR = MarkerFactory.getMarker("ERROR");

    private static final long SLOW_THRESHOLD_MS = 500L;

    private final Keycloak keycloak;
    private final String realm;
    private final KeycloakEmailService keycloakEmailService;

    public KeycloakService(Keycloak keycloak, MeterRegistry materRegistry,
                           @Value("${keycloak.realms.service-realms.realm}") String realm,
                           KeycloakEmailService keycloakEmailService) {
        this.keycloak = keycloak;
        this.realm = realm;
        this.keycloakEmailService = keycloakEmailService;
    }

    /**
     * @param username - Login для авторизации
     * @param firstName - Имя пользователя
     * @param lastName - Фамилия пользователя
     * @param email - Электронная почта пользователя
     * @param password - Пароль пользователя
     * @return UUID пользователя в keycloak
     * @throws UserDuplicateException - Выбрасывается если username/email заняты
     */
    public String createUser(String username, String firstName,
                             String lastName, String email, String password) throws UserDuplicateException{

        long start = System.currentTimeMillis();

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setRequiredActions(List.of("VERIFY_EMAIL"));
        user.setCredentials(List.of(credential));
        user.setRealmRoles(List.of("app-user"));



        try (Response response = keycloak.realm(realm).users().create(user)) {
            int status = response.getStatus();

            long elapsed = System.currentTimeMillis() - start;

            if (response.getStatus() == 201) {
                String userId = extractUserId(response);

                if (elapsed > SLOW_THRESHOLD_MS) {
                    log.warn(APP_CALL, "service=Keycloak | action=createUser | SLOW | ms={} | username={} | email={} | userUUID={}",
                            elapsed, username, email, userId);
                } else {
                    log.debug(APP_CALL, "service=Keycloak | action=createUser | ms={} | username={} | email={} | userUUID={}",
                            elapsed, username, email, userId);
                }

                keycloakEmailService.sendVerifyEmail(userId, username, email);

                log.info(AUDIT, "action=createUser | userId={} | username={} | status=SUCCESS", userId, username);

                return userId;
            } else if (status == 409){
                ErrorRepresentation errorRepresentation = null;
                try {
                    errorRepresentation = response.readEntity(ErrorRepresentation.class);
                } catch (Exception e) {

                }
                if (errorRepresentation != null && errorRepresentation.getErrorMessage() != null) {
                    throw new UserDuplicateException(errorRepresentation.getErrorMessage());
                }
            }
        } catch (RuntimeException exception) {

        }
        return null;
    }

    private String extractUserId(Response response) {
        String location = response.getLocation().getPath();
        return location.substring(location.lastIndexOf('/') + 1);
    }
}
