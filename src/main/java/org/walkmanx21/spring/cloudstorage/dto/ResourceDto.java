package org.walkmanx21.spring.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OldDirectoryDto.class, name = "DIRECTORY"),
        @JsonSubTypes.Type(value = OldFileDto.class, name = "FILE")
})
@SuperBuilder
@Getter
@NoArgsConstructor
public abstract class ResourceDto {
    private String path;
    private String name;
    private ResourceDtoType type;
}
