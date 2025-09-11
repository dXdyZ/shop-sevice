package com.shop.userservice.inetgration;

import com.shop.userservice.dto.*;
import com.shop.userservice.entity.User;
import com.shop.userservice.exception.ExternalServiceUnavailableException;
import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.inetgration.config.BasePostgresIntegrationTest;
import com.shop.userservice.keycloak.KeycloakEmailService;
import com.shop.userservice.keycloak.KeycloakService;
import com.shop.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
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
@Sql(scripts = {"/clean.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserControllerIntegrationTest extends BasePostgresIntegrationTest {

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

    @Test
    void registerUser_ShouldReturnServerInternalError_WhenServiceKeycloakDoesntRespond() {
        when(keycloakService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new ExternalServiceUnavailableException("Connection service error"));

        UserRegistrationDto userReg = UserRegistrationDto.builder()
                .username("NewUser")
                .lastName("User")
                .firstName("Test")
                .patronymic("User")
                .phoneNumber("+79062345324")
                .email("userIternalServer@gmail.com")
                .password("password")
                .build();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/registration",
                userReg,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().getMessageCode()).isEqualTo("SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Connection service error");
    }

    @Test
    void getByUUID_ShouldReturnSuccessResponse_WhenUserExist() {
        UUID userUUID = UUID.fromString("401bd388-7359-454b-b142-cfc9c559951c");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("test-token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                "/api/v1/by-uuid/{uuid}",
                HttpMethod.GET,
                entity,
                UserDto.class,
                userUUID
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserUUID()).isEqualTo(userUUID);
    }

    @Test
    void getByUUID_ShouldReturnNotFoundResponse_WhenUserDoesNotExist() {
        UUID userUUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("test-token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/by-uuid/{uuid}",
                HttpMethod.GET,
                entity,
                ErrorResponse.class,
                userUUID
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessageCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("User by uuid: %s not found".formatted(userUUID.toString()));
    }

    @Test
    void getByUUID_ShouldReturnBadRequest_WhenEmptyUUIDPassed() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("test-token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/by-uuid/{uuid}",
                HttpMethod.GET,
                entity,
                ErrorResponse.class,
                " "
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessageCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().getDetails()).isEqualTo(Map.of("uuid", "The user's UUID must be specified"));
    }

    @Test
    void searchUser_ShouldReturnSuccessResponse_WhenUserExist() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("test-token");


        UserSearchDto searchDto = new UserSearchDto("User", null,
                null, null, null, null, null);


        HttpEntity<UserSearchDto> entity = new HttpEntity<>(searchDto, headers);

        ResponseEntity<PageResponse<UserDto>> response = restTemplate.exchange(
                "/api/v1/search",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PageResponse<UserDto> body = response.getBody();


        assertThat(body.getTotalElement()).isEqualTo(1);
        assertThat(body.getTotalPages()).isEqualTo(1);

        UserDto result = body.getContent().get(0);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("User");
        assertThat(result.getEmail()).isEqualTo("user@gmail.com");
    }

    @Test
    void searchUser_ShouldReturnSuccessResponse_WhenUserDoesNotExist() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("test-token");


        UserSearchDto searchDto = new UserSearchDto("Hello", null,
                null, null, null, null, null);


        HttpEntity<UserSearchDto> entity = new HttpEntity<>(searchDto, headers);

        ResponseEntity<PageResponse<UserDto>> response = restTemplate.exchange(
                "/api/v1/search",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PageResponse<UserDto> body = response.getBody();


        assertThat(body.getTotalElement()).isEqualTo(0);
        assertThat(body.getTotalPages()).isEqualTo(0);
    }

    @Test
    void getUserPagingAndSort_ShouldReturnSuccessResult_WhenUserExist() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("admin-token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PageResponse<UserDto>> response = restTemplate.exchange(
                "/api/v1",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PageResponse<UserDto> body = response.getBody();

        assertThat(body.getTotalElement()).isEqualTo(2);
        assertThat(body.getTotalPages()).isEqualTo(1);

        assertThat(body.getContent())
                .isNotEmpty()
                .hasSize(2)
                .extracting(UserDto::getEmail)
                .containsExactly("user@gmail.com", "maria.sidorova@example.com");
    }

    @Test
    void getUserPagingAndSort_ShouldReturnSuccessResult_WhenUserDoesNotExist() {
        userRepository.deleteAll();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("admin-token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PageResponse<UserDto>> response = restTemplate.exchange(
                "/api/v1",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalPages()).isEqualTo(0);
        assertThat(response.getBody().getTotalElement()).isEqualTo(0);
    }
}















