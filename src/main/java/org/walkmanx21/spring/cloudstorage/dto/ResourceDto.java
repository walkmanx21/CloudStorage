package org.walkmanx21.spring.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
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
public abstract class ResourceDto {
    private String path;
    private String name;
    private ResourceDtoType type;
}
