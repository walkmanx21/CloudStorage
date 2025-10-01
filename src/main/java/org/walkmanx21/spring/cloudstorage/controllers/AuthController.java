package org.walkmanx21.spring.cloudstorage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;
import org.walkmanx21.spring.cloudstorage.dto.UserResponseDto;
import org.walkmanx21.spring.cloudstorage.services.UserService;
import org.walkmanx21.spring.cloudstorage.validation.UserRequestDtoValidator;

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

    @Operation(
            summary = "Зарегистрировать пользователя",
            description = "Осуществляет регистрацию пользователя в системе. Username должен быть уникальным. Username и пароль должны состоять из латинских букв и цифр",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Пользователь зарегистрирован"),
                    @ApiResponse(responseCode = "409", description = "Пользователь с таким username уже существует в системе")
            }
    )
    @PostMapping("/sign-up")
    public ResponseEntity <UserResponseDto> registration (@RequestBody @Valid UserRequestDto userRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(userRequestDto));
    }

    @Operation(
            summary = "Авторизация пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь авторизован")
            }
    )
    @PostMapping("/sign-in")
    public void authorization(@RequestBody @Valid UserRequestDto userRequestDto) {
    }

    @Operation(
            summary = "Выход из аккаунта пользователя",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Успешный выход. Тела ответа нет")
            }
    )
    @PostMapping("/sign-out")
    public void logout() {
    }
}
