package org.example.flowmanager.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.flowmanager.api.dto.ConversionMultipartFile;
import org.example.flowmanager.api.entity.ConversionFile;
import org.example.flowmanager.api.entity.ConversionFileOutbox;
import org.example.flowmanager.api.entity.mapper.FileMapper;
import org.example.flowmanager.api.exception.ManagerException;
import org.example.flowmanager.api.service.WorkflowService;
import org.example.flowmanager.api.service.minio.MinioServiceImpl;
import org.example.flowmanager.dto.FileResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final MinioServiceImpl minioServiceImpl;
    private final FileMapper fileMapper;
    private final ConversionFileServiceImpl conversionFileServiceImpl;
    private final ConversionFileOutboxServiceImpl conversionFileOutboxServiceImpl;
    private final SubscriptionServiceImpl subscriptionServiceImpl;

    @Transactional
    public FileResponseDto processConvert(String toExtension, String bucketName, MultipartFile multipartFile) throws  ManagerException {
        ConversionMultipartFile conversionMultipartFile;
        try {
            conversionMultipartFile = fileMapper.toConversionMultipartFile(multipartFile, toExtension, bucketName);
        } catch (IOException e) {
            throw new ManagerException(String.format("Ошибка при получении содержания файла: %s", multipartFile.getName()), e);
        }

        subscriptionServiceImpl.checkingSubscriptionCurrentUser(conversionMultipartFile);

        minioServiceImpl.upload(conversionMultipartFile);

        ConversionFile conversionFile = fileMapper.toConversionFile(conversionMultipartFile);
        conversionFileServiceImpl.saveConversionFile(conversionFile);

        ConversionFileOutbox fileConversionOutbox = fileMapper.toFileConversionOutbox(conversionFile);
        conversionFileOutboxServiceImpl.saveFileConversionOutbox(fileConversionOutbox);

        return fileMapper.toFileResponseDto(conversionFile);
    }
}