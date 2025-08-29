package com.shop.userservice.controller;

import com.shop.userservice.dto.ErrorResponse;
import com.shop.userservice.dto.UserDto;
import com.shop.userservice.dto.UserRegistrationDto;
import com.shop.userservice.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "User management")
public interface UserControllerDocs {

    @Operation(summary = "Registration new user")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Successful create user in the Keycloak and User-service"),
                    @ApiResponse(responseCode = "409", description = "The user with such data does not exist",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "DUPLICATE_USER",
                                                "httpCode": 409,
                                                "message": "User with email/username exists",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                            }
                                            """))),
                    @ApiResponse(responseCode = "500", description = "Keycloak connection failed",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "SERVER_ERROR",
                                                "httpCode": 500,
                                                "message": "Connection service error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "VALIDATION_FAILED",
                                                "httpCode": 400,
                                                "message": "Validation error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                                "details": {
                                                    "email": "The email must be in a standard format",
                                                    "username": "The name must be specified"
                                                }
                                            }
                                            """)))

            }
    )
    void registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto);


    @Operation(summary = "Get user by id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Successful completion of the request",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "The user with such data does not exist",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "USER_NOT_FOUND",
                                                "httpCode": 404,
                                                "message": "User by id: 1 not found",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "VALIDATION_FAILED",
                                                "httpCode": 400,
                                                "message": "Validation error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                                "details": {
                                                    "id": "The user ID must be greater than zero",
                                                }
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "VALIDATION_FAILED",
                                                "httpCode": 400,
                                                "message": "Validation error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                                "details": {
                                                    "id": "The user's id must be specified",
                                                }
                                            }
                                            """)))
            }
    )
    ResponseEntity<UserDto> getUserById(@PathVariable @Min(value = 0, message = "The user ID must be greater than zero")
                                        @NotNull(message = "The user's id must be specified") Long id);


    @Operation(summary = "Get user by uuid")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Successful completion of the request",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "The user with such data does not exist",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "USER_NOT_FOUND",
                                                "httpCode": 404,
                                                "message": "User by id: 550e8400-e29b-41d4-a716-446655440000 not found",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "VALIDATION_FAILED",
                                                "httpCode": 400,
                                                "message": "Validation error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                                "details": {
                                                    "id": "The user's UUID must be specified",
                                                }
                                            }
                                            """)))
            }
    )
    ResponseEntity<UserDto> getUserByUUID(@NotNull(message = "The user's UUID must be specified") @PathVariable UUID uuid);


    @Operation(summary = "Get user by email")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Successful completion of the request",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "The user with such data does not exist",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "USER_NOT_FOUND",
                                                "httpCode": 404,
                                                "message": "User by email: martos@example.com not found",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "VALIDATION_FAILED",
                                                "httpCode": 400,
                                                "message": "Validation error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                                "details": {
                                                    "email": "The user's email must be specified",
                                                }
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "VALIDATION_FAILED",
                                                "httpCode": 400,
                                                "message": "Validation error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                                "details": {
                                                    "email": "The email must comply with the standards",
                                                }
                                            }
                                            """)))
            }
    )
    ResponseEntity<UserDto> getUserByEmail(@PathVariable
                                           @NotBlank(message = "The user's email must be specified")
                                           @Email(message = "The email must comply with the standards") String email);


    @Operation(summary = "Get user by phone number")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Successful completion of the request",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "The user with such data does not exist",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "USER_NOT_FOUND",
                                                "httpCode": 404,
                                                "message": "User by phone number: +88888888888 not found",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "VALIDATION_FAILED",
                                                "httpCode": 400,
                                                "message": "Validation error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                                "details": {
                                                    "phone": "The user's phone number must be specified",
                                                }
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "VALIDATION_FAILED",
                                                "httpCode": 400,
                                                "message": "Validation error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                                "details": {
                                                    "phone": "The phone number must comply with the standard",
                                                }
                                            }
                                            """)))
            }
    )
    ResponseEntity<UserDto> getUserProneNumber(@PathVariable @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "The phone number must comply with the standard")
                                               @NotBlank(message = "The user's phone number must be specified") String phone);



    @Operation(summary = "Delete user by id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Successful completion of the request",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "The user with such data does not exist",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "USER_NOT_FOUND",
                                                "httpCode": 404,
                                                "message": "User by id: 1 not found",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "VALIDATION_FAILED",
                                                "httpCode": 400,
                                                "message": "Validation error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                                "details": {
                                                    "id": "The user id must be greater than zero",
                                                }
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "messageCode": "VALIDATION_FAILED",
                                                "httpCode": 400,
                                                "message": "Validation error",
                                                "timestamp": "2025-08-24T20:00:00Z"
                                                "details": {
                                                    "id": "The user's id must be specified",
                                                }
                                            }
                                            """)))
            }
    )
    void deleteUserById(@Min(value = 0, message = "The user id must be greater than zero")
                        @NotNull(message = "The user's id must be specified")
                        @PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal);
}
