package com.shop.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.entity.User;
import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.exception.UserNotFoundException;
import com.shop.userservice.keycloak.KeycloakService;
import com.shop.userservice.service.UserService;
import jakarta.ws.rs.core.MediaType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private KeycloakService keycloakService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUserByIdWhenUserByIdExist() throws Exception {
        long id = 1L;
        String email = "user@example.com";

        when(userService.getUserById(id)).thenReturn(User.builder()
                        .id(id)
                        .email(email)
                .build());

        mockMvc.perform(get("/api/v1/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }


    @Test
    void getUserByIdWhenUserByIdDoesntExistThenReturnNotFoundStatus() throws Exception {
        long id = 1L;

        when(userService.getUserById(id)).thenThrow(new UserNotFoundException("User by id: %s not found".formatted(id)));

        mockMvc.perform(get("/api/v1/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messageCode").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("User by id: 1 not found"));
    }

    @Test
    void getUserByIdWhenTransmittedIdLessThanZeroWhenReturnBadRequestStatus() throws Exception {
        long id = -1L;

        val jsonPathResultMatchers = jsonPath("$.messageCode");
        mockMvc.perform(get("/api/v1/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messageCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.details.id").value("The user ID must be greater than zero"));
    }

    @Test
    void registerUserWhenIsNoUserDuplication() throws Exception {
        var userReg = UserRegistrationDto.builder()
                .username("user")
                .lastName("User")
                .firstName("User")
                .email("user@example.com")
                .phoneNumber("+79991234567")
                .password("Pa55word!")
                .patronymic("Middle")
                .build();

        mockMvc.perform(post("/api/v1/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(objectMapper.writeValueAsString(userReg)))
                .andExpect(status().isCreated());
    }


    @Test
    void registrationUserWhenDuplicateUserExistThenReturnConflictStatus() throws Exception {
        var userReg = UserRegistrationDto.builder()
                .username("user")
                .lastName("User")
                .firstName("User")
                .email("user@example.com")
                .phoneNumber("+79991234567")
                .password("Pa55word!")
                .patronymic("Middle")
                .build();

        doThrow(new UserDuplicateException("User with email exists")).when(userService).registrationUser(userReg);

        mockMvc.perform(post("/api/v1/registration")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(userReg)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.messageCode").value("DUPLICATE_USER"))
                .andExpect(jsonPath("$.httpCode").value(409))
                .andExpect(jsonPath("$.message").value("User with email exists"));
    }

    @Test
    void registrationUserWhenInvalidEmailThenReturnBadRequest() throws Exception {
        var userReg = UserRegistrationDto.builder()
                .username("user")
                .lastName("User")
                .firstName("User")
                .email("hello-am")
                .phoneNumber("+79991234567")
                .password("Pa55word!")
                .patronymic("Middle")
                .build();

        mockMvc.perform(post("/api/v1/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(objectMapper.writeValueAsString(userReg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messageCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.details.email").value("The email must be in a standard format"));
    }

    @Test
    void getUserByUUIDWhenUserByUUIDExist() throws Exception {
        long id = 1L;
        UUID uuid = UUID.randomUUID();

        when(userService.getUserByUUID(uuid)).thenReturn(User.builder()
                .id(id)
                .userUUID(uuid)
                .build());

        mockMvc.perform(get("/api/v1/by-uuid/{uuid}", uuid.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userUUID").value(uuid.toString()));
    }

    @Test
    void getUserByEmailWhenUserByEmailExist() throws Exception {
        long id = 1L;
        String email = "user@example.com";

        when(userService.getUserByEmail(email)).thenReturn(User.builder()
                .id(id)
                .email(email)
                .build());

        mockMvc.perform(get("/api/v1/by-email/{email}", email)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void getUserProneNumberWhenUserByPhoneNumberExist() throws Exception {
        long id = 1L;
        String phoneNumber = "+999999999999";

        when(userService.getUserByPhoneNumber(phoneNumber)).thenReturn(
                User.builder()
                        .id(id)
                        .phoneNumber(phoneNumber)
                        .build());
        mockMvc.perform(get("/api/v1/by-phone/{phone}", phoneNumber)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.phoneNumber").value(phoneNumber));
    }
}