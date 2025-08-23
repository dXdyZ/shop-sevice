package com.shop.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User data")
public class UserDto {
    @Schema(description = "User id", example = "1")
    private Long id;

    @Schema(description = "The user's UUID issued by Keycloak", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userUUID;

    @Schema(description = "Last name", example = "Ivanov")
    private String lastName;

    @Schema(description = "First name", example = "Alexander")
    private String firstName;

    @Schema(description = "patronymic", example = "Aleksandrovich")
    private String patronymic;

    @Schema(description = "The user's phone number", example = "88888888888")
    private String phoneNumber;

    @Schema(description = "User's email address", example = "martos@example.com")
    private String email;
}
