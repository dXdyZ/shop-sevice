package com.example.productcatalogservice.util;

public final class SlugMapper {
    public static String from(String world) {
        String trimWorld = world.trim().toLowerCase();
        return trimWorld.replace(' ', '-');
    }
}
