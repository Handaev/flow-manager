package org.example.flowmanager.api.service.minio;

import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.example.flowmanager.api.dto.ConversionMultipartFile;
import org.example.flowmanager.api.exception.ManagerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioServiceImplTest {

    @Mock
    private S3Template s3Template;

    @InjectMocks
    private MinioServiceImpl minioService;

    @Test
    void shouldUploadFileSuccessfully() {
        ConversionMultipartFile fileDto = new ConversionMultipartFile();
        fileDto.setPath("user-bucket");
        fileDto.setName("document.pdf");
        byte[] input = "test data".getBytes();
        fileDto.setContent(input);

        minioService.upload(fileDto);

        verify(s3Template, times(1)).upload(eq("user-bucket"), eq("document.pdf"), any());
    }

    @Test
    void shouldThrowManagerExceptionWhenUploadFails() {
        ConversionMultipartFile fileDto = new ConversionMultipartFile();
        fileDto.setPath("error-bucket");
        fileDto.setName("failed.mp4");
        byte[] input = "test data".getBytes();
        fileDto.setContent(input);

        doThrow(mock(S3Exception.class))
                .when(s3Template).upload(anyString(), anyString(), any(InputStream.class));

        ManagerException exception = assertThrows(ManagerException.class, () ->
                minioService.upload(fileDto)
        );

        assertEquals("Ошибка при загрузке файла в бакет: error-bucket", exception.getMessage());
    }

    @Test
    void shouldDownloadResourceSuccessfully() throws IOException {
        String path = "user-bucket";
        String fileName = "video.mp4";
        S3Resource mockS3Resource = mock(S3Resource.class);
        InputStream mockInputStream = new ByteArrayInputStream("bytes".getBytes());

        when(s3Template.download(path, fileName)).thenReturn(mockS3Resource);
        when(mockS3Resource.getInputStream()).thenReturn(mockInputStream);

        Resource result = minioService.download(path, fileName);

        assertNotNull(result);
        assertNotNull(result.getInputStream());
        verify(s3Template, times(1)).download(path, fileName);
        verify(mockS3Resource, times(1)).getInputStream();
    }

    @Test
    void shouldThrowManagerExceptionWhenDownloadFails() {
        String path = "missing-bucket";
        String fileName = "ghost.avi";

        when(s3Template.download(path, fileName)).thenThrow(mock(S3Exception.class));

        ManagerException exception = assertThrows(ManagerException.class, () ->
                minioService.download(path, fileName)
        );

        assertEquals("Ошибка при скачивании файла: missing-bucket", exception.getMessage());
    }
}
