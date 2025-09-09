package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.walkmanx21.spring.cloudstorage.dto.UserDto;
import org.walkmanx21.spring.cloudstorage.services.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public String registration (@RequestBody @Valid UserDto userDto, BindingResult bindingResult) {
        authService.register(userDto);
        return null;
    }
}
