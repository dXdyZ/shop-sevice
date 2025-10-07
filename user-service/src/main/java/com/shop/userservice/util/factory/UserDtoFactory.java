package com.shop.userservice.util.factory;

import com.shop.userservice.dto.UserDto;
import com.shop.userservice.entity.User;

public class UserDtoFactory {
    public static UserDto createUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .userUUID(user.getPublicId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .patronymic(user.getPatronymic())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .build();
    }
}
