package com.shop.userservice.keycloak;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KeycloakServiceTest {

    private static final String REALM = "shop";
    private static final String USERNAME = "user1";
    private static final String EMAIL = "user1@test.dev";

    private Response resp201(String id) {
        return Response.status(201)
                .location(URI.create("http://localhost:8080/admin/realms/" + REALM + "/users/" + id))
                .build();
    }

    @Test
    @DisplayName("Создание: 201 + роль app-user существует")
    void createUser_success() throws Exception {
        String userId = UUID.randomUUID().toString();

        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);
        RoleMappingResource roleMapping = mock(RoleMappingResource.class);
        RoleScopeResource roleScope = mock(RoleScopeResource.class);
        RolesResource rolesResource = mock(RolesResource.class);

        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(resp201(userId));
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMapping);
        when(roleMapping.realmLevel()).thenReturn(roleScope);

        when(realmResource.roles()).thenReturn(rolesResource);
        RoleRepresentation appRole = new RoleRepresentation();
        appRole.setName("app-user");
        when(rolesResource.list()).thenReturn(List.of(appRole));

        doNothing().when(userResource).sendVerifyEmail();

        KeycloakService service = new KeycloakService(keycloak, REALM);

        String returned = service.createUser(USERNAME, "F", "L", EMAIL, "pwd12345");
        assertThat(returned).isEqualTo(userId);

        ArgumentCaptor<UserRepresentation> captor = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(usersResource).create(captor.capture());
        UserRepresentation sent = captor.getValue();
        assertThat(sent.getUsername()).isEqualTo(USERNAME);
        assertThat(sent.getEmail()).isEqualTo(EMAIL);

        verify(roleScope).add(argThat(list ->
                list.stream().anyMatch(r -> "app-user".equals(r.getName()))
        ));
        verify(userResource).sendVerifyEmail();
    }

    @Test
    @DisplayName("Роль app-user отсутствует — всё равно 201, роль не назначена")
    void createUser_noRole() throws Exception {
        String userId = UUID.randomUUID().toString();
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);
        RoleMappingResource roleMapping = mock(RoleMappingResource.class);
        RoleScopeResource roleScope = mock(RoleScopeResource.class);
        RolesResource rolesResource = mock(RolesResource.class);

        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(resp201(userId));
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMapping);
        when(roleMapping.realmLevel()).thenReturn(roleScope);

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.list()).thenReturn(List.of()); // нет роли

        doNothing().when(userResource).sendVerifyEmail();

        KeycloakService service = new KeycloakService(keycloak, REALM);
        String returned = service.createUser(USERNAME, "F", "L", EMAIL, "pwd12345");

        assertThat(returned).isEqualTo(userId);
        verify(roleScope, never()).add(any());
    }

    @Test
    @DisplayName("Создание не 201 (500) — метод возвращает null (текущее поведение)")
    void createUser_500() throws Exception {
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);

        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(Response.status(500).build());

        KeycloakService service = new KeycloakService(keycloak, REALM);
        String ret = service.createUser(USERNAME, "F", "L", EMAIL, "pwd");
        assertThat(ret).isNull();
    }

    @Test
    @DisplayName("Исключение в sendVerifyEmail пробрасывается")
    void createUser_sendVerifyEmailThrows() throws Exception {
        String userId = UUID.randomUUID().toString();
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);
        RoleMappingResource roleMapping = mock(RoleMappingResource.class);
        RoleScopeResource roleScope = mock(RoleScopeResource.class);
        RolesResource rolesResource = mock(RolesResource.class);

        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(resp201(userId));
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMapping);
        when(roleMapping.realmLevel()).thenReturn(roleScope);

        when(realmResource.roles()).thenReturn(rolesResource);
        RoleRepresentation appRole = new RoleRepresentation();
        appRole.setName("app-user");
        when(rolesResource.list()).thenReturn(List.of(appRole));

        doThrow(new InternalServerErrorException("Mail fail"))
                .when(userResource).sendVerifyEmail();

        KeycloakService service = new KeycloakService(keycloak, REALM);

        assertThatThrownBy(() ->
                service.createUser(USERNAME, "F", "L", EMAIL, "pwd")
        ).isInstanceOf(InternalServerErrorException.class)
                .hasMessageContaining("Mail fail");
    }
}