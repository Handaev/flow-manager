package org.example.flowmanager.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.flowmanager.api.exception.ManagerException;
import org.example.flowmanager.api.utils.CacheUtils;
import org.example.flowmanager.api.service.UserService;
import org.example.flowmanager.api.service.feign.SubscriptionUserFeignClient;
import org.example.subscription.client.dto.UserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SubscriptionUserFeignClient subscriptionUserFeignClient;
    private final ManualCacheServiceImpl manualCacheService;
    private final ObjectMapper objectMapper;

    @Override
    public UserResponseDto getUserByName(String username) {
        return subscriptionUserFeignClient.getUserByName(username).getBody();
    }

    @Override
    public UserResponseDto editTypeSubscriptionForUser(String username, String subscriptionType) {
        return subscriptionUserFeignClient.editTypeSubscriptionForUser(username, subscriptionType).getBody();
    }

    @Override
    public UserResponseDto updateSubscriptionForUser(String username) {
        return subscriptionUserFeignClient.updateSubscriptionForUser(username).getBody();
    }

    public UserResponseDto getOrLoadUserSubscription(String currentLoginUser) {
        UserResponseDto cachedDto = getUserSubscriptionCacheRecord(currentLoginUser);

        if (Objects.nonNull(cachedDto)) {
            return cachedDto;
        }

        ResponseEntity<UserResponseDto> response = subscriptionUserFeignClient.getUserByName(currentLoginUser);
        UserResponseDto remoteDto = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new ManagerException(String.format("User not found in subscription service: %s", currentLoginUser)));

        setValue(currentLoginUser, remoteDto);
        return remoteDto;
    }

    public UserResponseDto getUserSubscriptionCacheRecord(String username) {
        String cacheKey = CacheUtils.PREFIX_KEY_REDIS_SUBSCRIPTION + username;
        String value = manualCacheService.getValue(cacheKey);

        if (Objects.isNull(value) || value.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(value, UserResponseDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing user subscription JSON from cache for user: {}", username, e);
            return null;
        }
    }

    public void setValue(String username, UserResponseDto userResponseDto) {
        String cacheKey = CacheUtils.PREFIX_KEY_REDIS_SUBSCRIPTION + username;

        try {
            String jsonString = objectMapper.writeValueAsString(userResponseDto);
            manualCacheService.setValue(cacheKey, jsonString, CacheUtils.EXPIRATION_24_HOURS);
        } catch (JsonProcessingException e) {
            log.error("Error serializing user subscription object to JSON for user: {}", username, e);
        }
    }
}
