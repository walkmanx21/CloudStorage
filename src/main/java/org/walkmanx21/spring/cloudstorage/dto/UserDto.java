package org.walkmanx21.spring.cloudstorage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    @NotEmpty
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only latin letters and numbers are allowed.")
    private String username;

    @NotEmpty
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only latin letters and numbers are allowed.")
    private String password;
}
