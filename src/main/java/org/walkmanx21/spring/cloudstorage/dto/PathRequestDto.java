package org.walkmanx21.spring.cloudstorage.dto;

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
public class PathRequestDto {

    @NotEmpty(message = "Path field must not be empty")
    @Size(max = 1024, message = "Path field must not be longer than 1024 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_.\\/]+$",  message = "The following characters are allowed for the path field: Latin letters, numbers, and symbols: '- _ . /'")
    private String path;
}
