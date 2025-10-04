package org.walkmanx21.spring.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Data
@NoArgsConstructor
@Schema(description = "DTO ответа при скачивании файла")
public class DownloadResponseDto {

    private InputStreamResource inputStreamResource;
    private StreamingResponseBody streamingResponseBody;
    private String fileName;
    private long contentLength;

    public DownloadResponseDto(StreamingResponseBody streamingResponseBody, String fileName) {
        this.streamingResponseBody = streamingResponseBody;
        this.fileName = fileName;
    }

    public DownloadResponseDto(long contentLength, InputStreamResource inputStreamResource, String fileName) {
        this.contentLength = contentLength;
        this.inputStreamResource = inputStreamResource;
        this.fileName = fileName;
    }
}
