package com.shop.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public record UserSearchDto(
        @Schema(description = "Filter by last name")
        String firstName,
        @Schema(description = "Filter by last name")
        String lastName,
        @Schema(description = "Filter by patronymic")
        String patronymic,
        @Schema(description = "Filter by phone number")
        String phoneNumber,
        @Schema(description = "Filter by email")
        String email,
        @Schema(description = "Date of creation from")
        LocalDate createdFrom,
        @Schema(description = "Date of creation to")
        LocalDate createdTo
) {

    @AssertTrue(message = "The date 'from' must be earlier than the date 'to'")
    public boolean isDateRangeValid() {
        return createdFrom == null || createdTo == null || !createdFrom.isAfter(createdTo);
    }
}
