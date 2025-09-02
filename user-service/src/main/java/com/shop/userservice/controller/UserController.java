package com.shop.userservice.controller;

import com.shop.userservice.dto.UserDto;
import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.dto.UserSearchDto;
import com.shop.userservice.security.UserPrincipal;
import com.shop.userservice.service.UserService;
import com.shop.userservice.util.NormalizerPhoneNumber;
import com.shop.userservice.util.factory.UserDtoFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
        userRegistrationDto.setPhoneNumber(NormalizerPhoneNumber.normalizerPhone(userRegistrationDto.getPhoneNumber()));
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

    @GetMapping
    public ResponseEntity<Page<UserDto>> getUserPagingAndSort(@PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC)
                                                                  Pageable pageable) {
        return ResponseEntity.ok(userService.getUserByPaginateAndSort(pageable).map(UserDtoFactory::createUserDto));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<UserDto>> searchUser(@RequestBody UserSearchDto userSearchDto,
                                                    @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC)
                                                    Pageable pageable) {
        return ResponseEntity.ok(userService.searchUserByFilter(userSearchDto, pageable).map(UserDtoFactory::createUserDto));
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userService.deleteUserById(id, userPrincipal.getName());
    }
}
