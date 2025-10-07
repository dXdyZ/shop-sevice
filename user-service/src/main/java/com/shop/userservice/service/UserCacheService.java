package com.shop.userservice.service;

import com.shop.userservice.entity.User;
import com.shop.userservice.util.LogMarker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {
    private final CacheManager cacheManager;

    public void evictUserCaches(User user) {
        evictUserCaches(
                user.getId(),
                user.getPublicId(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }

    public void evictUserCaches(Long id, UUID uuid, String email, String phoneNumber) {
        try {
            if (cacheManager.getCache("users:byId") != null) {
                cacheManager.getCache("users:byId").evict(id);
            }
            if (cacheManager.getCache("users:byUUID") != null) {
                cacheManager.getCache("users:byUUID").evict(uuid);
            }
            if (cacheManager.getCache("users:byEmail") != null) {
                cacheManager.getCache("users:byEmail").evict(email);
            }
            if (cacheManager.getCache("users:byPhone") != null) {
                cacheManager.getCache("users:byPhone").evict(phoneNumber);
            }
        } catch (Exception exception) {
            log.error(LogMarker.ERROR.getMarker(), "service=User-Cache-Service | EVICT CACHE ERROR | causer={}",
                    exception.getMessage());
        }
    }
}
