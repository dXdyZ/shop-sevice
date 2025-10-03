package com.example.productcatalogservice.util.mapper;

import com.example.productcatalogservice.dto.create.CreateFeedback;
import com.example.productcatalogservice.entity.Feedback;

public final class FeedbackMapper {
    public static Feedback createFromDto(CreateFeedback createDto) {
        return Feedback.builder()
                .description(createDto.description())
                .estimation(createDto.estimation())
                .build();
    }
}
