package org.example.flowmanager.api.controller;

import io.restassured.http.ContentType;
import org.example.flowmanager.api.controller.base.BaseContext;
import org.example.flowmanager.api.dto.ConversionMultipartFile;
import org.example.flowmanager.api.entity.ConversionFile;
import org.example.flowmanager.api.entity.StatusFile;
import org.example.flowmanager.api.repository.ConversionFileRepository;
import org.example.flowmanager.api.service.minio.MinioServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ConversionFileControllerIT extends BaseContext {

    @Autowired
    private ConversionFileRepository conversionFileRepository;

    @Autowired
    private MinioServiceImpl minioService;

    @Test
    public void flowManagerFilesFileIdConvertedFileGet_Successfully() {
        ConversionFile conversionFile = new ConversionFile();
        conversionFile.setFromExtension("txt");
        conversionFile.setToExtension("pdf");
        conversionFile.setPath("sources");
        conversionFile.setStatus(StatusFile.SUCCESS);
        conversionFile.setCreatedAt(LocalDateTime.now());
        conversionFile.setName("file.txt");

        ConversionFile savedFirst = conversionFileRepository.save(conversionFile);
        long firstFileId = savedFirst.getId();

        ConversionFile convertedFile = new ConversionFile();
        convertedFile.setFromExtension("pdf");
        convertedFile.setPath("pdf");
        convertedFile.setStatus(StatusFile.SUCCESS);
        convertedFile.setCreatedAt(LocalDateTime.now());
        convertedFile.setName("file.pdf");
        conversionFileRepository.save(convertedFile);

        ConversionMultipartFile multipartFile = new ConversionMultipartFile();
        byte[] value = "1234567890000".getBytes();
        multipartFile.setFromExtension("pdf");
        multipartFile.setPath("pdf");
        multipartFile.setContent(value);
        multipartFile.setName("file.pdf");
        multipartFile.setOriginalFileName("file.txt");
        multipartFile.setContentType("application/pdf");
        minioService.upload(multipartFile);

        byte[] expectedBytes = value;
        byte[] actualBytes = given()
                .pathParam("file_id", firstFileId)
                .when()
                .get("/flow-manager/files/{file_id}/converted_file")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        Assertions.assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    public void flowManagerFilesFileIdConvertedFileGet_NotFound() {
        long fileId = 12345678L;

        given()
                .pathParam("file_id", fileId)
                .when()
                .get("/flow-manager/files/{file_id}/converted_file")
                .then()
                .statusCode(400)
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .body("statusCode", equalTo(400))
                .body("message", equalTo(String.format("Файла с таким id: %s не существует", fileId)));
    }

    @Test
    public void flowManagerFilesFileIdStatusGet_Successfully() {
        ConversionFile conversionFile = new ConversionFile();
        conversionFile.setFromExtension("txt");
        conversionFile.setToExtension("pdf");
        conversionFile.setPath("sources");
        conversionFile.setStatus(StatusFile.IN_PROCESSING);
        conversionFile.setCreatedAt(LocalDateTime.now());
        conversionFile.setName("file.txt");

        ConversionFile savedFirst = conversionFileRepository.save(conversionFile);
        long firstFileId = savedFirst.getId();
        int expectedId = (int) firstFileId;

        given()
                .pathParam("file_id", firstFileId)
                .when()
                .get("/flow-manager/files/{file_id}/status")
                .then()
                .statusCode(200)
                .body("id",  equalTo(expectedId))
                .body("name",   equalTo("file.txt"))
                .body("fromExtension",  equalTo("txt"))
                .body("toExtension",  equalTo("pdf"))
                .body("path",  equalTo("sources"))
                .body("status",  equalTo(String.valueOf(StatusFile.IN_PROCESSING)));
    }

    @Test
    public void flowManagerFilesFileIdStatusGet_NotFound() {
        long fileId = 12345678L;
        given()
                .pathParam("file_id", fileId)
                .when()
                .get("/flow-manager/files/{file_id}/status")
                .then()
                .statusCode(400)
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .body("statusCode", equalTo(400))
                .body("message", equalTo(String.format("Файла с таким id: %s не существует", fileId)));
    }

    @Test
    public void flowManagerFilesUploadAndConvertPost_Successfully() {
        byte[] fileContent = "0123456789".getBytes();
        String fileName = "file.txt";

        String targetExtension = "pdf";
        String bucketName = "sources";

        given()
                .queryParam("toExtension", targetExtension)
                .queryParam("bucketName", bucketName)
                .multiPart("file", fileName, fileContent, "application/octet-stream")
                .when()
                .post("/flow-manager/files/upload-and-convert")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("name", equalTo(fileName))
                .body("fromExtension", equalTo("txt"))
                .body("toExtension", equalTo(targetExtension))
                .body("path",  equalTo("sources/file.txt"))
                .body("status", equalTo(String.valueOf(StatusFile.IN_PROCESSING)));
    }
}