package org.walkmanx21.spring.cloudstorage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.walkmanx21.spring.cloudstorage.controllers.api.UserApi;
import org.walkmanx21.spring.cloudstorage.dto.UserResponseDto;
import org.walkmanx21.spring.cloudstorage.services.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserResponseDto());
    }
}