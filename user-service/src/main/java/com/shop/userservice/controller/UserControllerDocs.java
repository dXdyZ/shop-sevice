package com.shop.userservice.controller;

import com.shop.userservice.dto.UserRegistrationDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User management")
public interface UserControllerDocs {

    void registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto);
}
