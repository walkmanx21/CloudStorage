package org.walkmanx21.spring.cloudstorage.models;

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
        @JsonSubTypes.Type(value = Folder.class, name = "DIRECTORY"),
        @JsonSubTypes.Type(value = File.class, name = "FILE")
})
@SuperBuilder
@Getter
public abstract class Resource {
    private String path;
    private String name;
    private ResourceType type;
}
