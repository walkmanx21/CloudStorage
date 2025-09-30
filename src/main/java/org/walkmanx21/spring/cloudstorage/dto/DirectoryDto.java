package org.walkmanx21.spring.cloudstorage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryDto {

    private String path;
    private String name;
    private ResourceDtoType type;
}
