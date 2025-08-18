package com.shop.userservice.keycloak;

import com.shop.userservice.exception.UserDuplicateException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class KeycloakService {
    private final Keycloak keycloak;

    private final String realm;

    public KeycloakService(Keycloak keycloak,
                           @Value("${keycloak.realms.service-realms.realm}") String realm) {
        this.keycloak = keycloak;
        this.realm = realm;
    }

    public String createUser(String username, String firstName,
                             String lastName, String email, String password) throws UserDuplicateException{
        isUsernameAndEmailTaken(username, email);

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


        try (Response response = keycloak.realm(realm).users().create(user)) {
            if (response.getStatus() == 201) {
                String userId = extractUserId(response);

                assignRolesToUser(userId);

                keycloak.realm(realm)
                        .users()
                        .get(userId)
                        .sendVerifyEmail();

                return userId;
            } else {
                log.error("Error creating user in the keycloak");
            }
        }
        return null;
    }

    private String extractUserId(Response response) {
        String location = response.getLocation().getPath();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private void assignRolesToUser(String userId) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);

        RolesResource realmRoles = keycloak.realm(realm).roles();

        List<RoleRepresentation> rolesToAdd = realmRoles.list().stream()
                .filter(role -> role.getName().equals("app-user"))
                .toList();

        if (!rolesToAdd.isEmpty()) {
            userResource.roles().realmLevel().add(rolesToAdd);
        } else {
            log.error("Role user not found in realm roles");
        }
    }

    private void isUsernameAndEmailTaken(String username, String email) {
        try {
            List<UserRepresentation> usernameRep = keycloak.realm(realm).users()
                    .searchByUsername(username, true);
            List<UserRepresentation> emailRep = keycloak.realm(realm).users()
                    .searchByEmail(email, true);

            if (!emailRep.isEmpty()) {
                throw new UserDuplicateException("The user with this email already exists");
            }
            if (!usernameRep.isEmpty()) {
                throw new UserDuplicateException("The user with this username already exists");
            }
        } catch (Exception e) {
            log.error("Duplicate check filed", e);
        }
    }
}
