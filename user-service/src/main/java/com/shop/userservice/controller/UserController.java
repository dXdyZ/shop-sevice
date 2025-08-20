package com.shop.userservice.controller;

import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class UserController {
    private final UserService userService;

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        log.info("Incoming INFO: {}", userRegistrationDto);
        userService.registrationUser(userRegistrationDto);
    }
}
