package org.walkmanx21.spring.cloudstorage.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Component;
import org.walkmanx21.spring.cloudstorage.dto.DirectoryDto;
import org.walkmanx21.spring.cloudstorage.dto.FileDto;
import org.walkmanx21.spring.cloudstorage.models.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class ResourceMapper {

    private final ModelMapper mapper = new ModelMapper();

    @PostConstruct
    public void prepare() {
        TypeMap<Resource, DirectoryDto> typeDirectoryMap = mapper.createTypeMap(Resource.class, DirectoryDto.class);
        TypeMap<Resource, FileDto> typeFileMap = mapper.createTypeMap(Resource.class, FileDto.class);

        typeDirectoryMap.addMappings(mapper -> {
            mapper.using(ctx -> {
                        Path path = Paths.get(ctx.getSource().toString());
                        Path parent = path.getParent();
                        return parent == null ? "/" : parent.toString().replace("\\", "/") + "/";
                    })
                    .map(Resource::getResource, DirectoryDto::setPath);
            mapper.using(ctx -> {
                Path path = Paths.get(ctx.getSource().toString());
                return path.getFileName().toString() + "/";
            }).map(Resource::getResource, DirectoryDto::setName);
        });

        typeFileMap.addMappings(mapper -> {
            mapper.using(ctx -> {
                        Path path = Paths.get(ctx.getSource().toString());
                        Path parent = path.getParent();
                        return parent == null ? "/" : parent.toString().replace("\\", "/") + "/";
                    })
                    .map(Resource::getResource, FileDto::setPath);
            mapper.using(ctx -> {
                Path path = Paths.get(ctx.getSource().toString());
                return path.getFileName().toString() + "/";
            }).map(Resource::getResource, FileDto::setName);
        });
    }

    public DirectoryDto convertToDirectoryDto(Resource resource) {
        return mapper.map(resource, DirectoryDto.class);
    }

    public FileDto convertToFileDto(Resource resource) {
        return mapper.map(resource, FileDto.class);
    }
}
