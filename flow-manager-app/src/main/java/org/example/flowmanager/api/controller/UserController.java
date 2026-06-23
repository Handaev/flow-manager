package org.example.flowmanager.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.flowmanager.api.service.impl.UserServiceImpl;
import org.example.subscription.client.api.UserApi;
import org.example.subscription.client.dto.UserResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserServiceImpl userService;

    @Override
    public ResponseEntity<UserResponseDto> editTypeSubscriptionForUser(String username, String subscriptionType) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.editTypeSubscriptionForUser(username, subscriptionType));
    }

    @Override
    public ResponseEntity<UserResponseDto> getUserByName(String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByName(username));
    }

    @Override
    public ResponseEntity<UserResponseDto> updateSubscriptionForUser(String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateSubscriptionForUser(username));
    }
}