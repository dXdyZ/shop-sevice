package com.shop.userservice.keycloak;

import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.exception.UserNotFoundException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

    @Mock
    private KeycloakEmailService keycloakEmailService;

    @Mock
    private Keycloak keycloak;

    @Mock
    private UserResource userResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private RealmResource resource;

    private KeycloakService keycloakService;
    private Response response;

    private final String username = "user";
    private final String firstName = "user";
    private final String lastName = "user";
    private final String email = "user@gmail.com";
    private final String password = "password";

    @BeforeEach
    void setUp() throws URISyntaxException {
        keycloakService = new KeycloakService(keycloak, "shop", keycloakEmailService);

        response = mock(Response.class);

        when(keycloak.realm(anyString())).thenReturn(resource);
        when(resource.users()).thenReturn(usersResource);

    }

    @Test
    void createUserWhenDuplicateUserDoesNotExist() throws URISyntaxException {
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(new URI("/user/123"));


        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        String userId = keycloakService.createUser(username, firstName, lastName, email, password);

        assertThat(userId).isNotNull();
        verify(usersResource).create(argThat(user ->
                user.getUsername().equals(username) &&
                user.getFirstName().equals(firstName) &&
                user.getLastName().equals(lastName) &&
                user.getEmail().equals(email)));

        verify(keycloakEmailService).sendVerifyEmail("123", username, email);

        assertEquals("123", userId);
    }

    @Test
    void createUserWhenDuplicateUserExistThenThrowUserDuplicateException() {

        when(response.getStatus()).thenReturn(409);
        when(response.readEntity(ErrorRepresentation.class)).thenReturn(new ErrorRepresentation("email", "User with email exists", new Object[]{"email"}));
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        UserDuplicateException exception = assertThrows(UserDuplicateException.class,
                () -> keycloakService.createUser(username, firstName, lastName, email, password));

        verifyNoInteractions(keycloakEmailService);
        assertEquals("User with email exists", exception.getMessage());
    }

    @Test
    void deleteUserByUUIDWhenUserByUUIDExist() {
        when(usersResource.get("123")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(new UserRepresentation());

        keycloakService.deleteUserByUUID("123");

        verify(keycloak).realm("shop");
        verify(resource).users();
        verify(usersResource).get("123");
        verify(userResource).toRepresentation();
        verify(userResource).remove();
    }

    @Test
    void deleteUserByUUIDWhenUserNotFoundThenThrowUserNotFoundException() {
        when(usersResource.get("123")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(new NotFoundException("User not found"));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> keycloakService.deleteUserByUUID("123"));

        assertEquals("User by uuid: 123 not found", exception.getMessage());
        verify(userResource, never()).remove();
    }
}










