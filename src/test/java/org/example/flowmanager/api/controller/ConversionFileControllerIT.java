package org.example.flowmanager.api.controller;

import org.example.flowmanager.api.controller.base.BaseContext;
import org.example.flowmanager.api.dto.ConversionMultipartFile;
import org.example.flowmanager.api.entity.ConversionFile;
import org.example.flowmanager.api.entity.StatusFile;
import org.example.flowmanager.api.exception.ManagerException;
import org.example.flowmanager.api.repository.ConversionFileRepository;
import org.example.flowmanager.api.service.minio.MinioServiceImpl;
import org.example.flowmanager.dto.FileResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ConversionFileControllerIT extends BaseContext {

    @Autowired
    private ConversionFileController conversionFileController;

    @Autowired
    private ConversionFileRepository conversionFileRepository;

    @Autowired
    private MinioServiceImpl minioService;

    private long firstFileId;
    private long secondFileId;
    private ConversionMultipartFile multipartFile = new ConversionMultipartFile();

    @BeforeEach
    public void setUp() {
        conversionFileRepository.deleteAll();

        ConversionFile conversionFile = new ConversionFile();
        conversionFile.setFromExtension("txt");
        conversionFile.setToExtension("pdf");
        conversionFile.setPath("aaaa");
        conversionFile.setStatus(StatusFile.SUCCESS);
        conversionFile.setCreatedAt(LocalDateTime.now());
        conversionFile.setName("file.txt");

        ConversionFile savedFirst = conversionFileRepository.save(conversionFile);
        this.firstFileId = savedFirst.getId();

        ConversionFile convertedFile = new ConversionFile();
        convertedFile.setFromExtension("pdf");
        convertedFile.setToExtension("");
        convertedFile.setPath("pdf");
        convertedFile.setStatus(StatusFile.SUCCESS);
        convertedFile.setCreatedAt(LocalDateTime.now());
        convertedFile.setName("file.pdf");

        ConversionFile savedSecond = conversionFileRepository.save(convertedFile);
        this.secondFileId = savedSecond.getId();

        multipartFile.setFromExtension("txt");
        multipartFile.setToExtension("pdf");
        multipartFile.setPath("pdf");
        multipartFile.setContent("1234567890000".getBytes());
        multipartFile.setName("file.pdf");
        multipartFile.setOriginalFileName("file.txt");
        multipartFile.setContentType("application/pdf");

        minioService.upload(multipartFile);
    }

    @Test
    public void flowManagerFilesFileIdConvertedFileGet_Successfully() throws IOException {
        ResponseEntity<Resource> file = conversionFileController.flowManagerFilesFileIdConvertedFileGet(this.firstFileId);

        Assertions.assertNotNull(file.getBody());
        Assertions.assertArrayEquals("1234567890000".getBytes(), file.getBody().getContentAsByteArray());
    }

    @Test
    public void flowManagerFilesFileIdConvertedFileGet_NotFound() {
        long fileId = 12345678L;
        assertThatThrownBy(() -> conversionFileController.flowManagerFilesFileIdConvertedFileGet(fileId))
                .isInstanceOf(ManagerException.class)
                .hasMessageContaining(String.format("Файла с таким id: %s не существует", fileId));

    }

    @Test
    public void flowManagerFilesFileIdStatusGet_Successfully() {
        ResponseEntity<FileResponseDto> response = conversionFileController.flowManagerFilesFileIdStatusGet(this.secondFileId);

        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("SUCCESS", response.getBody().getStatus());
    }

    @Test
    public void flowManagerFilesFileIdStatusGet_NotFound() {
        long fileId = 12345678L;
        assertThatThrownBy(() -> conversionFileController.flowManagerFilesFileIdStatusGet(fileId))
                .isInstanceOf(ManagerException.class)
                .hasMessageContaining(String.format("Файла с таким id: %s не существует", fileId));

    }

    @Test
    public void flowManagerFilesUploadAndConvertPost_Successfully() {
        FileResponseDto dto = new FileResponseDto();
        dto.setFromExtension("txt");
        dto.setToExtension("pdf");
        dto.setPath("aaaa/file.txt");
        dto.setStatus(String.valueOf(StatusFile.IN_PROCESSING));
        dto.setName("file.txt");

        ResponseEntity<FileResponseDto> response = conversionFileController
                .flowManagerFilesUploadAndConvertPost("pdf", "aaaa", multipartFile);

        FileResponseDto dtoRes = response.getBody();

        Assertions.assertNotNull(dtoRes);
        assertThat(dtoRes.getName()).isEqualTo(dto.getName());
        assertThat(dtoRes.getStatus()).isEqualTo(dto.getStatus());
        assertThat(dtoRes.getPath()).isEqualTo(dto.getPath());
        assertThat(dtoRes.getFromExtension()).isEqualTo(dto.getFromExtension());
        assertThat(dtoRes.getToExtension()).isEqualTo(dto.getToExtension());
    }
}