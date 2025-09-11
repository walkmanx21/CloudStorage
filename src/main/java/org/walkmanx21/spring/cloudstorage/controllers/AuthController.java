package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;
import org.walkmanx21.spring.cloudstorage.dto.UserResponseDto;
import org.walkmanx21.spring.cloudstorage.exceptions.InvalidCredentialsException;
import org.walkmanx21.spring.cloudstorage.services.UserService;
import org.walkmanx21.spring.cloudstorage.util.UserRequestDtoValidator;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRequestDtoValidator userRequestDtoValidator;

    public AuthController(UserService userService, UserRequestDtoValidator userRequestDtoValidator) {
        this.userService = userService;
        this.userRequestDtoValidator = userRequestDtoValidator;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userRequestDtoValidator);
    }

    @PostMapping("/sign-up")
    public ResponseEntity <UserResponseDto> registration (@RequestBody @Valid UserRequestDto userRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throwInvalidCredentialsException(bindingResult);
        }
        return new ResponseEntity<>(userService.register(userRequestDto), HttpStatus.CREATED);
    }

    private void throwInvalidCredentialsException(BindingResult bindingResult) {
        StringBuilder builder = new StringBuilder();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error -> builder.append(error.getDefaultMessage()).append("; "));
        throw new InvalidCredentialsException(builder.toString());
    }
}
