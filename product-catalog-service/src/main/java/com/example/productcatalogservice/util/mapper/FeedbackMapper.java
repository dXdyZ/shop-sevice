package com.example.productcatalogservice.util.mapper;

import com.example.productcatalogservice.dto.create.CreateFeedbackDto;
import com.example.productcatalogservice.entity.Feedback;

public final class FeedbackMapper {
    public static Feedback createFromDto(CreateFeedbackDto createDto) {
        return Feedback.builder()
                .description(createDto.description())
                .estimation(createDto.estimation())
                .build();
    }
}
