package com.shop.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Body for create user")
public class UserRegistrationDto {

    @NotBlank(message = "The name must be specified")
    @Size(min = 1, max = 50, message = "The name must be in the range from 1 to 50 characters")
    @Schema(description = "Username for Keycloak", example = "john_doe")
    private String username;

    @NotBlank(message = "The last name must be specified")
    @Size(min = 1, max = 50, message = "The last name must be in the range from 1 to 50 characters")
    @Schema(description = "Last name", example = "Ivanov")
    private String lastName;

    @NotBlank(message = "The last first name must be specified")
    @Size(min = 1, max = 50, message = "The last first name must be in the range from 1 to 50 characters")
    @Schema(description = "First name", example = "Alexander")
    private String firstName;

    @NotBlank(message = "The patronymic must be specified")
    @Size(min = 1, max = 50, message = "The patronymic must be in the range from 1 to 50 characters")
    @Schema(description = "patronymic", example = "Aleksandrovich")
    private String patronymic;

    @NotBlank(message = "The phone number must be specified")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "The phone number must comply with the standard")
    @Schema(description = "The user's phone number", example = "+88888888888")
    private String phoneNumber;

    @Email(message = "The email must be in a standard format")
    @NotBlank(message = "The email must be specified")
    @Schema(description = "User's email address", example = "martos@example.com")
    private String email;

    @NotBlank(message = "The password must be specified")
    @Size(min = 8, max = 20, message = "The password must be between 8 and 20 characters long")
    @Schema(description = "User's password", example = "password123")
    private String password;
}
