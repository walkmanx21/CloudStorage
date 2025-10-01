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

    @NotBlank(message = "Username must not be empty")
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only latin letters and numbers are allowed for username")
    @Schema(description = "DTO пользователя", example = "Alice")
    private String username;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 5, max = 20, message = "Password must be between 5 and 20 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only latin letters and numbers are allowed for password")
    @Schema(description = "DTO пользователя", example = "yudayidydu22123")
    private String password;
}