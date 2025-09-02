package com.shop.userservice.keycloak;

import com.shop.userservice.exception.ExternalServiceUnavailableException;
import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.exception.UserNotFoundException;
import com.shop.userservice.util.LogMarker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class KeycloakService {
    private static final long SLOW_THRESHOLD_MS = 500L;

    private final Keycloak keycloak;
    private final String realm;
    private final KeycloakEmailService keycloakEmailService;

    public KeycloakService(Keycloak keycloak,
                           @Value("${keycloak.realms.service-realms.realm}") String realm,
                           KeycloakEmailService keycloakEmailService) {
        this.keycloak = keycloak;
        this.realm = realm;
        this.keycloakEmailService = keycloakEmailService;
    }

    /**
     * @param username  Login для авторизации
     * @param firstName  Имя пользователя
     * @param lastName  Фамилия пользователя
     * @param email  Электронная почта пользователя
     * @param password  Пароль пользователя
     * @return UUID пользователя в keycloak будет использоваться для идентификации в других сервисах
     * @throws UserDuplicateException - Выбрасывается если username/email заняты
     */
    @Retry(name = "keycloakCreateUser", fallbackMethod = "createUserFallback")
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
                    log.warn(LogMarker.APP_CALL.getMarker(), "service=Keycloak | action=createUser | SLOW | ms={} | username={} | email={} | userUUID={}",
                            elapsed, username, email, userId);
                } else {
                    log.debug(LogMarker.APP_CALL.getMarker(), "service=Keycloak | action=createUser | ms={} | username={} | email={} | userUUID={}",
                            elapsed, username, email, userId);
                }

                keycloakEmailService.sendVerifyEmail(userId, username, email);

                log.info(LogMarker.AUDIT.getMarker(), "action=createUser | userId={} | username={} | status=SUCCESS", userId, username);

                return userId;
            } else if (status == 409){
                ErrorRepresentation errorRepresentation = null;
                try {
                    errorRepresentation = response.readEntity(ErrorRepresentation.class);
                } catch (Exception ignored) {}

                if (errorRepresentation != null && errorRepresentation.getErrorMessage() != null) {
                    throw new UserDuplicateException(errorRepresentation.getErrorMessage());
                }
            } else {
                String errMsg = null;
                try {
                    errMsg = response.readEntity(String.class);
                } catch (Exception ignored) {}

                log.error(LogMarker.ERROR.getMarker(), "service=Keycloak | returned UNEXPECTED status | status={} | username={} | body={}",
                        response.getStatus(), username, errMsg);
            }
        } catch (ProcessingException exception) {
            throw exception;
        }
        return null;
    }

    private String extractUserId(Response response) {
        String location = response.getLocation().getPath();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    @Retry(name = "keycloakDeleteUser", fallbackMethod = "deleteUserByUUIDFallback")
    public void deleteUserByUUID(String uuid) {
        try {
            long start = System.currentTimeMillis();

            UserResource userResource = keycloak.realm(realm).users().get(uuid);

            try {
                userResource.toRepresentation();
            } catch (NotFoundException ignore) {
                throw new UserNotFoundException("User by uuid: %s not found".formatted(uuid));

            }

            userResource.remove();

            long elapsed = System.currentTimeMillis() - start;

            if (elapsed > SLOW_THRESHOLD_MS) {
                log.warn(LogMarker.APP_CALL.getMarker(), "service=Keycloak | action=deleteUser | SLOW | ms={} | userUUID={}",
                        elapsed, uuid);
            }

        } catch (WebApplicationException exception) {
            throw exception;
        }
    }

    public String createUserFallback(String username, String firstName,
                                   String lastName, String email, String password, Throwable throwable) {

        log.error(LogMarker.INFRA_ERROR.getMarker(), "service=Keycloak | CONNECTION ERROR | cause={}", throwable.getMessage());

        throw new ExternalServiceUnavailableException("Connection service error");
    }

    public void deleteUserByUUIDFallback(String uuid, Throwable throwable) {
        log.warn(LogMarker.ERROR.getMarker(), "service=Keycloak | error DELETE user | userId={} | message={}",
                uuid, throwable.getMessage());

        throw new ExternalServiceUnavailableException("Connection service error");
    }
}
