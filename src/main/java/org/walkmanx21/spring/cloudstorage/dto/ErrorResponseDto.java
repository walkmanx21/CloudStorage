package org.walkmanx21.spring.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "DTO ошибки")
public class ErrorResponseDto {
    @Schema(description = "Поле message, которое и содержит краткое описание ошибки", example = "Пользователь уже существует")
    private String message;
}
