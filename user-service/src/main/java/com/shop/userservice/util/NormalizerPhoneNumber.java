package com.shop.userservice.util;

public class NormalizerPhoneNumber {
    public static String normalizerPhone(String phone) {
        return phone.replaceAll("[^0-9+]", "");
    }
}
