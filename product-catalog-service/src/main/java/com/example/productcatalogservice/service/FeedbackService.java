package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.create.CreateFeedbackDto;
import com.example.productcatalogservice.entity.Feedback;
import com.example.productcatalogservice.entity.Product;
import com.example.productcatalogservice.exception.FeedbackDuplicateException;
import com.example.productcatalogservice.exception.FeedbackNotFoundException;
import com.example.productcatalogservice.exception.ProductNotFoundException;
import com.example.productcatalogservice.repositoty.FeedbackRepository;
import com.example.productcatalogservice.util.RatingCalculation;
import com.example.productcatalogservice.util.mapper.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ProductService productService;

    @Transactional
    public Feedback createFeedback(UUID productPublicId, UUID userPublicId, CreateFeedbackDto createFeedbackDto) throws ProductNotFoundException,
            FeedbackDuplicateException{

        if (feedbackRepository.existsByProduct_PublicIdAndUserPublicId(productPublicId, userPublicId))  {
            throw new FeedbackDuplicateException("Product reviews from user already exists");
        }

        Product product = productService.getProductByPublicId(productPublicId);
        Feedback feedback = FeedbackMapper.createFromDto(createFeedbackDto);
        feedback.setProduct(product);
        feedback.setUserPublicId(userPublicId);

        double newRating = RatingCalculation.ratingCalculate(product.getRating(), product.getRatingCount(), createFeedbackDto.estimation());

        productService.updateRating(newRating, product);

        return feedbackRepository.save(feedback);
    }


    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback by id: %s not found".formatted(id)));
    }

    public Feedback getFeedbackByPublicId(UUID publicId) {
        return feedbackRepository.findByPublicId(publicId)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback by public id: %s not found".formatted(publicId)));
    }
}









