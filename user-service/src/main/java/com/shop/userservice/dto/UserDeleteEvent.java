package com.shop.userservice.dto;

import java.util.UUID;

public record UserDeleteEvent(
   Long id,
   UUID uuid,
   String email,
   String phoneNumber,
   String adminName
) {}
