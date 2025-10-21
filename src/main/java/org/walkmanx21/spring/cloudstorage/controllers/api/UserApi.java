package org.walkmanx21.spring.cloudstorage.controllers.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.walkmanx21.spring.cloudstorage.dto.UserResponseDto;

public interface UserApi {

    @Operation(
            summary = "Вывести текущего пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Текущий пользователь")
            }
    )
    @GetMapping("/me")
    ResponseEntity<UserResponseDto> getCurrentUser();
}
