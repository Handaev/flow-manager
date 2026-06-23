package org.example.flowmanager.api.service;

import org.example.flowmanager.api.dto.InvalidateSubscriptionUserRecord;

import java.time.Duration;
import java.util.List;

public interface CacheService {

    void invalidateCacheSubscriptions(List<InvalidateSubscriptionUserRecord> records);

    String getValue(String key);

    void setValue(String key, String jsonString, Duration ttl);
}