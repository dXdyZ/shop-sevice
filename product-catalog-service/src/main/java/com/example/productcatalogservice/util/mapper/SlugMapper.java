package com.example.productcatalogservice.util.mapper;

public final class SlugMapper {
    public static String from(String world) {
        String trimWorld = world.trim().toLowerCase();
        return trimWorld.replace(' ', '-');
    }
}
