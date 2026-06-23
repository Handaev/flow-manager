package org.example.flowmanager.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.flowmanager.api.dto.InvalidateSubscriptionUserRecord;
import org.example.flowmanager.api.service.CacheService;
import org.example.flowmanager.api.utils.CacheUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualCacheServiceImpl implements CacheService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void invalidateCacheSubscriptions(List<InvalidateSubscriptionUserRecord> usersInvalidateSubscription) {
        List<String> keysToDelete = usersInvalidateSubscription.stream()
                .map(InvalidateSubscriptionUserRecord::name)
                .map(username -> CacheUtils.PREFIX_KEY_REDIS_SUBSCRIPTION + username)
                .toList();

        redisTemplate.delete(keysToDelete);
    }

    @Override
    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setValue(String key, String jsonString, Duration ttl) {
        redisTemplate.opsForValue().set(key, jsonString, ttl);
    }
}