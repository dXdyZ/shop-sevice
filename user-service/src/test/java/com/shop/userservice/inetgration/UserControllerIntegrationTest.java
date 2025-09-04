package com.shop.userservice.inetgration;

import com.shop.userservice.dto.ErrorResponse;
import com.shop.userservice.dto.UserDto;
import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.entity.User;
import com.shop.userservice.keycloak.KeycloakEmailService;
import com.shop.userservice.keycloak.KeycloakService;
import com.shop.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;



@Slf4j
@Sql("/data.sql")
public class UserControllerIntegrationTest extends BasePostgresIntegrationTest{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private KeycloakEmailService keycloakEmailService;


    @BeforeEach
    void setupMocks() {
        doNothing().when(keycloakEmailService).sendVerifyEmail(anyString(), anyString(), anyString());
    }

    @Test
    void getUserById_ShouldReturnSuccessResponse_WhenUserExist() {
        long userId = 1L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("test-token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                "/api/v1/{id}",
                HttpMethod.GET,
                entity,
                UserDto.class,
                userId
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(userId);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() {
        long userId = 10L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("test-token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/{id}",
                HttpMethod.GET,
                entity,
                ErrorResponse.class,
                userId
        );

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessageCode()).isEqualTo("USER_NOT_FOUND");
    }

    @Test
    void getUserById_ShouldReturnBadRequest_WhenInvalidIdPassed() {
        long userId = -1;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("test-token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/{id}",
                HttpMethod.GET,
                entity,
                ErrorResponse.class,
                userId
        );

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessageCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().getDetails()).isEqualTo(Map.of("id", "The user id must be greater than zero"));
    }

    @Test
    void registrationUser_ShouldReturnSuccessResponse_WhenDuplicateUserDoesNotExist() {
        String email = "userTest@gmail.com";

        UserRegistrationDto userReg = UserRegistrationDto.builder()
                .username("testUser")
                .lastName("User")
                .firstName("Test")
                .patronymic("User")
                .phoneNumber("+79062345324")
                .email(email)
                .password("password")
                .build();

        ResponseEntity<?> response = restTemplate.postForEntity(
                "/api/v1/registration",
                userReg,
                Void.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        Optional<User> newUser = userRepository.findByEmail(email);
        assertThat(newUser).isPresent();
        assertThat(newUser.get().getUserUUID()).isNotNull().isInstanceOf(UUID.class);
    }

    @Test
    void registerUser_ShouldReturnConflict_WhenUserWithSameUsernameExistsInKeycloak() throws InterruptedException {
        String username = "another";

        keycloakService.createUser(username, "NewTestUser", "NewTestUserLastName", "newTestUser@gmail.com", "password");

        UserRegistrationDto userReg = UserRegistrationDto.builder()
                .username(username)
                .lastName("User")
                .firstName("Test")
                .patronymic("User")
                .phoneNumber("+79062345324")
                .email("anotherTest@gmail.com")
                .password("password")
                .build();

        Thread.sleep(5000);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/registration",
                userReg,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().getMessageCode()).isEqualTo("DUPLICATE_USER");
        assertThat(response.getBody().getMessage()).isEqualTo("User exists with same username");
    }
}














