package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.create.CreateFeedbackDto;
import com.example.productcatalogservice.entity.Feedback;
import com.example.productcatalogservice.entity.Product;
import com.example.productcatalogservice.exception.FeedbackDuplicateException;
import com.example.productcatalogservice.exception.FeedbackNotFoundException;
import com.example.productcatalogservice.exception.ProductNotFoundException;
import com.example.productcatalogservice.repositoty.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {
    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private FeedbackService feedbackService;


    @Test
    void createFeedback_ShouldReturnSuccessCreateFeedback_WhenProductExistAndUserDoesNotDuplicateReview() {
        var productPublicId = UUID.randomUUID();
        var userPublicId = UUID.randomUUID();
        var createDto = new CreateFeedbackDto("New review", 4);
        var product = Product.builder()
                .publicId(productPublicId)
                .rating(4.1)
                .ratingCount(100L)
                .build();

        when(feedbackRepository.existsByProduct_PublicIdAndUserPublicId(any(UUID.class), any(UUID.class))).thenReturn(false);
        when(productService.getProductByPublicId(any(UUID.class))).thenReturn(product);
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(inv -> {
            Feedback f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });

        doAnswer(inv -> {
            double newRating = inv.getArgument(0, Double.class);
            Product p = inv.getArgument(1, Product.class);
            p.setRating(newRating);
            p.incRatingCount();
            return null;
        }).when(productService).updateRating(anyDouble(), any(Product.class));

        Feedback result = feedbackService.createFeedback(productPublicId, userPublicId, createDto);

        assertNotNull(result);
        assertNotNull(result.getProduct());
        assertEquals(product, result.getProduct());

        assertEquals(101L, result.getProduct().getRatingCount());
    }

    @Test
    void createFeedback_ShouldReturnProductNotFoundException_WhenProductDoesNotExist() {
        var productPublicId = UUID.randomUUID();
        var userPublicId = UUID.randomUUID();
        var createDto = new CreateFeedbackDto("New review", 4);

        when(feedbackRepository.existsByProduct_PublicIdAndUserPublicId(any(UUID.class), any(UUID.class))).thenReturn(false);
        when(productService.getProductByPublicId(any(UUID.class))).thenThrow(new ProductNotFoundException("Product reviews from user already exists"));

        assertThrows(ProductNotFoundException.class,
                () -> feedbackService.createFeedback(productPublicId, userPublicId, createDto));
        verify(productService, never()).updateRating(anyDouble(), any(Product.class));
        verify(feedbackRepository, never()).save(any(Feedback.class));
    }

    @Test
    void createFeedback_ShouldReturnFeedbackDuplicateException_UserDoesNotDuplicateExist() {
        var productPublicId = UUID.randomUUID();
        var userPublicId = UUID.randomUUID();
        var createDto = new CreateFeedbackDto("New review", 4);

        when(feedbackRepository.existsByProduct_PublicIdAndUserPublicId(any(UUID.class), any(UUID.class))).thenReturn(true);

        assertThrows(FeedbackDuplicateException.class,
                () -> feedbackService.createFeedback(productPublicId, userPublicId, createDto));

        verifyNoInteractions(productService);
        verify(feedbackRepository, never()).save(any(Feedback.class));
    }

    @Test
    void getFeedbackById_ShouldReturnFeedbackById_WhenFeedbackExist() {
        var id = 1L;
        var feedback = Feedback.builder()
                .id(1L)
                .build();

        when(feedbackRepository.findById(anyLong())).thenReturn(Optional.of(feedback));

        Feedback result = feedbackService.getFeedbackById(id);

        assertThat(result)
                .isNotNull()
                .isEqualTo(feedback);
    }

    @Test
    void getFeedbackById_ShouldReturnFeedbackNotFoundException_WhenFeedbackDoesNotExist() {
        var id = 1L;

        when(feedbackRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(FeedbackNotFoundException.class,
                () -> feedbackService.getFeedbackById(id));
    }

    @Test
    void getFeedbackByPublicId_ShouldReturnFeedbackByPublicId_WhenFeedbackExist() {
        var publicId = UUID.randomUUID();
        var feedback = Feedback.builder()
                .publicId(publicId)
                .build();

        when(feedbackRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(feedback));

        Feedback result = feedbackService.getFeedbackByPublicId(publicId);

        assertThat(result)
                .isNotNull()
                .isEqualTo(feedback);
    }

    @Test
    void getFeedbackByPublicId_ShouldReturnFeedbackNotFoundException_WhenFeedbackDoesNotExist() {
        var publicId = UUID.randomUUID();

        when(feedbackRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(FeedbackNotFoundException.class,
                () -> feedbackService.getFeedbackByPublicId(publicId));
    }
}












