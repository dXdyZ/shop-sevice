package com.shop.userservice.inetgration.keycloak;

import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.exception.UserNotFoundException;
import com.shop.userservice.keycloak.KeycloakEmailService;
import com.shop.userservice.keycloak.KeycloakService;
import com.shop.userservice.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration"
        }
)
@Testcontainers
@ActiveProfiles({"integration", "no-security"})
class KeycloakServiceIntegrationTest {

    private static final String REALM = "shop";


    @Container
    static final GenericContainer<?> keycloak = new GenericContainer<>("quay.io/keycloak/keycloak:26.3")
            .withExposedPorts(8080)
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("config/realm-export.json"),
                    "/opt/keycloak/data/import/realm-test.json"
            )
            .withCommand("start-dev --import-realm")
            .waitingFor(
                    Wait.forHttp("/realms/master")
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofSeconds(50))
            );

    @DynamicPropertySource
    static void dynamicProperty(DynamicPropertyRegistry registry) {
        String kcBaseUrl = "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080);

        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("keycloak.server-url", () -> kcBaseUrl);
        registry.add("keycloak.realms.service-realms.realm", () -> REALM);
        registry.add("keycloak.realms.admin-realms.realm", () -> "master");
        registry.add("keycloak.realms.admin-realms.username", () -> "admin");
        registry.add("keycloak.realms.admin-realms.password", () -> "admin");
        registry.add("keycloak.realms.admin-realms.client.client-id", () -> "admin-cli");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> kcBaseUrl + "/realms/" + REALM);
    }

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private KeycloakEmailService keycloakEmailService;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private org.keycloak.admin.client.Keycloak keycloakAdmin;

    private final List<String> createdUserIds = new ArrayList<>();

    @AfterEach
    void cleanup() {
        for (String id : createdUserIds) {
            try {
                keycloakAdmin.realm(REALM).users().get(id).remove();
            } catch (Exception ignored) {}
        }
        createdUserIds.clear();
    }

    @Test
    @DisplayName("createUser: возвращает UUID и вызывает отправку письма при валидных данных")
    void createUser_ShouldReturnUserUUID_AndSendVerifyEmail_WhenDataValid() {
        doNothing().when(keycloakEmailService).sendVerifyEmail(anyString(), anyString(), anyString());

        String unique = UUID.randomUUID().toString().substring(0, 8);
        String username = "user_" + unique;
        String firstName = "First";
        String lastName = "Last";
        String email = "u_" + unique + "@example.com";
        String password = "Passw0rd!";

        String userId = keycloakService.createUser(username, firstName, lastName, email, password);
        createdUserIds.add(userId);

        assertThat(userId)
                .as("UUID возвращён")
                .isNotNull()
                .hasSize(36);

        verify(keycloakEmailService, times(1))
                .sendVerifyEmail(eq(userId), eq(username), eq(email));

        var userRep = keycloakAdmin.realm(REALM).users().get(userId).toRepresentation();

        assertThat(userRep.getUsername()).isEqualTo(username);
        assertThat(userRep.getEmail()).isEqualTo(email);
        assertThat(userRep.getFirstName()).isEqualTo(firstName);
        assertThat(userRep.getLastName()).isEqualTo(lastName);
        assertThat(userRep.isEnabled()).isTrue();

        assertThat(userRep.getRequiredActions()).contains("VERIFY_EMAIL");

        var roles = keycloakAdmin.realm(REALM).users().get(userId).roles().realmLevel().listAll()
                .stream().map(RoleRepresentation::getName).toList();

        assertThat(roles).contains("app-user");
    }

    @Test
    @DisplayName("createUser: выбрасывает UserDuplicateException при конфликте username/email")
    void createUser_ShouldThrowUserDuplicateException_WhenUserAlreadyExists() {
        doNothing().when(keycloakEmailService).sendVerifyEmail(anyString(), anyString(), anyString());

        String unique = UUID.randomUUID().toString().substring(0, 8);
        String username = "dup_" + unique;
        String email = "dup_" + unique + "@example.com";

        String id1 = keycloakService.createUser(username, "F", "L", email, "Passw0rd!");
        createdUserIds.add(id1);

        assertThatThrownBy(() ->
                keycloakService.createUser(username, "F2", "L2", email, "OtherPass!")
        ).isInstanceOf(UserDuplicateException.class);
    }

    @Test
    @DisplayName("deleteUserByUUID: нечего не возвращает если удаляемый пользователь существует")
    void deleteUserByUUID_ShouldVoidReturn_WhenUserExist() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String username = "dup_" + unique;
        String email = "dup_" + unique + "@example.com";

        String id = keycloakService.createUser(username, "F", "L", email, "Passw0rd!");
        createdUserIds.add(id);

        keycloakService.deleteUserByUUID(id);

        assertThatThrownBy(() ->
                keycloakAdmin.realm(REALM).users().get(id).toRepresentation()
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteUserByUUIDThrowUserNotFoundException_WhenUserDoesNotExist() {
        String id = UUID.randomUUID().toString();

        assertThatThrownBy(() ->
                keycloakService.deleteUserByUUID(id)
        ).isInstanceOf(UserNotFoundException.class);
    }
}






