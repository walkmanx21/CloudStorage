package org.walkmanx21.spring.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Data
@AllArgsConstructor
@Schema(description = "DTO ответа при скачивании файла")
public class DownloadResponseDto {

    private StreamingResponseBody streamingResponseBody;
    private String fileName;
}
