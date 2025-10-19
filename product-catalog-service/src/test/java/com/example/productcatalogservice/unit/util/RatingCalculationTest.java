package com.example.productcatalogservice.unit.util;

import com.example.productcatalogservice.util.RatingCalculation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RatingCalculationTest {

    @Test
    void ratingCalculate() {
        var currentAvg = 4.1;
        var currentCount = 150;
        var newGrade = 4;

        double result = RatingCalculation.ratingCalculate(currentAvg, currentCount, newGrade);

        assertEquals(4.1, result);
    }
}