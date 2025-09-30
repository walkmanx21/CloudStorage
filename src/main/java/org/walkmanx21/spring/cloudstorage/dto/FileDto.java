package org.walkmanx21.spring.cloudstorage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDto extends ResourceDto {

    private long size;
}
