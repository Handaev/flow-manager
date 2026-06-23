package org.example.flowmanager.api.controller;

import org.example.flowmanager.api.controller.base.BaseContext;
import org.example.flowmanager.api.dto.ConversionMultipartFile;
import org.example.flowmanager.api.entity.StatusFile;
import org.example.flowmanager.api.repository.ConversionFileRepository;
import org.example.flowmanager.api.service.feign.SubscriptionUserFeignClient;
import org.example.flowmanager.api.service.minio.MinioServiceImpl;
import org.example.subscription.client.dto.UserResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDate;


public class ConversionFileControllerIT extends BaseContext {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ConversionFileRepository conversionFileRepository;

    @Autowired
    private MinioServiceImpl minioService;

    @MockBean
    private SubscriptionUserFeignClient subscriptionUserFeignClient;

    @AfterEach
    void tearDown() {
        conversionFileRepository.deleteAll();
    }

    @Test
    @Sql(scripts = "/db/sql/insert_files.sql")
    public void flowManagerFilesFileIdConvertedFileGet_Successfully() {
        long targetFileId = 101L;

        byte[] value = "1234567890000".getBytes();
        ConversionMultipartFile multipartFile = new ConversionMultipartFile();
        multipartFile.setFromExtension("pdf");
        multipartFile.setPath("pdf");
        multipartFile.setContent(value);
        multipartFile.setName("file.pdf");
        multipartFile.setOriginalFileName("file.txt");
        multipartFile.setContentType("application/pdf");
        minioService.upload(multipartFile);

        byte[] actualBytes = webTestClient.get()
                .uri("/files/{file_id}/converted_file", targetFileId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .returnResult()
                .getResponseBody();

        Assertions.assertArrayEquals(value, actualBytes);
    }

    @Test
    public void flowManagerFilesFileIdConvertedFileGet_NotFound() {
        long fileId = 12345678L;

        webTestClient.get()
                .uri("/files/{file_id}/converted_file", fileId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(400)
                .jsonPath("$.message").isEqualTo(String.format("Файла с таким id: %s не существует", fileId));
    }

    @Test
    @Sql(scripts = "/db/sql/insert_files.sql")
    public void flowManagerFilesFileIdStatusGet_Successfully() {
        long firstFileId = 103L;

        webTestClient.get()
                .uri("/files/{file_id}/status", firstFileId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo((int) firstFileId)
                .jsonPath("$.name").isEqualTo("file.txt")
                .jsonPath("$.fromExtension").isEqualTo("txt")
                .jsonPath("$.toExtension").isEqualTo("pdf")
                .jsonPath("$.path").isEqualTo("sources")
                .jsonPath("$.status").isEqualTo(StatusFile.IN_PROCESSING.name());
    }

    @Test
    public void flowManagerFilesFileIdStatusGet_NotFound() {
        long fileId = 12345678L;

        webTestClient.get()
                .uri("/files/{file_id}/status", fileId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(400)
                .jsonPath("$.message").isEqualTo(String.format("Файла с таким id: %s не существует", fileId));
    }

    @Test
    public void flowManagerFilesUploadAndConvertPostAndGetUserFromCache_Successfully() {
        executeRedisJsonScript("db/nosql/redis-data.json");
        byte[] fileContent = "0123456789".getBytes();
        String fileName = "file.txt";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        body.add("file", resource);

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/files/upload-and-convert")
                        .queryParam("toExtension", "pdf")
                        .queryParam("bucketName", "sources")
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("X-User-Login", "paid_user")
                .body(BodyInserters.fromMultipartData(body))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo(fileName)
                .jsonPath("$.fromExtension").isEqualTo("txt")
                .jsonPath("$.toExtension").isEqualTo("pdf")
                .jsonPath("$.path").isEqualTo("sources/file.txt")
                .jsonPath("$.status").isEqualTo(StatusFile.IN_PROCESSING.name());
    }

    @Test
    public void flowManagerFilesUploadAndConvertPostAndGetUserFromFeign_Successfully() {
        byte[] fileContent = "0123456789".getBytes();
        String fileName = "file.txt";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        body.add("file", resource);

        UserResponseDto paidUser = new UserResponseDto();
        paidUser.setName("free_user");
        paidUser.setSubscriptionType(UserResponseDto.SubscriptionTypeEnum.PAID);
        paidUser.setEndDate(LocalDate.now());

        Mockito.when(subscriptionUserFeignClient.getUserByName("free_user"))
                .thenReturn(ResponseEntity.ok().body(paidUser));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/files/upload-and-convert")
                        .queryParam("toExtension", "pdf")
                        .queryParam("bucketName", "sources")
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("X-User-Login", "free_user")
                .body(BodyInserters.fromMultipartData(body))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo(fileName)
                .jsonPath("$.fromExtension").isEqualTo("txt")
                .jsonPath("$.toExtension").isEqualTo("pdf")
                .jsonPath("$.path").isEqualTo("sources/file.txt")
                .jsonPath("$.status").isEqualTo(StatusFile.IN_PROCESSING.name());
    }
}
