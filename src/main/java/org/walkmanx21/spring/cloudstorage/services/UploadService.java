package org.walkmanx21.spring.cloudstorage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceAlreadyExistException;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.util.PathUtil;
import org.walkmanx21.spring.cloudstorage.util.ResourceBuilder;
import org.walkmanx21.spring.cloudstorage.util.ResourceMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final MinioService minioService;
    private final PathUtil pathUtil;
    private final ResourceBuilder resourceBuilder;
    private final ResourceMapper resourceMapper;
    private final SearchService searchService;

    private static final String ROOT_BUCKET = "user-files";

    public List<ResourceDto> uploadResources(String path, List<MultipartFile> files) {
        String fullObject = pathUtil.getFullObject(path);

        Map<String, MultipartFile> filesMap = new HashMap<>();
        for (MultipartFile file : files) {
            boolean fileExist = minioService.checkResourceExist(ROOT_BUCKET, fullObject + file.getOriginalFilename());

            if (!fileExist) {
                filesMap.put(file.getOriginalFilename(), file);
            } else {
                log.warn("Загружаемый ресурс {} уже существует", file.getOriginalFilename());
                throw new ResourceAlreadyExistException();
            }
        }

        minioService.uploadResources(ROOT_BUCKET, fullObject, filesMap);
        List<ResourceDto> resourceDtos = new ArrayList<>();
        filesMap.forEach((key, file) -> {
            String object = path + file.getOriginalFilename();
            Resource resource = resourceBuilder.buildFile(object, file.getSize());
            resourceDtos.add(resourceMapper.convertToFileDto(resource));
            searchService.saveUserResourceToDatabase(resource);
        });

        return resourceDtos;
    }

}
