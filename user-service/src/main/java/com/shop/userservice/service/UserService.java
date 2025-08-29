package com.shop.userservice.service;

import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.entity.User;
import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.exception.UserNotFoundException;
import com.shop.userservice.keycloak.KeycloakService;
import com.shop.userservice.repository.UserRepository;
import com.shop.userservice.util.LogMarker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;


    public void registrationUser(UserRegistrationDto userRegistrationDto) throws UserDuplicateException {
        String userUUID = keycloakService.createUser(userRegistrationDto.getUsername(), userRegistrationDto.getFirstName(),
                userRegistrationDto.getLastName(), userRegistrationDto.getEmail(), userRegistrationDto.getPassword());

        User user = User.builder()
                .userUUID(UUID.fromString(userUUID))
                .firstName(userRegistrationDto.getFirstName())
                .lastName(userRegistrationDto.getLastName())
                .patronymic(userRegistrationDto.getPatronymic())
                .phoneNumber(userRegistrationDto.getPhoneNumber())
                .email(userRegistrationDto.getEmail())
                .build();

        userRepository.save(user);
    }

    public User getUserByUUID(UUID uuid) {

        
        return userRepository.findByUserUUID(uuid).orElseThrow(
                () -> new UserNotFoundException("User by uuid: %s not found".formatted(uuid)));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User by id: %s not found".formatted(id)));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User by email: %s not found".formatted(email)));
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElseThrow(
                () -> new UserNotFoundException("User by phone number: %s not found".formatted(phoneNumber)));
    }

    @Transactional
    public void deleteUserById(Long id, String adminName) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User by id: %s not found".formatted(id)));

        userRepository.delete(user);

        log.info(LogMarker.AUDIT.getMarker(), "action=deleteUser | deletedUserId={} | deletedUsername={} | performedBy={}",
                user.getId(), user.getEmail(), adminName);
    }
}
