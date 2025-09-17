package org.walkmanx21.spring.cloudstorage.models;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class File extends Resource {

    private long size;

}
