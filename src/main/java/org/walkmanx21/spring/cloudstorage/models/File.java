package org.walkmanx21.spring.cloudstorage.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class File {

    private String path;
    private String name;
    private long size;
    private ResourceType type;

}
