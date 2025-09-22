package com.example.productcatalogservice.util;

public final class SlugMapper {
    public static String fromName(String name) {
        String trimName = name.trim().toLowerCase();
        return trimName.replace(' ', '-');
    }
}
