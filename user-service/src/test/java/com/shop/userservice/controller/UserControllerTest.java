package com.shop.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.entity.User;
import com.shop.userservice.keycloak.KeycloakService;
import com.shop.userservice.security.SecurityConfig;
import com.shop.userservice.security.UserJwtConverter;
import com.shop.userservice.service.UserService;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
    private UserJwtConverter userJwtConverter;

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

        mockMvc.perform(get("/api/v1/%s".formatted(id))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@example.com"));
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
}