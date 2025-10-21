package org.walkmanx21.spring.cloudstorage.controllers.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;
import org.walkmanx21.spring.cloudstorage.dto.UserResponseDto;

public interface AuthApi {

    @Operation(
            summary = "Зарегистрировать пользователя",
            description = "Осуществляет регистрацию пользователя в системе. Username должен быть уникальным. Username и пароль должны состоять из латинских букв и цифр",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Пользователь зарегистрирован"),
                    @ApiResponse(responseCode = "409", description = "Пользователь с таким username уже существует в системе")
            }
    )
    @PostMapping("/sign-up")
    ResponseEntity<UserResponseDto> registration (@RequestBody @Valid UserRequestDto userRequestDto);

    @Operation(
            summary = "Авторизация пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь авторизован")
            }
    )
    @PostMapping("/sign-in")
    void authorization(@RequestBody @Valid UserRequestDto userRequestDto);

    @Operation(
            summary = "Выход из аккаунта пользователя",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Успешный выход. Тела ответа нет")
            }
    )
    @PostMapping("/sign-out")
    void logout();
}
