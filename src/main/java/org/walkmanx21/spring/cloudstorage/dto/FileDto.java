package org.walkmanx21.spring.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO файла")
public class FileDto extends ResourceDto {

    private long size;
}
