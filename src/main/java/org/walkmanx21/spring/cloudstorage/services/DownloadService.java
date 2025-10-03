package org.walkmanx21.spring.cloudstorage.services;

import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.exceptions.DownloadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class DownloadService {

    private final MinioService minioService;

    private static final String ROOT_BUCKET = "user-files";

    protected void downloadDirectory(OutputStream outputStream, String fullPath, String userDirectory) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            List<Item> directoryContents = minioService.getListObjects(ROOT_BUCKET, fullPath, true);
            directoryContents.forEach(object -> {
                if (!object.isDir()) {
                    String entryName = object.objectName().substring(userDirectory.length());
                    try (InputStream inputStream = minioService.getObject(ROOT_BUCKET, object.objectName())) {
                        zipOutputStream.putNextEntry(new ZipEntry(entryName));
                        inputStream.transferTo(zipOutputStream);
                        zipOutputStream.closeEntry();
                    } catch (IOException e) {
                        log.warn("Ошибка скачивания файла {}", object.objectName());
                        throw new DownloadException();
                    }
                }
            });
        } catch (IOException e) {
            log.warn("Ошибка скачивания папки {}", fullPath);
            throw new DownloadException();
        }
    }

    protected void downloadFile(OutputStream outputStream, String fullPath) {
        try (InputStream inputStream = minioService.getObject(ROOT_BUCKET, fullPath)) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            log.warn("Ошибка скачивания файла {}", fullPath);
            throw new DownloadException();
        }
    }
}
