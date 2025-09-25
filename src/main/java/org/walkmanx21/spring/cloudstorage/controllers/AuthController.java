package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;
import org.walkmanx21.spring.cloudstorage.dto.UserResponseDto;
import org.walkmanx21.spring.cloudstorage.services.UserService;
import org.walkmanx21.spring.cloudstorage.util.UserRequestDtoValidator;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRequestDtoValidator userRequestDtoValidator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userRequestDtoValidator);
    }

    @PostMapping("/sign-up")
    public ResponseEntity <UserResponseDto> registration (@RequestBody @Valid UserRequestDto userRequestDto, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(userRequestDto, request));
    }
}
