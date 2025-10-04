package org.walkmanx21.spring.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO запроса (request) пользователя")
public class UserRequestDto {

    @NotBlank(message = "Поле username не должно быть пустым, состоять из пробелов")
    @Size(min = 2, max = 20, message = "Поле username должно быть размером от 2 до 20 символов")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Поле username должно состоять из латинских букв и цифр")
    @Schema(description = "DTO пользователя", example = "Alice")
    private String username;

    @NotBlank(message = "Поле password не должно быть пустым, состоять из пробелов")
    @Size(min = 5, max = 20, message = "Поле password должно быть размером от 5 до 20 символов")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Поле password должно состоять из латинских букв и цифр")
    @Schema(description = "DTO пользователя", example = "yudayidydu22123")
    private String password;
}