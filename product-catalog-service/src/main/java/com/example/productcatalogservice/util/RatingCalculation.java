package com.example.productcatalogservice.util;

public final class RatingCalculation {
    public static double ratingCalculate(double currentAvg, long currentCount, int newGrade) {
        double newRating =  (currentAvg * currentCount + newGrade) / (currentCount + 1);
        return Math.round(newRating * 100.0) / 100.0;
    }
}
