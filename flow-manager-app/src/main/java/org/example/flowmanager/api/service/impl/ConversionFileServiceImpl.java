package org.example.flowmanager.api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.flowmanager.api.dto.ConversionResponseRecord;
import org.example.flowmanager.api.entity.ConversionFile;
import org.example.flowmanager.api.entity.StatusFile;
import org.example.flowmanager.api.entity.mapper.FileMapper;
import org.example.flowmanager.api.exception.ManagerException;
import org.example.flowmanager.api.repository.ConversionFileRepository;
import org.example.flowmanager.api.repository.jpa.ConversionFileRepositoryJpa;
import org.example.flowmanager.api.service.ConversionFileService;
import org.example.flowmanager.api.service.minio.MinioServiceImpl;
import org.example.flowmanager.dto.FileResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.example.flowmanager.api.utils.Utils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionFileServiceImpl implements ConversionFileService {

    private final ConversionFileRepository conversionFileRepository;
    private final ConversionFileRepositoryJpa conversionFileRepositoryJpa;
    private final FileMapper fileMapper;
    private final MinioServiceImpl minioServiceImpl;

    @Value("${server.conversion.fromBucket}")
    private String FROM_BUCKET;

    public void saveConversionFile(ConversionFile conversionFile) {
        ConversionFile savedConversionFile = conversionFileRepository
                .findByNameAndPathAndToExtension(conversionFile.getName(), conversionFile.getPath(), conversionFile.getToExtension())
                .orElse(null);

        if(Objects.isNull(savedConversionFile)) {
            conversionFileRepository.save(conversionFile);
        }
    }

    public void saveConversionFile(ConversionResponseRecord value) {
        String fullFileName = value.fileName();
        StringBuilder path = new StringBuilder();
        path.append(value.bucketName());
        path.append(Utils.SLASH);
        path.append(fullFileName);
        String name = fullFileName.split("\\.")[Utils.ONLY_NAME];
        String toExtension = fullFileName.split("\\.")[Utils.ONLY_EXTENSION];

        try {
            ConversionFile conversionFile = conversionFileRepositoryJpa.findConversionFileByBucketNameAndFileName(FROM_BUCKET, name, toExtension);

            if(Objects.nonNull(conversionFile)) {
                conversionFile.setStatus(StatusFile.SUCCESS);

                ConversionFile convertedFile = ConversionFile.builder()
                        .name(fullFileName)
                        .fromExtension(toExtension)
                        .path(path.toString())
                        .status(StatusFile.SUCCESS)
                        .build();

                conversionFileRepository.save(convertedFile);
            }
        } catch (EmptyResultDataAccessException e) {
            log.debug(String.format("Исходного файла для %s не существует", fullFileName));
        }
    }

    public List<ConversionFile> findAllByCreatedAtMoreTenMinutesAndStatusFailed(int limit, StatusFile  status) {
        return conversionFileRepository.findAllByCreatedAtMoreTenMinutesAndStatusFailed(limit, status.getStatus());
    }

    @Transactional
    public void saveConversionFiles(List<ConversionFile> conversionFiles) {
        conversionFiles.forEach(conversionFile -> {
            conversionFile.setStatus(StatusFile.FAILED);
        });
        conversionFileRepository.saveAll(conversionFiles);
    }

    public FileResponseDto findStatusByFileId(Long fileId) throws ManagerException {
        ConversionFile conversionFile = findConversionFileByFileId(fileId);

        return fileMapper.toFileResponseDto(conversionFile);
    }

    public Resource findConvertedFileByFileId(Long fileId) throws ManagerException {
        ConversionFile conversionFile = findConversionFileByFileId(fileId);

        String toExtension = conversionFile.getToExtension();
        String originalPath = Utils.REMOVE_LAST_ELEMENT_PATTERN.matcher(conversionFile.getPath()).replaceAll("");
        String path = Utils.REPLACE_FIRST_WORD_PATTERN.matcher(originalPath).replaceFirst(toExtension);
        String name = Utils.REPLACE_EXTENSION_PATTERN.matcher(conversionFile.getName()).replaceAll("." + toExtension);

        try {
            return minioServiceImpl.download(path, name);
        } catch (IOException e) {
            throw new ManagerException(String.format("Ошибка получения потока файла: %s", fileId));
        }
    }

    public ConversionFile findConversionFileByFileId(Long fileId) {
        return conversionFileRepository.findById(fileId)
                .orElseThrow(() -> new ManagerException(String.format("Файла с таким id: %s не существует", fileId), HttpStatus.BAD_REQUEST));
    }
}