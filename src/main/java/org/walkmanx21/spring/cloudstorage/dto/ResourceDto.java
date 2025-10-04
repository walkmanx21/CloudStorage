package org.walkmanx21.spring.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DirectoryDto.class, name = "DIRECTORY"),
        @JsonSubTypes.Type(value = FileDto.class, name = "FILE")
})
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Schema(description = "DTO ресурса (родительский для DTO файла/папки)")
public abstract class ResourceDto {
    private String path;
    private String name;
    private ResourceDtoType type;


}
