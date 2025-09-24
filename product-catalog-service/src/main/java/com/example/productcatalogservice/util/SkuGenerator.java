package com.example.productcatalogservice.util;

import java.util.concurrent.atomic.AtomicLong;

public class SkuGenerator {
    private static final AtomicLong counter = new AtomicLong(1000);

    public static String generateSku(String categorySlug, String brandSlug) {
        String catCode = categorySlug.substring(0, 3).toUpperCase();
        String brandCode = brandSlug.substring(0, 3).toUpperCase();
        long uniqueNum = counter.getAndIncrement();

        return String.format("%s-%s-%04d", catCode, brandCode, uniqueNum);
    }
}

