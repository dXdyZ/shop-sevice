package com.shop.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.dto.UserSearchDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
    void getUserPagingAndSortWhenUsersExist() throws Exception {
        List<User> users = new ArrayList<>() {{
            for (int i = 0; i < 5; i++) {
                add(User.builder()
                        .id((long) i)
                        .lastName("test" + i)
                        .email("user" + i + "@gmail.com")
                        .build());
            }
        }};
        Pageable pageable = PageRequest.of(0, 5);

        Page<User> page = new PageImpl<>(
                users,
                pageable,
                users.size()
        );

        when(userService.getUserByPaginateAndSort(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/")
                .param("page", "0")
                .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].lastName").value("test0"))
                .andExpect(jsonPath("$.content[1].lastName").value("test1"))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(5));

    }

    @Test
    void searchUserWhenUsersExist() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        Page<User> page = new PageImpl<>(
                List.of(User.builder()
                                .id(1L)
                                .lastName("test1")
                                .email("user1@gmail.com")
                        .build()),
                pageable,
                1
        );

        UserSearchDto userSearchDto = new UserSearchDto(null, "test1", null, null,
                "user1@gmail.com", null, null);

        when(userService.searchUserByFilter(eq(userSearchDto), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(post("/api/v1/search")
                        .param("page", "0")
                        .param("size", "5")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(userSearchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].lastName").value("test1"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(5));
    }
}