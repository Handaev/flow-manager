package org.example.flowmanager.api.service.impl;

import org.example.flowmanager.api.dto.ConversionMultipartFile;
import org.example.flowmanager.api.entity.ConversionFile;
import org.example.flowmanager.api.entity.ConversionFileOutbox;
import org.example.flowmanager.api.entity.mapper.FileMapper;
import org.example.flowmanager.api.exception.ManagerException;
import org.example.flowmanager.api.service.minio.MinioServiceImpl;
import org.example.flowmanager.dto.FileResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceImplTest {

    @Mock
    private MinioServiceImpl minioServiceImpl;

    @Mock
    private FileMapper fileMapper;

    @Mock
    private ConversionFileServiceImpl conversionFileServiceImpl;

    @Mock
    private ConversionFileOutboxServiceImpl conversionFileOutboxServiceImpl;

    @Mock
    private SubscriptionServiceImpl subscriptionServiceImpl;

    @InjectMocks
    private WorkflowServiceImpl workflowService;

    @Test
    void shouldProcessConvertSuccessfully() throws IOException {
        String toExtension = "mp4";
        String bucketName = "user-bucket";
        MultipartFile mockMultipartFile = new MockMultipartFile("file", "test.avi", "video/avi", "bytes".getBytes());

        ConversionMultipartFile mockMultipartDto = new ConversionMultipartFile();
        ConversionFile mockFileEntity = new ConversionFile();
        ConversionFileOutbox mockOutboxEntity = new ConversionFileOutbox();
        FileResponseDto expectedResponseDto = new FileResponseDto();

        when(fileMapper.toConversionMultipartFile(mockMultipartFile, toExtension, bucketName)).thenReturn(mockMultipartDto);
        when(fileMapper.toConversionFile(mockMultipartDto)).thenReturn(mockFileEntity);
        when(fileMapper.toFileConversionOutbox(mockFileEntity)).thenReturn(mockOutboxEntity);
        when(fileMapper.toFileResponseDto(mockFileEntity)).thenReturn(expectedResponseDto);

        FileResponseDto actualResponseDto = workflowService.processConvert(toExtension, bucketName, mockMultipartFile);

        assertNotNull(actualResponseDto);
        assertEquals(expectedResponseDto, actualResponseDto);

        verify(fileMapper, times(1)).toConversionMultipartFile(mockMultipartFile, toExtension, bucketName);
        verify(minioServiceImpl, times(1)).upload(mockMultipartDto);
        verify(fileMapper, times(1)).toConversionFile(mockMultipartDto);
        verify(conversionFileServiceImpl, times(1)).saveConversionFile(mockFileEntity);
        verify(fileMapper, times(1)).toFileConversionOutbox(mockFileEntity);
        verify(conversionFileOutboxServiceImpl, times(1)).saveFileConversionOutbox(mockOutboxEntity);
        verify(fileMapper, times(1)).toFileResponseDto(mockFileEntity);
    }

    @Test
    void shouldThrowManagerExceptionWhenMapperThrowsIOException() throws IOException {
        String toExtension = "mp4";
        String bucketName = "user-bucket";
        MultipartFile mockMultipartFile = new MockMultipartFile("file", "test.avi", "video/avi", "bytes".getBytes());

        when(fileMapper.toConversionMultipartFile(mockMultipartFile, toExtension, bucketName))
                .thenThrow(new IOException("Read error"));

        ManagerException exception = assertThrows(ManagerException.class, () ->
                workflowService.processConvert(toExtension, bucketName, mockMultipartFile)
        );

        assertEquals("Ошибка при получении содержания файла: file", exception.getMessage());

        verify(minioServiceImpl, never()).upload(any(ConversionMultipartFile.class));
        verify(conversionFileServiceImpl, never()).saveConversionFile(any(ConversionFile.class));
        verify(conversionFileOutboxServiceImpl, never()).saveFileConversionOutbox(any(ConversionFileOutbox.class));
    }
}