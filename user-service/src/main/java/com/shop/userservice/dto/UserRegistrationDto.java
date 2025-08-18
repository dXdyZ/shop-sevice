package com.shop.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

    @NotBlank(message = "The name must be specified")
    @Size(min = 1, max = 50, message = "The name must be in the range from 1 to 50 characters")
    private String username;

    @NotBlank(message = "The last name must be specified")
    @Size(min = 1, max = 50, message = "The last name must be in the range from 1 to 50 characters")
    private String lastName;

    @NotBlank(message = "The last first name must be specified")
    @Size(min = 1, max = 50, message = "The last first name must be in the range from 1 to 50 characters")
    private String firstName;

    @NotBlank(message = "The paronymic must be specified")
    @Size(min = 1, max = 50, message = "The patronymic must be in the range from 1 to 50 characters")
    private String patronymic;

    @NotBlank(message = "The phone number must be specified")
    @Size(min = 1, max = 50, message = "The phone number must contain 11 characters")
    private String phoneNumber;

    @Email(message = "The email must be in a standard format")
    private String email;

    @NotBlank(message = "The password must be specified")
    @Size(min = 8, max = 20, message = "The password must be between 8 and 20 characters long")
    private String password;
}
