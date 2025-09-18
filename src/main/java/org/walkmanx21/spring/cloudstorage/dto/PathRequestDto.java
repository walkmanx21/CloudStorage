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
    @Pattern(regexp = "^(?=.{1,260}$)(?:[^\\\\/:*?\"<>|]{1,255}(?<![ .])(?:[\\\\/]|$))+$",  message = "It is forbidden to use \\ / : * ? \" < > |, end a segment with a space or a period, make the segment longer than 255 characters and the entire path longer than 260 characters")
    private String path;
}
