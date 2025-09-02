package com.shop.userservice.service;

import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.dto.UserSearchDto;
import com.shop.userservice.entity.User;
import com.shop.userservice.exception.IternalServerError;
import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.exception.UserNotFoundException;
import com.shop.userservice.keycloak.KeycloakService;
import com.shop.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private UserService userService;

    @Test
    void registrationUserWhenDuplicateUserDoesNotExist() {
        String userUUID = "550e8400-e29b-41d4-a716-446655440000";
        UserRegistrationDto regDto = UserRegistrationDto.builder()
                .username("test")
                .lastName("test")
                .firstName("test")
                .email("test@gmail.com")
                .password("password")
                .patronymic("test")
                .phoneNumber("777777777777")
                .build();

        when(keycloakService.createUser(any(), any(), any(), any(), any())).thenReturn(userUUID);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(any())).thenReturn(false);

        userService.registrationUser(regDto);

        verify(keycloakService).createUser(any(), any(), any(), any(), any());
        verify(userRepository).save(argThat(actUser ->
                actUser.getUserUUID().toString().equals(userUUID)));

        verify(keycloakService, never()).deleteUserByUUID(userUUID);
    }

    @Test
    void registrationUserWhenDuplicateUserExistThenThrowUserDuplicateException() {
        UserRegistrationDto regDto = UserRegistrationDto.builder()
                .username("test")
                .lastName("test")
                .firstName("test")
                .email("test@gmail.com")
                .password("password")
                .patronymic("test")
                .phoneNumber("777777777777")
                .build();

        when(keycloakService.createUser(any(), any(), any(), any(), any())).thenThrow(new UserDuplicateException("User with email exists"));

        assertThrows(UserDuplicateException.class,
                () -> userService.registrationUser(regDto));

        verify(userRepository, never()).save(any());
        verify(keycloakService, never()).deleteUserByUUID(any());
    }

    @Test
    void registrationUserWhenThrowDataIntegrityViolationException() {
        String userUUID = "550e8400-e29b-41d4-a716-446655440000";
        UserRegistrationDto regDto = UserRegistrationDto.builder()
                .username("test")
                .lastName("test")
                .firstName("test")
                .email("test@gmail.com")
                .password("password")
                .patronymic("test")
                .phoneNumber("777777777777")
                .build();

        when(keycloakService.createUser(any(), any(), any(), any(), any())).thenReturn(userUUID);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(any())).thenReturn(false);

        when(userRepository.save(any())).thenThrow(new DataIntegrityViolationException("ERROR: duplicate key value violates unique constraint \"user_userUUID_key\""));

        assertThrows(IternalServerError.class,
                () -> userService.registrationUser(regDto));

        verify(keycloakService).createUser(any(), any(), any(), any(), any());
        verify(userRepository).existsByEmail(any());
        verify(userRepository).existsByPhoneNumber(any());
        verify(keycloakService).deleteUserByUUID(userUUID);
    }

    @Test
    void registrationUserWhenDuplicateUserByEmailExistThrowUserDuplicateException() {
        UserRegistrationDto regDto = UserRegistrationDto.builder()
                .username("test")
                .lastName("test")
                .firstName("test")
                .email("test@gmail.com")
                .password("password")
                .patronymic("test")
                .phoneNumber("777777777777")
                .build();

        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(UserDuplicateException.class,
                () -> userService.registrationUser(regDto));

        verifyNoInteractions(keycloakService);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registrationUserWhenDuplicateUserByPhoneExistThrowUserDuplicateException() {
        UserRegistrationDto regDto = UserRegistrationDto.builder()
                .username("test")
                .lastName("test")
                .firstName("test")
                .email("test@gmail.com")
                .password("password")
                .patronymic("test")
                .phoneNumber("777777777777")
                .build();

        when(userRepository.existsByPhoneNumber(any())).thenReturn(true);

        assertThrows(UserDuplicateException.class,
                () -> userService.registrationUser(regDto));

        verifyNoInteractions(keycloakService);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserByUUIDWhenUserByUUIDExist() {
        UUID uuid = UUID.randomUUID();
        User user = User.builder()
                .id(1L)
                .build();

        when(userRepository.findByUserUUID(uuid)).thenReturn(Optional.of(user));

        User result = userService.getUserByUUID(uuid);

        assertEquals(user, result);
    }

    @Test
    void getUserByUUIDWhenUserByUUIDDoesNotExistThenThrowUserNotFoundException() {
        UUID uuid = UUID.randomUUID();

        when(userRepository.findByUserUUID(uuid)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserByUUID(uuid));
    }

    @Test
    void getUserByIdWhenUserByIdExist() {
        Long id = 1L;
        User user = User.builder()
                .id(1L)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertEquals(user, result);
    }

    @Test
    void getUserByIdWhenUserByIdDoesNotExistThenThrowUserNotFoundException() {
        Long id = 1L;
        User user = User.builder()
                .id(1L)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(id));
    }

    @Test
    void getUserByEmailWhenUserByEmailExists() {
        String email = "user@gmail.com";
        User user = User.builder()
                .id(1L)
                .email("user@gmail.com")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(email);

        assertEquals(user, result);
    }

    @Test
    void getUserByEmailWhenUserByEmailDoesNotExistThenThrowUserNotFoundException() {
        String email = "user@gmail.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail(email));
    }

    @Test
    void getUserByPhoneNumberWhenUserByPhoneExist() {
        String phone = "77777777777777";
        User user = User.builder()
                .id(1L)
                .phoneNumber("77777777777777")
                .build();

        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(user));

        User result = userService.getUserByPhoneNumber(phone);

        assertEquals(user, result);
    }

    @Test
    void getUserByPhoneNumberWhenUserByPhoneDoesNotExistThenThrowUserNotFoundException() {
        String phone = "77777777777777";

        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserByPhoneNumber(phone));
    }

    @Test
    void deleteUserByIdWhenUserByIdExist() {
        Long id = 1L;
        UUID uuid = UUID.randomUUID();
        User user = User.builder()
                .id(1L)
                .userUUID(uuid)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUserById(id, "admin");

        verify(userRepository).findById(id);
        verify(userRepository).delete(user);
        verify(keycloakService).deleteUserByUUID(uuid.toString());
    }

    @Test
    void deleteUserByIdWhenUserByIdDoesNotExistThenThrowUserNotFoundException() {
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUserById(id, "admin"));
        verifyNoInteractions(keycloakService);
    }

    @Test
    void getUserByPaginateAndSort() {
        User user = User.builder()
                .id(1L)
                .build();

        Pageable pageable = PageRequest.of(0, 5);

        Page<User> userPage = new PageImpl<>(
                List.of(user),
                pageable,
                1
        );

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = userService.getUserByPaginateAndSort(pageable);

        assertEquals(userPage, result);
    }

    @Test
    void searchUserByFilter() {
        User user = User.builder()
                .id(1L)
                .build();

        Pageable pageable = PageRequest.of(0, 5);

        Page<User> userPage = new PageImpl<>(
                List.of(user),
                pageable,
                1
        );

        UserSearchDto searchDto = new UserSearchDto(null, null, null, null, null, null, null);

        when(userRepository.searchUser(searchDto, pageable)).thenReturn(userPage);

        Page<User> result = userService.searchUserByFilter(searchDto, pageable);

        assertEquals(userPage, result);
    }
}










