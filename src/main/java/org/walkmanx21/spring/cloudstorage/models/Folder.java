package org.walkmanx21.spring.cloudstorage.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Folder {

    private String path;
    private String name;
    private ResourceType type;

}
