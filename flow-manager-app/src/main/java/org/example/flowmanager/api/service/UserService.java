package org.example.flowmanager.api.service;

import org.example.subscription.client.dto.UserResponseDto;

public interface UserService {
    UserResponseDto getUserByName(String username);

    UserResponseDto editTypeSubscriptionForUser(String username, String subscriptionType);

    UserResponseDto updateSubscriptionForUser(String username);
}