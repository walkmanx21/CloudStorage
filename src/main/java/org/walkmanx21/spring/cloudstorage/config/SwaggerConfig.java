package org.walkmanx21.spring.cloudstorage.config;

import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer globalResponseCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    ApiResponses responses = operation.getResponses();
                    responses.addApiResponse("400", new ApiResponse().description("Невалидные данные запроса"));
                    responses.addApiResponse("401", new ApiResponse().description("Пользователь не авторизован"));
                    responses.addApiResponse("500", new ApiResponse().description("Неизвестная ошибка"));
                }));
    }
}
