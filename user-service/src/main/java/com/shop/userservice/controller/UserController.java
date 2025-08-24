package com.shop.userservice.controller;

import com.shop.userservice.dto.UserDto;
import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.security.UserPrincipal;
import com.shop.userservice.service.UserService;
import com.shop.userservice.util.factory.UserDtoFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class UserController implements UserControllerDocs {
    private final UserService userService;

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        userService.registrationUser(userRegistrationDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(UserDtoFactory.createUserDto(userService.getUserById(id)));
    }

    @GetMapping("/by-uuid/{uuid}")
    public ResponseEntity<UserDto> getUserByUUID(@PathVariable UUID uuid) {
        return ResponseEntity.ok(UserDtoFactory.createUserDto(userService.getUserByUUID(uuid)));
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(UserDtoFactory.createUserDto(userService.getUserByEmail(email)));
    }

    @GetMapping("/by-phone/{phone}")
    public ResponseEntity<UserDto> getUserProneNumber(@PathVariable String phone) {
        return ResponseEntity.ok(UserDtoFactory.createUserDto(userService.getUserByPhoneNumber(phone)));
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userService.deleteUserById(id, userPrincipal.getName());
    }
}
