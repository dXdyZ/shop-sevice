package com.shop.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String messageCode;
    private String message;
    private Integer httpCode;
    private Map<String, String> details;
    private Instant timestamp;

    public static ErrorResponse validationError(Map<String, String> errors) {
        return ErrorResponse.builder()
                .messageCode("VALIDATION_FAILED")
                .message("Validation error")
                .httpCode(400)
                .details(errors)
                .timestamp(Instant.now())
                .build();
    }

}
