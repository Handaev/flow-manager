package org.example.flowmanager.api.service.impl;

import org.example.flowmanager.api.dto.ConversionResponseRecord;
import org.example.flowmanager.api.entity.ConversionFile;
import org.example.flowmanager.api.entity.StatusFile;
import org.example.flowmanager.api.entity.mapper.FileMapper;
import org.example.flowmanager.api.exception.ManagerException;
import org.example.flowmanager.api.repository.ConversionFileRepository;
import org.example.flowmanager.api.repository.jpa.ConversionFileRepositoryJpa;
import org.example.flowmanager.api.service.minio.MinioServiceImpl;
import org.example.flowmanager.dto.FileResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ConversionFileServiceImplTest {

    @Mock
    private ConversionFileRepository conversionFileRepository;

    @Mock
    private ConversionFileRepositoryJpa conversionFileRepositoryJpa;

    @Mock
    private FileMapper fileMapper;

    @Mock
    private MinioServiceImpl minioServiceImpl;

    @InjectMocks
    private ConversionFileServiceImpl conversionFileService;

    private static final String FROM_BUCKET_VALUE = "aaaa";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(conversionFileService, "FROM_BUCKET", FROM_BUCKET_VALUE);
    }

    @Test
    void shouldSaveNewConversionFileWhenNotExists() {
        ConversionFile file = new ConversionFile();
        file.setName("asd");
        file.setPath("path/asd.txt");
        file.setToExtension("pdf");

        when(conversionFileRepository.findByNameAndPathAndToExtension("asd", "path/asd.txt", "pdf"))
                .thenReturn(Optional.empty());

        conversionFileService.saveConversionFile(file);

        verify(conversionFileRepository, times(1)).save(file);
    }

    @Test
    void shouldNotSaveConversionFileWhenAlreadyExists() {
        ConversionFile file = new ConversionFile();
        file.setName("asd");
        file.setPath("path/asd.txt");
        file.setToExtension("pdf");

        when(conversionFileRepository.findByNameAndPathAndToExtension("asd", "path/asd.txt", "pdf"))
                .thenReturn(Optional.of(file));

        conversionFileService.saveConversionFile(file);

        verify(conversionFileRepository, never()).save(any(ConversionFile.class));
    }

    @Test
    void shouldUpdateStatusAndSaveConvertedFileSuccessfully() {
        ConversionResponseRecord recordValue = new ConversionResponseRecord("123456", "target-bucket", "video.txt");
        ConversionFile existingFile = new ConversionFile();
        existingFile.setStatus(StatusFile.IN_PROCESSING);

        when(conversionFileRepositoryJpa.findConversionFileByBucketNameAndFileName(eq(FROM_BUCKET_VALUE), eq("video"), eq("txt")))
                .thenReturn(existingFile);

        conversionFileService.saveConversionFile(recordValue);

        ArgumentCaptor<ConversionFile> captor = ArgumentCaptor.forClass(ConversionFile.class);
        verify(conversionFileRepository, times(1)).save(captor.capture());

        ConversionFile savedResult = captor.getValue();
        assertEquals("video.txt", savedResult.getName());
        assertEquals("txt", savedResult.getFromExtension());
        assertEquals("target-bucket/video.txt", savedResult.getPath());
        assertEquals(StatusFile.SUCCESS, savedResult.getStatus());
    }

    @Test
    void shouldHandleEmptyResultDataAccessExceptionWhenSourceFileNotFound() {
        ConversionResponseRecord recordValue = new ConversionResponseRecord("123456","target-bucket", "video.mp4");

        when(conversionFileRepositoryJpa.findConversionFileByBucketNameAndFileName(FROM_BUCKET_VALUE, "video", "mp4"))
                .thenThrow(new EmptyResultDataAccessException(1));

        conversionFileService.saveConversionFile(recordValue);

        verify(conversionFileRepository, never()).save(any(ConversionFile.class));
    }

    @Test
    void shouldFindAllByCreatedAtMoreTenMinutesAndStatusFailed() {
        ConversionFile file = new ConversionFile();
        List<ConversionFile> expectedList = Collections.singletonList(file);

        when(conversionFileRepository.findAllByCreatedAtMoreTenMinutesAndStatusFailed(5, "failed"))
                .thenReturn(expectedList);

        List<ConversionFile> actualList = conversionFileService.findAllByCreatedAtMoreTenMinutesAndStatusFailed(5, StatusFile.FAILED);

        assertEquals(expectedList, actualList);
    }

    @Test
    void shouldUpdateStatusesToFailedAndSaveAll() {
        ConversionFile file1 = new ConversionFile();
        file1.setStatus(StatusFile.IN_PROCESSING);
        ConversionFile file2 = new ConversionFile();
        file2.setStatus(StatusFile.IN_PROCESSING);
        List<ConversionFile> list = List.of(file1, file2);

        conversionFileService.saveConversionFiles(list);

        assertEquals(StatusFile.FAILED, file1.getStatus());
        assertEquals(StatusFile.FAILED, file2.getStatus());
        verify(conversionFileRepository, times(1)).saveAll(list);
    }

    @Test
    void shouldFindStatusByFileIdSuccessfully() {
        Long fileId = 1L;
        ConversionFile file = new ConversionFile();
        FileResponseDto dto = new FileResponseDto();

        when(conversionFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(fileMapper.toFileResponseDto(file)).thenReturn(dto);

        FileResponseDto result = conversionFileService.findStatusByFileId(fileId);

        assertNotNull(result);
        assertEquals(dto, result);
    }

    @Test
    void shouldFindConvertedFileByFileIdSuccessfully() throws IOException {
        Long fileId = 1L;
        ConversionFile file = new ConversionFile();
        file.setName("movie.avi");
        file.setPath("video/input/movie.avi");
        file.setToExtension("mp4");

        Resource mockResource = new ByteArrayResource("bytes".getBytes());

        when(conversionFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(minioServiceImpl.download("mp4/input", "movie.mp4")).thenReturn(mockResource);

        Resource result = conversionFileService.findConvertedFileByFileId(fileId);

        assertNotNull(result);
        assertEquals(mockResource, result);
    }

    @Test
    void shouldThrowManagerExceptionWhenMinioFails() throws IOException {
        Long fileId = 1L;
        ConversionFile file = new ConversionFile();
        file.setName("movie.avi");
        file.setPath("video/input/movie.avi");
        file.setToExtension("mp4");

        when(conversionFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(minioServiceImpl.download(anyString(), anyString())).thenThrow(new IOException("Minio error"));

        ManagerException exception = assertThrows(ManagerException.class, () ->
                conversionFileService.findConvertedFileByFileId(fileId)
        );

        assertEquals("Ошибка получения потока файла: 1", exception.getMessage());
    }

    @Test
    void shouldThrowManagerExceptionWhenFileNotFoundInDb() {
        Long fileId = 999L;
        when(conversionFileRepository.findById(fileId)).thenReturn(Optional.empty());

        ManagerException exception = assertThrows(ManagerException.class, () ->
                conversionFileService.findConversionFileByFileId(fileId)
        );

        assertEquals("Файла с таким id: 999 не существует", exception.getMessage());
    }
}
