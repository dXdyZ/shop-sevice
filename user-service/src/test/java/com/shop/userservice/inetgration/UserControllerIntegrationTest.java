package com.shop.userservice.inetgration;

import com.shop.userservice.dto.ErrorResponse;
import com.shop.userservice.dto.UserDto;
import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.entity.User;
import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.keycloak.KeycloakEmailService;
import com.shop.userservice.keycloak.KeycloakService;
import com.shop.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
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
import static org.mockito.Mockito.*;


@Slf4j
@Sql("/data.sql")
public class UserControllerIntegrationTest extends BasePostgresIntegrationTest{

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private KeycloakService keycloakService;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private KeycloakEmailService keycloakEmailService;

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
        when(keycloakService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("11111111-1111-1111-1111-111111111111");
        doNothing().when(keycloakEmailService).sendVerifyEmail(anyString(), anyString(), anyString());


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

        verify(keycloakService, times(1))
                .createUser(eq("testUser"), eq("Test"), eq("User"), eq(email), eq("password"));
    }

    @Test
    void registerUser_ShouldReturnConflict_WhenUserWithSameUsernameExistsInKeycloak() throws InterruptedException {
        when(keycloakService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new UserDuplicateException("User exists with same username"));

        String username = "another";

        UserRegistrationDto userReg = UserRegistrationDto.builder()
                .username(username)
                .lastName("User")
                .firstName("Test")
                .patronymic("User")
                .phoneNumber("+79062345324")
                .email("anotherTest@gmail.com")
                .password("password")
                .build();

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

    @Test
    void registerUser_ShouldReturnConflict_WhenUserWithSameEmailExistsInKeycloak() {
        when(keycloakService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new UserDuplicateException("User exists with same email"));

        String email = "testEmailExists@gmail.com";


        UserRegistrationDto userReg = UserRegistrationDto.builder()
                .username("NewUser")
                .lastName("User")
                .firstName("Test")
                .patronymic("User")
                .phoneNumber("+79062345324")
                .email(email)
                .password("password")
                .build();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/registration",
                userReg,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().getMessageCode()).isEqualTo("DUPLICATE_USER");
        assertThat(response.getBody().getMessage()).isEqualTo("User exists with same email");
    }
}














