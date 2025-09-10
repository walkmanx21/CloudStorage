package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;
import org.walkmanx21.spring.cloudstorage.dto.UserResponseDto;
import org.walkmanx21.spring.cloudstorage.exceptions.BadCredentialsException;
import org.walkmanx21.spring.cloudstorage.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity <UserResponseDto> registration (@RequestBody @Valid UserRequestDto userRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throwNewBadCredentialsException(bindingResult);
        }
        UserResponseDto userResponseDto = userService.register(userRequestDto);
        return new ResponseEntity<>(userResponseDto, HttpStatus.CREATED);
    }


    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDto> authorization(@RequestBody @Valid UserRequestDto userRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throwNewBadCredentialsException(bindingResult);
        }
        UserResponseDto userResponseDto = userService.authorize(userRequestDto);
        return new ResponseEntity<>(userResponseDto, HttpStatus.CREATED);
    }


    private void throwNewBadCredentialsException(BindingResult bindingResult) {
        StringBuilder builder = new StringBuilder();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error -> builder.append(error.getDefaultMessage()).append("; "));
        throw new BadCredentialsException(builder.toString());
    }
}
