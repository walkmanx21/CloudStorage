package org.walkmanx21.spring.cloudstorage.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class OldFileDto extends OldResourceDto {

    private long size;

}
