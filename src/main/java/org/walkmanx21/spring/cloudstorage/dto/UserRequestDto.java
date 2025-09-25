package org.walkmanx21.spring.cloudstorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class UserRequestDto {

    @NotBlank(message = "Username must not be empty")
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only latin letters and numbers are allowed for username")
    private String username;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 5, max = 20, message = "Password must be between 5 and 20 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only latin letters and numbers are allowed for password")
    private String password;
}