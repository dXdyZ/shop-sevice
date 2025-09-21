package com.shop.userservice.service;

import com.shop.userservice.dto.UserDeleteEvent;
import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.dto.UserSearchDto;
import com.shop.userservice.entity.User;
import com.shop.userservice.exception.ExternalServiceUnavailableException;
import com.shop.userservice.exception.IternalServerError;
import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.exception.UserNotFoundException;
import com.shop.userservice.keycloak.KeycloakService;
import com.shop.userservice.repository.UserRepository;
import com.shop.userservice.util.LogMarker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final UserCacheService userCacheService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void registrationUser(UserRegistrationDto userRegistrationDto)
            throws UserDuplicateException, ExternalServiceUnavailableException {

        if (userRepository.existsByEmail(userRegistrationDto.getEmail())) {
            throw new UserDuplicateException("User with email exists");
        }
        if (userRepository.existsByPhoneNumber(userRegistrationDto.getPhoneNumber())) {
            throw new UserDuplicateException("User with phone number exists");
        }

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

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            try {
                keycloakService.deleteUserByUUID(userUUID);
            } catch (UserNotFoundException ignore) {}

            log.error(LogMarker.ERROR.getMarker(), "service=UserService | error SAVING the user | username={} | message={}",
                    userRegistrationDto.getUsername(), exception.getMessage());

            throw new IternalServerError("Server error - try again later");
        }
    }


    @Cacheable(value = "users:byUUID", key = "#uuid")
    public User getUserByUUID(UUID uuid) {
        return userRepository.findByUserUUID(uuid).orElseThrow(
                () -> new UserNotFoundException("User by uuid: %s not found".formatted(uuid)));
    }

    @Cacheable(value = "users:byId", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User by id: %s not found".formatted(id)));
    }

    @Cacheable(value = "users:byEmail", key = "#email")
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User by email: %s not found".formatted(email)));
    }

    @Cacheable(value = "users:ByPhone", key = "#phoneNumber")
    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElseThrow(
                () -> new UserNotFoundException("User by phone number: %s not found".formatted(phoneNumber)));
    }

    /**
     * Удаляет пользователя по id
     *
     * @param id пользователя которого нужно удалить
     * @param adminName имя администратора который удалил пользователя
     */
    @Transactional
    public void deleteUserById(Long id, String adminName) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User by id: %s not found".formatted(id)));

        userRepository.delete(user);

        eventPublisher.publishEvent(new UserDeleteEvent(
                user.getId(),
                user.getUserUUID(),
                user.getEmail(),
                user.getPhoneNumber(),
                adminName
        ));
    }

    /**
     * Обработчик событий после коммита транзакции
     *
     * @param event данные удаленного пользователя
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUserDeletedEvent(UserDeleteEvent event) {
        try {
            userCacheService.evictUserCaches(
                    event.id(),
                    event.uuid(),
                    event.email(),
                    event.phoneNumber()
            );

            keycloakService.deleteUserByUUID(event.uuid().toString());

            log.info(LogMarker.AUDIT.getMarker(), "service=UserService | action=deleteUser | deletedUserId={} | performedBy={}",
                    event.id(), event.adminName());
        } catch (Exception exception) {
            log.error(LogMarker.ERROR.getMarker(), "service=UserService | DELETION ERROR | userId={} | adminName={} | causer={}",
                    event.id(), event.adminName(), exception.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<User> getUserByPaginateAndSort(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> searchUserByFilter(UserSearchDto userSearchDto, Pageable pageable) {
        return userRepository.searchUser(userSearchDto, pageable);
    }
}
