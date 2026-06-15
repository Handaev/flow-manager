package org.example.flowmanager.api.service.minio;

import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.example.flowmanager.api.dto.ConversionMultipartFile;
import org.example.flowmanager.api.exception.ManagerException;
import org.example.flowmanager.api.service.MinioService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final S3Template s3Template;

    public void upload(ConversionMultipartFile file) {
        String path = file.getPath();
        try {
            s3Template.upload(path, file.getName(), file.getInputStream());
        } catch (S3Exception e) {
            throw new ManagerException(String.format("Ошибка при загрузке файла в бакет: %s", path), e);
        }
    }

    @Override
    public Resource download(String path, String fileName) throws IOException {
        try {
            S3Resource s3ObjectResponse = s3Template.download(path, fileName);

            return new InputStreamResource(s3ObjectResponse.getInputStream());
        } catch (S3Exception e) {
            throw new ManagerException(String.format("Ошибка при скачивании файла: %s", path), e);
        }
    }
}